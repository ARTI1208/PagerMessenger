package ru.art2000.pager.ui.fragments.chat

import android.content.Context
import android.content.res.ColorStateList
import android.opengl.Visibility
import android.util.Log
import android.view.*
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.view.setMargins
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListUpdateCallback
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import ru.art2000.pager.R
import ru.art2000.pager.databinding.MessageItemBinding
import ru.art2000.pager.extensions.getColorAttribute
import ru.art2000.pager.models.Message
import ru.art2000.pager.models.MessageAction
import kotlin.system.measureTimeMillis

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
        return MessageItemViewHolder( kotlin.run {
            var binding: MessageItemBinding
            val time = measureTimeMillis {
                binding = MessageItemBinding.inflate(LayoutInflater.from(mContext), parent, false)
            }
            Log.e("MItemLoad", time.toString())
            binding

//            var binding: MessageItemCustomBinding
//            val time = measureTimeMillis {
//                binding = createItemBinding()
//            }
//            Log.e("MItemLoad", time.toString())
//            binding
        }
        )
    }

    override fun onBindViewHolder(holder: MessageItemViewHolder, position: Int) {
        val message = messages[position]
        holder.viewBinding.textTv.text = message.text
        holder.viewBinding.errorImage.visibility = if (message.isError) View.VISIBLE else View.GONE
    }

    override fun getItemCount(): Int = messages.size

    inner class MessageItemCustomBinding(
        val root: View,
        val messageCard: ViewGroup,
        val textTv: TextView,
        val errorImage: ImageView,
    )

    private val dp1: Float by lazy { mContext.resources.getDimension(R.dimen.common_dp_1) }
    private val cardBackground by lazy { ColorStateList.valueOf(mContext.getColorAttribute(R.attr.colorPrimary)) }
    private val errorDrawable by lazy { ContextCompat.getDrawable(mContext, R.drawable.ic_message_failed) }
    private val backgroundDrawable by lazy { ContextCompat.getDrawable(mContext, R.drawable.message_background) }

    private val dp10 by lazy { (10 * dp1).toInt() }

    private fun createItemBinding(): MessageItemCustomBinding {



        val root: FrameLayout
        var time = measureTimeMillis {
            root = FrameLayout(mContext)
            root.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
            )

            root.updatePadding(left = dp10, right = dp10)
        }

        Log.e("MItemLoadRoot", time.toString())

        val card: FrameLayout
        time = measureTimeMillis {
            card = FrameLayout(mContext)
            root.addView(card)

            card.layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.END
                topMargin = (5 * dp1).toInt()
                bottomMargin = (5 * dp1).toInt()
            }

            card.background = backgroundDrawable
            card.minimumWidth = (80 * dp1).toInt()
            card.minimumHeight = (36 * dp1).toInt()
//            card.radius = dp10.toFloat()
        }

        Log.e("MItemLoadCard", time.toString())

        val wrapper: LinearLayout
        time = measureTimeMillis {
            wrapper = LinearLayout(mContext)
            card.addView(wrapper)

            wrapper.orientation = LinearLayout.VERTICAL
            wrapper.layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins((6 * dp1).toInt())
                gravity = Gravity.CENTER
            }
            wrapper.minimumWidth = card.minimumWidth
        }

        Log.e("MItemLoadWrapper", time.toString())

        val messageTextView: TextView
        time = measureTimeMillis {
            messageTextView = TextView(mContext, null, 0, R.style.MessageTextStyle)
            messageTextView.id = R.id.text_tv
            wrapper.addView(messageTextView)

            messageTextView.layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.START or Gravity.CENTER_VERTICAL
//                addRule(RelativeLayout.ALIGN_PARENT_START)
//                addRule(RelativeLayout.CENTER_HORIZONTAL)
            }
        }
        Log.e("MItemLoadText", time.toString())

        val errorImageView: ImageView
        time = measureTimeMillis {
            errorImageView = ImageView(mContext)
            wrapper.addView(errorImageView)

            errorImageView.layoutParams = LinearLayout.LayoutParams(
                (16 * dp1).toInt(), (16 * dp1).toInt()
            ).apply {
//                addRule(RelativeLayout.ALIGN_PARENT_END)
//                addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
//                addRule(RelativeLayout.BELOW, R.id.text_tv)
//                addRule(RelativeLayout.END_OF, R.id.text_tv)
                gravity = Gravity.END
            }

            errorImageView.visibility = View.GONE
            errorImageView.setImageDrawable(errorDrawable)
        }

        Log.e("MItemLoadImg", time.toString())

        return MessageItemCustomBinding(root, card, messageTextView, errorImageView)
    }

    inner class MessageItemViewHolder(
//        val viewBinding: MessageItemCustomBinding
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

            actions.filter { it.displayOnLongClick(message) }.forEach { action ->
                menu.add(action.name).setOnMenuItemClickListener {
                    action(message)
                    true
                }
            }
        }
    }
}