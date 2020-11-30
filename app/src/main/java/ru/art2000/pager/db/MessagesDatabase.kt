package ru.art2000.pager.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import ru.art2000.pager.models.Addressee
import ru.art2000.pager.models.Message
import ru.art2000.pager.models.MessageDraft

@Database(
    entities = [Addressee::class, Message::class, MessageDraft::class],
    version = 1,
    exportSchema = true
)
abstract class MessagesDatabase : RoomDatabase() {

    public abstract fun addresseeDao(): AddresseeDao

    public abstract fun messagesDao(): MessagesDao

    public abstract fun draftsDao(): DraftsDao

    fun <T> addresseeTable(actions: AddresseeDao.() -> T): T = actions(addresseeDao())

    fun <T> messagesTable(actions: MessagesDao.() -> T): T = actions(messagesDao())

    fun <T> draftsTable(actions: DraftsDao.() -> T): T = actions(draftsDao())

    companion object {

        private var INSTANCE: MessagesDatabase? = null

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(
                context.applicationContext,
                MessagesDatabase::class.java, "chat.db"
            )
                .fallbackToDestructiveMigration()
                .build()

        public fun getInstance(context: Context): MessagesDatabase {
            return INSTANCE ?: synchronized(this) {
                buildDatabase(context).also { INSTANCE = it }
            }
        }
    }
}