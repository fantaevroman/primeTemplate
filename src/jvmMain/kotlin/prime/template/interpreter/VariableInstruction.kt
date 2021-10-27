package prime.template.interpreter

import prime.combinator.ParsingContext
import prime.combinator.pasers.EnglishLetters
import prime.combinator.pasers.createContext

class VariableInstruction() : BlockInstruction("Block") {
    override fun processInstruction(templateInstructionContext: ParsingContext): ParsingContext {
        val instructionBody = templateInstructionContext.context["body"] as String
        val instructionBodyTrimmed = instructionBody.trim()
        return EnglishLetters()
            .parse(createContext(instructionBodyTrimmed))
    }

    override fun generateNewText(processedInstructionContext: ParsingContext, variables: Map<String, String>): String {
        val variableName = processedInstructionContext.context["letters"] as String
        return variables.getOrDefault(variableName, "variable:[$variableName] not found")
    }
}