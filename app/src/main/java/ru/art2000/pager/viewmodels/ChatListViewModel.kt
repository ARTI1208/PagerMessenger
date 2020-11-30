package ru.art2000.pager.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.art2000.pager.db.addresseeTable
import ru.art2000.pager.db.messagesTable
import ru.art2000.pager.models.*
import kotlin.concurrent.thread

class ChatListViewModel(application: Application) : AndroidViewModel(application) {

    val selectedChats = mutableSetOf<Addressee>()

    private val mIsSelectMode = MutableLiveData(false)
    val isSelectMode get() = mIsSelectMode

    var shouldScrollToTop: Boolean = false

    fun allChatsFlow(): Flow<PagingData<ChatView>> {

        val config = PagingConfig(40, 20, maxSize = 100)
        val pager = Pager(config) { addresseeTable(getApplication()) { sourceAllJoinedOrdered() } }

        return pager.flow
    }

    fun createChat(addressee: Int): Addressee {
        addresseeTable(getApplication()) {
            if (byNumber(addressee) != null) return@addresseeTable

            insertAddressee(Addressee(addressee))
        }

        val initialMessage = Message(0, addressee, "", status = Message.STATUS_CHAT_CREATED)
        shouldScrollToTop = messagesTable(getApplication()) {
            insertChatCreatedMessage(initialMessage)
        }

        return Addressee(addressee)
    }

    fun onSelectModeCanceled() {
        selectedChats.clear()
        mIsSelectMode.value = false
    }

    fun deleteSelectedChats() {
        thread {
            addresseeTable(getApplication()) {
                deleteAddressees(selectedChats)
            }

            selectedChats.clear()
            mIsSelectMode.postValue(false)
        }
    }

    fun addOrRemoveChat(addressee: Addressee, add: Boolean) {
        if (add) {
            selectedChats += addressee
        } else {
            selectedChats -= addressee
        }
        mIsSelectMode.value = selectedChats.isNotEmpty()
    }
}