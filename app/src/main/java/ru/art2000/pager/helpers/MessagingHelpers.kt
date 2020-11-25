package ru.art2000.pager.helpers

import android.content.Context
import ru.art2000.pager.PagerApplication
import ru.art2000.pager.db.messagesTable
import ru.art2000.pager.hardware.AntennaCommunicator
import ru.art2000.pager.models.Message
import kotlin.concurrent.thread

fun sendMessageAndSave(
    context: Context,
    addressee: Int,
    text: String,
    tone: AntennaCommunicator.Tone = AntennaCommunicator.Tone.A,
    frequency: AntennaCommunicator.Frequency = AntennaCommunicator.Frequency.F2400,
    invert: Boolean = false,
    alpha: Boolean = true
): Int {

    return AntennaCommunicator.sendToPager(
        context,
        addressee,
        text,
        tone,
        frequency,
        invert,
        alpha
    ).also {

        if (it in Message.FIRST_ERROR_CODE..Message.LAST_ERROR_CODE) {
            PagerApplication.Logger.log("Sending result: $it")
        }

        thread {
            messagesTable(context) {
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