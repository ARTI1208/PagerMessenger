package ru.art2000.pager.ui.fragments.chat

import android.content.Context
import android.util.Log
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListUpdateCallback
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
        holder.viewBinding.errorImage.visibility = if (message.status < 0) View.VISIBLE else View.GONE


//        holder.viewBinding.messageCard.backgroundTintList = if (message.success) {
//            ColorStateList.valueOf(ContextCompat.getColor(mContext, R.color.colorPrimary))
//        } else {
//            ColorStateList.valueOf(ContextCompat.getColor(mContext, R.color.messageFailed))
//        }

//        holder.viewBinding.statusTv.text = messages[position].success.toString()
    }

    override fun getItemCount(): Int {
        return messages.size
    }

    inner class MessageItemViewHolder(val viewBinding: MessageItemBinding) :
        RecyclerView.ViewHolder(viewBinding.root), View.OnCreateContextMenuListener {

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

            actions.filter { it.displayOnLongClick(message) }.forEach { action ->
                menu.add(action.name).setOnMenuItemClickListener {
                    action(message)
                    true
                }
            }
        }
    }
}