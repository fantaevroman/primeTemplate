package prime.template.engine.common

import prime.template.engine.Template
import prime.template.engine.TemplateCache
import java.util.*

class NoTemplateCache : TemplateCache {
  override fun cacheTemplate(template: Optional<Template>): Optional<Template> {
    return template;
  }
}