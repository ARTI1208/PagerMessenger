package ru.art2000.pager.plugins

import com.ibm.icu.text.Transliterator
import ru.art2000.pager.models.CommunicatorPlugin
import ru.art2000.pager.models.PagerMessage

/**
 * Plugin to transliterate cyrillic symbols to latin. This implementation
 * uses external IBM icu4j library, as Android version requires minSdkApi 29
 */
class CyrillicToLatinTransliterator : CommunicatorPlugin {

    override fun transform(message: PagerMessage): PagerMessage {
        val transliterator = Transliterator.getInstance("Cyrillic-Latin") ?: return message
        val newText = transliterator.transliterate(message.text)

        return message.copy(text = newText)
    }
}