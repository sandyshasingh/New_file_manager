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
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuItemCompat
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import com.simplemobiletools.commons.AppProgressDialog
import com.simplemobiletools.commons.BottomNavigationVisible
import com.simplemobiletools.commons.ThemeUtils
import com.simplemobiletools.commons.activities.BaseSimpleActivity
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.helpers.*
import com.simplemobiletools.commons.models.FolderItem
import com.simplemobiletools.filemanager.pro.*
import com.simplemobiletools.filemanager.pro.dialogs.ChangeSortingDialog
import com.simplemobiletools.filemanager.pro.dialogs.CreateNewItemDialog
import com.simplemobiletools.filemanager.pro.extensions.config
import com.simplemobiletools.filemanager.pro.extensions.tryOpenPathIntent
import com.simplemobiletools.filemanager.pro.fragments.ItemsFragment
import com.simplemobiletools.filemanager.pro.helpers.DataViewModel
import com.simplemobiletools.filemanager.pro.helpers.MAX_COLUMN_COUNT
import com.simplemobiletools.filemanager.pro.helpers.MIN_COLUMN_COUNT
import com.simplemobiletools.filemanager.pro.helpers.RootHelpers
import com.simplemobiletools.filemanager.pro.models.ListItem
import com.stericson.RootTools.RootTools
import kotlinx.android.synthetic.main.file_manager_activity.*
import kotlinx.android.synthetic.main.items_fragment.view.*
import kotlinx.android.synthetic.main.layout.*
import java.io.File
import kotlin.collections.ArrayList
import kotlin.collections.HashSet

const val REQUEST_CODE_FOR_STORAGE_PERMISSION =  101
const val FRAGMENT_STACK = "fragment_stack"
class FileManagerMainActivity : BaseSimpleActivity(),MoreItemsList, BottomNavigationVisible {
    private val PICKED_PATH = "picked_path"
    var pathList = ArrayList<String>()
    var itemsListFragtment:ItemsListFragment?=null
  //  var searchFragment= SearchFragment()
    private var isSearchOpen = true
    private var searchMenuItem: MenuItem? = null
    var sharedPrefrences : SharedPreferences? = null
    private lateinit var fragment: ItemsFragment
    private var isDarkTheme = false
    var viewModel : DataViewModel? = null

    private val sharedPrefFile = "com.example.new_file_manager"
    private var folderItems = ArrayList<FolderItem>()
     var mProgressDialog: AppProgressDialog? = null


    /* override fun attachBaseContext(newBase: Context) {
         super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase!!))
     }*/


   /* override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }*/

    override fun onCreate(savedInstanceState: Bundle?) {
       ThemeUtils.onActivityCreateSetTheme(this)
       super.onCreate(savedInstanceState)
        setContentView(R.layout.file_manager_activity)
        viewModel = ViewModelProvider(this).get(DataViewModel::class.java)
//        setSupportActionBar(toolbar)

      // var userWallet = intent?.getIntExtra("USER_WALLET_PRICE")

        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
//       toolbar?.setNavigationOnClickListener {
//           onBackPressed()
//       }



//        if(supportActionBar!=null)
//            supportActionBar?.title = "File Manager"

        bottomSheetClickListener()

        sharedPrefrences = getSharedPrefs()
       val fragmentManager: FragmentManager = supportFragmentManager
        fragment = ItemsFragment()

       fragmentManager.beginTransaction().replace(R.id.fragment_holder,fragment).commit()
       fragment.apply {
            isGetRingtonePicker = intent.action == RingtoneManager.ACTION_RINGTONE_PICKER
            isGetContentIntent = intent.action == Intent.ACTION_GET_CONTENT
            isPickMultipleIntent = intent.getBooleanExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)
        }
        if(intent!=null){
            intent.extras
        }

        setting.setOnClickListener {
            val intent = Intent(this, SettingsBurger::class.java).apply {

            }
            startActivity(intent)

        }

        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        search_df.setOnSearchClickListener {
//            itemsListFragtment = ItemsListFragment()
            itemsListFragtment?.searchClicked=true
            val fragment = supportFragmentManager.findFragmentById(R.id.fragment_holder)
            if (fragment !is ItemsListFragment){

                onCategoryClick(INTERNAL_STORAGE,"abc")
            }

//            val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
//            fragmentTransaction.add(R.id.fragment_holder,itemsListFragtment!!).addToBackStack("")
//            fragmentTransaction.commit()

            isSearchOpen = true
            //fragment.searchOpened()
            itemsListFragtment?.searchOpened()
             true
        }
        search_df.setOnCloseListener (SearchView.OnCloseListener { //your code here
            isSearchOpen = false
            // fragment.searchClosed()
            itemsListFragtment?.searchClosed()
            val fragment = supportFragmentManager.findFragmentById(R.id.fragment_holder)
            if (fragment is ItemsListFragment){
                onBackPressed()
                //onBackPressed()

            }


             false

            //super.onBackPressed()


        })






        search_df?.apply {
            setSearchableInfo(searchManager.getSearchableInfo(componentName))
            isSubmitButtonEnabled = false
            queryHint = getString(R.string.search)
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String) = false

                override fun onQueryTextChange(newText: String): Boolean {
                    if (isSearchOpen) {
                        //fragment.searchQueryChanged(newText)
                        //showDialog()
                       // searchFragment.searchQueryChanged(newText)
                           if(TextUtils.isEmpty(newText))
                           {

                           }
                        else
                           {
                               itemsListFragtment?.searchQueryChanged(newText)
                           }
                    }

                    return true
                }
            })


        }

        val searchView = findViewById<View>(R.id.search_df) as SearchView?
        val searchEditText =
            searchView?.findViewById<View>(R.id.search_src_text) as EditText?
        searchEditText?.setTextColor(resources.getColor(R.color.btm_background))
        searchEditText?.setHintTextColor(resources.getColor(R.color.hint_black))

        if (savedInstanceState == null) {
            tryInitFileManager()
            checkIfRootAvailable()
//            checkInvalidFavorites()
        }
        isDarkTheme = isDarkTheme()
        viewModel?.fetchVideos(this)
        viewModel?.fetchAudios(this)
        viewModel?.fetchImages(this)
        viewModel?.fetchApps(this)
        viewModel?.fetchDocuments(this)
        viewModel?.fetchZip(this)
       viewModel?.fetchRecent(this)
//        showDialog()


//        fragment.setZRPImage(ZRP_image)
       // mBannerAdmobUnitId = applicationContext?.resources?.getString(R.string.file_manager_banner_unit_id)

//        loadAds()
    }

    override fun onDestroy() {
        super.onDestroy()
        config.temporarilyShowHidden = false
    }

    /*override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        setupSearch(menu)
        return super.onCreateOptionsMenu(menu)
    }*/

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu!!.apply {
            findItem(R.id.settings_show_hidden).isChecked = config.showHidden
            findItem(R.id.go_home).isVisible = fragment.currentPath != config.homeFolder
            findItem(R.id.increase_column_count).isVisible = config.viewType == VIEW_TYPE_GRID && config.fileColumnCnt < MAX_COLUMN_COUNT
            findItem(R.id.reduce_column_count).isVisible = config.viewType == VIEW_TYPE_GRID && config.fileColumnCnt > MIN_COLUMN_COUNT
            findItem(R.id.sort).isVisible = isSortByVisible()

            //findItem(R.id.change_view_type).icon = drawable()
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
        return !(path == "$internalStoragePath/$AUDIO_NAME" || path == "$internalStoragePath/$VIDEOS_NAME"
                 || path == "$internalStoragePath/$PHOTOS_NAME")
    }


    override fun onPause() {
        super.onPause()
        if(sharedPrefrences!=null) {
            sharedPrefrences?.edit()?.putLong(PHOTOS_NAME, PHOTOS_CLICK)?.apply()
           // sharedPrefrences?.edit()?.putLong(WHATSAPP_NAME, WHATSAPP_CLICK)?.apply()
            sharedPrefrences?.edit()?.putLong(VIDEOS_NAME, VIDEOS_CLICK)?.apply()
            sharedPrefrences?.edit()?.putLong(AUDIO_NAME, AUDIO_CLICK)?.apply()
           // sharedPrefrences?.edit()?.putLong(FILTER_DUPLICATE_NAME, FILTER_DUPLICATE_CLICK)?.apply()
        }
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.home -> onBackPressed()
           // R.id.go_home -> goHome()
           // R.id.sort -> showSortingDialog()
            //R.id.change_view_type -> changeViewType(item)
           // R.id.increase_column_count -> fragment.increaseColumnCount()
//            R.id.reduce_column_count -> fragment.reduceColumnCount()
//            R.id.refresh -> fragment.refreshItems(false)

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
      //  fragment.setupLayoutManager()
      //  fragment.refreshItems(false)
    }

//    override fun onSaveInstanceState(outState: Bundle) {
//        super.onSaveInstanceState(outState)
//        outState.putString(PICKED_PATH, fragment.currentPath)
//    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        val path = savedInstanceState.getString(PICKED_PATH) ?: internalStoragePath
        //openPath(path, true)
    }

  /*  private fun setupSearch(menu: Menu) {
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
                        itemsListFragtment?.searchQueryChanged(newText)
                       // searchFragment.searchQueryChanged(newText)
                        //fragment.searchQueryChanged(newText)
                    }
                    return true
                }
            })
        }

        MenuItemCompat.setOnActionExpandListener(searchMenuItem, object : OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
                isSearchOpen = true
                //fragment.searchOpened()
                itemsListFragtment?.searchOpened()
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                isSearchOpen = false
               // fragment.searchClosed()
                itemsListFragtment?.searchClosed()
                return true
            }
        })
    }*/


    private fun tryInitFileManager() {
        handlePermission(PERMISSION_WRITE_STORAGE) {
            checkOTGPath()
            if (it) {
                initFileManager()
            } else {
                val intent = Intent(this, PermissionActivity::class.java).apply {  }
                startActivityForResult(intent,REQUEST_CODE_FOR_STORAGE_PERMISSION)
              //  startActivityForResult(Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,Uri.parse("package:$packageName")), REQUEST_CODE_TO_MANAGE_EXTERNAL_STORAGE)
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

            if (checkPermission(PERMISSION_WRITE_STORAGE))
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
               // openPath(data.path!!)
            } else {
                val path = getRealPathFromURI(data!!)
                if (path != null) {
                   // openPath(path)
                } else {
                 //   openPath(config.homeFolder)
                }
            }

            if (!File(data.path!!).isDirectory) {
                tryOpenPathIntent(data.path!!, false)
            }
        } else {
            //openPath(config.homeFolder)
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

    fun bottomSheetClickListener(){

        bottom_send.setOnClickListener {
            itemsListFragtment?.send()
        }

        bottom_move.setOnClickListener {
            itemsListFragtment?.move()
        }
        bottom_rename.setOnClickListener {
            itemsListFragtment?.rename()
        }
        bottom_copyto.setOnClickListener {
            itemsListFragtment?.copy_to()
        }
        bottom_copy_path.setOnClickListener {
            itemsListFragtment?.copy_path()
        }
        bottom_hide.setOnClickListener {
            itemsListFragtment?.hide()
        }
        bottom_unhide.setOnClickListener {
            itemsListFragtment?.unhide()
        }
        bottom_compress.setOnClickListener {
            itemsListFragtment?.compress()
        }
        bottom_decompress.setOnClickListener {
            itemsListFragtment?.decompress()
        }
        bottom_openwith.setOnClickListener {
            itemsListFragtment?.openWith()
        }
        bottom_delete.setOnClickListener {
            itemsListFragtment?.delete()
        }
        bottom_details.setOnClickListener {
            itemsListFragtment?.details()
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

        itemsListFragtment?.openPath(newPath, forceRefresh)


    }

    private fun goHome() {
        if (config.homeFolder != fragment.currentPath) {
            pathList.clear()
            pathList.add(internalStoragePath)
            fragment.mView.my_recyclerView?.beGone()
          //  openPath(config.homeFolder)
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
        if (pathList.size <= 1) {
            super.onBackPressed()
        }
        else {
            val i =pathList.size
            pathList.removeAt(i - 1)
             val path = pathList[pathList.size - 1]
            openPath(path, false)
        }
//        if(search_df.isIconified)
//            search_df.isIconified = false
        //super.onBackPressed()
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
        val uri = getFilePublicUri(File(path), "com.example.new_file_manager")
        val type = path.getMimeType()
        resultIntent.setDataAndType(uri, type)
        resultIntent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }

    fun pickedRingtone(path: String) {
        val uri = getFilePublicUri(File(path), "com.example.new_file_manager")
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
        val newPaths = paths.map { getFilePublicUri(File(it), "com.example.new_file_manager") } as ArrayList
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

    fun onCategoryClick(id: Int,path: String) {
        val fragmentManager: FragmentManager = supportFragmentManager

        itemsListFragtment = ItemsListFragment.newInstance(id,path)
        itemsListFragtment?.listener = this
        val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.add(R.id.fragment_holder, itemsListFragtment!!).addToBackStack("")
        fragmentTransaction.commit()
    }


    fun onAddShortcutClicked(item:ArrayList<String>){
        val sharedPreferences: SharedPreferences? = getSharedPreferences(sharedPrefFile,
            Context.MODE_PRIVATE)
        var set:Set<String>?=sharedPreferences?.getStringSet("SHORTCUT_FOLDERS",null)
        if(set == null)
            set = HashSet()
        for (value in item){
            set=set?.plus(value)
        }

       sharedPreferences?.edit().apply(){
            this?.putStringSet("SHORTCUT_FOLDERS",set)
           this?.apply()
        }
        if (set != null) {
            fragment.add_the_shortcutfolder(item)
        }
    }

// fun getDrawable(id: Int): Drawable {
//        return  this.resources.getDrawable(id)
//    }



    override fun moreItemsList(item: List<ListItem>) {
        val fragmentManager: FragmentManager = supportFragmentManager
        val myFragment = MoreItemFragment()
        myFragment.arrayList = item
        val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.add(R.id.fragment_holder,myFragment).addToBackStack("")
        fragmentTransaction.commit()
    }

    override fun btmVisible(yes: Boolean) {
        if(yes)
            bottomnavigation?.visibility = View.VISIBLE
        else
            bottomnavigation?.visibility = View.GONE
    }

}
