package ru.art2000.pager

import android.content.IntentFilter
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavDirections
import androidx.navigation.Navigation
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import ru.art2000.pager.databinding.MainActivityBinding
import ru.art2000.pager.receivers.ACTION_USB_PERMISSION
import ru.art2000.pager.receivers.usbReceiver
import ru.art2000.pager.ui.NavigationCoordinator

class MainActivity : AppCompatActivity(), NavigationCoordinator,
    NavController.OnDestinationChangedListener {

    private lateinit var navigationController: NavController
    private lateinit var mainActivityBinding: MainActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mainActivityBinding = MainActivityBinding.inflate(layoutInflater)
        setContentView(mainActivityBinding.root)

        navigationController = Navigation.findNavController(this, R.id.fragment_container)

        setup()
    }

    override fun onDestroy() {
        super.onDestroy()

        navigationController.removeOnDestinationChangedListener(this)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.popBackStack()
    }

    private fun setup() {
        val config = AppBarConfiguration.Builder(R.id.login, R.id.chatListFragment).build()
        setupActionBarWithNavController(navigationController, config)

        navigationController.addOnDestinationChangedListener(this)

        val filter = IntentFilter(ACTION_USB_PERMISSION)
        registerReceiver(usbReceiver, filter)
    }

    override val navController: NavController
        get() = navigationController

    override fun navigateTo(direction: NavDirections) {
        navController.navigate(direction)
    }

    override fun setWindowTitle(title: CharSequence) {
        supportActionBar?.title = title
    }

    override fun setWindowTitle(@StringRes title: Int) {
        supportActionBar?.setTitle(title)
    }

    override fun onDestinationChanged(
        controller: NavController,
        destination: NavDestination,
        arguments: Bundle?
    ) {
        val imm = ContextCompat.getSystemService(this, InputMethodManager::class.java)!!
        imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
    }
}