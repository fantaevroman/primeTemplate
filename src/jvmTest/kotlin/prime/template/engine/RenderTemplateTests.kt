package prime.template.engine

import org.junit.jupiter.api.Test
import prime.template.engine.classloader.ClassLoaderTemplateFetcher
import prime.template.engine.common.NoTemplateCache
import prime.template.engine.common.SamePathResolver
import prime.template.interpreter.*
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
                        ExtendInstruction(),
                        IncludeInstruction()
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
        assertEquals(Optional.of(Template("Parent B Section B modified from child C!")), renderedTemplate)
    }

    @Test
    fun testRenderIncluded() {
        val renderedTemplate = renderTemplate(
            listOf("prime", "template", "engine", "testRenderIncluded", "base.txt.prime"),
            hashMapOf(Pair("name", "User's name"))
        )
        assertEquals(Optional.of(Template("Base template: Included template text")), renderedTemplate)
    }

    @Test
    fun testRenderExtendWithInclusion() {
        val renderedTemplate = renderTemplate(
            listOf("prime", "template", "engine", "testRenderExtendWithInclusion", "child.txt.prime"),
            hashMapOf(Pair("name", "User's name"))
        )
        assertEquals(Optional.of(Template("Parent B: Section B with included text!")), renderedTemplate)
    }
}