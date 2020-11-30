package ru.art2000.pager.ui.fragments.chat

import android.content.Context
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ru.art2000.pager.databinding.MessageItemBinding
import ru.art2000.pager.models.Message
import ru.art2000.pager.models.MessageAction

class MessagesListAdapter(
    private val mContext: Context,
    private val actions: List<MessageAction>
) : PagingDataAdapter<Message, MessagesListAdapter.MessageItemViewHolder>(diffCallback) {

    companion object {

        val diffCallback = object : DiffUtil.ItemCallback<Message>() {

            override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageItemViewHolder {
        return MessageItemViewHolder(
            MessageItemBinding.inflate(LayoutInflater.from(mContext), parent, false)
        )
    }

    override fun onBindViewHolder(holder: MessageItemViewHolder, position: Int) {
        val message = getItem(position)
        holder.viewBinding.textTv.text = message?.text
        holder.viewBinding.errorImage.visibility =
            if (message?.isError == true) View.VISIBLE else View.GONE
    }

    inner class MessageItemViewHolder(
        val viewBinding: MessageItemBinding
    ) : RecyclerView.ViewHolder(viewBinding.root), View.OnCreateContextMenuListener {

        init {
            viewBinding.textTv.setTextIsSelectable(true) //TODO cancel selection on tap outside of message

            viewBinding.root.setOnCreateContextMenuListener(this)
        }

        override fun onCreateContextMenu(
            menu: ContextMenu,
            v: View,
            menuInfo: ContextMenu.ContextMenuInfo?
        ) {
            menu.clear()
            val message = getItem(bindingAdapterPosition) ?: return

            actions.filter { it.displayOnClick(message) }.forEach { action ->
                menu.add(action.name).setOnMenuItemClickListener {
                    action(message)
                    true
                }
            }
        }
    }
}