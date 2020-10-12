package ru.art2000.pager.extensions

import android.content.Context
import android.content.SharedPreferences

/**
 * TODO Under construction
 */
class HashingSharedPreferences(context: Context, fileName: String) : SharedPreferences by context.getSharedPreferences(fileName, Context.MODE_PRIVATE) {

//    private val underlyingSharedPreferences = context.getSharedPreferences(fileName, Context.MODE_PRIVATE)
//
//    override fun getAll(): MutableMap<String, *> {
//        return underlyingSharedPreferences.all
//    }
//
//    override fun getString(key: String, defValue: String?): String? {
//        return underlyingSharedPreferences.getString(key, defValue)
//    }
//
//    override fun getStringSet(key: String, defValues: MutableSet<String>?): MutableSet<String> {
//        underlyingSharedPreferences.getStringSet(key, defValues)
//    }
//
//    override fun getInt(key: String, defValue: Int): Int {
//        TODO("Not yet implemented")
//    }
//
//    override fun getLong(key: String, defValue: Long): Long {
//        TODO("Not yet implemented")
//    }
//
//    override fun getFloat(key: String, defValue: Float): Float {
//        TODO("Not yet implemented")
//    }
//
//    override fun getBoolean(key: String, defValue: Boolean): Boolean {
//        TODO("Not yet implemented")
//    }
//
//    override fun contains(key: String): Boolean {
//        TODO("Not yet implemented")
//    }
//
//    override fun edit(): SharedPreferences.Editor {
//        TODO("Not yet implemented")
//    }
//
//    override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
//        TODO("Not yet implemented")
//    }
//
//    override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
//        TODO("Not yet implemented")
//    }
}