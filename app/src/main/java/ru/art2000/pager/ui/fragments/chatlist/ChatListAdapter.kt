package ru.art2000.pager.ui.fragments.chatlist

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import ru.art2000.pager.databinding.CheckableChatListItemBinding
import ru.art2000.pager.models.Addressee
import ru.art2000.pager.models.ChatView

abstract class ChatListAdapter<VH : ChatItemViewHolderBase<*>>(
    protected val mContext: Context,
) : PagingDataAdapter<ChatView, VH>(diffCallback) {

    companion object {

        private val diffCallback = object : DiffUtil.ItemCallback<ChatView>() {

            override fun areItemsTheSame(oldItem: ChatView, newItem: ChatView): Boolean {
                return oldItem.addressee.number == newItem.addressee.number
            }

            override fun areContentsTheSame(oldItem: ChatView, newItem: ChatView): Boolean {
                return oldItem.addressee.nickname == newItem.addressee.nickname
                        && oldItem.chatPreview?.text == newItem.chatPreview?.text
            }
        }
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val chatView = getItem(position)
        holder.bind(chatView)
    }
}

class MainChatListAdapter(
    mContext: Context,
    private val onChatClick: (ChatView, MainChatItemViewHolder) -> Unit,
    private val reportSelect: (Addressee, Boolean) -> Unit,
    private val isSelected: (Addressee) -> Boolean,
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
            { getItem(it) },
            {
                if (viewType == SELECT_MODE) return@MainChatItemViewHolder

                val chatView = getItem(it.bindingAdapterPosition) ?: return@MainChatItemViewHolder

                reportSelect(chatView.addressee, true)
                notifyItemRangeChanged(0, itemCount)
            },
            { holder, isChecked ->
                val chatView =
                    getItem(holder.bindingAdapterPosition) ?: return@MainChatItemViewHolder
                reportSelect(chatView.addressee, isChecked)
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
    private val isChatChecked: (Addressee) -> Boolean,
    private val onChatChecked: (Addressee, Boolean) -> Unit,
) : ChatListAdapter<ChatSelectItemViewHolder>(mContext) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatSelectItemViewHolder =
        ChatSelectItemViewHolder(
            CheckableChatListItemBinding.inflate(LayoutInflater.from(mContext), parent, false),
            onChatClick,
            { getItem(it) },
            checkable,
            isChatChecked,
            onChatChecked
        )

}