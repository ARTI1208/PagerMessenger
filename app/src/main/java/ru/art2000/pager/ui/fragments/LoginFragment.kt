package ru.art2000.pager.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import ru.art2000.pager.R
import ru.art2000.pager.databinding.LoginFragmentBinding
import ru.art2000.pager.extensions.contextNavigationCoordinator
import ru.art2000.pager.extensions.requireCompatActivity
import ru.art2000.pager.ui.views.PinCodeInput
import ru.art2000.pager.viewmodels.LoginViewModel

class LoginFragment : Fragment() {

    private val viewModel: LoginViewModel by viewModels()

    private val navigationCoordinator by contextNavigationCoordinator()

    private lateinit var viewBinding: LoginFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = LoginFragmentBinding.inflate(inflater, container, false).also {
        viewBinding = it
    }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!viewModel.isSafeLogin()) {
            gotoChatList()
            return
        }

        setupPinInput()

        if (viewModel.isUsingBiometrics()) {
            openBiometricPrompt()
        }
    }

    override fun onResume() {
        super.onResume()
        requireCompatActivity().supportActionBar?.hide()
    }

    override fun onDestroy() {
        super.onDestroy()
        requireCompatActivity().supportActionBar?.show()
    }

    private fun setupPinInput() {

        val pinView = PinCodeInput(requireContext())
        pinView.layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )

        pinView.onInput = verifier@{
            val asInt = it.toIntOrNull() ?: return@verifier false
            if (viewModel.loginWithPin(asInt)) {
                gotoChatList()
                return@verifier true
            }
            false
        }

        pinView.biometricPromptOpener =
            if (viewModel.isUsingBiometrics()) ::openBiometricPrompt else null

        viewBinding.root.addView(pinView)
    }

    private fun openBiometricPrompt() {
        val prompt = BiometricPrompt(this, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                Toast.makeText(
                    requireContext(),
                    R.string.biometrics_check_failed,
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                gotoChatList()
            }

            override fun onAuthenticationFailed() {
                Toast.makeText(
                    requireContext(),
                    R.string.biometrics_check_failed,
                    Toast.LENGTH_SHORT
                ).show()
            }
        })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(requireContext().getString(R.string.biometrics_auth_title))
            .setSubtitle(requireContext().getString(R.string.biometrics_auth_subtitle))
            .setDescription(requireContext().getString(R.string.biometrics_auth_description))
            .setNegativeButtonText(requireContext().getString(R.string.biometrics_auth_negative_button))
            .build()

        prompt.authenticate(promptInfo)
    }

    private fun gotoChatList() {
        navigationCoordinator.navigateTo(
            LoginFragmentDirections.actionLoginFragmentToChatListFragment()
        )
    }
}