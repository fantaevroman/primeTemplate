package prime.template.engine

import java.util.*

interface TemplateFetcher {
  fun fetchTemplate(path: List<String>): Optional<Template>
}