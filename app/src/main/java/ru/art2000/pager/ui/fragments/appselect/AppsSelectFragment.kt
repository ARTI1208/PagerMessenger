package ru.art2000.pager.ui.fragments.appselect

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import ru.art2000.pager.R
import ru.art2000.pager.databinding.AppSelectingFragmentBinding
import ru.art2000.pager.extensions.contextNavigationCoordinator
import ru.art2000.pager.models.AppInfo
import ru.art2000.pager.viewmodels.ForwardingViewModel

class AppsSelectFragment : Fragment() {

    private val args: AppsSelectFragmentArgs by navArgs()
    private val viewModel: ForwardingViewModel by viewModels()
    private val navigationCoordinator by contextNavigationCoordinator()

    private val isSelectMode: Boolean inline get() = args.addresseeId >= 0

    private lateinit var viewBinding: AppSelectingFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = AppSelectingFragmentBinding.inflate(inflater, container, false).also {
        viewBinding = it
    }.root

    @SuppressLint("QueryPermissionsNeeded")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val title =
            if (isSelectMode) R.string.app_select_by_chats_title
            else R.string.app_select_by_apps_title

        navigationCoordinator.setWindowTitle(title)
        setHasOptionsMenu(true)

        viewModel.loadApps()

        viewBinding.recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val appsListAdapter = AppsListAdapter(
            requireContext(),
            emptyList(),
            isSelectMode,
            ::showChatsForApp,
            { viewModel.isPackageSelectedForForwarding(args.addresseeId, it.packageName) },
            { app, isChecked ->
                viewModel.savePackage(
                    args.addresseeId,
                    app.packageName,
                    isChecked
                )
            },
        )

        viewBinding.recyclerView.adapter = appsListAdapter

        viewModel.loading.observe(viewLifecycleOwner) { loading ->
            viewBinding.loadIndicator.apply {
                if (loading) show() else hide()
            }
        }

        viewModel.apps.observe(viewLifecycleOwner) {
            appsListAdapter.setNewData(it)
        }

        viewBinding.searchView.apply {
            setQuery(viewModel.textFilter, false)
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return false
                }

                override fun onQueryTextChange(newText: String): Boolean {
                    viewModel.textFilter = newText
                    return true
                }

            })
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.app_list_menu, menu)

        menu.findItem(R.id.show_system_apps).isChecked = viewModel.showSystemApps
        menu.findItem(R.id.only_launchable_apps).isChecked = viewModel.onlyLaunchableApps
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.isCheckable) item.isChecked = !item.isChecked

        when (item.itemId) {
            R.id.show_system_apps -> viewModel.showSystemApps = item.isChecked
            R.id.only_launchable_apps -> viewModel.onlyLaunchableApps = item.isChecked
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onPause() {
        super.onPause()
        viewModel.onDestroy(isSelectMode)
    }

    private fun showChatsForApp(appInfo: AppInfo) {
        navigationCoordinator.navigateTo(
            AppsSelectFragmentDirections.actionAppsListeningSelectFragmentToSelectChatFragment(
                appInfo.packageName
            )
        )
    }

}