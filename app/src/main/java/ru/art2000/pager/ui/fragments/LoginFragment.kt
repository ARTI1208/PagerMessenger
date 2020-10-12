package ru.art2000.pager.ui.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import ru.art2000.pager.databinding.LoginFragmentBinding
import ru.art2000.pager.extensions.requireCompatActivity
import ru.art2000.pager.ui.NavigationCoordinator
import ru.art2000.pager.ui.views.PinCodeInput
import ru.art2000.pager.viewmodels.LoginViewModel

class LoginFragment : Fragment() {

    private lateinit var viewBinding: LoginFragmentBinding
    private lateinit var navigationCoordinator: NavigationCoordinator

    private val viewModel: LoginViewModel by viewModels()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        navigationCoordinator = context as NavigationCoordinator
    }

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
            navigationCoordinator.navigateTo(LoginFragmentDirections.actionLoginFragmentToChatListFragment())
            return
        }

        setupPinInput()
    }

    override fun onResume() {
        super.onResume()
        requireCompatActivity().supportActionBar?.hide()
    }

    private fun setupPinInput() {

        val pinView = PinCodeInput(requireContext())
        pinView.layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        pinView.onInput = verifier@ {
            val asInt = it.toIntOrNull() ?: return@verifier false
            if (viewModel.loginWithPin(asInt)) {
                Toast.makeText(requireContext(), "success", Toast.LENGTH_SHORT).show()
                navigationCoordinator.navigateTo(LoginFragmentDirections.actionLoginFragmentToChatListFragment())
                return@verifier true
            }
            false
        }

        pinView.biometricPromptOpener = if (viewModel.isUsingBiometrics()) ::openBiometricPrompt else null

        viewBinding.root.addView(pinView)
    }

    private fun openBiometricPrompt() {
        val prompt = BiometricPrompt(this, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                Toast.makeText(requireContext(), "error", Toast.LENGTH_SHORT).show()
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                Toast.makeText(requireContext(), "success", Toast.LENGTH_SHORT).show()
                navigationCoordinator.navigateTo(LoginFragmentDirections.actionLoginFragmentToChatListFragment())
            }

            override fun onAuthenticationFailed() {
                Toast.makeText(requireContext(), "fail", Toast.LENGTH_SHORT).show()
            }
        })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Title")
            .setSubtitle("Subtitle")
            .setDescription("Desc")
            .setNegativeButtonText("Negative Button")
            .build()

        prompt.authenticate(promptInfo)
    }
}