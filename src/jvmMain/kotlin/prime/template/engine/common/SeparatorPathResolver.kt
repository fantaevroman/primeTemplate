package prime.template.engine.common

import prime.template.engine.PathResolver

class SeparatorPathResolver : PathResolver {
  override fun resolvePath(path: List<String>): List<String> {
    return path;
  }
}