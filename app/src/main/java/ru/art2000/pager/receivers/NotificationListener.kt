package ru.art2000.pager.receivers

import android.app.Notification
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.preference.PreferenceManager
import ru.art2000.pager.helpers.SettingsKeys
import ru.art2000.pager.helpers.sendMessageAndSave

class NotificationListener : NotificationListenerService() {

    private val preferences: SharedPreferences by lazy { PreferenceManager.getDefaultSharedPreferences(this) }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val validPackages = preferences.getStringSet(
            SettingsKeys.NOTIFICATION_FORWARDING_FROM_PACKAGES_KEY, emptySet()
        ) ?: return
        if (!validPackages.contains(sbn.packageName)) return


        val addresseeIds = preferences.getStringSet(
            SettingsKeys.NOTIFICATION_FORWARDING_TO_CHATS_KEY, emptySet()
        ) ?: return
        if (addresseeIds.isEmpty()) return

        val title = sbn.notification.extras[Notification.EXTRA_TITLE];
        val text = sbn.notification.extras[Notification.EXTRA_TEXT];

        val notificationAppInfo = try {
            packageManager.getApplicationInfo(sbn.packageName, 0)
        } catch (e : PackageManager.NameNotFoundException) {
            null
        }

        val notificationAppTitle = if (notificationAppInfo == null)
            "Unknown"
        else
            packageManager.getApplicationLabel(notificationAppInfo)

        addresseeIds.forEach {
            val intId = it.toIntOrNull() ?: return@forEach
            sendMessageAndSave(this, intId, "*$notificationAppTitle* !$title! $text")
        }
    }
}