package ru.art2000.pager.plugins

object PluginManager {

    val allPlugins: List<CyrillicToLatinTransliterator>
        get() {
            return listOf(CyrillicToLatinTransliterator())
        }
}