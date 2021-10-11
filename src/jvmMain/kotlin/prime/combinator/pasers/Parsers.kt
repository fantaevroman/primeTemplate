package prime.combinator.pasers

import prime.combinator.Parser
import prime.combinator.ParsingContext
import prime.combinator.ParsingError
import java.util.*


fun createContext(text: String) = ParsingContext(text, -1, -1, emptyMap(), "empty", Optional.empty())


abstract class EndOfInputParser : Parser {

  override fun parse(context: ParsingContext): ParsingContext {
    val currentIndex = context.indexStart + 1
    return if (context.text.length - 1 < currentIndex) {
      context.copy(
        error = Optional.of(ParsingError("Can't parse at index:[${currentIndex}] end of text")),
        type = getType(),
        indexEnd = currentIndex,
        context = emptyMap()
      )
    } else {
      parseNext(context)
    }
  }

  abstract fun parseNext(context: ParsingContext): ParsingContext
}

class Long : EndOfInputParser() {
  override fun getType() = "Long"

  override fun parseNext(context: ParsingContext): ParsingContext {
    val scanner = Scanner(context.text)
    val currentIndex = context.indexEnd + 1
    val indexEnd = currentIndex + 1

    return if (scanner.hasNextLong()) {
      context.copy(
        indexStart = currentIndex,
        indexEnd = indexEnd,
        context = hashMapOf(
          Pair("longValue", scanner.nextInt())
        )
      )
    } else {
      context.copy(
        error = Optional.of(ParsingError("Can't parse Long at index:[${currentIndex}]")),
        type = "Long",
        indexEnd = indexEnd,
        context = emptyMap()
      )
    }
  }
}

class AnyCharacter : EndOfInputParser() {
  override fun getType() = "AnyCharacter"


  override fun parseNext(context: ParsingContext): ParsingContext {
    val currentIndex = context.indexEnd + 1
    return context.copy(
      indexStart = currentIndex,
      indexEnd = currentIndex + 1,
      context = hashMapOf(Pair("anyCharacter", context.text[currentIndex.toInt()])),
      type = getType()
    )
  }

  companion object {
    fun join(list: List<ParsingContext>, separator: String = ""): ParsingContext {
      return if (list.isEmpty()) {
        return ParsingContext("empty join Str", 0,0, emptyMap(), "emptyJoin", Optional.empty())
      } else {
        list.last().copy(type = "Str",
          context = hashMapOf(Pair("str",
            list.map { anyCharContext -> anyCharContext.context["anyCharacter"] }.joinToString(separator = separator)
          )
          )
        )
      }
    }
  }

}

class Str(val string: String) : Parser {
  override fun getType() = "Str"

  override fun parse(context: ParsingContext): ParsingContext {
    val currentIndex = context.indexEnd + 1
    if (context.text.length - 1 < currentIndex - 1 + string.length) {
      return context.copy(
        indexStart = currentIndex,
        indexEnd = currentIndex + string.length - 1,
        error = Optional.of(ParsingError("Can't parse at index:[${currentIndex}] end of text")),
        type = "Str",
        context = emptyMap()
      )
    } else {
      val expectedIndex = currentIndex.toInt()
      val indexOf = context.text.indexOf(string, expectedIndex)
      return if (indexOf == expectedIndex) {
        context.copy(
          indexStart = currentIndex,
          indexEnd = currentIndex + string.length - 1,
          context = hashMapOf(
            Pair("str", string)
          ),
          type = "Str"
        )
      } else {
        context.copy(
          error = Optional.of(ParsingError("Can't parse at index:[${currentIndex}] [$string] not found")),
          type = "Str",
          context = emptyMap()
        )
      }
    }
  }
}

class Character(private val char: Char) : EndOfInputParser() {
  override fun getType() = "Character"

  override fun parseNext(context: ParsingContext): ParsingContext {
    val next = Scanner(context.text).next()
    val currentIndex = context.indexEnd + 1
    return if (next.equals(char.toString())) {
      context.copy(
        indexStart = currentIndex,
        indexEnd = currentIndex + 1,
        context = hashMapOf(Pair("character", next)),
        type = getType()
      )
    } else {
      context.copy(
        error = Optional.of(
          ParsingError("Can't parse character at index:[${currentIndex}], required:[$char] but was:[$next]")
        ),
        type = getType(),
        context = emptyMap()
      )
    }
  }
}

class SequenceOf(
  private vararg val parsers: Parser
) : Parser {
  override fun getType() = "SequenceOf"

  override fun parse(context: ParsingContext): ParsingContext {
    val successSequence = mutableListOf<ParsingContext>()
    val parsersIterator = parsers.iterator()
    var currentContext = context

    while (parsersIterator.hasNext()) {
      currentContext = parsersIterator.next().parse(currentContext)
      if (currentContext.success()) {
        successSequence.add(currentContext)
      } else {
        return currentContext.copy(
          type = getType(),
          context = hashMapOf(Pair("sequence", successSequence))
        )
      }
    }

    return successSequence.last().copy(
      type = getType(),
      context = hashMapOf(Pair("sequence", successSequence))
    )
  }
}

class RepeatableBetween(
  private val left: Parser,
  private val between: Parser,
  private val right: Parser
) : Parser {
  override fun getType() = "RepeatableBetween"

  override fun parse(context: ParsingContext): ParsingContext {
    return SequenceOf(left, RepeatUntil(between, right)).map {
      val between = (it.context["sequence"] as List<ParsingContext>)[1]
      val repeaters = (between.context["repeaters"] as List<ParsingContext>)
      it.copy(context = hashMapOf(Pair("between", repeaters)))
    }.parse(context).copy(
      type = getType()
    )
  }

  fun joinBetween(joinBetween: (contexts: List<ParsingContext>) -> ParsingContext): Parser {
    return this.map { original ->
      original.copy(
        context = hashMapOf(
          Pair(
            "between",
            joinBetween(original.context["between"] as List<ParsingContext>)
          )
        )
      )
    }
  }
}

class Between(
  private val left: Parser,
  private val between: Parser,
  private val right: Parser,
) : Parser {
  override fun getType() = "Between"

  override fun parse(context: ParsingContext): ParsingContext {
    return SequenceOf(left, between, right).parse(context).copy(
      type = getType()
    )
  }
}

class End() : Parser {
  override fun getType() = "End"

  override fun parse(context: ParsingContext): ParsingContext {
    val currentIndex = context.index + 1
    return if (context.text.length - 1 < currentIndex) {
      context.copy(
        index = currentIndex,
        context = hashMapOf(Pair("end", "reached")),
        type = getType()
      )
    } else {
      context.copy(
        error = Optional.of(
          ParsingError("Can't parse character at index:[${currentIndex}], required:[end] but has more characters")
        ),
        type = "End",
        context = emptyMap()
      )
    }
  }
}

class Any(private vararg val parsers: Parser) : EndOfInputParser() {
  override fun getType() = "Any"

  override fun parseNext(context: ParsingContext): ParsingContext {
    val iterator = parsers.iterator()
    while (iterator.hasNext()) {
      val parserResult = iterator.next().parse(context)
      if (parserResult.success()) {
        return parserResult
      }
    }

    return context.copy(
      error = Optional.of(
        ParsingError(
          "Non of supplied parsers matched:[${parsers.joinToString(separator = ",") { it.getType() }}]"
        )
      )
    )
  }
}

class RepeatUntil(
  private val repeater: Parser,
  private val until: Parser
) : Parser {
  override fun getType() = "RepeatUntil"

  override fun parse(context: ParsingContext): ParsingContext {
    val repeaters = mutableListOf(repeater.parse(context))
    val untils = mutableListOf<ParsingContext>()

    while (true) {
      if (repeaters.last().fail()) {
        return repeaters.last().copy(
          context = hashMapOf(Pair("repeaters", repeaters)),
          type = "RepeatUntil"
        )
      }

      val currentUntil = until.parse(repeaters.last())
      if (currentUntil.success()) {
        untils.add(currentUntil)
        return currentUntil.copy(
          context = hashMapOf(
            Pair("repeaters", repeaters),
            Pair("untils", untils)
          ),
          type = "RepeatUntil"
        )
      }

      repeaters.add(repeater.parse(repeaters.last()))
    }
  }
}




