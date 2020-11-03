package ru.art2000.pager.viewmodels

import android.app.Application
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.core.os.ConfigurationCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.common.collect.HashMultimap
import ru.art2000.pager.BuildConfig
import ru.art2000.pager.extensions.set
import ru.art2000.pager.models.AppInfo
import java.io.FileNotFoundException
import java.text.Collator
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread

class ForwardingViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        const val DATA_FILE_NAME = "forwarding_settings"
    }

    private val mAllApps = MutableLiveData<List<ApplicationInfo>>(emptyList())
    private val mFilteredApps = MutableLiveData<List<AppInfo>>(emptyList())
    val apps: LiveData<List<AppInfo>> get() = mFilteredApps

    private val mLoading = MutableLiveData(true)
    val loading get() = mLoading

    private val appsRead = AtomicBoolean(false)

    private val cache = mutableMapOf<String, AppInfo>()

    private val chatPackageMapping = HashMultimap.create<Int, String>()


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

    fun isPackageSelectedForForwarding(addresseeId: Int, packageName: String): Boolean =
        chatPackageMapping.containsEntry(addresseeId, packageName)

    fun savePackage(addresseeId: Int, packageName: String, add: Boolean) {
        if (add) {
            chatPackageMapping[addresseeId] = packageName
        } else {
            chatPackageMapping.remove(addresseeId, packageName)
        }
    }

    private fun readPackages() {
        val inputStream = try {
            getApplication<Application>().openFileInput(DATA_FILE_NAME)
        } catch (e: FileNotFoundException) {
            null
        } ?: return

        chatPackageMapping.clear()

        inputStream.reader().use { reader ->
            reader.forEachLine {
                val split = it.split("=")
                if (split.size != 2) return@forEachLine

                val addresseeId = split.first().toIntOrNull() ?: return@forEachLine

                chatPackageMapping[addresseeId] = split[1]
            }
        }
    }

    private fun writePackages() {
        getApplication<Application>()
            .openFileOutput(DATA_FILE_NAME, Context.MODE_PRIVATE)
            .writer()
            .use { writer ->
                chatPackageMapping.entries().forEach {
                    writer.write("${it.key}=${it.value}\n")
                }
            }
    }

    fun loadApps() {
        if (appsRead.getAndSet(true)) return
        readSettings()

        mAllApps.observeForever {
            applyFilter(it)
        }

        thread {
            val pm = getApplication<Application>().packageManager
            val allApps: List<ApplicationInfo> =
                pm.getInstalledApplications(PackageManager.GET_META_DATA)

            mAllApps.postValue(allApps)
        }
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
                    || fullInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP != 0)
        ) return false

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

    fun readSettings() {
        readPackages()
    }

    fun onDestroy(isSelectMode: Boolean) {
        if (!isSelectMode) return

        writePackages()
    }
}