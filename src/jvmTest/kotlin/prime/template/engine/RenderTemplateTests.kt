package prime.template.engine

import org.junit.jupiter.api.Test
import prime.template.engine.classloader.ClassLoaderTemplateFetcher
import prime.template.engine.common.NoTemplateCache
import prime.template.engine.common.SamePathResolver
import prime.template.interpreter.PrimeTemplateInterpreter
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
        PrimeTemplateInterpreter(hashSetOf())
      ).renderTemplate(
        listOf("prime","template","engine","testRenderHelloWorld", "helloWorld.txt.prime"),
        emptyMap()
      )


    assertEquals(Optional.of("Hello world!"), renderedTemplate)
  }
}