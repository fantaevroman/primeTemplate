package prime.template.interpreter

import prime.combinator.pasers.ParsingContext
import prime.template.engine.Template
import java.util.*
import kotlin.collections.ArrayList

typealias InterpretTemplateFnType = (path: List<String>, variables: Map<String, String>) -> Optional<List<ParsingContext>>
typealias RenderTemplateFnType = (path: List<String>, variables: Map<String, String>) -> Optional<Template>
typealias RenderTextFnType = (String, Map<String, String>) -> Optional<String>

class PrimeTemplateInterpreter(
    val supportedInstructions: Set<Instruction>
) {

    fun performContextTransformation(
        contexts: List<ParsingContext>,
        variables: Map<String, String>,
        text: String,
        interpretTemplate: InterpretTemplateFnType
    ): Optional<List<ParsingContext>> {
        val extendPathOpt = Optional.ofNullable(contexts.find { ExtendInstruction().supportContext(it) })
            .map { extendInstructionContext ->
                ExtendInstruction().getPath(extendInstructionContext)
            }

        return if (extendPathOpt.isPresent) {
            interpretTemplate(extendPathOpt.get().splitToSequence("/").toList().filter { it.isNotEmpty() }, variables)
                .map { interpretedParentContexts ->
                    replaceSections(contexts, interpretedParentContexts)
                }
        } else {
            val withBody = addBodyContext(text, contexts)
            return Optional.of(withBody)
        }
    }

    private fun replaceSections(source: List<ParsingContext>, target: List<ParsingContext>): List<ParsingContext> {
        val sectionNameWithTemplateContext = source.filter { SectionInstruction().supportContext(it) }.map {
            Pair(SectionInstruction().processInstruction(it).context["sectionName"], it)
        }.toMap()

        val targetMutable = ArrayList(target)
        targetMutable.replaceAll { from ->
            if (SectionInstruction().supportContext(from)) {
                val processInstruction = SectionInstruction().processInstruction(from)
                sectionNameWithTemplateContext.getOrDefault(processInstruction.context["sectionName"], from).copy(
                    indexStart = from.indexStart,
                    indexEnd = from.indexEnd
                )
            } else {
                from
            }
        }

        return targetMutable
    }

    private fun addBodyContext(
        text: String,
        contexts: List<ParsingContext>
    ): MutableList<ParsingContext> {
        val withBody = mutableListOf<ParsingContext>()
        withBody.add(
            ParsingContext(
                "",
                0,
                text.length.toLong() - 1,
                mapOf(Pair("body", text)),
                "body",
                Optional.empty()
            )
        )
        withBody.addAll(contexts)
        return withBody
    }

    fun renderContexts(
        contexts: List<ParsingContext>,
        variables: Map<String, String>,
        renderTemplate: RenderTemplateFnType,
        renderText: RenderTextFnType
    ): Pair<String?, String> {
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
                charactersShift = instruction.replaceInText(
                    context,
                    mutableTemplate,
                    variables,
                    charactersShift,
                    renderTemplate,
                    renderText
                )
            }
        }

        return Pair(null, mutableTemplate.toString())
    }
}