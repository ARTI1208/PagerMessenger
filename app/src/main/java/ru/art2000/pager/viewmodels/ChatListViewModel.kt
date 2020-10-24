package ru.art2000.pager.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import ru.art2000.pager.db.MessagesDatabase
import ru.art2000.pager.db.addresseeTable
import ru.art2000.pager.db.chatsTable
import ru.art2000.pager.db.messagesTable
import ru.art2000.pager.models.Addressee
import ru.art2000.pager.models.Chat
import ru.art2000.pager.models.ChatView
import ru.art2000.pager.models.Message
import kotlin.concurrent.thread

class ChatListViewModel(application: Application) : AndroidViewModel(application) {

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

        val initialMessage = Message(0, addressee, "", status = Message.STATUS_DRAFT)
        messagesTable(getApplication()) {
            insertOrUpdateDraft(initialMessage)
        }

        return ChatView(Addressee(addressee), initialMessage)
    }

    fun addressees(): LiveData<List<Addressee>> = addresseeTable(getApplication()) { liveAll() }

    fun getAddressee(chat: Chat): Addressee? {
        return addresseeTable(getApplication()) { byNumber(chat.addresseeNumber) }
    }

    fun getLastMessage(chat: Chat): Message? {
        return messagesTable(getApplication()) { this.allByChatId(chat.addresseeNumber).lastOrNull() }
    }
}