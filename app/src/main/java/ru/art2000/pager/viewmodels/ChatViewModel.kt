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

    fun getMessageActions(chat: ChatView): List<MessageAction> {
        return listOf(
            SimpleAction(
                R.string.message_action_resend,
                { sendMessage(it) },
                { it.status < 0 },
                { it.status < 0 })
        )
    }

    fun renameAddressee(addressee: Addressee, newName: String) {
        addresseeTable(getApplication()) {
            deleteAddressee(addressee.number)
            insertAddressee(Addressee(addressee.number, newName))
        }
    }

    fun allMessages(chat: ChatView): LiveData<List<Message>> {
        return messagesTable(getApplication()) { liveAllByChatId(chat.addressee.number) }
    }

    private fun sendMessage(message: Message): Int = sendMessage(
        message.chatId,
        message.text,
        message.tone,
        message.frequency,
        message.invert,
        message.alpha
    )

    fun sendMessage(
        addressee: Int,
        text: String,
        tone: AntennaCommunicator.Tone = AntennaCommunicator.Tone.A,
        frequency: AntennaCommunicator.Frequency = AntennaCommunicator.Frequency.F2400,
        invert: Boolean = false,
        alpha: Boolean = true
    ): Int {

        return AntennaCommunicator.sendToPager(
            getApplication(),
            addressee,
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
                            addressee,
                            text,
                            tone, frequency, invert, alpha,
                            it
                        )
                    )
                    deleteDrafts(addressee)
                }
            }
        }
    }

    fun saveDraft(chat: ChatView,
                  text: String,
                  tone: AntennaCommunicator.Tone = AntennaCommunicator.Tone.A,
                  frequency: AntennaCommunicator.Frequency = AntennaCommunicator.Frequency.F2400,
                  invert: Boolean = false,
                  alpha: Boolean = true) {
        thread {
            messagesTable(getApplication()) {
                insertOrUpdateDraft(
                    Message(
                        0,
                        chat.addressee.number,
                        text,
                        tone, frequency, invert, alpha,
                        Message.STATUS_DRAFT
                    )
                )
            }
        }
    }
}