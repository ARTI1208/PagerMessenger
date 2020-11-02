package ru.art2000.pager.ui

import androidx.navigation.NavController
import androidx.navigation.NavDirections

interface NavigationCoordinator {

    val navController: NavController

    fun navigateTo(direction: NavDirections)
}