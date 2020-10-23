package ru.art2000.pager.models

import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.art2000.pager.hardware.AntennaCommunicator

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
) {

    val tone: AntennaCommunicator.Tone
        get() = when {
            (settings and 0x10 * 3) != 0 -> AntennaCommunicator.Tone.D
            (settings and 0x10 * 2) != 0 -> AntennaCommunicator.Tone.C
            (settings and 0x10 * 1) != 0 -> AntennaCommunicator.Tone.B
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