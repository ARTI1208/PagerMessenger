package ru.art2000.pager.ui.fragments.chatlist

import android.content.Context
import android.graphics.Typeface
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.buildSpannedString
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ru.art2000.pager.R
import ru.art2000.pager.databinding.CheckableChatListItemBinding
import ru.art2000.pager.models.ChatView

class ChatListAdapter(
    private val mContext: Context,
    private var chatViews: List<ChatView>,
    private val onChatClick: (ChatView, ChatItemViewHolder) -> Unit,
    private val checkable: Boolean,
    private val isChatChecked: (ChatView) -> Boolean,
    private val onChatChecked: (ChatView, Boolean) -> Unit,
) : RecyclerView.Adapter<ChatListAdapter.ChatItemViewHolder>() {

    var data: List<ChatView>
        get() = chatViews
        set(value) {
            setNewData(value)
        }

    private fun setNewData(newChats: List<ChatView>) {
        val result = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize(): Int {
                return chatViews.size
            }

            override fun getNewListSize(): Int {
                return newChats.size
            }

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return chatViews[oldItemPosition].addressee.number == newChats[newItemPosition].addressee.number
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                val old = chatViews[oldItemPosition]
                val new = newChats[newItemPosition]
                return old.addressee.nickname == new.addressee.nickname
                        && old.lastMessage?.text == new.lastMessage?.text
                        && old.lastMessage?.status == new.lastMessage?.status
            }

        })

        chatViews = newChats
        result.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatItemViewHolder =
        ChatItemViewHolder(
            CheckableChatListItemBinding.inflate(LayoutInflater.from(mContext), parent, false)
        )

    override fun onBindViewHolder(holder: ChatItemViewHolder, position: Int) {
        val chatView = chatViews[position]
        holder.bind(chatView)
    }

    override fun getItemCount(): Int = chatViews.size

    inner class ChatItemViewHolder(val viewBinding: CheckableChatListItemBinding) :
        RecyclerView.ViewHolder(viewBinding.root) {

        init {
            viewBinding.itemSelectCheckBox.visibility = if (checkable)
                View.VISIBLE
            else
                View.GONE

            viewBinding.root.setOnClickListener {
                onChatClick(chatViews[bindingAdapterPosition], this)

                if (checkable) viewBinding.itemSelectCheckBox.performClick()
            }
        }

        fun bind(chatView: ChatView) {
            viewBinding.addresseeTv.text = chatView.addressee.toDisplayName()
            viewBinding.lastMessageTv.text = getLastMessagePreview(chatView)

            viewBinding.itemSelectCheckBox.apply {
                setOnCheckedChangeListener(null)
                isChecked = isChatChecked(chatView)
                setOnCheckedChangeListener { _, isChecked ->
                    onChatChecked(chatView, isChecked)
                }
            }

        }

        private fun getLastMessagePreview(chatView: ChatView): CharSequence =
            chatView.lastMessage?.let {
                if (it.isDraft) buildSpannedString {
                    append(
                        "${this@ChatListAdapter.mContext.getString(R.string.chat_item_draft)}: ",
                        StyleSpan(Typeface.ITALIC),
                        0
                    )
                    append(it.text)
                } else it.text
            } ?: buildSpannedString {
                append(
                    this@ChatListAdapter.mContext.getString(R.string.no_messages),
                    StyleSpan(Typeface.ITALIC),
                    0
                )
            }
    }
}