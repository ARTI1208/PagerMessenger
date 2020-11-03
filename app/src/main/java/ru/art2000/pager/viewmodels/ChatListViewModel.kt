package ru.art2000.pager.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import ru.art2000.pager.db.MessagesDatabase
import ru.art2000.pager.db.chatsTable
import ru.art2000.pager.db.messagesTable
import ru.art2000.pager.models.Addressee
import ru.art2000.pager.models.Chat
import ru.art2000.pager.models.ChatView
import ru.art2000.pager.models.Message
import kotlin.concurrent.thread

class ChatListViewModel(application: Application) : AndroidViewModel(application) {

    val selectedChats = mutableSetOf<ChatView>()

    private val mIsSelectMode = MutableLiveData(false)
    val isSelectMode get() = mIsSelectMode

    var shouldScrollToTop: Boolean = false

    fun allChats(): LiveData<List<ChatView>> {
        val mediator = MediatorLiveData<List<ChatView>>()
        val messagesDatabase = MessagesDatabase.getInstance(getApplication())
        mediator.addSource(messagesDatabase.chatsDao().liveAll()) { chats ->
            thread {
                val views = chats.map {
                    val addressee = messagesDatabase.addresseeTable { byNumber(it.addresseeNumber) }
                        ?: Addressee(it.addresseeNumber)

                    val lastMessage =
                        messagesDatabase.messagesTable { lastMessageForChatWithDraft(it.addresseeNumber) }

                    ChatView(addressee, lastMessage)
                }.sortedByDescending { it.lastMessage?.time ?: -1 }

                mediator.postValue(views)
            }
        }

        return mediator
    }

    fun createChat(addressee: Int): ChatView {
        chatsTable(getApplication()) {
            if (byAddresseeNumber(addressee) != null) return@chatsTable

            insertChat(Chat(addressee))
        }

        val initialMessage = Message(0, addressee, "", status = Message.STATUS_CHAT_CREATED)
        shouldScrollToTop = messagesTable(getApplication()) {
            insertChatCreatedMessage(initialMessage)
        }

        return ChatView(Addressee(addressee), initialMessage)
    }

    fun onSelectModeCanceled() {
        selectedChats.clear()
        mIsSelectMode.value = false
    }

    fun deleteSelectedChats() {
        thread {
            chatsTable(getApplication()) {
                deleteChatsAndMessages(selectedChats.map { it.chat })
            }

            selectedChats.clear()
            mIsSelectMode.postValue(false)
        }
    }

    fun addOrRemoveChat(chatView: ChatView, add: Boolean) {
        if (add) {
            selectedChats += chatView
        } else {
            selectedChats -= chatView
        }
        mIsSelectMode.value = selectedChats.isNotEmpty()
    }
}