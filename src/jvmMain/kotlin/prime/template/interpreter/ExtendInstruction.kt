package prime.template.interpreter

import prime.combinator.pasers.*
import prime.combinator.pasers.implementations.*

class ExtendInstruction() : BlockInstruction("Block") {
    val sequenceOf = SequenceOf(
        Str("extend"),
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

    fun getPath(templateInstructionContext: ParsingContext): String {
        return sequenceOf.parse(createContext((templateInstructionContext.context["body"] as String).trim())).context["path"].toString()
    }

    override fun generateNewText(processedInstructionContext: ParsingContext,
                                 variables: Map<String, String>,
                                 renderTemplate: RenderTemplateFnType,
                                 renderText: RenderTextFnType): String {
        return ""
    }
}