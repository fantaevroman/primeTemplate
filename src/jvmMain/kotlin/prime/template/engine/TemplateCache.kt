package prime.template.engine

import java.util.*

interface TemplateCache {
  fun cacheTemplate(template: Optional<Template>): Optional<Template>
}