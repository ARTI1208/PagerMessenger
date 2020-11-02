package ru.art2000.pager.ui.fragments.appselect

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ru.art2000.pager.databinding.AppListItemBinding
import ru.art2000.pager.models.AppInfo
import ru.art2000.pager.viewmodels.AppListViewModel

class AppsListAdapter(
    private val mContext: Context,
    private var apps: List<AppInfo>,
    private val viewModel: AppListViewModel
) : RecyclerView.Adapter<AppsListAdapter.AppItemViewHolder>() {

    fun setNewData(newApps: List<AppInfo>) {
        val result = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize(): Int {
                return apps.size
            }

            override fun getNewListSize(): Int {
                return newApps.size
            }

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return apps[oldItemPosition].packageName == newApps[newItemPosition].packageName
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return apps[oldItemPosition].packageName == newApps[newItemPosition].packageName
            }

        })

        apps = newApps
        result.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppItemViewHolder {
        return AppItemViewHolder(
            AppListItemBinding.inflate(
                LayoutInflater.from(mContext),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: AppItemViewHolder, position: Int) {
        val appItem = apps[position]

        val packageName = appItem.packageName

        holder.viewBinding.itemSelectCheckBox.setOnCheckedChangeListener(null)
        holder.viewBinding.root.setOnClickListener(null)

        holder.viewBinding.itemSelectCheckBox.isChecked =
            viewModel.isPackageSelectedForForwarding(packageName)

        holder.viewBinding.itemSelectCheckBox.setOnCheckedChangeListener { _, isChecked ->
            viewModel.savePackage(packageName, isChecked)
        }
        holder.viewBinding.root.setOnClickListener {
            holder.viewBinding.itemSelectCheckBox.performClick()
        }

        holder.viewBinding.appName.text = appItem.title
        holder.viewBinding.appPackage.text = appItem.packageName
        holder.viewBinding.appLogo.setImageDrawable(appItem.icon)
    }

    override fun getItemCount(): Int = apps.size

    class AppItemViewHolder(
        val viewBinding: AppListItemBinding
    ) : RecyclerView.ViewHolder(viewBinding.root)
}