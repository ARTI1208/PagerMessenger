package ru.art2000.pager.ui.fragments.chatlist

import android.os.Bundle
import android.text.Editable
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.paging.LoadState
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.internal.TextWatcherAdapter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.art2000.pager.R
import ru.art2000.pager.databinding.ChatListFragmentBinding
import ru.art2000.pager.extensions.contextNavigationCoordinator
import ru.art2000.pager.models.Addressee
import ru.art2000.pager.models.ChatView
import ru.art2000.pager.viewmodels.ChatListViewModel
import ru.art2000.pager.viewmodels.ForwardingViewModel
import kotlin.concurrent.thread

abstract class ChatListFragment : Fragment() {

    protected val viewModel: ChatListViewModel by activityViewModels()

    protected val navigationCoordinator by contextNavigationCoordinator()

    protected lateinit var viewBinding: ChatListFragmentBinding
    protected lateinit var adapter: ChatListAdapter<*>

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

        viewBinding.newChatFab.setOnClickListener { openChatCreationDialog() }

        adapter = createListAdapter()

        viewBinding.chatListRecycler.adapter = adapter
        viewBinding.chatListRecycler.layoutManager = LinearLayoutManager(requireContext())

        val dividerItemDecoration = DividerItemDecoration(
            requireContext(),
            LinearLayoutManager.VERTICAL
        )
        viewBinding.chatListRecycler.addItemDecoration(dividerItemDecoration)
    }

    override fun onResume() {
        super.onResume()

        val title = getTitleRes()
        navigationCoordinator.setWindowTitle(title)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        adapter.addLoadStateListener {
            if (adapter.itemCount == 0) {
                viewBinding.emptyTextView.visibility = View.VISIBLE
                viewBinding.chatListRecycler.visibility = View.GONE
            } else {
                viewBinding.emptyTextView.visibility = View.GONE
                viewBinding.chatListRecycler.visibility = View.VISIBLE
            }

            if (it.refresh is LoadState.NotLoading && viewModel.shouldScrollToTop) {
                viewBinding.chatListRecycler.scrollToPosition(0)
                viewModel.shouldScrollToTop = false
            }
        }

        lifecycleScope.launch {
            viewModel.allChatsFlow().collectLatest {
                adapter.submitData(it)
            }
        }
    }

    private fun openChatCreationDialog() {
        val addresseeInput = EditText(requireContext())
        addresseeInput.inputType =
            EditorInfo.TYPE_CLASS_NUMBER or EditorInfo.TYPE_NUMBER_FLAG_DECIMAL

        val okButtonRes = getFabDialogOkButtonRes()

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(R.string.dialog_new_chat_title)
            .setView(addresseeInput)
            .setNegativeButton(R.string.dialog_new_chat_cancel_button) { dialog, _ -> dialog.cancel() }
            .setPositiveButton(okButtonRes) { _, _ ->
                onFabDialogOkButtonClick(addresseeInput.text.toString())
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

    protected fun checkChatNumber(text: String): Int? {
        val number = text.toIntOrNull()

        if (number == null) {

            AlertDialog.Builder(requireContext())
                .setTitle(R.string.invalid_number_dialog_title)
                .setMessage(R.string.invalid_number_dialog_message)
                .setNegativeButton(R.string.cancel) { _, _ -> }
                .setPositiveButton(R.string.ok) { _, _ -> openChatCreationDialog() }
                .create()
                .show()
        }

        return number
    }

    abstract fun createListAdapter(): ChatListAdapter<*>

    @StringRes
    abstract fun getTitleRes(): Int

    @StringRes
    abstract fun getFabDialogOkButtonRes(): Int

    abstract fun onFabDialogOkButtonClick(text: String)

}

class MainChatListFragment : ChatListFragment() {

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.chat_list_menu, menu)

        viewModel.isSelectMode.observe(viewLifecycleOwner, {
            val settingsItem = menu.findItem(R.id.settings_item)
            val deleteItem = menu.findItem(R.id.delete_item)
            val cancelItem = menu.findItem(R.id.cancel_item)

            settingsItem.isVisible = !it
            deleteItem.isVisible = it
            cancelItem.isVisible = it
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.settings_item -> {
                navigationCoordinator.navigateTo(
                    MainChatListFragmentDirections.actionChatListFragmentToSettingsNavigation()
                )
            }
            R.id.delete_item -> {
                viewModel.deleteSelectedChats()
                (adapter as MainChatListAdapter).disableSelectMode()
            }
            R.id.cancel_item -> {
                viewModel.onSelectModeCanceled()
                (adapter as MainChatListAdapter).disableSelectMode()
            }
            else -> return super.onOptionsItemSelected(item)
        }

        return true
    }

    override fun createListAdapter(): ChatListAdapter<*> = MainChatListAdapter(
        requireActivity(),
        { chat, _ -> openChat(chat) },
        viewModel::addOrRemoveChat,
        viewModel.selectedChats::contains,
        viewModel.selectedChats::size
    )


    @StringRes
    override fun getTitleRes(): Int = R.string.app_name

    @StringRes
    override fun getFabDialogOkButtonRes(): Int = R.string.dialog_new_chat_button_create_or_open

    override fun onFabDialogOkButtonClick(text: String) {
        val number = checkChatNumber(text) ?: return

        thread {
            val chat = viewModel.createChat(number)

            requireActivity().runOnUiThread {
                openChat(ChatView(chat, null, null))
            }
        }
    }

    private fun openChat(chat: ChatView) {
        navigationCoordinator.navigateTo(
            MainChatListFragmentDirections.actionChatListFragmentToChatFragment(
                chat.addressee,
                chat.draft
            )
        )
    }

}

class SelectChatFragment : ChatListFragment() {

    private val forwardingViewModel: ForwardingViewModel by viewModels()

    private val args by navArgs<SelectChatFragmentArgs>()

    private val isPackageAlreadySelected: Boolean inline get() = args.appPackage != null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (isPackageAlreadySelected) {
            forwardingViewModel.readSettings()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        forwardingViewModel.onDestroy(isPackageAlreadySelected)
    }

    override fun createListAdapter(): ChatListAdapter<*> {
        return if (isPackageAlreadySelected) {
            ChatSelectListAdapter(
                requireActivity(),
                { _, _ -> },
                true,
                {
                    forwardingViewModel.isPackageSelectedForForwarding(
                        it.number,
                        args.appPackage!!
                    )
                },
                { chatView, isChecked ->
                    forwardingViewModel.savePackage(
                        chatView.number,
                        args.appPackage!!,
                        isChecked
                    )
                },
            )
        } else {
            ChatSelectListAdapter(
                requireActivity(),
                { chatView, _ -> showAppsForChat(chatView.addressee) },
                false,
                { false },
                { _, _ -> },
            )
        }
    }

    override fun getTitleRes(): Int {
        return if (isPackageAlreadySelected) R.string.select_chats_by_apps_title
        else R.string.select_chats_by_chats_title
    }

    override fun getFabDialogOkButtonRes(): Int = R.string.dialog_new_chat_button_create

    override fun onFabDialogOkButtonClick(text: String) {

        val number = checkChatNumber(text) ?: return

        thread { viewModel.createChat(number) }
    }

    private fun showAppsForChat(chat: Addressee) {
        navigationCoordinator.navigateTo(
            SelectChatFragmentDirections.actionSelectChatFragmentToAppsListeningSelectFragment(chat.number)
        )
    }
}