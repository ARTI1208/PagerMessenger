package ru.art2000.pager.ui

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import ru.art2000.pager.databinding.LoginFragmentBinding
import ru.art2000.pager.extensions.requireCompatActivity

class LoginFragment : Fragment() {

    private lateinit var viewBinding: LoginFragmentBinding
    private lateinit var navigationCoordinator: NavigationCoordinator

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

        viewBinding.navigateNext.setOnClickListener {
            navigationCoordinator.navigateTo(LoginFragmentDirections.actionLoginFragmentToChatListFragment())
        }
    }

    override fun onResume() {
        super.onResume()
        requireCompatActivity().supportActionBar?.hide()
    }
}