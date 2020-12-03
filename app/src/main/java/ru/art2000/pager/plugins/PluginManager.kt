package ru.art2000.pager.plugins

import ru.art2000.pager.models.CommunicatorPlugin

object PluginManager {

    val allPlugins: List<CommunicatorPlugin>
        get() {
            return listOf(CyrillicToLatinTransliterator())
        }
}