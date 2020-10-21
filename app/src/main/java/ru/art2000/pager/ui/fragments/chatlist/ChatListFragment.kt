package ru.art2000.pager.ui.fragments.chatlist

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.internal.TextWatcherAdapter
import ru.art2000.pager.R
import ru.art2000.pager.databinding.ChatListFragmentBinding
import ru.art2000.pager.extensions.requireCompatActivity
import ru.art2000.pager.models.Chat
import ru.art2000.pager.ui.NavigationCoordinator
import ru.art2000.pager.viewmodels.ChatListViewModel
import kotlin.concurrent.thread

class ChatListFragment : Fragment() {

    private val viewModel: ChatListViewModel by activityViewModels {
        ViewModelProvider.AndroidViewModelFactory(
            requireActivity().application
        )
    }

    private lateinit var viewBinding: ChatListFragmentBinding

    private lateinit var navigationCoordinator: NavigationCoordinator

    override fun onAttach(context: Context) {
        super.onAttach(context)
        navigationCoordinator = context as NavigationCoordinator
        navigationCoordinator.setSupportsBack(false)
    }

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
        setHasOptionsMenu(true)

        viewBinding.newChatFab.setOnClickListener {

            val addresseeInput = EditText(requireContext())
            addresseeInput.inputType =
                EditorInfo.TYPE_CLASS_NUMBER or EditorInfo.TYPE_NUMBER_FLAG_DECIMAL


            val dialog = AlertDialog.Builder(requireContext())
                .setTitle("Input addressee")
                .setView(addresseeInput)
                .setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
                .setPositiveButton("Create/Open") { dialog, _ ->
                    dialog.dismiss()

                    thread {
                        val chat = viewModel.createChat(addresseeInput.text.toString().toInt())
                        openChat(chat)
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


        viewBinding.chatListRecycler.adapter = ChatListAdapter(requireContext(), emptyList()) {}
        viewBinding.chatListRecycler.layoutManager = LinearLayoutManager(requireContext())

    }

    override fun onResume() {
        super.onResume()
        navigationCoordinator.setSupportsBack(false)
        requireCompatActivity().supportActionBar?.show()
        requireCompatActivity().supportActionBar?.title = "Pager"
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.chat_list_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (item.itemId == R.id.settings_item) {
            navigationCoordinator.navigateTo(
                ChatListFragmentDirections.actionChatListFragmentToSettingsFragment()
            )
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)


        viewModel.allChats().observe(viewLifecycleOwner) {
            viewBinding.chatListRecycler.adapter = ChatListAdapter(requireContext(), it, ::openChat)
        }
    }

    private fun openChat(chat: Chat) {
        navigationCoordinator.navigateTo(
            ChatListFragmentDirections.actionChatListFragmentToChatFragment(chat)
        )
    }
}