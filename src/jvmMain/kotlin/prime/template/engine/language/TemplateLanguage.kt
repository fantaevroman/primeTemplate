package prime.template.engine.language

import prime.combinator.Parser
import prime.combinator.ParsingContext
import prime.combinator.pasers.*
import prime.combinator.pasers.Any


// {{ name }}
fun generalBlock(left: Parser, right: Parser) = RepeatableBetween(left, AnyCharacter(), right)
    .joinBetween { AnyCharacter.join(it) }
    .map {
        val body = (it.context["between"] as ParsingContext).context["str"]!!
        it.copy(
            context = hashMapOf(Pair("body", body)),
            type = "Block"
        )
    }

fun block() = generalBlock(Str("{{"), Str("}}"))


//{{ for }} text {{ end for }}
fun doubleBlock() = RepeatableBetween(block(), AnyCharacter(), block())
    .mapEach { currentContext, currentParser, previous, currentIndex ->
        if (currentIndex == 2) {
            val firstCommandInBody =
                Word()
                    .parse(createContext(previous[0].context["body"].toString().trim()))
                    .context["word"].toString()
            generalBlock(Str("{{ end $firstCommandInBody"), Str("}}"))
        } else {
            currentParser
        }
    }
    .joinBetween {
        AnyCharacter.join(it)
    }
    .map {
        it.copy(
            context = hashMapOf(
                Pair("topBlock", (it.context["left"] as ParsingContext).context["body"]!!),
                Pair("between", (it.context["between"] as ParsingContext).context["str"]!!),
                Pair("bottomBlock", (it.context["right"] as ParsingContext).context["body"]!!)
            ),
            type = "DoubleBlock"
        )
    }

fun allBlocksInTemplate() = RepeatableBetween(Beginning(), Any(doubleBlock(), block(), AnyCharacter()), End())
    .joinBetweenCharsToStrings()
    .map {
        it.copy(
            context = hashMapOf(Pair("textAndBlocks", it.context["between"]!!)),
            type = "AllBlocksBetweenText"
        )
    }