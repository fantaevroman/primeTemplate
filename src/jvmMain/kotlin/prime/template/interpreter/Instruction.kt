package prime.template.interpreter

import prime.combinator.ParsingContext
import prime.combinator.pasers.*

interface Instruction {
    fun supportContext(templateInstructionContext: ParsingContext): Boolean
    fun replaceInText(
        templateInstructionContext: ParsingContext,
        text: StringBuilder,
        variables: Map<String, String>,
        charsShift: Int
    ): Int
}

abstract class BlockInstruction(val blockType: String) : Instruction {
    abstract fun processInstruction(templateInstructionContext: ParsingContext): ParsingContext
    abstract fun generateNewText(processedInstructionContext: ParsingContext, variables: Map<String, String>): String

    override fun supportContext(templateInstructionContext: ParsingContext): Boolean {
        return if (templateInstructionContext.type == blockType) {
            processInstruction(templateInstructionContext).success()
        } else {
            false
        }
    }

    private fun replace(
        templateInstructionContext: ParsingContext,
        text: StringBuilder,
        charsShift: Int,
        newText: String
    ): Int {
        val start = templateInstructionContext.indexStart.toInt() + charsShift
        val end = templateInstructionContext.indexEnd.toInt() + charsShift + 1
        text.replace(start, end, newText)
        return newText.length - (end - start)
    }

    override fun replaceInText(
        templateInstructionContext: ParsingContext,
        text: StringBuilder,
        variables: Map<String, String>,
        charsShift: Int
    ): Int {
        val processedInstructionContext = processInstruction(templateInstructionContext)
        val renderedInstructionText = generateNewText(processedInstructionContext, variables)
        return replace(templateInstructionContext, text, charsShift, renderedInstructionText)
    }
}

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

class SectionInstruction() : BlockInstruction("DoubleBlock") {
    val sequenceOf = SequenceOf(Str("section"), Spaces(), DoubleQuote(), Word(), DoubleQuote()).map {
        it.copy(
            context = hashMapOf(
                Pair(
                    "sectionName",
                    (it.context["sequence"] as List<ParsingContext>)[3].context["word"].toString()
                )
            )
        )
    }

    override fun processInstruction(templateInstructionContext: ParsingContext): ParsingContext {
        val topBlockBody = (templateInstructionContext.context["topBlock"] as String).trim()
        val parsed = sequenceOf.parse(createContext(topBlockBody))
        return parsed.copy(
            context = hashMapOf(
                Pair("sectionName", parsed.context["sectionName"].toString()),
                Pair("body", templateInstructionContext.context["between"].toString())
            )
        )
    }

    override fun generateNewText(processedInstructionContext: ParsingContext, variables: Map<String, String>): String {
        return processedInstructionContext.context["body"].toString()
    }
}