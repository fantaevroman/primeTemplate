package prime.template.engine

import prime.combinator.ParsingContext
import prime.combinator.pasers.*
import prime.template.engine.language.allBlocksInTemplate
import prime.template.interpreter.Instruction
import prime.template.interpreter.PrimeTemplateInterpreter
import java.util.*

class TemplateEngine constructor(
    val templateFetcher: TemplateFetcher,
    val templateCache: TemplateCache,
    val pathResolver: PathResolver,
    val interpreter: PrimeTemplateInterpreter
) {

    fun renderTemplate(path: List<String>, variables: Map<String, String>): Optional<Template> {
        return findTemplate(path)
            .map { template ->
                val parsedTemplateContexts = allBlocksInTemplate().parse(createContext(template.text)).context["textAndBlocks"] as List<ParsingContext>
                val transformedContexts = interpreter.performContextTransformation(parsedTemplateContexts, template)
                val templateBody = interpreter.renderContexts(transformedContexts, variables)

                Template(templateBody.second)
            }
    }

    fun findTemplate(path: List<String>): Optional<Template> {
        return templateCache.cacheTemplate(templateFetcher.fetchTemplate(pathResolver.resolvePath(path)))
    }

    data class TemplateWithInstructions(
        val templateWithReplacedInstructions: Template,
        val instructionWithReplaceId: Set<Pair<Instruction, String>>
    )
}

