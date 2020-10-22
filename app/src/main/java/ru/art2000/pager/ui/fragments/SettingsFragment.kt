package ru.art2000.pager.ui.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.biometric.BiometricPrompt
import androidx.core.content.edit
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import ru.art2000.pager.R
import ru.art2000.pager.extensions.SecureSharedPreferences
import ru.art2000.pager.extensions.requireCompatActivity
import ru.art2000.pager.ui.NavigationCoordinator

class SettingsFragment : PreferenceFragmentCompat() {

    private lateinit var navigationCoordinator: NavigationCoordinator

    private lateinit var encryptedPreferences: SharedPreferences

    private var pinPreference: SwitchPreference? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        encryptedPreferences = SecureSharedPreferences.create(context, "credentials_preferences")
        navigationCoordinator = context as NavigationCoordinator
    }

    override fun onResume() {
        super.onResume()
        navigationCoordinator.setSupportsBack(true)
        requireCompatActivity().supportActionBar?.setTitle(R.string.settings)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.settings)

        pinPreference = findPreference("use_pin_code")
        pinPreference?.setOnPreferenceChangeListener { _, newValue ->
            if (newValue as Boolean) {
                openPinCreator()
            }
            true
        }

        findPreference<SwitchPreference>("use_biometrics")?.apply {
            setOnPreferenceChangeListener { _, newValue ->
                if (newValue as Boolean) {
                    openBiometricPrompt(this)
                }
                true
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navigationCoordinator.navController.currentBackStackEntry?.savedStateHandle
            ?.getLiveData<Int>("pin_set")?.observe(viewLifecycleOwner) { result ->
                pinPreference?.isChecked = result >= 0

                encryptedPreferences.edit {
                    if (result >= 0) {
                        putInt("pin_code", result)
                    } else {
                        remove("pin_code")
                    }
                }
            }
    }

    private fun openPinCreator() {
        navigationCoordinator.navigateTo(SettingsFragmentDirections.actionSettingsFragmentToPinCreatorFragment())
    }

    private fun openBiometricPrompt(biometricPreference: SwitchPreference) {
        val prompt = BiometricPrompt(this, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                Toast.makeText(requireContext(), R.string.biometrics_check_failed, Toast.LENGTH_SHORT).show()
                biometricPreference.isChecked = false
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            }

            override fun onAuthenticationFailed() {
            }
        })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(requireContext().getString(R.string.biometrics_auth_title))
            .setSubtitle(requireContext().getString(R.string.biometrics_auth_subtitle))
            .setDescription(requireContext().getString(R.string.biometrics_setup_description))
            .setNegativeButtonText(requireContext().getString(R.string.biometrics_setup_negative_button))
            .build()

        prompt.authenticate(promptInfo)
    }

}