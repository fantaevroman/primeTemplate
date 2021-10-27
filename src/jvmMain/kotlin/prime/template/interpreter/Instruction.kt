package prime.template.interpreter

import prime.combinator.ParsingContext
import prime.combinator.pasers.*

interface Instruction {
    fun supportContext(parsingContext: ParsingContext): Boolean
    fun replaceInText(
        parsingContext: ParsingContext,
        text: StringBuilder,
        variables: Map<String, String>,
        charsShift: Int
    ): Int
}

class VariableInstruction() : Instruction {
    private fun parseInstruction(parsingContext: ParsingContext): ParsingContext {
        val instructionBody = parsingContext.context["body"] as String
        val instructionBodyTrimmed = instructionBody.trim()
        return EnglishLetters()
            .parse(createContext(instructionBodyTrimmed))
    }

    override fun supportContext(parsingContext: ParsingContext) : Boolean {
        return if(parsingContext.type == "Block"){
            parseInstruction(parsingContext).success()
        }else {
            false
        }
    }

    override fun replaceInText(
        parsingContext: ParsingContext,
        text: StringBuilder,
        variables: Map<String, String>,
        charsShift: Int
    ): Int {
        val parsedInstruction = parseInstruction(parsingContext)
        val variableName = parsedInstruction.context["letters"] as String
        val newText = variables.getOrDefault(variableName, "variable:[$variableName] not found")
        val start = parsingContext.indexStart.toInt() + charsShift
        val end = parsingContext.indexEnd.toInt() + charsShift + 1
        text.replace(start, end, newText)

        return newText.length - (end - start)
    }
}

class SectionInstruction() : Instruction {
    val sequenceOf = SequenceOf(Str("section"), Spaces(), DoubleQuote(), Word(), DoubleQuote()).map {
        it.copy(
            context = hashMapOf(Pair("sectionName", (it.context["sequence"] as List<ParsingContext>)[3].context["word"].toString()))
        )
    }

    private fun parseInstruction(parsingContext: ParsingContext): ParsingContext {
        val topBlockBody = (parsingContext.context["topBlock"] as String).trim()
        val parsed = sequenceOf.parse(createContext(topBlockBody))
        return parsed.copy(
            context = hashMapOf(Pair("sectionName",parsed.context["sectionName"].toString()),
                Pair("body", parsingContext.context["between"].toString()))
        )
    }

    override fun supportContext(parsingContext: ParsingContext) : Boolean {
        return if(parsingContext.type == "DoubleBlock"){
            parseInstruction(parsingContext).success()
        }else {
            false
        }
    }

    override fun replaceInText(
        parsingContext: ParsingContext,
        text: StringBuilder,
        variables: Map<String, String>,
        charsShift: Int
    ): Int {
        val parsedSection = parseInstruction(parsingContext)
        val start = parsingContext.indexStart.toInt() + charsShift
        val end = parsingContext.indexEnd.toInt()  + charsShift + 1
        val newText = parsedSection.context["body"].toString()
        text.replace(start,end, newText)
        return newText.length - (end - start)
    }
}