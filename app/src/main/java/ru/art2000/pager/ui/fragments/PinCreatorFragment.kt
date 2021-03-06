package ru.art2000.pager.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import ru.art2000.pager.R
import ru.art2000.pager.databinding.PinCreatorFragmentBinding
import ru.art2000.pager.extensions.contextNavigationCoordinator
import ru.art2000.pager.extensions.requireCompatActivity
import ru.art2000.pager.ui.views.PinCodeInput

class PinCreatorFragment : Fragment() {

    private lateinit var viewBinding: PinCreatorFragmentBinding
    private val navigationCoordinator by contextNavigationCoordinator()

    private val args: PinCreatorFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = PinCreatorFragmentBinding.inflate(inflater, container, false).also {
        viewBinding = it
    }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navigationCoordinator.navController.previousBackStackEntry?.savedStateHandle?.set(
            "pin_set",
            -1
        )

        val checkValidity = args.check?.toIntOrNull() != null

        if (checkValidity) {
            viewBinding.pinInput.onInput = verifier@{
                if (it == args.check) {
                    onCheckPassed(it.toInt())
                    return@verifier true
                }
                false
            }
        }

        viewBinding.pinInput.onOkPressed = input@{
            if (checkValidity && it == args.check) {
                onCheckPassed(it.toInt())
                return@input
            }

            if (it.length >= PinCodeInput.MIN_NUMBER_COUNT) {
                navigationCoordinator.navigateTo(
                    PinCreatorFragmentDirections.actionPinCreatorFragmentSelf(
                        it
                    )
                )
            }
        }
    }

    private fun onCheckPassed(pin: Int) {
        navigationCoordinator.navController.previousBackStackEntry?.savedStateHandle?.set(
            "pin_set",
            pin
        )
        navigationCoordinator.navController.popBackStack()
    }

    override fun onResume() {
        super.onResume()
        val title = if (args.check == null) R.string.pin_setup_title else R.string.pin_check_title
        requireCompatActivity().supportActionBar?.setTitle(title)
    }
}