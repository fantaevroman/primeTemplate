package prime.template.interpreter

import prime.combinator.Parser
import prime.combinator.ParsingContext

interface Instruction{
  fun supportContext(parsingContext: ParsingContext): Boolean
  fun replaceInText(text: StringBuilder): StringBuilder
}


