package ru.art2000.pager.ui.fragments.chatlist

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ru.art2000.pager.databinding.CheckableChatListItemBinding
import ru.art2000.pager.models.ChatView

abstract class ChatListAdapter<VH : ChatItemViewHolderBase<*>>(
    protected val mContext: Context,
) : RecyclerView.Adapter<VH>() {

    private var chatViews: List<ChatView> = emptyList()

    var data: List<ChatView>
        get() = chatViews
        set(value) = setNewData(value)

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

    override fun onBindViewHolder(holder: VH, position: Int) {
        val chatView = chatViews[position]
        holder.bind(chatView)
    }

    override fun getItemCount(): Int = chatViews.size
}

class MainChatListAdapter(
    mContext: Context,
    private val onChatClick: (ChatView, MainChatItemViewHolder) -> Unit,
    private val reportSelect: (ChatView, Boolean) -> Unit,
    private val isSelected: (ChatView) -> Boolean,
    private val selectCount: () -> Int,
) : ChatListAdapter<MainChatItemViewHolder>(mContext) {

    companion object {
        const val DEFAULT_MODE = 0

        const val SELECT_MODE = 1
    }

    fun disableSelectMode() {
        notifyItemRangeChanged(0, itemCount)
    }

    override fun getItemViewType(position: Int): Int {
        return if (selectCount() == 0) return DEFAULT_MODE else SELECT_MODE
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainChatItemViewHolder =
        MainChatItemViewHolder(
            CheckableChatListItemBinding.inflate(LayoutInflater.from(mContext), parent, false),
            { chatView, holder ->
                if (viewType == DEFAULT_MODE) {
                    onChatClick(chatView, holder)
                } else {
                    holder.viewBinding.itemSelectCheckBox.performClick()
                }
            },
            { data[it] },
            {
                if (viewType == SELECT_MODE) return@MainChatItemViewHolder

                val chatView = data[it.bindingAdapterPosition]

                reportSelect(chatView, true)
                notifyItemRangeChanged(0, itemCount)
            },
            { holder, isChecked ->

                reportSelect(data[holder.bindingAdapterPosition], isChecked)
                if (selectCount() == 0) {
                    disableSelectMode()
                }

            },
            viewType == SELECT_MODE,
            isSelected
        )

}

class ChatSelectListAdapter(
    mContext: Context,
    private val onChatClick: (ChatView, ChatSelectItemViewHolder) -> Unit,
    private val checkable: Boolean,
    private val isChatChecked: (ChatView) -> Boolean,
    private val onChatChecked: (ChatView, Boolean) -> Unit,
) : ChatListAdapter<ChatSelectItemViewHolder>(mContext) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatSelectItemViewHolder =
        ChatSelectItemViewHolder(
            CheckableChatListItemBinding.inflate(LayoutInflater.from(mContext), parent, false),
            onChatClick,
            { data[it] },
            checkable,
            isChatChecked,
            onChatChecked
        )

}