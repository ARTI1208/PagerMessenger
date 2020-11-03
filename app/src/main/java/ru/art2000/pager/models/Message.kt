package ru.art2000.pager.models

import android.os.Parcelable
import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize
import ru.art2000.pager.hardware.AntennaCommunicator

@Parcelize
@Entity(tableName = "messages")
data class Message(
    @NonNull
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val chatId: Int,
    val text: String,
    val settings: Int,
    val status: Int = 0,
    val time: Long = System.currentTimeMillis(),
) : Parcelable {

    constructor(id: Int = 0,
                chatId: Int,
                text: String,
                tone: AntennaCommunicator.Tone = AntennaCommunicator.Tone.A,
                frequency: AntennaCommunicator.Frequency = AntennaCommunicator.Frequency.F2400,
                invert: Boolean = false,
                alpha: Boolean = true,
                status: Int = 0,
                time: Long = System.currentTimeMillis()
    ): this(id, chatId, text, AntennaCommunicator.encodeSettings(tone, frequency, invert, alpha).toInt(), status, time)

    companion object {

        const val STATUS_CHAT_CREATED = -8

        const val STATUS_DRAFT = -7

        const val STATUS_SEND_ERROR = -1
    }

    val isDraft: Boolean
        get() = status == STATUS_DRAFT

    val isError: Boolean
        get() = status in -2..-1

    val isOk: Boolean
        get() = status >= 0

    val tone: AntennaCommunicator.Tone
        get() = when {
            (settings and 0x10 * 3) == 0x10 * 3 -> AntennaCommunicator.Tone.D
            (settings and 0x10 * 2) == 0x10 * 2 -> AntennaCommunicator.Tone.C
            (settings and 0x10 * 1) == 0x10 * 1 -> AntennaCommunicator.Tone.B
            else -> AntennaCommunicator.Tone.A
        }

    val frequency: AntennaCommunicator.Frequency
        get() = when {
            (settings and 0x2 * 2) != 0 -> AntennaCommunicator.Frequency.F2400
            (settings and 0x2 * 1) != 0 -> AntennaCommunicator.Frequency.F1200
            else -> AntennaCommunicator.Frequency.F512
        }

    val invert: Boolean
        get() = (settings and 0x1) != 0

    val alpha: Boolean
        get() = (settings and 0x8) != 0
}