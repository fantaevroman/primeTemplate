package prime.template.interpreter

import prime.combinator.ParsingContext

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

