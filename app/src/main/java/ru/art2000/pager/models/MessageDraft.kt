package ru.art2000.pager.models

import android.os.Parcelable
import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
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
) : LocalMessageLike, Parcelable {

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

}
