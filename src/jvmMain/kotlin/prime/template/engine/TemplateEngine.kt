package prime.template.engine

import prime.combinator.pasers.*
import prime.template.engine.language.allBlocksInTemplate
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
            val templateBody = interpreter.renderContexts(it, variables, this::renderTemplate, this::renderText)
            Template(templateBody.second)
        }
    }

    fun renderText(text:String, variables: Map<String, String>): Optional<String>{
        return interpretText(text, variables).map {
            val templateBody = interpreter.renderContexts(it, variables, this::renderTemplate, this::renderText)
            templateBody.second
        }
    }

    private fun interpretTemplate(path: List<String>, variables: Map<String, String>): Optional<List<ParsingContext>> {
        return findTemplate(path)
            .flatMap { template ->
                interpretText(template.text, variables)
            }
    }

    private fun interpretText(
        text: String,
        variables: Map<String, String>
    ): Optional<List<ParsingContext>> {
        val parsedTemplateContexts =
            allBlocksInTemplate().parse(createContext(text)).context["textAndBlocks"] as List<ParsingContext>
        return interpreter.performContextTransformation(
            parsedTemplateContexts,
            variables,
            text,
            this::interpretTemplate
        )
    }

    private fun findTemplate(path: List<String>): Optional<Template> {
        return templateCache.cacheTemplate(templateFetcher.fetchTemplate(pathResolver.resolvePath(path)))
    }
}

