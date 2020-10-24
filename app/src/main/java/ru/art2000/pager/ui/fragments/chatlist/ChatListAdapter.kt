package ru.art2000.pager.ui.fragments.chatlist

import android.app.Activity
import android.graphics.Typeface
import android.text.style.StyleSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.text.buildSpannedString
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListUpdateCallback
import androidx.recyclerview.widget.RecyclerView
import ru.art2000.pager.R
import ru.art2000.pager.databinding.ChatListItemBinding
import ru.art2000.pager.models.Chat
import ru.art2000.pager.models.ChatView
import ru.art2000.pager.models.Message
import ru.art2000.pager.viewmodels.ChatListViewModel
import kotlin.concurrent.thread

class ChatListAdapter(
    private val mActivity: Activity,
    private var chats: List<ChatView>,
    private val onChatClick: (ChatView) -> Unit
) : RecyclerView.Adapter<ChatListAdapter.ChatItemViewHolder>() {

    public var data: List<ChatView>
        get() = chats
        set(value) { setNewData(value) }

    private fun setNewData(newChats: List<ChatView>) {
        val result = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize(): Int {
                return chats.size
            }

            override fun getNewListSize(): Int {
                return newChats.size
            }

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return chats[oldItemPosition].addressee.number == newChats[newItemPosition].addressee.number
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                val old = chats[oldItemPosition]
                val new = newChats[newItemPosition]
                return old.addressee.nickname == new.addressee.nickname
                        && old.lastMessage?.text == new.lastMessage?.text
                        && old.lastMessage?.status == new.lastMessage?.status
            }

        })

        chats = newChats
        result.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatItemViewHolder =
        ChatItemViewHolder(
            ChatListItemBinding.inflate(LayoutInflater.from(mActivity), parent, false)
        )

    override fun onBindViewHolder(holder: ChatItemViewHolder, position: Int) {
        val chat = chats[position]

        holder.viewBinding.addresseeTv.text = chat.addressee.toDisplayName()
        holder.viewBinding.lastMessageTv.text = chat.lastMessage?.let {
            if (it.isDraft) buildSpannedString {
                append("${this@ChatListAdapter.mActivity.getString(R.string.chat_item_draft)}: ", StyleSpan(Typeface.ITALIC), 0)
                append(it.text)
            } else it.text
        } ?: ""
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