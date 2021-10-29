package prime.template.engine

import org.junit.jupiter.api.Test
import prime.template.engine.classloader.ClassLoaderTemplateFetcher
import prime.template.engine.common.NoTemplateCache
import prime.template.engine.common.SamePathResolver
import prime.template.interpreter.ExtendInstruction
import prime.template.interpreter.PrimeTemplateInterpreter
import prime.template.interpreter.SectionInstruction
import prime.template.interpreter.VariableInstruction
import java.util.*
import kotlin.test.assertEquals


class RenderTemplateTests {

    private fun renderTemplate(path: List<String>, variables: Map<String, String>): Optional<Template> {
        return TemplateEngine(
                ClassLoaderTemplateFetcher(javaClass),
                NoTemplateCache(),
                SamePathResolver(),
                PrimeTemplateInterpreter(
                    hashSetOf(
                        VariableInstruction(),
                        SectionInstruction(),
                        ExtendInstruction()
                    )
                )
            ).renderTemplate(
                path,
                variables)
    }

    @Test
    fun testRenderHelloWorld() {
        val renderedTemplate = renderTemplate(
            listOf("prime", "template", "engine", "testRenderHelloWorld", "helloWorld.txt.prime"),
            hashMapOf(Pair("name", "User's name"))
        )
        assertEquals(Optional.of(Template("Section text Hello User's name!")), renderedTemplate)
    }

    @Test
    fun testRenderExtended() {
        val renderedTemplate = renderTemplate(
            listOf("prime", "template", "engine", "testRenderExtended", "child.txt.prime"),
            hashMapOf(Pair("name", "User's name"))
        )
        assertEquals(Optional.of(Template("Parent B")), renderedTemplate)
    }
}