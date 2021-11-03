package prime.template.interpreter

import prime.combinator.pasers.*
import prime.combinator.pasers.implementations.Beginning
import prime.combinator.pasers.implementations.End
import prime.combinator.pasers.implementations.SequenceOf
import prime.combinator.pasers.implementations.Word

class VariableInstruction() : BlockInstruction("Block") {
    override fun processInstruction(templateInstructionContext: ParsingContext): ParsingContext {
        val instructionBody = templateInstructionContext.context["body"] as String
        val instructionBodyTrimmed = instructionBody.trim()
        return SequenceOf(Beginning(), Word(), End())
            .map {
                it.copy(
                    context = hashMapOf(
                        Pair(
                            "word",
                            (it.context["sequence"] as List<ParsingContext>)[1].context["word"].toString()
                        )
                    )
                )
            }
            .parse(createContext(instructionBodyTrimmed))
    }

    override fun generateNewText(
        processedInstructionContext: ParsingContext,
        variables: Map<String, String>,
        renderTemplate: RenderTemplateFnType,
        renderText: RenderTextFnType
    ): String {
        val variableName = processedInstructionContext.context["word"] as String
        return variables.getOrDefault(variableName, "variable:[$variableName] not found")
    }
}