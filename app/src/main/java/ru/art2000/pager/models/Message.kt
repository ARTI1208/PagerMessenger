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
    override val chatId: Int,
    override val text: String,
    override val settings: Int,
    val status: Int = 0,
    override val time: Long = System.currentTimeMillis(),
) : LocalMessageLike {

    constructor(
        id: Int = 0,
        chatId: Int,
        text: String,
        tone: AntennaCommunicator.Tone = AntennaCommunicator.Tone.A,
        frequency: AntennaCommunicator.Frequency = AntennaCommunicator.Frequency.F2400,
        invert: Boolean = false,
        alpha: Boolean = true,
        status: Int = 0,
        time: Long = System.currentTimeMillis()
    ) : this(
        id,
        chatId,
        text,
        AntennaCommunicator.encodeSettings(tone, frequency, invert, alpha).toInt(),
        status,
        time
    )

    companion object {

        // Error codes
        const val DRIVER_NOT_FOUND = -1

        const val USB_PERMISSION_RESTRICTED = -2

        const val DEVICE_OPEN_FAILED = -3

        // Event codes
        const val STATUS_CHAT_CREATED = -8

        // Helpers
        const val FIRST_ERROR_CODE = DEVICE_OPEN_FAILED

        const val LAST_ERROR_CODE = DRIVER_NOT_FOUND

        const val FIRST_VISIBLE_CODE = FIRST_ERROR_CODE
    }

    val isError: Boolean
        get() = status in FIRST_ERROR_CODE..LAST_ERROR_CODE

    val isOk: Boolean
        get() = status >= 0
}