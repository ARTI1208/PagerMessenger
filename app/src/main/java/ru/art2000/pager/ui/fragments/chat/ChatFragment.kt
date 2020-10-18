package ru.art2000.pager.ui.fragments.chat

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import ru.art2000.pager.databinding.ChatFragmentBinding
import ru.art2000.pager.hardware.AntennaCommunicator
import ru.art2000.pager.ui.NavigationCoordinator
import ru.art2000.pager.viewmodels.ChatViewModel

class ChatFragment : Fragment() {

    private lateinit var viewBinding: ChatFragmentBinding
    private lateinit var navigationCoordinator: NavigationCoordinator

    private val args: ChatFragmentArgs by navArgs()

    private val viewModel: ChatViewModel by activityViewModels {
        ViewModelProvider.AndroidViewModelFactory(
            requireActivity().application
        )
    }

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

        viewBinding.tmpTv.text = args.chat.addresseeNumber.toString()

        viewBinding.messagesListRecycler.layoutManager = LinearLayoutManager(requireContext())
        viewBinding.messagesListRecycler.adapter = MessagesListAdapter(requireContext(), emptyList())
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.allMessages(args.chat).observe(viewLifecycleOwner) {
            viewBinding.messagesListRecycler.adapter = MessagesListAdapter(requireContext(), it)
        }


        viewBinding.sendButton.setOnClickListener {
            val tone =
                AntennaCommunicator.Tone.values()[viewBinding.toneSpinner.selectedItemPosition]
            val frequency =
                AntennaCommunicator.Frequency.values()[viewBinding.frequencySpinner.selectedItemPosition]
            viewModel.sendMessage(
                args.chat,
                viewBinding.messageEt.text.toString(),
                tone,
                frequency,
                viewBinding.invertPolarityCb.isChecked,
                viewBinding.typeSwitch.isChecked
            )
        }
    }

    override fun onResume() {
        super.onResume()
        navigationCoordinator.setSupportsBack(true)
    }
}