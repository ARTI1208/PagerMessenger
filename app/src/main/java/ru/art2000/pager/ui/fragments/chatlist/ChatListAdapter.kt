package ru.art2000.pager.ui.fragments.chatlist

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.art2000.pager.databinding.ChatListItemBinding
import ru.art2000.pager.models.Chat
import ru.art2000.pager.viewmodels.ChatListViewModel
import kotlin.concurrent.thread

class ChatListAdapter(
    private val mActivity: Activity,
    private val chats: List<Chat>,
    private val viewModel: ChatListViewModel,
    private val onChatClick: (Chat) -> Unit
) : RecyclerView.Adapter<ChatListAdapter.ChatItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatItemViewHolder =
        ChatItemViewHolder(
            ChatListItemBinding.inflate(LayoutInflater.from(mActivity), parent, false)
        )

    override fun onBindViewHolder(holder: ChatItemViewHolder, position: Int) {
        holder.viewBinding.addresseeTv.text = chats[position].addresseeNumber.toString()
        thread {
            val name = viewModel.getAddressee(chats[position])
            val message = viewModel.getLastMessage(chats[position])
            mActivity.runOnUiThread {
                holder.viewBinding.addresseeTv.text = name?.toDisplayName()
                    ?: chats[position].addresseeNumber.toString()

                holder.viewBinding.lastMessageTv.text = message?.text ?: ""
            }
        }

    }

    override fun getItemCount(): Int = chats.size

    inner class ChatItemViewHolder(val viewBinding: ChatListItemBinding) :
        RecyclerView.ViewHolder(viewBinding.root) {

        init {
            viewBinding.root.setOnClickListener {
                onChatClick(chats[bindingAdapterPosition])
            }
        }
    }
}