package prime.template.engine

import prime.combinator.ParsingContext
import prime.combinator.pasers.*
import prime.template.interpreter.Instruction
import prime.template.interpreter.PrimeTemplateInterpreter
import java.util.*

class TemplateEngine constructor(
  val templateFetcher: TemplateFetcher,
  val templateCache: TemplateCache,
  val pathResolver: PathResolver,
  val interpreter: PrimeTemplateInterpreter
) {

  fun block() = RepeatableBetween(Str("{{"), AnyCharacter(), Str("}}"))
    .joinBetween { AnyCharacter.join(it) }
    .map {
      val body = (it.context["between"] as ParsingContext).context["str"]!!
      it.copy(context = hashMapOf(Pair("between", body)))
    }

  fun renderTemplate(path: List<String>, variables: Map<String, String>): Optional<Template> {
    return findTemplate(path)
      .map { template ->
        val repeatBlock = RepeatUntil(
          AnyCharacter(),
          block()
        )

        // val parsedTemplateContext = RepeatUntil(Any(block, AnyCharacter()), End()).parse(createContext(template.text))

        val parsedTemplateContexts= listOf(block().parse(createContext(template.text)))
        val transformedContexts = interpreter.performContextTransformation(parsedTemplateContexts, template)
        val templateBody = interpreter.renderContexts(transformedContexts)

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

