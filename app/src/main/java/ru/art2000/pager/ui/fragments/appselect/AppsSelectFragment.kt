package ru.art2000.pager.ui.fragments.appselect

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import ru.art2000.pager.R
import ru.art2000.pager.databinding.AppSelectingFragmentBinding
import ru.art2000.pager.viewmodels.AppListViewModel

class AppsSelectFragment : Fragment() {

    private lateinit var viewBinding: AppSelectingFragmentBinding

    private val viewModel: AppListViewModel by viewModels()

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
        setHasOptionsMenu(true)

        viewBinding.recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val appsListAdapter = AppsListAdapter(
            requireContext(),
            emptyList(),
            viewModel
        )

        viewBinding.recyclerView.adapter = appsListAdapter

        viewModel.loading.observe(viewLifecycleOwner) { loading ->
            viewBinding.loadIndicator.apply {
                if (loading) show() else hide()
            }
        }

        viewModel.apps.observe(viewLifecycleOwner) {
            (viewBinding.recyclerView.adapter as AppsListAdapter).setNewData(it)
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
        viewModel.writePackages()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.cleanUp(this)
        viewModelStore.clear()
    }

}