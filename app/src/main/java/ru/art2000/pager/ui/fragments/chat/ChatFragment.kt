package ru.art2000.pager.ui.fragments.chat

import android.os.Bundle
import android.text.Editable
import android.util.Log
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
import ru.art2000.pager.models.Addressee
import ru.art2000.pager.viewmodels.ChatViewModel
import kotlin.concurrent.thread
import kotlin.system.measureTimeMillis


class ChatFragment : Fragment() {

    private lateinit var viewBinding: ChatFragmentBinding

    private val args: ChatFragmentArgs by navArgs()
    private lateinit var addressee: Addressee

    private val viewModel: ChatViewModel by activityViewModels {
        ViewModelProvider.AndroidViewModelFactory(
            requireActivity().application
        )
    }

    private lateinit var messagesAdapter: MessagesListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ChatFragmentBinding.inflate(inflater, container, false).also {
        viewBinding = it
    }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setHasOptionsMenu(true)

        addressee = args.chatView.addressee

        val setupTime = measureTimeMillis {
            viewBinding.messagesListRecycler.layoutManager =
                LinearLayoutManager(requireContext()).apply { stackFromEnd = true }

            messagesAdapter = MessagesListAdapter(
                requireContext(),
                emptyList(),
                viewModel.getMessageActions(args.chatView)
            )
            viewBinding.messagesListRecycler.adapter = messagesAdapter

            viewModel.allMessages(args.chatView).observe(viewLifecycleOwner) {
                val newDataTime = measureTimeMillis {
                    messagesAdapter.setNewData(it)
                    if (it.isNotEmpty()) {
                        viewBinding.messagesListRecycler.smoothScrollToPosition(it.lastIndex)
                    }
                }
                Log.e("TimeMeas2", newDataTime.toString())
            }
        }

        Log.e("TimeMeas1", setupTime.toString())
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val activCretaed = measureTimeMillis {
            updateActionBar()

            val lastMessage = args.chatView.lastMessage
            if (lastMessage?.isDraft == true) {
                viewBinding.messageEt.text.append(lastMessage.text)
                viewBinding.invertPolarityCb.isChecked = lastMessage.invert
                viewBinding.typeSwitch.isChecked = lastMessage.alpha

                val toneRbToSelect = when (lastMessage.tone) {
                    AntennaCommunicator.Tone.A -> viewBinding.toneARadio.id
                    AntennaCommunicator.Tone.B -> viewBinding.toneBRadio.id
                    AntennaCommunicator.Tone.C -> viewBinding.toneCRadio.id
                    AntennaCommunicator.Tone.D -> viewBinding.toneDRadio.id
                }

                viewBinding.toneGroup.check(toneRbToSelect)

                val freqRbToSelect = when (lastMessage.frequency) {
                    AntennaCommunicator.Frequency.F512 -> viewBinding.freq512Radio.id
                    AntennaCommunicator.Frequency.F1200 -> viewBinding.freq1200Radio.id
                    AntennaCommunicator.Frequency.F2400 -> viewBinding.freq2400Radio.id
                }

                viewBinding.freqGroup.check(freqRbToSelect)
            }

            viewBinding.messageEt.addTextChangedListener(object : TextWatcherAdapter() {
                override fun afterTextChanged(s: Editable) {
                    saveMessage(s.toString(), false)
                }
            })

            viewBinding.sendButton.setOnClickListener {
                val text = viewBinding.messageEt.text.toString()
                if (text.isEmpty()) {
                    Toast.makeText(requireContext(), "No input provided", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                saveMessage(text, true)

                viewBinding.messageEt.text.clear()
            }

            viewBinding.invertPolarityCb.setOnCheckedChangeListener {  _, value ->
                saveMessage(viewBinding.messageEt.text.toString(), false)
            }

            viewBinding.typeSwitch.setOnCheckedChangeListener { _, value ->
                saveMessage(viewBinding.messageEt.text.toString(), false)
            }

            viewBinding.toneGroup.setOnCheckedChangeListener { _, _ ->
                saveMessage(viewBinding.messageEt.text.toString(), false)
            }

            viewBinding.freqGroup.setOnCheckedChangeListener { _, _ ->
                saveMessage(viewBinding.messageEt.text.toString(), false)
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
        Log.e("TimeMeas3", activCretaed.toString())
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.chat_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (item.itemId == R.id.rename_addressee) {
            val addresseeInput = EditText(requireContext())
            addresseeInput.hint = addressee.nickname ?: ""
            addresseeInput.text.append(addresseeInput.hint)

            val dialog = AlertDialog.Builder(requireContext())
                .setTitle(R.string.dialog_rename_chat_title)
                .setView(addresseeInput)
                .setNegativeButton(R.string.dialog_rename_chat_cancel_button) { dialog, _ -> dialog.cancel() }
                .setPositiveButton(R.string.dialog_rename_chat_ok_button) { dialog, _ ->
                    dialog.dismiss()

                    addressee = Addressee(addressee.number, addresseeInput.text.toString())
                    updateActionBar()

                    thread {
                        viewModel.renameAddressee(
                            args.chatView.addressee,
                            addresseeInput.text.toString()
                        )
                    }
                }.create()

            dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
            dialog.setOnShowListener {
                addresseeInput.requestFocus()
                addresseeInput.selectAll()
            }

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

    private fun updateActionBar(newTitle: String = addressee.toDisplayName()) {
        requireCompatActivity().supportActionBar?.title = newTitle
    }

    private fun saveMessage(text: String, sendToPager: Boolean) {

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

        if (sendToPager) {
            viewModel.sendMessage(
                args.chatView.addressee.number,
                text,
                tone,
                frequency,
                viewBinding.invertPolarityCb.isChecked,
                viewBinding.typeSwitch.isChecked
            )

        } else {
            viewModel.saveDraft(
                args.chatView,
                text,
                tone,
                frequency,
                viewBinding.invertPolarityCb.isChecked,
                viewBinding.typeSwitch.isChecked
            )
        }
    }
}