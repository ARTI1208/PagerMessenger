package ru.art2000.pager.viewmodels

import android.app.Application
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.core.content.edit
import androidx.core.os.ConfigurationCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import ru.art2000.pager.BuildConfig
import ru.art2000.pager.helpers.SettingsKeys.NOTIFICATION_FORWARDING_FROM_PACKAGES_KEY
import ru.art2000.pager.models.AppInfo
import java.text.Collator
import java.util.*
import kotlin.concurrent.thread

class AppListViewModel(application: Application) : AndroidViewModel(application) {

    private val mAllApps = MutableLiveData<List<ApplicationInfo>>(emptyList())
    private val mFilteredApps = MutableLiveData<List<AppInfo>>(emptyList())
    val apps: LiveData<List<AppInfo>> get() = mFilteredApps

    private val mLoading = MutableLiveData(true)
    val loading get() = mLoading

    private val cache = mutableMapOf<String, AppInfo>()

    private val packages = mutableSetOf<String>()

    private val prefs = PreferenceManager.getDefaultSharedPreferences(application)

    var textFilter: String = ""
        set(value) {
            field = value
            applyFilter(mAllApps.value!!)
        }

    var showSystemApps: Boolean = true
        set(value) {
            field = value
            applyFilter(mAllApps.value!!)
        }

    var onlyLaunchableApps: Boolean = true
        set(value) {
            field = value
            applyFilter(mAllApps.value!!)
        }

    init {
        readPackages()
        Log.e("CreatingModel", "aaa")

        mAllApps.observeForever {
            applyFilter(it)
        }

        thread {
            val pm = application.packageManager
            val allApps: List<ApplicationInfo> =
                pm.getInstalledApplications(PackageManager.GET_META_DATA)
                    .sortedBy { it.packageName }

            mAllApps.postValue(allApps)
        }
    }

    fun isPackageSelectedForForwarding(packageName: String): Boolean =
        packages.contains(packageName)

    fun savePackage(packageName: String, add: Boolean) {
        if (add) {
            packages.add(packageName)
        } else {
            packages.remove(packageName)
        }
    }

    private fun readPackages() {
        packages.addAll(prefs.getStringSet(NOTIFICATION_FORWARDING_FROM_PACKAGES_KEY, emptySet())!!)
    }

    fun writePackages() {
        prefs.edit {
            putStringSet(NOTIFICATION_FORWARDING_FROM_PACKAGES_KEY, packages)
        }
    }

    fun cleanUp(lifecycleOwner: LifecycleOwner) {
        packages.clear()

        mFilteredApps.removeObservers(lifecycleOwner)
        mFilteredApps.postValue(emptyList())
    }

    private fun loadAdditionalAppInfo(app: ApplicationInfo): AppInfo {
        val cached = cache[app.packageName]
        if (cached != null) return cached

        val context = getApplication<Application>()
        val pm = context.packageManager
        val info = AppInfo(app.loadLabel(pm), app.packageName, loadIcon(app))

        cache[app.packageName] = info

        return info
    }

    private fun loadIcon(app: ApplicationInfo): Drawable {
        val context = getApplication<Application>()
        val pm = context.packageManager
        var icon = app.loadIcon(pm)

        if (icon is BitmapDrawable && icon.intrinsicHeight > 1024) {
            icon = BitmapDrawable(
                context.resources,
                Bitmap.createScaledBitmap(icon.bitmap, 1024, 1024, true)
            )
        }
        return icon
    }

    private fun infoCorrespondsToFilter(fullInfo: ApplicationInfo): Boolean {
        if (!showSystemApps
            && (fullInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0
                    || fullInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP != 0)) return false

        val context = getApplication<Application>()
        val pm = context.packageManager

        if (onlyLaunchableApps && pm.getLaunchIntentForPackage(fullInfo.packageName) == null) return false

        return true
    }

    private fun labelCorrespondsToFilter(app: AppInfo): Boolean {
        // Remove this app from list in release
        if (!BuildConfig.DEBUG && app.packageName == getApplication<Application>().packageName) return false

        if (textFilter.isEmpty()) return true

        return app.packageName.contains(textFilter, true)
                || app.title.contains(textFilter, true)
    }

    private fun sortApps(apps: List<AppInfo>): List<AppInfo> {
        val locale =
            ConfigurationCompat.getLocales(getApplication<Application>().resources.configuration)[0]

        val collator = Collator.getInstance(locale)
        collator.strength = Collator.PRIMARY

        return apps.sortedWith(Comparator { o1, o2 ->
            return@Comparator collator.compare(o1.title.toString(), o2.title.toString())
        })
    }

    private fun applyFilter(apps: List<ApplicationInfo>) {
        loading.value = true
        thread {
            val filteredApps = apps
                .filter(::infoCorrespondsToFilter)
                .map { loadAdditionalAppInfo(it) }
                .filter(::labelCorrespondsToFilter)

            val sortedApps = sortApps(filteredApps)

            loading.postValue(false)
            mFilteredApps.postValue(sortedApps)
        }
    }

}