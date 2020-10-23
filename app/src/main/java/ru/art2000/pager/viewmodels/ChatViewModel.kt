package ru.art2000.pager.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import ru.art2000.pager.R
import ru.art2000.pager.db.addresseeTable
import ru.art2000.pager.db.messagesTable
import ru.art2000.pager.hardware.AntennaCommunicator
import ru.art2000.pager.models.*
import kotlin.concurrent.thread

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    fun getMessageActions(chat: Chat): List<MessageAction> {
        return listOf(
            SimpleAction(
                R.string.message_action_resend,
                { sendMessage(it) },
                { it.status < 0 },
                { it.status < 0 })
        )
    }

    fun getAddressee(chat: Chat): Addressee? {
        return addresseeTable(getApplication()) { byNumber(chat.addresseeNumber) }
    }

    fun renameAddressee(chat: Chat, newName: String) {
        addresseeTable(getApplication()) {
            deleteAddressee(chat.addresseeNumber)
            insertAddressee(Addressee(chat.addresseeNumber, newName))
        }
    }

    fun allMessages(chat: Chat): LiveData<List<Message>> {
        return messagesTable(getApplication()) { liveAllByChatId(chat.addresseeNumber) }
    }

    private fun sendMessage(message: Message): Int = sendMessage(
        Chat(message.chatId),
        message.text,
        message.tone,
        message.frequency,
        message.invert,
        message.alpha
    )

    fun sendMessage(
        chat: Chat,
        text: String,
        tone: AntennaCommunicator.Tone = AntennaCommunicator.Tone.A,
        frequency: AntennaCommunicator.Frequency = AntennaCommunicator.Frequency.F2400,
        invert: Boolean = false,
        alpha: Boolean = true
    ): Int {

        return AntennaCommunicator.sendToPager(
            getApplication(),
            chat.addresseeNumber,
            text,
            tone,
            frequency,
            invert,
            alpha
        ).also {
            thread {
                messagesTable(getApplication()) {
                    safeInsertMessage(
                        Message(
                            0,
                            chat.addresseeNumber,
                            text,
                            AntennaCommunicator.encodeSettings(tone, frequency, invert, alpha)
                                .toInt(),
                            it
                        )
                    )
                }
            }
        }
    }

    fun cleanUp(chat: Chat) {

    }
}