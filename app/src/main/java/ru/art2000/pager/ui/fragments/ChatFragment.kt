package ru.art2000.pager.ui.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import ru.art2000.pager.databinding.ChatFragmentBinding
import ru.art2000.pager.ui.NavigationCoordinator

class ChatFragment : Fragment() {

    private lateinit var viewBinding: ChatFragmentBinding
    private lateinit var navigationCoordinator: NavigationCoordinator

    override fun onAttach(context: Context) {
        super.onAttach(context)
        navigationCoordinator = context as NavigationCoordinator
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ChatFragmentBinding.inflate(inflater, container, false).also {
        viewBinding = it
    }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

}