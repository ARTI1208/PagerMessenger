package ru.art2000.pager.extensions

import com.google.common.collect.Multimap

operator fun <K, V> Multimap<K, V>.set(key: K, value: V) = put(key, value)