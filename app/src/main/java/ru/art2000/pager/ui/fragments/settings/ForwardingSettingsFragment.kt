package ru.art2000.pager.ui.fragments.settings

import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import ru.art2000.pager.R
import ru.art2000.pager.extensions.contextNavigationCoordinator

class ForwardingSettingsFragment : PreferenceFragmentCompat() {

    private val navigationCoordinator by contextNavigationCoordinator()

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.forwarding_settings)

        findPreference<Preference>("apps_to_listen")?.apply {
            setOnPreferenceClickListener {
                navigationCoordinator.navigateTo(
                    ForwardingSettingsFragmentDirections.actionForwardingSettingsFragmentToAppsListeningSelectFragment()
                )
                return@setOnPreferenceClickListener true
            }
        }

        findPreference<Preference>("forward_to_chats")?.apply {
            setOnPreferenceClickListener {
                navigationCoordinator.navigateTo(
                    ForwardingSettingsFragmentDirections.actionForwardingSettingsFragmentToChatListFragment2()
                )
                return@setOnPreferenceClickListener true
            }
        }
    }


}