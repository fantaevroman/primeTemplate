package prime.template.engine.common

import prime.template.engine.PathResolver

class SamePathResolver : PathResolver {
  override fun resolvePath(path: List<String>): List<String> {
    return path;
  }
}