package ru.art2000.pager.viewmodels

import android.app.Application
import android.util.Log
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.preference.PreferenceManager
import ru.art2000.pager.extensions.SecureSharedPreferences

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val defaultPreferences = PreferenceManager.getDefaultSharedPreferences(application)

    private val encryptedPreferences = SecureSharedPreferences.create(application, "credentials_preferences")

    fun isSafeLogin(): Boolean {
        return isUsingPin()
    }

    fun isUsingPin(): Boolean {
        return defaultPreferences.getBoolean("use_pin_code", false)
    }

    fun isUsingBiometrics(): Boolean {
        return defaultPreferences.getBoolean("use_biometrics", false)
    }

    fun loginWithPin(code: Int): Boolean {

        return encryptedPreferences.getInt("pin_code", -1) == code
    }
}