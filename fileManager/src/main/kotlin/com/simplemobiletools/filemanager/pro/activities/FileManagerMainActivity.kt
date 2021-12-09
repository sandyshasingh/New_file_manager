package com.simplemobiletools.filemanager.pro.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.app.SearchManager
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.drawable.Drawable
import android.media.RingtoneManager
import android.os.Bundle
import android.os.PersistableBundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuItemCompat
import androidx.core.view.MenuItemCompat.OnActionExpandListener
import androidx.lifecycle.ViewModelProvider
import com.simplemobiletools.commons.AppProgressDialog
import com.simplemobiletools.commons.ThemeUtils
import com.simplemobiletools.commons.activities.BaseSimpleActivity
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.helpers.*
import com.simplemobiletools.filemanager.pro.PermissionActivity
import com.simplemobiletools.filemanager.pro.R
import com.simplemobiletools.filemanager.pro.dialogs.ChangeSortingDialog
import com.simplemobiletools.filemanager.pro.dialogs.CreateNewItemDialog
import com.simplemobiletools.filemanager.pro.extensions.config
import com.simplemobiletools.filemanager.pro.extensions.tryOpenPathIntent
import com.simplemobiletools.filemanager.pro.fragments.ItemsFragment
import com.simplemobiletools.filemanager.pro.helpers.DataViewModel
import com.simplemobiletools.filemanager.pro.helpers.MAX_COLUMN_COUNT
import com.simplemobiletools.filemanager.pro.helpers.MIN_COLUMN_COUNT
import com.simplemobiletools.filemanager.pro.helpers.RootHelpers
import com.stericson.RootTools.RootTools
import kotlinx.android.synthetic.main.file_manager_activity.*
import kotlinx.android.synthetic.main.items_fragment.view.*
import java.io.File
import java.util.*

const val REQUEST_CODE_FOR_STORAGE_PERMISSION =  101
class FileManagerMainActivity : BaseSimpleActivity() {
    private val PICKED_PATH = "picked_path"
    private var isSearchOpen = false
    private var searchMenuItem: MenuItem? = null
    var sharedPrefrences : SharedPreferences? = null
    private lateinit var fragment: ItemsFragment
    private var isDarkTheme = false
    var viewModel : DataViewModel? = null
    private var mProgressDialog: AppProgressDialog? = null

   /* override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase!!))
    }*/


   /* override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }*/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.file_manager_activity)
        viewModel = ViewModelProvider(this).get(DataViewModel::class.java)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
//        if(supportActionBar!=null)
//            supportActionBar?.title = "File Manager"

        sharedPrefrences = getSharedPrefs()
        fragment = (fragment_holder as ItemsFragment).apply {
            isGetRingtonePicker = intent.action == RingtoneManager.ACTION_RINGTONE_PICKER
            isGetContentIntent = intent.action == Intent.ACTION_GET_CONTENT
            isPickMultipleIntent = intent.getBooleanExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)
        }
        if(intent!=null){
            intent.extras
        }

        if (savedInstanceState == null) {
            tryInitFileManager()
            checkIfRootAvailable()
//            checkInvalidFavorites()
        }
        isDarkTheme = isDarkTheme()
        viewModel?.fetchVideos(this)
        viewModel?.fetchAudios(this)
        viewModel?.fetchImages(this)
        fragment.setZRPImage(ZRP_image)
       // mBannerAdmobUnitId = applicationContext?.resources?.getString(R.string.file_manager_banner_unit_id)

//        loadAds()
    }

    override fun onDestroy() {
        super.onDestroy()
        config.temporarilyShowHidden = false
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        setupSearch(menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu!!.apply {
            findItem(R.id.settings_show_hidden).isChecked = config.showHidden
            findItem(R.id.go_home).isVisible = fragment.currentPath != config.homeFolder
            findItem(R.id.increase_column_count).isVisible = config.viewType == VIEW_TYPE_GRID && config.fileColumnCnt < MAX_COLUMN_COUNT
            findItem(R.id.reduce_column_count).isVisible = config.viewType == VIEW_TYPE_GRID && config.fileColumnCnt > MIN_COLUMN_COUNT
            findItem(R.id.sort).isVisible = isSortByVisible()

            findItem(R.id.change_view_type).icon = drawable()
        }
        return true
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun drawable(): Drawable? {
        return if (config.viewType == VIEW_TYPE_GRID) {
            if (isDarkTheme)
                resources.getDrawable(R.drawable.ic_list)
            else
                resources.getDrawable(R.drawable.ic_list_black)
        } else {
            if (isDarkTheme)
                resources.getDrawable(R.drawable.ic_grid)
            else
                resources.getDrawable(R.drawable.ic_grid_black)
        }
    }

    private fun isSortByVisible(): Boolean {
        val path = fragment.currentPath
        return !(path == "$internalStoragePath/$AUDIO_NAME" || path == "$internalStoragePath/$VIDEOS_NAME" || path == "$internalStoragePath/$WHATSAPP_NAME"
                || path == "$internalStoragePath/$FILTER_DUPLICATE_NAME" || path == "$internalStoragePath/$PHOTOS_NAME")
    }


    override fun onPause() {
        super.onPause()
        if(sharedPrefrences!=null) {
            sharedPrefrences?.edit()?.putLong(PHOTOS_NAME, PHOTOS_CLICK)?.apply()
            sharedPrefrences?.edit()?.putLong(WHATSAPP_NAME, WHATSAPP_CLICK)?.apply()
            sharedPrefrences?.edit()?.putLong(VIDEOS_NAME, VIDEOS_CLICK)?.apply()
            sharedPrefrences?.edit()?.putLong(AUDIO_NAME, AUDIO_CLICK)?.apply()
            sharedPrefrences?.edit()?.putLong(FILTER_DUPLICATE_NAME, FILTER_DUPLICATE_CLICK)?.apply()
        }
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.go_home -> goHome()
            R.id.sort -> showSortingDialog()
            R.id.change_view_type -> changeViewType(item)
            R.id.increase_column_count -> fragment.increaseColumnCount()
            R.id.reduce_column_count -> fragment.reduceColumnCount()
            R.id.refresh -> fragment.refreshItems(false)

//            R.id.settings -> startActivity(Intent(applicationContext, SettingsActivity::class.java))
            R.id.create_new_folder -> createNewItem()
            R.id.settings_show_hidden -> setupShowHidden(item)
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }
    private fun setupShowHidden(item: MenuItem) {
        val checkOrNot = config.showHidden
        item.isChecked = !checkOrNot
        config.showHidden = !checkOrNot
        fragment.refreshItems(false)
    }

    private fun createNewItem() {
        CreateNewItemDialog(this, "Create", "Cancel", fragment.currentPath) {
            if (it) {
                fragment.refreshItems(false)
            } else {
                toast(R.string.unknown_error_occurred)
            }
        }
    }
    @SuppressLint("UseCompatLoadingForDrawables")
    private fun changeViewType(item: MenuItem) {
        if (config.viewType == VIEW_TYPE_GRID) {
            config.viewType = VIEW_TYPE_LIST
            item.icon =  if (isDarkTheme)
                resources.getDrawable(R.drawable.ic_grid)
            else
                resources.getDrawable(R.drawable.ic_grid_black)
        } else {
            config.viewType = VIEW_TYPE_GRID
            item.icon =  if (isDarkTheme)
                resources.getDrawable(R.drawable.ic_list)
            else
                resources.getDrawable(R.drawable.ic_list_black)
        }
        fragment.setupLayoutManager()
        fragment.refreshItems(false)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(PICKED_PATH, (fragment_holder as ItemsFragment).currentPath)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        val path = savedInstanceState.getString(PICKED_PATH) ?: internalStoragePath
        openPath(path, true)
    }

    private fun setupSearch(menu: Menu) {
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        searchMenuItem = menu.findItem(R.id.search)
        (searchMenuItem!!.actionView as SearchView).apply {
            setSearchableInfo(searchManager.getSearchableInfo(componentName))
            isSubmitButtonEnabled = false
            queryHint = getString(R.string.search)
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String) = false

                override fun onQueryTextChange(newText: String): Boolean {
                    if (isSearchOpen) {
                        fragment.searchQueryChanged(newText)
                    }
                    return true
                }
            })
        }

        MenuItemCompat.setOnActionExpandListener(searchMenuItem, object : OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
                isSearchOpen = true
                fragment.searchOpened()
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                isSearchOpen = false
                fragment.searchClosed()
                return true
            }
        })
    }


    private fun tryInitFileManager() {
        handlePermission(PERMISSION_WRITE_STORAGE) {
            checkOTGPath()
            if (it) {
                initFileManager()
            } else {
                val intent = Intent(this, PermissionActivity::class.java).apply {  }
                startActivityForResult(intent,REQUEST_CODE_FOR_STORAGE_PERMISSION)
               // toast(R.string.no_storage_permissions)
               // finish()
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)
        if(requestCode == REQUEST_CODE_FOR_STORAGE_PERMISSION)
        {
            isAskingPermissions = false
            if (hasPermission(PERMISSION_WRITE_STORAGE))
            {
                initFileManager()
            }
            else{
                toast(R.string.no_storage_permissions)
                finish()
            }
        }

    }

    private fun initFileManager() {
        if (intent.action == Intent.ACTION_VIEW && intent.data != null) {
            val data = intent.data
            if (data?.scheme == "file") {
                openPath(data.path!!)
            } else {
                val path = getRealPathFromURI(data!!)
                if (path != null) {
                    openPath(path)
                } else {
                    openPath(config.homeFolder)
                }
            }

            if (!File(data.path!!).isDirectory) {
                tryOpenPathIntent(data.path!!, false)
            }
        } else {
            openPath(config.homeFolder)
        }
    }

    private fun checkOTGPath() {
        ensureBackgroundThread {
            if (!config.wasOTGHandled && hasPermission(PERMISSION_WRITE_STORAGE) && hasOTGConnected() && config.OTGPath.isEmpty()) {
                getStorageDirectories().firstOrNull { it.trimEnd('/') != internalStoragePath && it.trimEnd('/') != sdCardPath }?.apply {
                    config.wasOTGHandled = true
                    config.OTGPath = trimEnd('/')
                }
            }
        }
    }

    private fun openPath(path: String, forceRefresh: Boolean = false) {
        var newPath = path
        val file = File(path)
        if (config.OTGPath.isNotEmpty() && config.OTGPath == path.trimEnd('/')) {
            newPath = path
        } else if (file.exists() && !file.isDirectory) {

            newPath = file.parent
        } else if (!file.exists() && !isPathOnOTG(newPath)) {
            newPath = internalStoragePath
        }

        (fragment_holder as ItemsFragment).openPath(newPath, forceRefresh)
    }

    private fun goHome() {
        if (config.homeFolder != fragment.currentPath) {
            fragment.pathList.clear()
            fragment.pathList.add(internalStoragePath)
            fragment.mView.my_recyclerView?.beGone()
            openPath(config.homeFolder)
        }
    }

    private fun showSortingDialog() {
//        showDialog()
        ChangeSortingDialog(this, fragment.currentPath) {
//            dismissDialog()
            fragment.refreshItems(false)
        }
    }
    private fun showDialog() {
        try {
            if (ThemeUtils.getActivityIsAlive(this)) {
                mProgressDialog = AppProgressDialog(this)
                mProgressDialog?.setCancelable(true)
                mProgressDialog?.setCanceledOnTouchOutside(true)
                mProgressDialog?.show()
            }
        } catch (e: Exception) {
        }
    }

    private fun dismissDialog() {
        if (ThemeUtils.getActivityIsAlive(this) && mProgressDialog != null && mProgressDialog!!.isShowing) {
            mProgressDialog!!.dismiss()
        }
    }
    override fun onBackPressed() {
        if (fragment.pathList.size <= 1) {
            finish()
        }else {
            val i =fragment.pathList.size
            fragment.pathList.removeAt(i - 1)
             val path = fragment.pathList[fragment.pathList.size - 1]
            openPath(path, false)
        }
    }

    private fun checkIfRootAvailable() {
        ensureBackgroundThread {
            config.isRootAvailable = RootTools.isRootAvailable()
            if (config.isRootAvailable && config.enableRootAccess) {
                RootHelpers(this).askRootIfNeeded {
                    config.enableRootAccess = it
                }
            }
        }
    }

//    private fun checkInvalidFavorites() {
//        ensureBackgroundThread {
//            config.favorites.forEach {
//                if (!isPathOnOTG(it) && !isPathOnSD(it) && !File(it).exists()) {
//                    config.removeFavorite(it)
//                }
//            }
//        }
//    }

    fun pickedPath(path: String) {
        val resultIntent = Intent()
        val uri = getFilePublicUri(File(path), "com.rocks.music.videoplayer.provider")
        val type = path.getMimeType()
        resultIntent.setDataAndType(uri, type)
        resultIntent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }

    fun pickedRingtone(path: String) {
        val uri = getFilePublicUri(File(path), "com.rocks.music.videoplayer.provider")
        val type = path.getMimeType()
        Intent().apply {
            setDataAndType(uri, type)
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            putExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI, uri)
            setResult(Activity.RESULT_OK, this)
        }
        finish()
    }

    fun pickedPaths(paths: ArrayList<String>) {
        val newPaths = paths.map { getFilePublicUri(File(it), "com.rocks.music.videoplayer.provider") } as ArrayList
        val clipData = ClipData("Attachment", arrayOf(paths.getMimeType()), ClipData.Item(newPaths.removeAt(0)))
        newPaths.forEach {
            clipData.addItem(ClipData.Item(it))
        }

        Intent().apply {
            this.clipData = clipData
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            setResult(Activity.RESULT_OK, this)
        }
        finish()
    }

    fun openedDirectory() {
        if (searchMenuItem != null) {
            MenuItemCompat.collapseActionView(searchMenuItem)
        }
    }
}
