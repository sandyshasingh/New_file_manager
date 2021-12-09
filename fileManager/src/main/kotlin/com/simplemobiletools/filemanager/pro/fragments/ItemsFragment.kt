package com.simplemobiletools.filemanager.pro.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.simplemobiletools.commons.AppProgressDialog
import com.simplemobiletools.commons.MemorySizeUtils
import com.simplemobiletools.commons.ThemeUtils
import com.simplemobiletools.commons.activities.BaseSimpleActivity
import com.simplemobiletools.commons.dialogs.StoragePickerDialog
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.helpers.*
import com.simplemobiletools.commons.interfaces.ItemOperationsListener
import com.simplemobiletools.commons.models.FileDirItem
import com.simplemobiletools.commons.models.FolderItem
import com.simplemobiletools.commons.adapters.AdapterForPath
import com.simplemobiletools.commons.views.MyGridLayoutManager
import com.simplemobiletools.commons.views.MyRecyclerView
import com.simplemobiletools.filemanager.pro.R
import com.simplemobiletools.filemanager.pro.activities.FileManagerMainActivity
import com.simplemobiletools.filemanager.pro.adapters.ItemsAdapter
import com.simplemobiletools.filemanager.pro.extensions.*
import com.simplemobiletools.filemanager.pro.extensions.getPositionOfImage
import com.simplemobiletools.filemanager.pro.helpers.DataViewModel
import com.simplemobiletools.filemanager.pro.helpers.MAX_COLUMN_COUNT
import com.simplemobiletools.filemanager.pro.helpers.RootHelpers
import com.simplemobiletools.filemanager.pro.models.ListItem
import kotlinx.android.synthetic.main.items_fragment.*
import kotlinx.android.synthetic.main.items_fragment.view.*
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

class ItemsFragment : Fragment(), ItemOperationsListener, AdapterForPath.BreadcrumbsListenerNew {

    private var mProgressDialog: AppProgressDialog? = null

    private var firstTime = true
    var currentPath = ""
    var currentFolderHeader = ""
    var isGetContentIntent = false
    var isGetRingtonePicker = false
    var isPickMultipleIntent = false
    private var isFirstResume = true
    private var showHidden = false
    private var skipItemUpdating = false
    private var isSearchOpen = false
    private var lastSearchedText = ""
    private var currentViewType = VIEW_TYPE_LIST
    private var scrollStates = HashMap<String, Parcelable>()
    private var zoomListener: MyRecyclerView.MyZoomListener? = null
    private var storedItems = ArrayList<ListItem>()
    private var folderItems = ArrayList<FolderItem>()
    var pathList = ArrayList<String>()
    var mainAdapter : ItemsAdapter? = null
    private var storedTextColor = 0
    private var storedFontSize = 0
    lateinit var mView: View
    private var baseSimpleActivity : BaseSimpleActivity? = null
    var list : ArrayList<ListItem> = ArrayList()
    var adapterForPath : AdapterForPath? = null
    var sharedPrefrences : SharedPreferences? = null
    private var internalStoragePath : String? = ""
    var isHeaderShow = false
    var model : DataViewModel? = null
    var zrpImage : ImageView? = null

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        mView = inflater.inflate(R.layout.items_fragment, container, false)!!
        sharedPrefrences = activity?.getSharedPrefs()
        baseSimpleActivity = activity as BaseSimpleActivity
        internalStoragePath = context?.config?.internalStoragePath

        model = ViewModelProvider(baseSimpleActivity!!).get(DataViewModel::class.java)
        model?.photoSize?.observe(baseSimpleActivity!!, androidx.lifecycle.Observer {
            updatePhotosSize(it)
        })

        model?.videoSize?.observe(baseSimpleActivity!!, androidx.lifecycle.Observer {
            updateVideoSize(it)
            mainAdapter?.updateFolderItems(folderItems)
        })

        model?.audioSize?.observe(baseSimpleActivity!!, androidx.lifecycle.Observer {
            updateAudioSize(it)
            mainAdapter?.updateFolderItems(folderItems)
        })

        createFolderList()
        vibhor?.beGone()
        return mView
    }

    private fun updatePhotosSize(size : Long){
        folderItems.forEach{
            if(it.id == PHOTOS_ID){
                it.size = size/1024
                it.sizeString = MemorySizeUtils.formatSize(size)
            }
        }
    }

    private fun updateVideoSize(size: Long) {
        folderItems.forEach{
            if(it.id == VIDEOS_ID){
                it.size = size/1024
                it.sizeString = MemorySizeUtils.formatSize(size)
            }
        }
    }

    private fun updateAudioSize(size: Long) {
        folderItems.forEach{
            if(it.id == AUDIO_ID){
                it.size = size/1024
                it.sizeString = MemorySizeUtils.formatSize(size)
            }
        }
    }

    private fun createFolderList() {
        if (sharedPrefrences != null) {
            PHOTOS_CLICK = sharedPrefrences?.getLong(PHOTOS_NAME, PHOTOS_CLICK)!!
            WHATSAPP_CLICK = sharedPrefrences?.getLong(WHATSAPP_NAME, WHATSAPP_CLICK)!!
            VIDEOS_CLICK = sharedPrefrences?.getLong(VIDEOS_NAME, VIDEOS_CLICK)!!
            AUDIO_CLICK = sharedPrefrences?.getLong(AUDIO_NAME, AUDIO_CLICK)!!
            FILTER_DUPLICATE_CLICK = sharedPrefrences?.getLong(FILTER_DUPLICATE_NAME, FILTER_DUPLICATE_CLICK)!!
        }
//        ensureBackgroundThread {

            val internalStoragePath = activity?.baseConfig?.internalStoragePath
//            val available = MemorySizeUtils.getAvailableInternalMemorySizeInLong()
//            val totalSize = MemorySizeUtils.getTotalInternalMemorySizeInLong()
//            val usedSpace = totalSize - available
//            val size = getFolderSize(File("$internalStoragePath/$WHATSAPP_NAME"), baseSimpleActivity!!)
//            val whatsappFolderSize = MemorySizeUtils.formatSize(size)

            folderItems.add(FolderItem(PHOTOS_ID, PHOTOS_NAME, R.drawable.ic_icon_photos, getDrawable(R.drawable.rectangle_semitranparent_photo),
                    activity!!.resources.getColor(R.color.photo_text_color), PHOTOS_CLICK))
            folderItems.add(FolderItem(WHATSAPP_ID, WHATSAPP_NAME, R.drawable.ic_icon_whtsap, getDrawable(R.drawable.rectangle_semitranparent_whatsapp),
                    activity!!.resources.getColor(R.color.whatsapp_text_color), WHATSAPP_CLICK))
            folderItems.add(FolderItem(VIDEOS_ID, VIDEOS_NAME, R.drawable.ic_icon_videos, getDrawable(R.drawable.rectangle_semitranparent_video),
                    activity!!.resources.getColor(R.color.video_text_color), VIDEOS_CLICK))
            folderItems.add(FolderItem(AUDIO_ID, AUDIO_NAME, R.drawable.ic_icon_audio, getDrawable(R.drawable.rectangle_semitranparent_audio),
                    activity!!.resources.getColor(R.color.audio_text_color), AUDIO_CLICK))
            folderItems.add(FolderItem(FILTER_DUPLICATE_ID, FILTER_DUPLICATE_NAME, R.drawable.ic_icon_duplicate, getDrawable(R.drawable.rectangle_semitranparent_filter),
                    activity!!.resources.getColor(R.color.filter_text_color), FILTER_DUPLICATE_CLICK))
//        }
    }

    fun getDrawable(id: Int): Drawable {
        return  activity!!.resources.getDrawable(id)
    }

//    override fun onSaveInstanceState(outState: Bundle) {
//        outState.putString(PATH, currentPath)
//        super.onSaveInstanceState(outState)
//    }
//
//    override fun onViewStateRestored(savedInstanceState: Bundle?) {
//        super.onViewStateRestored(savedInstanceState)
//
//        if (savedInstanceState != null) {
//            currentPath = savedInstanceState?.getString(PATH)!!
//            storedItems.clear()
//        }
//    }

    override fun onResume() {
        super.onResume()
        context!!.updateTextColors(mView as ViewGroup)
//        val newTextColor = context!!.config.textColor
//        if (storedTextColor != newTextColor) {
//            storedItems = ArrayList()
//
//            storedTextColor = newTextColor
//        }

//        val configFontSize = context!!.config.fontSize
//        if (storedFontSize != configFontSize) {
//            getRecyclerAdapter()?.updateFontSizes()
//            storedFontSize = configFontSize
//        }
        isFirstResume = false
    }

    fun openPath(path: String, forceRefresh: Boolean = false) {
        if (!isAdded || (activity as BaseSimpleActivity).isAskingPermissions) {
            return
        }
        showDialog()
        var realPath = path.trimEnd('/')
        if (realPath.isEmpty()) {
            realPath = "/"
        }

        isHeaderShow = realPath == activity?.config?.homeFolder

        scrollStates[currentPath] = getScrollState()!!
        currentPath = realPath
        showHidden = context!!.config.shouldShowHidden
        getItems(currentPath) { originalPath, listItems ->
            if (currentPath != originalPath || !isAdded) {
                return@getItems
            }

            FileDirItem.sorting = context!!.config.getFolderSorting(currentPath)
            listItems.sort()
            activity?.runOnUiThread {
                activity?.invalidateOptionsMenu()
                addItems(listItems, isHeaderShow)
                if (context != null && currentViewType != context!!.config.viewType) {
                    setupLayoutManager()
                }
            }
        }
    }

    private fun addItems(items: ArrayList<ListItem>, forceRefresh: Boolean = false) {
        skipItemUpdating = false

        mView.apply {
            activity?.runOnUiThread {
                if (!forceRefresh && items.hashCode() == storedItems.hashCode()) {
                    return@runOnUiThread
                }
                dismissDialog()
                storedItems = items
                if(firstTime) {
                    pathList.add(currentPath)
                    firstTime = false
                }
                if(pathList.size<=1){
                    my_recyclerView?.beGone()
                }else{
                    my_recyclerView?.beVisible()
                }
                if(adapterForPath == null) {
                    adapterForPath = AdapterForPath(pathList, this@ItemsFragment, requireActivity())
                    my_recyclerView?.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
                    my_recyclerView?.adapter = adapterForPath
                }else{
                    adapterForPath?.updateDataAndNotify(pathList)
                }
                if(storedItems.isNotEmpty()) {
                    zrpImage?.beGone()
                    mainAdapter = ItemsAdapter(activity as BaseSimpleActivity, isHeaderShow, folderItems, bottomnavigation, storedItems, this@ItemsFragment, items_fastscroller, items_list) {
                        itemClicked(it as FileDirItem)
                    }.apply {
                        setupZoomListener(zoomListener)
                        items_list.adapter = this
                    }
                }else{
                    zrpImage?.beVisible()
                }


                items_fastscroller.setViews(items_list, null) {}
                getRecyclerLayoutManager().onRestoreInstanceState(scrollStates[currentPath])
                items_list.onGlobalLayout {
                    items_fastscroller.setScrollToY(items_list.computeVerticalScrollOffset())
                    calculateContentHeight(storedItems)
                }
            }
        }
    }

    fun setZRPImage(image : ImageView?){
        zrpImage = image
    }
    private fun showDialog() {
        try {
            //Dismiss if already exist
//            dismissDialog()
            if (ThemeUtils.getActivityIsAlive(activity)) {
                mProgressDialog = AppProgressDialog(activity)
                mProgressDialog?.setCancelable(true)
                mProgressDialog?.setCanceledOnTouchOutside(true)
                mProgressDialog?.show()
            }
        } catch (e: Exception) {
        }
    }

    private fun dismissDialog() {
        if (ThemeUtils.getActivityIsAlive(activity) && mProgressDialog != null && mProgressDialog!!.isShowing) {
            mProgressDialog!!.dismiss()
        }
    }
    private fun getScrollState() = getRecyclerLayoutManager().onSaveInstanceState()

    private fun getRecyclerLayoutManager() = (mView.items_list.layoutManager as MyGridLayoutManager)

    private fun getItems(path: String, callback: (originalPath: String, items: ArrayList<ListItem>) -> Unit) {
        skipItemUpdating = false
        ensureBackgroundThread {
            if (activity?.isDestroyed == false && activity?.isFinishing == false) {
                val config = requireContext().config
                if (requireContext().isPathOnOTG(path) && config.OTGTreeUri.isNotEmpty()) {
                    val getProperFileSize = requireContext().config.getFolderSorting(currentPath) and SORT_BY_SIZE != 0
                    requireContext().getOTGItems(path, config.shouldShowHidden, getProperFileSize) {
                        callback(path, getListItemsFromFileDirItems(it))
                    }
                } else if (!config.enableRootAccess || !requireContext().isPathOnRoot(path)) {
                    getRegularItemsOf(path, callback)
                } else {
                    RootHelpers(activity!!).getFiles(path, callback)
                }
            }
        }
    }

    private fun getRegularItemsOf(path: String, callback: (originalPath: String, items: ArrayList<ListItem>) -> Unit) {
        val items = ArrayList<ListItem>()
        val files = File(path).listFiles()?.filterNotNull()
        if (context == null) {
            callback(path, items)
            return
        }

        val lastModifieds = if (isRPlus()) context!!.getFolderLastModifieds(path) else HashMap()
        val isSortingBySize = context!!.config.getFolderSorting(currentPath) and SORT_BY_SIZE != 0
        if (files != null) {
            for (file in files) {
                val fileDirItem = getFileDirItemFromFile(file, isSortingBySize, lastModifieds)
                if (fileDirItem != null) {
                    items.add(fileDirItem)
                }
            }
        }

        callback(path, items)
    }

    private fun getFileDirItemFromFile(file: File, isSortingBySize: Boolean, lastModifieds: HashMap<String, Long>): ListItem? {
        val curPath = file.absolutePath
        val curName = file.name
        if (!showHidden && curName.startsWith(".") ) {
            return null
        }
        if(file.length()<=0 ){
            return null
        }

        var lastModified = lastModifieds.remove(curPath)
        val isDirectory = if (lastModified != null) false else file.isDirectory
        val children = if (isDirectory) file.getDirectChildrenCount(showHidden) else 0
        val size = if (isDirectory) {
            if (isSortingBySize) {
                file.getProperSize(showHidden)
            } else {
                0L
            }
        } else {
            file.length()
        }

        if (lastModified == null) {
            lastModified = file.lastModified()
        }
//        val newUri = activity?.getFinalUriFromPath(file.path, "com.rocks.music.videoplayer.provider")

//        val audioImageUri = if (file.path.getMimeType().contains("audio")) {
//            activity?.let { getAudioImageFromPath(it, file.path) }
//        } else null

        return ListItem(curPath, curName, isDirectory, children, size, lastModified, false, null)
    }

    private fun getListItemsFromFileDirItems(fileDirItems: ArrayList<FileDirItem>): ArrayList<ListItem> {
        val listItems = ArrayList<ListItem>()
        fileDirItems.forEach {
            val listItem = ListItem(it.path, it.name, it.isDirectory, it.children, it.size, it.modified, false, null)
            listItems.add(listItem)
        }
        return listItems
    }

    private fun itemClicked(item: FileDirItem) {
        if (item.isDirectory) {
            pathList.add(item.path)
            (activity as? FileManagerMainActivity)?.apply {
                skipItemUpdating = isSearchOpen
                openedDirectory()
            }
            openPath(item.path)
        } else {
            val path = item.path
            if (isGetContentIntent) {
                (activity as FileManagerMainActivity).pickedPath(path)
            } else if (isGetRingtonePicker) {
                if (path.isAudioFast()) {
                    (activity as FileManagerMainActivity).pickedRingtone(path)
                } else {
                    activity?.toast(R.string.select_audio_file)
                }
            } else {
                if(isFolderView()){
                    if(currentFolderHeader == PHOTOS_NAME) {
                        val photoList = queryImages(activity!!, null)
                        val position = getPositionOfImage(photoList, item.path)
                        //FullScreenPhotos.startFullScreenActivity(activity, FullScreenPhotos::class.java, photoList, position)
                    }
                }else{
                    activity!!.tryOpenPathIntent(path, false)
                }
            }
        }
    }
    private fun isFolderView(): Boolean {
        return ( currentPath == "$internalStoragePath/$PHOTOS_NAME")
    }
    fun searchQueryChanged(text: String) {
        val searchText = text.trim()
        lastSearchedText = searchText
        ensureBackgroundThread {
            if (context == null) {
                return@ensureBackgroundThread
            }

            when {
                searchText.isEmpty() -> activity?.runOnUiThread {
                    mView.apply {
                        items_list.beVisible()
                        getRecyclerAdapter()?.updateItems(storedItems)
                        items_placeholder.beGone()
                        //  items_placeholder_2.beGone()
                    }
                }
                /*
                It is for search when searching character is more than 1

                searchText.length == 1 -> activity?.runOnUiThread {
                     mView.apply {
                         items_list.beGone()
                         items_placeholder.beVisible()
                         items_placeholder_2.beVisible()
                     }
                 }*/
                else -> {
                    if (lastSearchedText != searchText) {
                        return@ensureBackgroundThread
                    }
                    val files = searchFiles(searchText, currentPath)
                    files.sortBy { it.getParentPath() }

                    /*
                    It is for showing file path above all same directry files

                    val listItems = ArrayList<ListItem>()
                     var previousParent = ""
                     files.forEach {
                         val parent = it.mPath.getParentPath()
                         if (parent != previousParent && context != null) {
                             listItems.add(ListItem("", context!!.humanizePath(parent), false, 0, 0, 0, true))
                             previousParent = parent
                         }
                         listItems.add(it)
                     }*/

                    activity?.runOnUiThread {
                        getRecyclerAdapter()?.updateItems(files, text)
                        mView.apply {
                            items_list.beVisibleIf(files.isNotEmpty())
                            items_placeholder.beVisibleIf(files.isEmpty())
                            // items_placeholder_2.beGone()
                        }
                    }
                }
            }
        }
    }

    private fun searchFiles(text: String, path: String): ArrayList<ListItem> {
        val files = ArrayList<ListItem>()
        if (context == null) {
            return files
        }
        val sorting = context!!.config.getFolderSorting(path)
        FileDirItem.sorting = context!!.config.getFolderSorting(currentPath)
        val isSortingBySize = sorting and SORT_BY_SIZE != 0
        File(path).listFiles()?.sortedBy { it.isDirectory }?.forEach {
            if (it.name.contains(text, true)) {
                val fileDirItem = getFileDirItemFromFile(it, isSortingBySize, HashMap())
                if (fileDirItem != null) {
                    files.add(fileDirItem)
                }
            }

            /*
            It is for whole phone searching directory and files both

            if (it.isDirectory) {
                if (it.name.contains(text, true)) {
                    val fileDirItem = getFileDirItemFromFile(it, isSortingBySize, HashMap())
                    if (fileDirItem != null) {
                        files.add(fileDirItem)
                    }
                }
                files.addAll(searchFiles(text, it.absolutePath))
            } else {
                if (it.name.contains(text, true)) {
                    val fileDirItem = getFileDirItemFromFile(it, isSortingBySize, HashMap())
                    if (fileDirItem != null) {
                        files.add(fileDirItem)
                    }
                }
            }*/
        }
        return files
    }

    fun searchOpened() {
        isSearchOpen = true
        lastSearchedText = ""
//        items_fab.beGone()
    }

    fun searchClosed() {
        isSearchOpen = false
//        items_fab.beVisible()
        if (!skipItemUpdating) {
            getRecyclerAdapter()?.updateItems(storedItems)
        }
        skipItemUpdating = false
        lastSearchedText = ""
        mView.apply {
            items_list.beVisible()
            items_placeholder.beGone()
        }
    }

//    private fun createNewItem() {
//        CreateNewItemDialog(activity as BaseSimpleActivity, currentPath) {
//            if (it) {
//                refreshItems(false)
//            } else {
//                activity?.toast(R.string.unknown_error_occurred)
//            }
//        }
//    }

    private fun getRecyclerAdapter() = mView.items_list.adapter as? ItemsAdapter

    fun setupLayoutManager() {
        if (context!!.config.viewType == VIEW_TYPE_GRID) {
            currentViewType = VIEW_TYPE_GRID
            setupGridLayoutManager()
        } else {
            currentViewType = VIEW_TYPE_LIST
            setupListLayoutManager()
        }

        mView.items_list.adapter = null
        initZoomListener()
        addItems(storedItems, true)
    }

    private fun setupGridLayoutManager() {
        val layoutManager = mView.items_list.layoutManager as MyGridLayoutManager
        layoutManager.spanCount = context?.config?.fileColumnCnt ?: 3

        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {

            override fun getSpanSize(position: Int): Int {
                return when(getRecyclerAdapter()?.getItemViewType(position)) {
                    TYPE_HEADER ->
                        context?.config?.fileColumnCnt!!
//                    TYPE_ITEM -> 1
                    else ->
                        1
                }
            }
        }
    }

    private fun setupListLayoutManager() {
        val layoutManager = mView.items_list.layoutManager as MyGridLayoutManager
        layoutManager.spanCount = 1
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {

            override fun getSpanSize(position: Int): Int {
                return when(getRecyclerAdapter()?.getItemViewType(position)) {
                    TYPE_HEADER -> 1
//                    TYPE_ITEM -> 1
                    else ->
                        1

                }
            }
        }



        zoomListener = null
    }

    private fun initZoomListener() {
        if (context?.config?.viewType == VIEW_TYPE_GRID) {
            val layoutManager = mView.items_list.layoutManager as MyGridLayoutManager
            zoomListener = object : MyRecyclerView.MyZoomListener {
                override fun zoomIn() {
                    if (layoutManager.spanCount > 1) {
                        reduceColumnCount()
                        getRecyclerAdapter()?.finishActMode()
                    }
                }

                override fun zoomOut() {
                    if (layoutManager.spanCount < MAX_COLUMN_COUNT) {
                        increaseColumnCount()
                        getRecyclerAdapter()?.finishActMode()
                    }
                }
            }
        } else {
            zoomListener = null
        }
    }

    private fun calculateContentHeight(items: ArrayList<ListItem>) {
        val layoutManager = mView.items_list.layoutManager as MyGridLayoutManager
        val thumbnailHeight = layoutManager.getChildAt(0)?.height ?: 0
        val fullHeight = ((items.size - 1) / layoutManager.spanCount + 1) * thumbnailHeight
        mView.items_fastscroller.setContentHeight(fullHeight)
        mView.items_fastscroller.setScrollToY(mView.items_list.computeVerticalScrollOffset())
    }

    fun increaseColumnCount() {
        context?.config?.fileColumnCnt = ++(mView.items_list.layoutManager as MyGridLayoutManager).spanCount
        columnCountChanged()
    }

    fun reduceColumnCount() {
        context?.config?.fileColumnCnt = --(mView.items_list.layoutManager as MyGridLayoutManager).spanCount
        columnCountChanged()
    }

    private fun columnCountChanged() {
        mView.items_list.adapter?.notifyDataSetChanged()
        calculateContentHeight(storedItems)
    }

    override fun refreshItems(isHeaderFolder: Boolean) {
        val internalStoragePath = context?.config?.internalStoragePath
        if(isHeaderFolder){
            isHeaderShow = false
            currentPath = "$internalStoragePath/$currentFolderHeader"
            if(currentFolderHeader == WHATSAPP_NAME){
                openPath(currentPath)
            }else {
                storedItems = list
                addItems(storedItems, true)
            }
        }else {
            /*if(currentPath == "$internalStoragePath/$currentFolderHeader"){
                addItems(storedItems, true)
            }else {
                openPath(currentPath)
            }*/
            showDialog()
            if(currentPath != "$internalStoragePath/$currentFolderHeader")
                openPath(currentPath)
        }
    }

    override fun deleteFiles(files: ArrayList<FileDirItem>) {
        val hasFolder = files.any { it.isDirectory }
        val firstPath = files.firstOrNull()?.path
        if (firstPath == null || firstPath.isEmpty() || context == null) {
            return
        }

        if (context!!.isPathOnRoot(firstPath)) {
            RootHelpers(activity!!).deleteFiles(files)
        } else {
            (activity as BaseSimpleActivity).deleteFiles(files, hasFolder) {
                if (!it) {
                    activity!!.runOnUiThread {
                        activity!!.toast(R.string.unknown_error_occurred)
                    }
                }
            }
        }
    }

    override fun selectedPaths(paths: ArrayList<String>) {
        (activity as FileManagerMainActivity).pickedPaths(paths)
    }

    override fun headerFolderClick(folder: FolderItem) {
        if(folder.id!= FILTER_DUPLICATE_ID) {
            currentFolderHeader = folder.folderName
            pathList.add("$internalStoragePath/$currentFolderHeader")
        }
        folder.ClickCount++
        when (folder.id) {
            AUDIO_ID -> {
                AUDIO_CLICK++
                model?.audios?.observe(baseSimpleActivity!!, androidx.lifecycle.Observer {
                    if (!it.isNullOrEmpty()) {
                        list = it as ArrayList<ListItem>
                        refreshItems(true)
                    }
                })
            }
            VIDEOS_ID -> {
                VIDEOS_CLICK++
                model?.videos?.observe(baseSimpleActivity!!, androidx.lifecycle.Observer {
                    if (!it.isNullOrEmpty()) {
                        list = it as ArrayList<ListItem>
                        refreshItems(true)
                    }
                })
            }
            PHOTOS_ID -> {
                PHOTOS_CLICK++
                model?.photos?.observe(baseSimpleActivity!!, androidx.lifecycle.Observer {
                    if (!it.isNullOrEmpty()) {
                        list = it as ArrayList<ListItem>
                        refreshItems(true)
                    }
                })
            }
            WHATSAPP_ID -> {
                WHATSAPP_CLICK++
                refreshItems(true)
            }
            FILTER_DUPLICATE_ID -> {
                FILTER_DUPLICATE_CLICK++
                val intent = Intent("com.rocks.music.hamburger.FilterDuplicateActivity")
                startActivity(intent)
            }
        }
    }

    override fun breadcrumbClickedNew(path: String, position: Int) {
        val size = pathList.size
        for(i in 0 until size){
            if(i>position) {
                pathList.removeAt(pathList.size - 1)
            }
        }
        if (position == 0) {
            if(activity!!.hasExternalSDCard() || activity!!.hasOTGConnected()) {
                StoragePickerDialog(activity as BaseSimpleActivity, currentPath, false) {
                    getRecyclerAdapter()?.finishActMode()
//                    openPath(it)    //For SD Card And Otg
                    openPath(activity!!.internalStoragePath)
                }
            }else{
                getRecyclerAdapter()?.finishActMode()
                openPath(activity!!.internalStoragePath)
            }
        } else {
            if(path!= "$internalStoragePath/$PHOTOS_NAME/"
                    && path!= "$internalStoragePath/$AUDIO_NAME/"
                    && path!= "$internalStoragePath/$VIDEOS_NAME/") {
                openPath(path)
            }
        }

    }
}
