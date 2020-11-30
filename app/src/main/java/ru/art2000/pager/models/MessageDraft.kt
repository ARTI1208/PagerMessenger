package ru.art2000.pager.models

import android.os.Parcelable
import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize
import ru.art2000.pager.hardware.AntennaCommunicator

@Parcelize
@Entity(tableName = "drafts")
data class MessageDraft(
    @NonNull
    @PrimaryKey
    @ColumnInfo(name = "draft_chatId")
    override val chatId: Int,
    @ColumnInfo(name = "draft_text")
    override val text: String,
    @ColumnInfo(name = "draft_settings")
    override val settings: Int,
    @ColumnInfo(name = "draft_time")
    override val time: Long = System.currentTimeMillis(),
) : MessageLike, Parcelable {

    constructor(
        chatId: Int,
        text: String,
        tone: AntennaCommunicator.Tone = AntennaCommunicator.Tone.A,
        frequency: AntennaCommunicator.Frequency = AntennaCommunicator.Frequency.F2400,
        invert: Boolean = false,
        alpha: Boolean = true,
    ) : this(
        chatId,
        text,
        AntennaCommunicator.encodeSettings(tone, frequency, invert, alpha).toInt()
    )

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
