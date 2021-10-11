package prime.template.engine.classloader

import prime.template.engine.Template
import prime.template.engine.TemplateFetcher
import java.util.*

class ClassLoaderTemplateFetcher(private val classLoader: Class<Any>): TemplateFetcher {
  override fun fetchTemplate(path: List<String>): Optional<Template> {
    return getResourceAsText(path.joinToString(separator = "/")).map { Template(it) }
  }

  private fun getResourceAsText(path: String): Optional<String> {
    val resource = classLoader.getResource("/$path")
    return Optional.ofNullable(resource).map {
      resource.readText()
    }
  }
}