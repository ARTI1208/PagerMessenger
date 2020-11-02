package ru.art2000.pager.viewmodels

import android.app.Application
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.preference.PreferenceManager
import ru.art2000.pager.db.MessagesDatabase
import ru.art2000.pager.db.chatsTable
import ru.art2000.pager.db.messagesTable
import ru.art2000.pager.helpers.SettingsKeys
import ru.art2000.pager.models.Addressee
import ru.art2000.pager.models.Chat
import ru.art2000.pager.models.ChatView
import ru.art2000.pager.models.Message
import java.util.*
import kotlin.concurrent.thread

class ChatListViewModel(application: Application) : AndroidViewModel(application) {

    private var prefs: SharedPreferences? = null
    private var selectedChats: MutableSet<String>? = null

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

    private fun init() {
        if (prefs == null) {
            prefs = PreferenceManager.getDefaultSharedPreferences(getApplication())
        }

        if (selectedChats == null) {
            selectedChats = HashSet(
                prefs?.getStringSet(SettingsKeys.NOTIFICATION_FORWARDING_TO_CHATS_KEY, emptySet())!!
            )
        }
    }

    fun isChatSelected(chatView: ChatView): Boolean {
        init()

        return selectedChats?.contains(chatView.addressee.number.toString()) ?: false
    }

    fun onChatChecked(chatView: ChatView, checked: Boolean) {
        init()

        selectedChats?.apply {
            val asString = chatView.addressee.number.toString()
            if (checked) add(asString)
            else remove(asString)
        }
    }

    fun onDestroy(isSelectMode: Boolean) {
        if (!isSelectMode) return

        prefs?.edit {
            putStringSet(
                SettingsKeys.NOTIFICATION_FORWARDING_TO_CHATS_KEY,
                selectedChats ?: emptySet<String>()
            )
        }
    }
}