package prime.template.interpreter

import prime.combinator.Parser
import prime.combinator.ParsingContext
import prime.combinator.pasers.*

interface Instruction {
    fun supportContext(parsingContext: ParsingContext): Boolean
    fun     replaceInText(parsingContext: ParsingContext,
                      text: StringBuilder,
                      variables: Map<String, String>): StringBuilder
}

class VariableInstruction() : Instruction {
    private fun parseInstruction(parsingContext: ParsingContext): ParsingContext {
        val instructionBody = parsingContext.context["body"] as String
        val instructionBodyTrimmed = instructionBody.trim()
        return EnglishLetters()
            .parse(createContext(instructionBodyTrimmed))
    }

    override fun supportContext(parsingContext: ParsingContext) = parseInstruction(parsingContext).success()

    override fun replaceInText(parsingContext: ParsingContext,
                               text: StringBuilder,
                               variables: Map<String, String>): StringBuilder {
        val parsedInstruction = parseInstruction(parsingContext)
        val variableName = parsedInstruction.context["letters"] as String
        text.replace(parsingContext.indexStart.toInt(), parsingContext.indexEnd.toInt() + 1,
            variables.getOrDefault(variableName, "variable:[$variableName] not found") )
        return text
    }
}


