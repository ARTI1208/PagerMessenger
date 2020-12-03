package ru.art2000.pager.plugins

import com.ibm.icu.text.Transliterator
import ru.art2000.pager.models.CommunicatorPlugin
import ru.art2000.pager.models.PagerMessage

class CyrillicToLatinTransliterator : CommunicatorPlugin {

    override fun transform(message: PagerMessage): PagerMessage {
        val transliterator = Transliterator.getInstance("Cyrillic-Latin") ?: return message
        val newText = transliterator.transliterate(message.text)

        return message.copy(text = newText)
    }
}