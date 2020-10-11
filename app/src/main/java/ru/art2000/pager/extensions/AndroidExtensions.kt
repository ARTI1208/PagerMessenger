package ru.art2000.pager.extensions

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

fun Fragment.requireCompatActivity() = (requireActivity() as AppCompatActivity)