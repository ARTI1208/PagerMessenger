package ru.art2000.pager.ui.fragments.chat

import android.app.Activity
import android.content.Context
import android.view.*
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ru.art2000.pager.databinding.MessageItemBinding
import ru.art2000.pager.models.Message
import ru.art2000.pager.models.MessageAction

class MessagesListAdapter(
    private val mContext: Context,
    private var messages: List<Message>,
    private val actions: List<MessageAction> = emptyList()
) : RecyclerView.Adapter<MessagesListAdapter.MessageItemViewHolder>() {

    public fun setNewData(newMessages: List<Message>) {
        val result = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize(): Int {
                return messages.size
            }

            override fun getNewListSize(): Int {
                return newMessages.size
            }

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return messages[oldItemPosition] == newMessages[newItemPosition]
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return messages[oldItemPosition] == newMessages[newItemPosition]
            }

        })

        messages = newMessages
        result.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageItemViewHolder {
        return MessageItemViewHolder(
            MessageItemBinding.inflate(LayoutInflater.from(mContext), parent, false)
        )
    }

    override fun onBindViewHolder(holder: MessageItemViewHolder, position: Int) {
        val message = messages[position]
        holder.viewBinding.textTv.text = message.text
        holder.viewBinding.errorImage.visibility = if (message.isError) View.VISIBLE else View.GONE
    }

    override fun getItemCount(): Int = messages.size

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
            val message = messages[bindingAdapterPosition]
            menu.clear()

            actions.filter { it.displayOnClick(message) }.forEach { action ->
                menu.add(action.name).setOnMenuItemClickListener {
                    action(message)
                    true
                }
            }
        }
    }
}