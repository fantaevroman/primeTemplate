package prime.template.engine

import org.junit.jupiter.api.Test
import prime.template.engine.classloader.ClassLoaderTemplateFetcher
import prime.template.engine.common.NoTemplateCache
import prime.template.engine.common.SamePathResolver
import prime.template.interpreter.PrimeTemplateInterpreter
import prime.template.interpreter.VariableInstruction
import java.util.*
import kotlin.test.assertEquals


class RenderTemplateTests {
  @Test
  fun testRenderHelloWorld() {
    val renderedTemplate =
      TemplateEngine(
        ClassLoaderTemplateFetcher(javaClass),
        NoTemplateCache(),
        SamePathResolver(),
        PrimeTemplateInterpreter(hashSetOf(VariableInstruction()))
      ).renderTemplate(
        listOf("prime","template","engine","testRenderHelloWorld", "helloWorld.txt.prime"),
        hashMapOf(Pair("name", "User's name"))
      )

    assertEquals(Optional.of(Template("Hello User's name!")), renderedTemplate)
  }
}