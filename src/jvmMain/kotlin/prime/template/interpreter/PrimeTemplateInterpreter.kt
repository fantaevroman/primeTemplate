package prime.template.interpreter

import prime.combinator.ParsingContext
import prime.template.engine.Template
import java.util.*

class PrimeTemplateInterpreter(val supportedInstructions: Set<Instruction>) {

  fun performContextTransformation(contexts: List<ParsingContext>, template: Template): List<ParsingContext> {
    val withBody = mutableListOf<ParsingContext>()
    withBody.add(ParsingContext("", 0,template.text.length.toLong() - 1, mapOf(Pair("body", template.text)), "body", Optional.empty()))
    withBody.addAll(contexts)
    return withBody
  }

  fun renderContexts(contexts: List<ParsingContext>, variables: Map<String, String>): Pair<String?, String> {
    val body = contexts.first { it.type == "body" }
    val bodyText = body.context["body"] as String
    val mutableTemplate = StringBuilder(bodyText)

    contexts.filter {
      it.type == "Block"
    }.forEach { context ->
      val instruction = supportedInstructions.find { it.supportContext(context) }
      if (instruction == null) {
        return Pair("Can't parse instruction at between index: [${context.indexStart}, ${context.indexEnd} ]", bodyText)
      } else {
        instruction.replaceInText(context, mutableTemplate, variables)
      }
    }

    return Pair(null, mutableTemplate.toString())
  }
}