package ru.art2000.pager.ui.fragments.settings

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Bundle
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import ru.art2000.pager.BuildConfig
import ru.art2000.pager.R
import ru.art2000.pager.extensions.contextNavigationCoordinator

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
            navigationCoordinator.navigateTo(
                SettingsRootFragmentDirections.actionSettingsRootFragmentToForwardingSettingsFragment()
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

    private fun buildNotification(title: String, text: String) {
        val notif = NotificationCompat.Builder(requireContext(), "main")
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_edit)
            .build()

        val notificationManager = NotificationManagerCompat.from(requireContext())
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(
                NotificationChannel(
                    notif.channelId,
                    "lalala",
                    NotificationManager.IMPORTANCE_DEFAULT
                )
            )
        }
        notificationManager.notify(565, notif)
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