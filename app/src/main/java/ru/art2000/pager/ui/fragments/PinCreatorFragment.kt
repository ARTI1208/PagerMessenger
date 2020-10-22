package ru.art2000.pager.ui.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import ru.art2000.pager.R
import ru.art2000.pager.databinding.PinCreatorFragmentBinding
import ru.art2000.pager.extensions.requireCompatActivity
import ru.art2000.pager.ui.NavigationCoordinator
import ru.art2000.pager.ui.views.PinCodeInput

class PinCreatorFragment: Fragment() {

    private lateinit var viewBinding: PinCreatorFragmentBinding
    private lateinit var navigationCoordinator: NavigationCoordinator

    override fun onAttach(context: Context) {
        super.onAttach(context)
        navigationCoordinator = context as NavigationCoordinator
        navigationCoordinator.setSupportsBack(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = PinCreatorFragmentBinding.inflate(inflater, container, false).also {
        viewBinding = it
    }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navigationCoordinator.navController.previousBackStackEntry?.savedStateHandle?.set("pin_set", -1)
        viewBinding.pinInput.onOkPressed = input@ {
            if (it.length >= PinCodeInput.MIN_NUMBER_COUNT) {
                navigationCoordinator.navController.previousBackStackEntry?.savedStateHandle?.set("pin_set", it.toInt())
                navigationCoordinator.navController.popBackStack()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        requireCompatActivity().supportActionBar?.setTitle(R.string.pin_setup_title)
    }
}