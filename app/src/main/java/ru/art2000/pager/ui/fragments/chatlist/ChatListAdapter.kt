package ru.art2000.pager.ui.fragments.chatlist

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.art2000.pager.databinding.ChatListItemBinding
import ru.art2000.pager.models.Chat

class ChatListAdapter(
    private val mContext: Context,
    private val chats: List<Chat>,
    private val onChatClick: (Chat) -> Unit
) : RecyclerView.Adapter<ChatListAdapter.ChatItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatItemViewHolder {
        return ChatItemViewHolder(
            ChatListItemBinding.inflate(LayoutInflater.from(mContext), parent, false)
        )
    }

    override fun onBindViewHolder(holder: ChatItemViewHolder, position: Int) {
        holder.viewBinding.addresseeTv.text = chats[position].addresseeNumber.toString()
//        holder.viewBinding.lastMessageTv.text = chats[position].lastMessage?.text ?: ""

    }

    override fun getItemCount(): Int {
        return chats.size
    }


    inner class ChatItemViewHolder(val viewBinding: ChatListItemBinding)
        : RecyclerView.ViewHolder(viewBinding.root) {

        init {
            viewBinding.root.setOnClickListener {
                onChatClick(chats[bindingAdapterPosition])
            }
        }
    }
}