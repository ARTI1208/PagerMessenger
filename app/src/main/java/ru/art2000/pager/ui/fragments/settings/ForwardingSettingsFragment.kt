package ru.art2000.pager.ui.fragments.settings

import android.content.Context
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import ru.art2000.pager.R
import ru.art2000.pager.extensions.requireCompatActivity
import ru.art2000.pager.ui.NavigationCoordinator

class ForwardingSettingsFragment : PreferenceFragmentCompat() {

    private lateinit var navigationCoordinator: NavigationCoordinator

    override fun onAttach(context: Context) {
        super.onAttach(context)
        navigationCoordinator = context as NavigationCoordinator
    }

    override fun onResume() {
        super.onResume()
        navigationCoordinator.setSupportsBack(true)
        requireCompatActivity().supportActionBar?.title = preferenceScreen.title
    }

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