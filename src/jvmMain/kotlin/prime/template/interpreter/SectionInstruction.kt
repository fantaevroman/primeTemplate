package prime.template.interpreter

import prime.combinator.ParsingContext
import prime.combinator.pasers.*

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