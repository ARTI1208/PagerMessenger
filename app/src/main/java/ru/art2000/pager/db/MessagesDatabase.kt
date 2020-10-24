package ru.art2000.pager.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import ru.art2000.pager.models.Addressee
import ru.art2000.pager.models.Chat
import ru.art2000.pager.models.Message

@Database(entities = [Addressee::class, Chat::class, Message::class], version = 1, exportSchema = true)
abstract class MessagesDatabase : RoomDatabase() {

    public abstract fun addresseeDao(): AddresseeDao

    public abstract fun chatsDao(): ChatsDao

    public abstract fun messagesDao(): MessagesDao

    fun <T> addresseeTable(actions: AddresseeDao.() -> T): T = actions(addresseeDao())

    fun <T> chatsTable(actions: ChatsDao.() -> T): T = actions(chatsDao())

    fun <T> messagesTable(actions: MessagesDao.() -> T): T = actions(messagesDao())

    companion object {

        private var INSTANCE: MessagesDatabase? = null

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(context.applicationContext,
                MessagesDatabase::class.java, "chat.db")
                .fallbackToDestructiveMigration()
                .build()

        public fun getInstance(context: Context): MessagesDatabase {
            return INSTANCE ?: synchronized(this) {
                buildDatabase(context).also { INSTANCE = it }
            }
        }
    }
}