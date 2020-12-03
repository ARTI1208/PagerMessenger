package ru.art2000.pager.receivers

import android.app.Notification
import android.content.ComponentName
import android.content.pm.PackageManager
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import ru.art2000.pager.PagerApplication
import ru.art2000.pager.helpers.sendMessage
import ru.art2000.pager.viewmodels.ForwardingViewModel
import java.io.FileNotFoundException
import java.util.*

//TODO remove apps from list after being uninstalled?
class NotificationListener : NotificationListenerService() {

    override fun onListenerDisconnected() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            requestRebind(ComponentName(this, NotificationListener::class.java))
        }
    }

    @Suppress("DEPRECATION")
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (sbn.notification.flags and Notification.FLAG_FOREGROUND_SERVICE != 0
            && sbn.notification.category != Notification.CATEGORY_CALL
        ) return

        val title = sbn.notification.extras[Notification.EXTRA_TITLE]?.toString()
        val text = sbn.notification.extras[Notification.EXTRA_TEXT]?.toString()

        if (title.isNullOrEmpty() && text.isNullOrEmpty()) return

        val inputStream = try {
            openFileInput(ForwardingViewModel.DATA_FILE_NAME)
        } catch (e: FileNotFoundException) {
            null
        } ?: return

        val addresseeIds = hashSetOf<Int>()

        inputStream.reader().use { reader ->
            reader.forEachLine {
                val split = it.split("=")
                if (split.size != 2) return@forEachLine

                val addresseeId = split.first().toIntOrNull() ?: return@forEachLine

                if (split[1] == sbn.packageName) {
                    addresseeIds += addresseeId
                }
            }
        }

        if (addresseeIds.isEmpty()) return

        PagerApplication.Logger.log(
            "Fetched notification of ${sbn.packageName} with id = ${sbn.id}, postTime = ${
                Date(
                    sbn.postTime
                )
            }"
        )

        val notificationAppInfo = try {
            packageManager.getApplicationInfo(sbn.packageName, 0)
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }

        val notificationAppTitle = if (notificationAppInfo == null)
            "Unknown"
        else
            packageManager.getApplicationLabel(notificationAppInfo)

        val forwardText =
            "*$notificationAppTitle*" + (if (title != null) " !$title!" else "") + (if (text != null) " $text" else "")

        addresseeIds.forEach { id ->
            sendMessage(this, id, forwardText, saveIfError = false)
        }
    }
}