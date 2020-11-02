package ru.art2000.pager.ui.fragments.chatlist

import android.os.Bundle
import android.text.Editable
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.internal.TextWatcherAdapter
import ru.art2000.pager.R
import ru.art2000.pager.databinding.ChatListFragmentBinding
import ru.art2000.pager.extensions.contextNavigationCoordinator
import ru.art2000.pager.models.ChatView
import ru.art2000.pager.viewmodels.ChatListViewModel
import kotlin.concurrent.thread


class ChatListFragment : Fragment() {

    private val viewModel: ChatListViewModel by activityViewModels {
        ViewModelProvider.AndroidViewModelFactory(
            requireActivity().application
        )
    }

    private val args by navArgs<ChatListFragmentArgs>()

    private lateinit var viewBinding: ChatListFragmentBinding

    private val navigationCoordinator by contextNavigationCoordinator()

    private lateinit var adapter: ChatListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return if (::viewBinding.isInitialized) viewBinding.root
        else ChatListFragmentBinding.inflate(inflater, container, false).also {
            viewBinding = it
        }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (::adapter.isInitialized) return

        setHasOptionsMenu(true)

        viewBinding.newChatFab.setOnClickListener {

            val addresseeInput = EditText(requireContext())
            addresseeInput.inputType =
                EditorInfo.TYPE_CLASS_NUMBER or EditorInfo.TYPE_NUMBER_FLAG_DECIMAL

            val okButtonRes = if (args.isSelectMode)
                R.string.dialog_new_chat_button_create
            else
                R.string.dialog_new_chat_button_create_or_open

            val dialog = AlertDialog.Builder(requireContext())
                .setTitle(R.string.dialog_new_chat_title)
                .setView(addresseeInput)
                .setNegativeButton(R.string.dialog_new_chat_cancel_button) { dialog, _ -> dialog.cancel() }
                .setPositiveButton(okButtonRes) { _, _ ->
                    thread {
                        val chat = viewModel.createChat(addresseeInput.text.toString().toInt())

                        if (!args.isSelectMode) openChat(chat)
                    }
                }.create()

            dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
            dialog.setOnShowListener {
                addresseeInput.requestFocus()
            }

            dialog.show()

            dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
            addresseeInput.addTextChangedListener(object : TextWatcherAdapter() {
                override fun afterTextChanged(s: Editable) {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = s.isNotEmpty()
                }
            })
        }

        if (args.isSelectMode) {
            adapter = ChatListAdapter(
                requireActivity(),
                emptyList(),
                { _, holder -> holder.viewBinding.itemSelectCheckBox.performClick() },
                true,
                viewModel::isChatSelected,
                viewModel::onChatChecked
            )
        } else {
            adapter = ChatListAdapter(
                requireActivity(),
                emptyList(),
                { chatView, _ -> openChat(chatView) },
                false,
                { false },
                { _, _ -> }
            )
        }

        viewBinding.chatListRecycler.adapter = adapter
        viewBinding.chatListRecycler.layoutManager = LinearLayoutManager(requireContext())

        val dividerItemDecoration = DividerItemDecoration(
            requireContext(),
            LinearLayoutManager.VERTICAL
        )
        viewBinding.chatListRecycler.addItemDecoration(dividerItemDecoration)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        if (!args.isSelectMode) {
            inflater.inflate(R.menu.chat_list_menu, menu)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (item.itemId == R.id.settings_item) {
            navigationCoordinator.navigateTo(
                ChatListFragmentDirections.actionChatListFragmentToSettingsNavigation()
            )
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.allChats().observe(viewLifecycleOwner) {
            if (it.isEmpty()) {
                viewBinding.emptyTextView.visibility = View.VISIBLE
                viewBinding.chatListRecycler.visibility = View.GONE
            } else {
                viewBinding.emptyTextView.visibility = View.GONE
                viewBinding.chatListRecycler.visibility = View.VISIBLE
            }

            adapter.data = it
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.onDestroy(args.isSelectMode)
    }

    private fun openChat(chat: ChatView) {
        navigationCoordinator.navigateTo(
            ChatListFragmentDirections.actionChatListFragmentToChatFragment(chat)
        )
    }
}