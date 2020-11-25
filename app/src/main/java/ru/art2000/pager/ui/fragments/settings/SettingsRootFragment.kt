package ru.art2000.pager.ui.fragments.settings

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.FileProvider
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import ru.art2000.pager.BuildConfig
import ru.art2000.pager.PagerApplication
import ru.art2000.pager.R
import ru.art2000.pager.extensions.contextNavigationCoordinator
import ru.art2000.pager.receivers.NotificationListener

class SettingsRootFragment : PreferenceFragmentCompat() {

    private val navigationCoordinator by contextNavigationCoordinator()

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {

        addPreferencesFromResource(R.xml.settings)

        findPreference<Preference>("security_settings")?.setOnPreferenceClickListener {
            navigationCoordinator.navigateTo(
                SettingsRootFragmentDirections.actionSettingsRootFragmentToSecuritySettingsFragment()
            )
            true
        }

        findPreference<Preference>("forwarding_settings")?.setOnPreferenceClickListener {
            if (!isNotificationsReadingPermissionGranted()) {
                showPermissionHintDialog()
                return@setOnPreferenceClickListener true
            }

            navigationCoordinator.navigateTo(
                SettingsRootFragmentDirections.actionSettingsRootFragmentToForwardingSettingsFragment()
            )
            true
        }

        findPreference<Preference>("log_send")?.setOnPreferenceClickListener {
            val sendLogIntent = collectLogs() ?: kotlin.run {

                Toast.makeText(requireContext(), R.string.send_logs_no_logs, Toast.LENGTH_SHORT)
                    .show()
                return@setOnPreferenceClickListener true
            }

            startActivity(
                Intent.createChooser(
                    sendLogIntent,
                    requireContext().getString(R.string.send_logs_apps_select_title)
                )
            )
            true
        }

        val notificationPreference = findPreference<Preference>("notification_sender") ?: return

        notificationPreference.onPreferenceClickListener = Preference.OnPreferenceClickListener {

            buildNotificationDialog()

            return@OnPreferenceClickListener true
        }

        if (!BuildConfig.DEBUG) {
            preferenceScreen.removePreference(notificationPreference)
        }
    }

    private fun collectLogs(): Intent? {
        if (!PagerApplication.Logger.logFile.exists()) return null

        val sendIntent = Intent(Intent.ACTION_SEND)

        val uri = FileProvider.getUriForFile(
            requireContext(),
            requireActivity().packageName + ".provider",
            PagerApplication.Logger.logFile
        )
        sendIntent.type = "text/plain"

        sendIntent.putExtra(Intent.EXTRA_STREAM, uri)
        sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        return sendIntent
    }

    private fun isNotificationsReadingPermissionGranted(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            val notificationManager =
                requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            return notificationManager.isNotificationListenerAccessGranted(
                ComponentName(requireContext(), NotificationListener::class.java)
            )
        }

        val notificationListenerString = Settings.Secure.getString(
            requireContext().contentResolver, "enabled_notification_listeners"
        ) ?: return false

        val testString = requireContext().packageName + "/"

        return notificationListenerString.split(':').any { it.startsWith(testString) }
    }

    private fun showPermissionHintDialog() {
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(R.string.notification_reading_permission_dialog_title)
            .setMessage(R.string.notification_reading_permission_dialog_message)
            .setNegativeButton(R.string.cancel) { dialog, _ -> dialog.cancel() }
            .setPositiveButton(R.string.ok) { _, _ ->

                startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
            }.create()

        dialog.show()
    }

    private fun buildNotification(title: String, text: String) {
        val notification = NotificationCompat.Builder(requireContext(), "main")
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_edit)
            .build()

        val notificationManager = NotificationManagerCompat.from(requireContext())
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(
                NotificationChannel(
                    notification.channelId,
                    "lalala",
                    NotificationManager.IMPORTANCE_DEFAULT
                )
            )
        }
        notificationManager.notify(565, notification)
    }

    private fun buildNotificationDialog() {
        val titleInput = EditText(requireContext())
        val textInput = EditText(requireContext())

        val parent = LinearLayout(requireContext())
        parent.orientation = LinearLayout.VERTICAL

        parent.addView(titleInput)
        parent.addView(textInput)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(parent)
            .setPositiveButton("Create") { _, _ ->
                buildNotification(titleInput.text.toString(), textInput.text.toString())
            }.create()

        dialog.show()
    }
}