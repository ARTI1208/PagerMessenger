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
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.internal.TextWatcherAdapter
import ru.art2000.pager.R
import ru.art2000.pager.databinding.ChatListFragmentBinding
import ru.art2000.pager.extensions.contextNavigationCoordinator
import ru.art2000.pager.models.ChatView
import ru.art2000.pager.viewmodels.ChatListViewModel
import ru.art2000.pager.viewmodels.ForwardingViewModel
import kotlin.concurrent.thread


abstract class ChatListFragment : Fragment() {

    protected val viewModel: ChatListViewModel by activityViewModels()

    protected val navigationCoordinator by contextNavigationCoordinator()

    private lateinit var viewBinding: ChatListFragmentBinding
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

    abstract fun createListAdapter(): ChatListAdapter

    @StringRes
    abstract fun getTitleRes(): Int

    @StringRes
    abstract fun getFabDialogOkButtonRes(): Int

    abstract fun onFabDialogOkButtonClick(text: String)

}

class MainChatListFragment : ChatListFragment() {

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.chat_list_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (item.itemId == R.id.settings_item) {
            navigationCoordinator.navigateTo(
                MainChatListFragmentDirections.actionChatListFragmentToSettingsNavigation()
            )
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    override fun createListAdapter(): ChatListAdapter = ChatListAdapter(
        requireActivity(),
        emptyList(),
        { chatView, _ -> openChat(chatView) },
        false,
        { false },
        { _, _ -> },
    )

    @StringRes
    override fun getTitleRes(): Int = R.string.app_name

    @StringRes
    override fun getFabDialogOkButtonRes(): Int = R.string.dialog_new_chat_button_create_or_open

    override fun onFabDialogOkButtonClick(text: String) {
        thread {
            val chat = viewModel.createChat(text.toInt())

            openChat(chat)
        }
    }

    private fun openChat(chat: ChatView) {
        navigationCoordinator.navigateTo(
            MainChatListFragmentDirections.actionChatListFragmentToChatFragment(chat)
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

    override fun createListAdapter(): ChatListAdapter {
        return if (isPackageAlreadySelected) {
            ChatListAdapter(
                requireActivity(),
                emptyList(),
                { _, _ -> },
                true,
                {
                    forwardingViewModel.isPackageSelectedForForwarding(
                        it.addressee.number,
                        args.appPackage!!
                    )
                },
                { chatView, isChecked ->
                    forwardingViewModel.savePackage(
                        chatView.addressee.number,
                        args.appPackage!!,
                        isChecked
                    )
                },
            )
        } else {
            ChatListAdapter(
                requireActivity(),
                emptyList(),
                { chatView, _ -> showAppsForChat(chatView) },
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
        thread { viewModel.createChat(text.toInt()) }
    }

    private fun showAppsForChat(chat: ChatView) {
        navigationCoordinator.navigateTo(
            SelectChatFragmentDirections.actionSelectChatFragmentToAppsListeningSelectFragment(chat.addressee.number)
        )
    }
}