package prime.template.interpreter

import prime.combinator.pasers.*
import prime.combinator.pasers.implementations.*

class IncludeInstruction() : BlockInstruction("Block") {
    private val sequenceOf = SequenceOf(
        Str("include"),
        Spaces(),
        DoubleQuote(),
        CustomWord(EnglishLetter().asChar(), Character('/'), Character('.')),
        DoubleQuote()
    ).map {
        it.copy(
            context = hashMapOf(
                Pair(
                    "path",
                    (it.context["sequence"] as List<ParsingContext>)[3].context["word"].toString()
                )
            )
        )
    }

    override fun processInstruction(templateInstructionContext: ParsingContext): ParsingContext {
        return sequenceOf.parse(createContext((templateInstructionContext.context["body"] as String).trim()))
    }

    private fun getPath(templateInstructionContext: ParsingContext): List<String> {
        return templateInstructionContext.context["path"]
            .toString()
            .splitToSequence("/")
            .toList()
            .filter { it.isNotEmpty() }
    }

    override fun generateNewText(processedInstructionContext: ParsingContext,
                                 variables: Map<String, String>,
                                 renderTemplate: RenderTemplateFnType,
                                 renderText: RenderTextFnType
    ): String {
        return renderTemplate(getPath(processedInstructionContext), variables)
            .map { it.text }
            .orElseGet { "Template not found" }
    }
}