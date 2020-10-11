package ru.art2000.pager.ui

import androidx.navigation.NavDirections

interface NavigationCoordinator {

    fun navigateTo(direction: NavDirections)

    fun setSupportsBack(supports: Boolean)
}