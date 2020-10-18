package ru.art2000.pager.viewmodels

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import ru.art2000.pager.db.messagesTable
import ru.art2000.pager.hardware.AntennaCommunicator
import ru.art2000.pager.models.Chat
import ru.art2000.pager.models.Message
import kotlin.concurrent.thread

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    fun allMessages(chat: Chat): LiveData<List<Message>> {
        return messagesTable(getApplication()) { liveAllByChatId(chat.addresseeNumber) }
    }

    fun sendMessage(chat: Chat,
                    text: String,
                    tone: AntennaCommunicator.Tone = AntennaCommunicator.Tone.A,
                    frequency: AntennaCommunicator.Frequency = AntennaCommunicator.Frequency.F2400,
                    invert: Boolean = false,
                    alpha: Boolean = true) {

        thread {
            messagesTable(getApplication()) { insertMessage(Message(0, chat.addresseeNumber, text, 0)) }
        }

        val sendResult = AntennaCommunicator.sendToPager(getApplication(), chat.addresseeNumber, text, tone, frequency, invert, alpha)
        when (sendResult) {
            -2 -> {
                Toast.makeText(getApplication(), "Permission denied", Toast.LENGTH_SHORT).show()
            }
            -1 -> {
                Toast.makeText(getApplication(), "Error sending data", Toast.LENGTH_SHORT).show()
            }
            else -> {
                Toast.makeText(getApplication(), "Data successful sent", Toast.LENGTH_SHORT).show()
            }
        }
    }
}