package ru.art2000.pager.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbManager
import ru.art2000.pager.hardware.AntennaCommunicator

const val ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION"

val usbReceiver = object : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (ACTION_USB_PERMISSION == intent.action) {
            synchronized(this) {
                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                    val addressee = intent.getIntExtra("addressee", -1)
                    val text = intent.getStringExtra("text") ?: return@synchronized

                    AntennaCommunicator.sendToPager(context, addressee, text)
                }
            }
        }
    }
}
