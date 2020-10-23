package ru.art2000.pager.ui.fragments.chat

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.view.*
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.internal.TextWatcherAdapter
import ru.art2000.pager.R
import ru.art2000.pager.databinding.ChatFragmentBinding
import ru.art2000.pager.extensions.requireCompatActivity
import ru.art2000.pager.hardware.AntennaCommunicator
import ru.art2000.pager.ui.NavigationCoordinator
import ru.art2000.pager.viewmodels.ChatViewModel
import kotlin.concurrent.thread

class ChatFragment : Fragment() {

    private lateinit var viewBinding: ChatFragmentBinding
    private lateinit var navigationCoordinator: NavigationCoordinator

    private val args: ChatFragmentArgs by navArgs()

    private val viewModel: ChatViewModel by activityViewModels {
        ViewModelProvider.AndroidViewModelFactory(
            requireActivity().application
        )
    }

    private lateinit var messagesAdapter: MessagesListAdapter

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

        viewBinding.messagesListRecycler.layoutManager =
            LinearLayoutManager(requireContext()).apply { stackFromEnd = true }

        messagesAdapter = MessagesListAdapter(requireContext(), emptyList(), viewModel.getMessageActions(args.chat))
        viewBinding.messagesListRecycler.adapter = messagesAdapter

        viewModel.allMessages(args.chat).observe(viewLifecycleOwner) {
            messagesAdapter.setNewData(it)
            if (it.isNotEmpty()) {
                viewBinding.messagesListRecycler.smoothScrollToPosition(it.lastIndex)
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        thread {
            val title = viewModel.getAddressee(args.chat).let {
                if (it == null) return@let args.chat.addresseeNumber.toString()

                it.toDisplayName()
            }
            requireCompatActivity().supportActionBar?.title = title
        }

        viewBinding.sendButton.setOnClickListener {
            val text = viewBinding.messageEt.text.toString()
            if (text.isEmpty()) {
                Toast.makeText(requireContext(), "No input provided", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val tone = when (viewBinding.toneGroup.checkedRadioButtonId) {
                viewBinding.toneBRadio.id -> AntennaCommunicator.Tone.B
                viewBinding.toneCRadio.id -> AntennaCommunicator.Tone.C
                viewBinding.toneDRadio.id -> AntennaCommunicator.Tone.D
                else -> AntennaCommunicator.Tone.A
            }

            val frequency = when (viewBinding.freqGroup.checkedRadioButtonId) {
                viewBinding.freq512Radio.id -> AntennaCommunicator.Frequency.F512
                viewBinding.freq1200Radio.id -> AntennaCommunicator.Frequency.F1200
                else -> AntennaCommunicator.Frequency.F2400
            }

            val sendResult = viewModel.sendMessage(
                args.chat,
                text,
                tone,
                frequency,
                viewBinding.invertPolarityCb.isChecked,
                viewBinding.typeSwitch.isChecked
            )

            when (sendResult) {
                -2 -> Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT)
                    .show()
                -1 -> Toast.makeText(requireContext(), "Error sending data", Toast.LENGTH_SHORT)
                    .show()
            }

            viewBinding.messageEt.text.clear()
        }

        val behaviour = BottomSheetBehavior.from(viewBinding.sendLayout)
        behaviour.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {

            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                val params =
                    viewBinding.messagesListRecycler.layoutParams as RelativeLayout.LayoutParams
                params.bottomMargin =
                    ((bottomSheet.height - behaviour.peekHeight) * slideOffset).toInt() + behaviour.peekHeight
                viewBinding.messagesListRecycler.layoutParams = params
            }

        })


        viewBinding.sendSettingsButton.setOnClickListener {
            if (behaviour.state == BottomSheetBehavior.STATE_COLLAPSED) {
                behaviour.state = BottomSheetBehavior.STATE_EXPANDED
            } else {
                behaviour.state = BottomSheetBehavior.STATE_COLLAPSED
            }
        }
    }

    override fun onResume() {
        super.onResume()
        navigationCoordinator.setSupportsBack(true)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.chat_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (item.itemId == R.id.rename_addressee) {
            val addresseeInput = EditText(requireContext())
            thread {
                viewModel.getAddressee(args.chat)?.apply {
                    addresseeInput.hint = nickname ?: ""
                    addresseeInput.text.append(nickname ?: "")
                    addresseeInput.selectAll()
                }
            }

            val dialog = AlertDialog.Builder(requireContext())
                .setTitle(R.string.dialog_rename_chat_title)
                .setView(addresseeInput)
                .setNegativeButton(R.string.dialog_rename_chat_cancel_button) { dialog, _ -> dialog.cancel() }
                .setPositiveButton(R.string.dialog_rename_chat_ok_button) { dialog, _ ->
                    dialog.dismiss()

                    thread {
                        viewModel.renameAddressee(args.chat, addresseeInput.text.toString())
                        requireActivity().runOnUiThread {
                            updateActionBar()
                        }
                    }
                }.create()


            dialog.show()

            dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
            addresseeInput.addTextChangedListener(object : TextWatcherAdapter() {
                override fun afterTextChanged(s: Editable) {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = s.isNotEmpty()
                }
            })
        }

        return super.onOptionsItemSelected(item)
    }

    private fun updateActionBar() {


        thread {
            val title = viewModel.getAddressee(args.chat).let {
                if (it == null) return@let args.chat.addresseeNumber.toString()

                it.toDisplayName()
            }

            requireActivity().runOnUiThread {
                requireCompatActivity().supportActionBar?.title = title
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

    }
}