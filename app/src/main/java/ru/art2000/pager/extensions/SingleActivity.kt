package ru.art2000.pager.extensions

import androidx.fragment.app.Fragment
import ru.art2000.pager.ui.NavigationCoordinator

class ContextNavigationCoordinator internal constructor(private val fragment: Fragment) :
    Lazy<NavigationCoordinator> {

    private var cached: NavigationCoordinator? = null

    override val value: NavigationCoordinator
        get() {
            val context = fragment.context
                ?: throw IllegalStateException("Fragment is not attached to context")

            check(context is NavigationCoordinator) {
                "NavigationCoordinator is not implemented for fragment context"
            }

            return context
        }

    override fun isInitialized(): Boolean = cached != null
}

fun Fragment.contextNavigationCoordinator() = ContextNavigationCoordinator(this)