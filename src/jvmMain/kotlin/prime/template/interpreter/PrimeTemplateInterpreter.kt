package prime.template.interpreter

import prime.combinator.ParsingContext
import prime.template.engine.Template
import java.util.*

class PrimeTemplateInterpreter(val supportedInstructions: Set<Instruction>) {

  fun performContextTransformation(contexts: List<ParsingContext>, template: Template): List<ParsingContext> {
    val withBody = mutableListOf<ParsingContext>()
    withBody.addAll(contexts)
    withBody.add(ParsingContext("", 0, mapOf(Pair("body", template.text)), "body", Optional.empty()))
    return withBody
  }

  fun renderContexts(contexts: List<ParsingContext>): Pair<String?, String> {
    val body = contexts.first { it.type == "body" }
    val bodyText = body.context["body"] as String
    val mutableTemplate = StringBuilder(bodyText)

    contexts.filter {
      it.type != "body"
    }.forEach { context ->
      val instruction = supportedInstructions.find { it.supportContext(context) }
      if (instruction == null) {
        return Pair("Can't parse instruction at index: ${context.index}", bodyText)
      } else {
        instruction.replaceInText(mutableTemplate)
      }
    }

    return Pair(null, mutableTemplate.toString())
  }
}