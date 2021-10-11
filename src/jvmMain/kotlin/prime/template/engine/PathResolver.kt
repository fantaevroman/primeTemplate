package prime.template.engine

interface PathResolver {
  fun resolvePath(path: List<String>): List<String>
}