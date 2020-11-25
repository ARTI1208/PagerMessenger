@file:Suppress("EXPERIMENTAL_API_USAGE")

package ru.art2000.pager.hardware

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import androidx.core.content.ContextCompat
import com.hoho.android.usbserial.driver.UsbSerialDriver
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber
import ru.art2000.pager.extensions.toInt
import ru.art2000.pager.models.Message
import ru.art2000.pager.receivers.ACTION_USB_PERMISSION

object AntennaCommunicator {

    enum class Tone { A, B, C, D }

    enum class Frequency(val value: Int) { F512(512), F1200(1200), F2400(2400), }

    private fun getAntennaDriver(usbManager: UsbManager): UsbSerialDriver? {
        val availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(usbManager)
        return availableDrivers.firstOrNull()
    }

    public fun encodeSettings(
        tone: Tone,
        frequency: Frequency,
        invert: Boolean,
        alpha: Boolean
    ): Byte {

        return (-0x80 + 0x10 * tone.ordinal + 0x8 * alpha.toInt() + 0x2 * frequency.ordinal + 0x1 * invert.toInt()).toByte()
    }

    @ExperimentalUnsignedTypes
    private fun encodeToBytes(
        addressee: Int,
        text: String,
        tone: Tone = Tone.A,
        frequency: Frequency = Frequency.F2400,
        invert: Boolean = false,
        alpha: Boolean = true
    ): ByteArray {
        val prefix = byteArrayOf(0x19, 0x52)
        val addresseeBytes = addressee.toString().toByteArray()

        val settingsBytes = byteArrayOf(encodeSettings(tone, frequency, invert, alpha))

        val postfix = byteArrayOf(0x18u.toByte())

        return prefix + addresseeBytes + settingsBytes + text.toByteArray() + postfix
    }

    fun sendToPager(
        context: Context,
        addressee: Int,
        text: String,
        tone: Tone = Tone.A,
        frequency: Frequency = Frequency.F2400,
        invert: Boolean = false,
        alpha: Boolean = true
    ): Int {
        val usbManager = ContextCompat.getSystemService(context, UsbManager::class.java)!!
        val usbDriver = getAntennaDriver(usbManager) ?: return Message.DRIVER_NOT_FOUND

        if (!usbManager.hasPermission(usbDriver.device)) {
            requestPermission(context, usbManager, usbDriver.device, addressee, text)
            return Message.USB_PERMISSION_RESTRICTED
        }

        val bytes = encodeToBytes(addressee, text, tone, frequency, invert, alpha)

        return sendToPager(usbManager, usbDriver, bytes)
    }

    private fun requestPermission(
        context: Context,
        usbManager: UsbManager,
        device: UsbDevice,
        addressee: Int,
        text: String
    ) {
        val permissionIntent = PendingIntent.getBroadcast(
            context, 0, Intent(
                ACTION_USB_PERMISSION
            ).apply {
                putExtra("addressee", addressee)
                putExtra("text", text)
            }, 0
        )
        usbManager.requestPermission(device, permissionIntent)
    }

    private fun sendToPager(
        usbManager: UsbManager,
        usbDriver: UsbSerialDriver,
        bytes: ByteArray
    ): Int {

        val connection = usbManager.openDevice(usbDriver.device) ?: return Message.DEVICE_OPEN_FAILED

        val port = usbDriver.ports[0]

        port.open(connection)
        port.setParameters(9600, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE)

        val res = port.write(bytes, 0)

        port.close()

        return res
    }

}