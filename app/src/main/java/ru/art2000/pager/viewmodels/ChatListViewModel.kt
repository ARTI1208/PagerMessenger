package ru.art2000.pager.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import ru.art2000.pager.db.MessagesDatabase
import ru.art2000.pager.db.chatsTable
import ru.art2000.pager.models.Chat

class ChatListViewModel(application: Application) : AndroidViewModel(application) {

    fun allChats(): LiveData<List<Chat>> {
        return chatsTable(getApplication()) { liveAll() }
    }

    fun createChat(addressee: Int): Chat {
        val chat = Chat(addressee)
        chatsTable(getApplication()) { insertChat(chat) }
        return chat
    }
}