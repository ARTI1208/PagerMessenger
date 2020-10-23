package ru.art2000.pager.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import ru.art2000.pager.db.MessagesDatabase
import ru.art2000.pager.db.addresseeTable
import ru.art2000.pager.db.chatsTable
import ru.art2000.pager.db.messagesTable
import ru.art2000.pager.models.Addressee
import ru.art2000.pager.models.Chat
import ru.art2000.pager.models.Message

class ChatListViewModel(application: Application) : AndroidViewModel(application) {

    fun allChats(): LiveData<List<Chat>> {
        return chatsTable(getApplication()) {
            Transformations.map(liveAll()) { chats ->
                chats.sortedByDescending { chat -> chat.lastMessageId }
            }
        }
    }

    fun createChat(addressee: Int): Chat {
        val chat = Chat(addressee)
        chatsTable(getApplication()) {
            if (byAddresseeNumber(addressee) != null) return@chatsTable

            insertChat(chat)
        }
        return chat
    }

    fun getAddressee(chat: Chat): Addressee? {
        return addresseeTable(getApplication()) { byNumber(chat.addresseeNumber) }
    }

    fun getLastMessage(chat: Chat): Message? {
        return messagesTable(getApplication()) { this.allByChatId(chat.addresseeNumber).lastOrNull() }
    }
}