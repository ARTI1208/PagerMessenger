package ru.art2000.pager.plugins

import android.icu.text.Transliterator
import ru.art2000.pager.models.CommunicatorPlugin
import ru.art2000.pager.models.PagerMessage

/**
 * Plugin to transliterate cyrillic symbols to latin. This implementation
 * uses Android built-in version of icu4j
 */
class CyrillicToLatinTransliterator : CommunicatorPlugin {

    override fun transform(message: PagerMessage): PagerMessage {
        val transliterator = Transliterator.getInstance("Cyrillic-Latin") ?: return message
        val newText = transliterator.transliterate(message.text)

        return message.copy(text = newText)
    }
}