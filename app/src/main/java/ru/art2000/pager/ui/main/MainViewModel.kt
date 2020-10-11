package ru.art2000.pager.ui.main

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import ru.art2000.pager.hardware.AntennaCommunicator

@ExperimentalUnsignedTypes
class MainViewModel(app: Application) : AndroidViewModel(app) {

    fun sendToPager(
        addressee: Int,
        text: String,
        tone: AntennaCommunicator.Tone = AntennaCommunicator.Tone.A,
        frequency: AntennaCommunicator.Frequency = AntennaCommunicator.Frequency.F2400,
        invert: Boolean = false,
        alpha: Boolean = true
    ) {
        val sendResult = AntennaCommunicator.sendToPager(getApplication(), addressee, text, tone, frequency, invert, alpha)
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