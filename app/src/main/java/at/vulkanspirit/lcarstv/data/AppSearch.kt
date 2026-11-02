package at.vulkanspirit.lcarstv.data

/** Pure search and favorites helpers, kept free of Android types so they are unit-testable. */
object AppSearch {
    /**
     * Ranks: label starts with query, then a word in the label starts with it,
     * then label contains it, then package name contains it.
     */
    fun <T> search(items: List<T>, query: String, label: (T) -> String, pkg: (T) -> String): List<T> {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return items
        return items.mapNotNull { item ->
            val l = label(item).lowercase()
            val rank = when {
                l.startsWith(q) -> 0
                l.split(' ', '-', '_', '.').any { it.startsWith(q) } -> 1
                l.contains(q) -> 2
                pkg(item).lowercase().contains(q) -> 3
                else -> return@mapNotNull null
            }
            rank to item
        }.sortedBy { it.first }.map { it.second }
    }
}

object Favorites {
    fun toggle(favorites: List<String>, pkg: String): List<String> =
        if (pkg in favorites) favorites - pkg else favorites + pkg

    fun moveUp(favorites: List<String>, pkg: String): List<String> {
        val i = favorites.indexOf(pkg)
        if (i <= 0) return favorites
        return favorites.toMutableList().apply { add(i - 1, removeAt(i)) }
    }

    fun moveDown(favorites: List<String>, pkg: String): List<String> {
        val i = favorites.indexOf(pkg)
        if (i < 0 || i >= favorites.lastIndex) return favorites
        return favorites.toMutableList().apply { add(i + 1, removeAt(i)) }
    }

    /** Drops favorites whose app is no longer installed, keeping order. */
    fun prune(favorites: List<String>, installed: Set<String>): List<String> =
        favorites.filter { it in installed }
}
