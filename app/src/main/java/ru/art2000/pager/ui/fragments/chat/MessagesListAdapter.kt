package ru.art2000.pager.ui.fragments.chat

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.art2000.pager.databinding.MessageItemBinding
import ru.art2000.pager.models.Message

class MessagesListAdapter(
    private val mContext: Context,
    private val messages: List<Message>
): RecyclerView.Adapter<MessagesListAdapter.MessageItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageItemViewHolder {
        return MessageItemViewHolder(
            MessageItemBinding.inflate(LayoutInflater.from(mContext), parent, false)
        )
    }

    override fun onBindViewHolder(holder: MessageItemViewHolder, position: Int) {
        holder.viewBinding.textTv.text = messages[position].text
    }

    override fun getItemCount(): Int {
        return messages.size
    }

    class MessageItemViewHolder(val viewBinding: MessageItemBinding)
        : RecyclerView.ViewHolder(viewBinding.root)
}