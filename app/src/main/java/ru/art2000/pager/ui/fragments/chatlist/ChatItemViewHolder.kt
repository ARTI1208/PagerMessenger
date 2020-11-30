package ru.art2000.pager.ui.fragments.chatlist

import android.content.Context
import android.graphics.Typeface
import android.text.style.StyleSpan
import android.view.View
import androidx.core.text.buildSpannedString
import androidx.recyclerview.widget.RecyclerView
import ru.art2000.pager.R
import ru.art2000.pager.databinding.CheckableChatListItemBinding
import ru.art2000.pager.models.*

sealed class ChatItemViewHolderBase<VH : RecyclerView.ViewHolder>(
    val viewBinding: CheckableChatListItemBinding,
    private val onChatClick: (ChatView, VH) -> Unit,
    private val getChatView: (Int) -> ChatView?,
) : RecyclerView.ViewHolder(viewBinding.root) {

    private val mContext: Context
        get() = viewBinding.root.context

    init {
        viewBinding.root.setOnClickListener {
            val chatView = getChatView(bindingAdapterPosition) ?: return@setOnClickListener
            @Suppress("UNCHECKED_CAST")
            onChatClick(chatView, this as VH)
        }
    }

    open fun bind(chatView: ChatView?) {
        viewBinding.addresseeTv.text = chatView?.addressee?.toDisplayName() ?: "Loading.."
        viewBinding.lastMessageTv.text = getLastMessagePreview(chatView)
    }

    private val noMessagesString: CharSequence
        get() = buildSpannedString {
            append(
                mContext.getString(R.string.no_messages),
                StyleSpan(Typeface.ITALIC),
                0
            )
        }

    private fun getDraftString(preview: MessageLike): CharSequence? {
        return if (preview.text.isEmpty()) null else buildSpannedString {
            append(
                "${mContext.getString(R.string.chat_item_draft)}: ",
                StyleSpan(Typeface.ITALIC),
                0
            )
            append(preview.text)
        }
    }

    private fun getLastMessagePreview(chatView: ChatView?): CharSequence =
        chatView?.draft?.let { getDraftString(it) } ?: chatView?.lastMessage?.let {
            when (it.status) {
                Message.STATUS_CHAT_CREATED -> noMessagesString
                else -> it.text
            }
        } ?: noMessagesString
}

class MainChatItemViewHolder(
    viewBinding: CheckableChatListItemBinding,
    onChatClick: (ChatView, MainChatItemViewHolder) -> Unit,
    getChatView: (Int) -> ChatView?,
    private val onLongClick: (MainChatItemViewHolder) -> Unit = {},
    private val onCheck: (MainChatItemViewHolder, Boolean) -> Unit = { _, _ -> },
    checkable: Boolean = false,
    private val isChatChecked: (Addressee) -> Boolean
) : ChatItemViewHolderBase<MainChatItemViewHolder>(viewBinding, onChatClick, getChatView) {

    init {
        viewBinding.itemSelectCheckBox.visibility = if (checkable) View.VISIBLE else View.GONE

        if (checkable) {
            viewBinding.itemSelectCheckBox.setOnCheckedChangeListener { _, isChecked ->
                onCheck(this, isChecked)
            }
        }

        viewBinding.root.setOnLongClickListener {
            onLongClick(this)
            true
        }

    }

    override fun bind(chatView: ChatView?) {
        super.bind(chatView)

        if (viewBinding.itemSelectCheckBox.visibility == View.VISIBLE) {

            viewBinding.itemSelectCheckBox.isChecked = if (chatView == null)
                false
            else
                isChatChecked(chatView.addressee)
        }
    }

}

class ChatSelectItemViewHolder(
    viewBinding: CheckableChatListItemBinding,
    onChatClick: (ChatView, ChatSelectItemViewHolder) -> Unit,
    getChatView: (Int) -> ChatView?,
    private val checkable: Boolean,
    private val isChatChecked: (Addressee) -> Boolean,
    private val onChatChecked: (Addressee, Boolean) -> Unit,
) : ChatItemViewHolderBase<ChatSelectItemViewHolder>(viewBinding, onChatClick, getChatView) {

    init {
        viewBinding.itemSelectCheckBox.visibility = if (checkable) View.VISIBLE else View.GONE

        viewBinding.root.setOnClickListener {
            val addressee = getChatView(bindingAdapterPosition) ?: return@setOnClickListener
            onChatClick(addressee, this)

            if (checkable) viewBinding.itemSelectCheckBox.performClick()
        }
    }

    override fun bind(chatView: ChatView?) {
        super.bind(chatView)

        viewBinding.itemSelectCheckBox.apply {
            setOnCheckedChangeListener(null)

            if (chatView == null) {
                isEnabled = false
                isChecked = false
            } else {
                isEnabled = true
                isChecked = isChatChecked(chatView.addressee)
                setOnCheckedChangeListener { _, isChecked ->
                    onChatChecked(chatView.addressee, isChecked)
                }
            }
        }
    }
}