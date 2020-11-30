package ru.art2000.pager.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.art2000.pager.R
import ru.art2000.pager.db.addresseeTable
import ru.art2000.pager.db.draftsTable
import ru.art2000.pager.db.messagesTable
import ru.art2000.pager.hardware.AntennaCommunicator
import ru.art2000.pager.helpers.sendMessage
import ru.art2000.pager.models.*
import kotlin.concurrent.thread

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    fun getMessageActions(addressee: Addressee): List<MessageAction> {
        return listOf(
            SimpleAction(
                R.string.message_action_resend,
                { sendMessage(it) },
                { it.isError },
                { false })
        )
    }

    fun renameAddressee(addressee: Addressee, newName: String) {
        addresseeTable(getApplication()) { updateAddressee(Addressee(addressee.number, newName)) }
    }

    fun allPagedReversedMessages(addressee: Addressee): Flow<PagingData<Message>> {
        return Pager(PagingConfig(50, 20, false, maxSize = 150)) {
            messagesTable(getApplication()) { pagedReversedVisibleMessages(addressee.number) }
        }.flow
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
    ): Int = sendMessage(getApplication(), addressee, text, tone, frequency, invert, alpha)

    fun saveDraft(
        addressee: Addressee,
        text: String,
        tone: AntennaCommunicator.Tone = AntennaCommunicator.Tone.A,
        frequency: AntennaCommunicator.Frequency = AntennaCommunicator.Frequency.F2400,
        invert: Boolean = false,
        alpha: Boolean = true
    ) {

        thread {

            draftsTable(getApplication()) {
                updateDraft(
                    MessageDraft(
                        addressee.number,
                        text,
                        tone, frequency, invert, alpha,
                    )
                )
            }
        }
    }
}