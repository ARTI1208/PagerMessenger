package ru.art2000.pager

import android.app.Application
import android.content.Context
import java.io.File
import java.util.*

class PagerApplication : Application() {

    companion object {

        private lateinit var mInstance: PagerApplication

        val instance: PagerApplication get() = mInstance
    }

    object Logger {

        private const val filename = "logs.txt"

        val logFile: File get() = File(instance.filesDir, filename)

        fun log(message: String) {
            instance.openFileOutput(filename, Context.MODE_APPEND)
                .writer()
                .use {
                    it.write("${Date()}: $message\n")
                }

        }

    }

    init {
        mInstance = this
    }

}