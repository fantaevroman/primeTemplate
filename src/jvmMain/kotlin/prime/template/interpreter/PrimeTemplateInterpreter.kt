package prime.template.interpreter

import prime.combinator.ParsingContext
import prime.template.engine.Template
import java.util.*

typealias InterpretTemplateFnType = (path: List<String>, variables: Map<String, String>) -> Optional<List<ParsingContext>>

class PrimeTemplateInterpreter(
    val supportedInstructions: Set<Instruction>
) {

    fun performContextTransformation(
        contexts: List<ParsingContext>,
        variables: Map<String, String>,
        template: Template,
        interpretTemplate: InterpretTemplateFnType
    ): Optional<List<ParsingContext>> {
        val extendPathOpt = Optional.ofNullable(contexts.find { ExtendInstruction().supportContext(it) })
            .map { extendInstructionContext ->
                ExtendInstruction().getPath(extendInstructionContext)
            }

        return if (extendPathOpt.isPresent) {
            interpretTemplate(extendPathOpt.get().splitToSequence("/").toList(), variables)
        } else {
            val withBody = addBodyContext(template, contexts)
            return Optional.of(withBody)
        }
    }

    private fun addBodyContext(
        template: Template,
        contexts: List<ParsingContext>
    ): MutableList<ParsingContext> {
        val withBody = mutableListOf<ParsingContext>()
        withBody.add(
            ParsingContext(
                "",
                0,
                template.text.length.toLong() - 1,
                mapOf(Pair("body", template.text)),
                "body",
                Optional.empty()
            )
        )
        withBody.addAll(contexts)
        return withBody
    }

    fun renderContexts(contexts: List<ParsingContext>, variables: Map<String, String>): Pair<String?, String> {
        val body = contexts.first { it.type == "body" }
        val bodyText = body.context["body"] as String
        val mutableTemplate = StringBuilder(bodyText)
        var charactersShift = 0

        contexts.filter {
            it.type == "Block" || it.type == "DoubleBlock"
        }.forEach { context ->
            val instruction = supportedInstructions.find { it.supportContext(context) }
            if (instruction == null) {
                return Pair(
                    "Can't parse instruction at between index: [${context.indexStart}, ${context.indexEnd} ]",
                    bodyText
                )
            } else {
                charactersShift = instruction.replaceInText(context, mutableTemplate, variables, charactersShift)
            }
        }

        return Pair(null, mutableTemplate.toString())
    }
}