package prime.template.engine

import prime.combinator.ParsingContext
import prime.combinator.pasers.*
import prime.template.engine.language.allBlocksInTemplate
import prime.template.engine.language.block
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
        return interpretTemplate(path, variables).map {
            val templateBody = interpreter.renderContexts(it, variables, this::renderTemplate)
            Template(templateBody.second)
        }
    }

    private fun interpretTemplate(path: List<String>, variables: Map<String, String>): Optional<List<ParsingContext>> {
        return findTemplate(path)
            .flatMap { template ->
                val parsedTemplateContexts =
                    allBlocksInTemplate().parse(createContext(template.text)).context["textAndBlocks"] as List<ParsingContext>
                interpreter.performContextTransformation(
                    parsedTemplateContexts,
                    variables,
                    template,
                    this::interpretTemplate
                )
            }
    }

    private fun findTemplate(path: List<String>): Optional<Template> {
        return templateCache.cacheTemplate(templateFetcher.fetchTemplate(pathResolver.resolvePath(path)))
    }
}

