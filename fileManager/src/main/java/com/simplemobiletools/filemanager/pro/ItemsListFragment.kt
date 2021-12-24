package com.simplemobiletools.filemanager.pro

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Environment
import android.os.Parcelable
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.simplemobiletools.commons.AppProgressDialog
import com.simplemobiletools.commons.ThemeUtils
import com.simplemobiletools.commons.activities.BaseSimpleActivity
import com.simplemobiletools.commons.adapters.AdapterForFolders
import com.simplemobiletools.commons.adapters.AdapterForPath
import com.simplemobiletools.commons.dialogs.StoragePickerDialog
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.helpers.*
import com.simplemobiletools.commons.interfaces.ItemOperationsListener
import com.simplemobiletools.commons.models.FileDirItem
import com.simplemobiletools.commons.models.FolderItem
import com.simplemobiletools.commons.models.StorageItem
import com.simplemobiletools.commons.views.pathList
import com.simplemobiletools.filemanager.pro.activities.FileManagerMainActivity
import com.simplemobiletools.filemanager.pro.adapters.ItemsListAdapter
import com.simplemobiletools.filemanager.pro.extensions.*
import com.simplemobiletools.filemanager.pro.helpers.DataViewModel
import com.simplemobiletools.filemanager.pro.helpers.RootHelpers
import com.simplemobiletools.filemanager.pro.models.ListItem
import kotlinx.android.synthetic.main.fragment_items_list.*
import kotlinx.android.synthetic.main.fragment_items_list.view.*
import kotlinx.android.synthetic.main.this_is_it.*
import kotlinx.android.synthetic.main.this_is_it.view.*
import java.io.File
import java.util.HashMap

const val PARAM_ID = "idExtra"

class ItemsListFragment : Fragment(), ItemOperationsListener,AdapterForPath.BreadcrumbsListenerNew {

    var currentPath = ""
    var mainAdapter : AdapterForFolders? = null
    private var currentViewType = VIEW_TYPE_LIST
    private var storedItems = ArrayList<ListItem>()
    lateinit var mView: View
    var isGetContentIntent = false
    var isGetRingtonePicker = false
    private var isSearchOpen = false

    private var skipItemUpdating = false
    private var showHidden = false
    private var firstTime = true
    var adapterForPath : AdapterForPath? = null
    private var scrollStates = HashMap<String, Parcelable>()
    var list : ArrayList<ListItem> = ArrayList()
    private var folderItems = ArrayList<FolderItem>()
    var currentFolderHeader = ""
    var model : DataViewModel? = null
    private var baseSimpleActivity : BaseSimpleActivity? = null
//    var mainAdapter : AdapterForFolders? = null


    private var internalStoragePath : String? = ""
    private val sharedPrefFile = "com.example.new_file_manager"


    private var mProgressDialog: AppProgressDialog? = null


    companion object {

        val NAME = 0


        var folderClicked:Int? = 0


        fun newInstance(name: Int) : ItemsListFragment {
            val myFragment = ItemsListFragment()
            val bundle = Bundle()
            bundle.putInt(PARAM_ID, name)
            myFragment?.arguments = bundle
            return myFragment
        }


    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPreferences: SharedPreferences? = activity?.getSharedPreferences(sharedPrefFile,
            Context.MODE_PRIVATE)


        val bundle = arguments
        folderClicked = bundle?.getInt(PARAM_ID)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        mView = inflater.inflate(R.layout.fragment_items_list, container, false)
        baseSimpleActivity = activity as BaseSimpleActivity

        model = ViewModelProvider(baseSimpleActivity!!).get(DataViewModel::class.java)
//        model?.photoSize?.observe(baseSimpleActivity!!, androidx.lifecycle.Observer {
//            //updatePhotosSize(it)
//        })
//
//
//        model?.videoSize?.observe(baseSimpleActivity!!, androidx.lifecycle.Observer {
//            //updateVideoSize(it)
//            mainAdapter?.updateFolderItems(folderItems)
//        })
//
//        model?.apps?.observe(baseSimpleActivity!!, androidx.lifecycle.Observer {
//            //updateVideoSize(it)
//            mainAdapter?.updateFolderItems(folderItems)
//        })
//
//        model?.documents?.observe(baseSimpleActivity!!, androidx.lifecycle.Observer {
//            //updateVideoSize(it)
//            mainAdapter?.updateFolderItems(folderItems)
//        })
//        model?.zip_files?.observe(baseSimpleActivity!!, androidx.lifecycle.Observer {
//            //updateVideoSize(it)
//            mainAdapter?.updateFolderItems(folderItems)
//        })
//
//        model?.audioSize?.observe(baseSimpleActivity!!, androidx.lifecycle.Observer {
//            //updateAudioSize(it)
//            mainAdapter?.updateFolderItems(folderItems)
//        })

        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_items_list, container, false)
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        item_list_rv?.layoutManager = LinearLayoutManager(activity,LinearLayoutManager.HORIZONTAL,false)
//        itemsAdapter = ItemsListAdapter(storageItems,requireActivity() )
//        item_list_rv?.adapter = itemsAdapter

        when (folderClicked) {
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
            SHORTCUT_ID -> {
                currentFolderHeader = "Internal"
                refreshItems(true)

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
            DOWNLOAD_ID -> {
                DOWNLOAD_CLICK++
                currentFolderHeader = Environment.DIRECTORY_DOWNLOADS
                refreshItems(true)
            }
            APPLICATIONS_ID ->{
                APPLICATION_CLICK++
                model?.apps?.observe(baseSimpleActivity!!, androidx.lifecycle.Observer {
                    if (!it.isNullOrEmpty()) {
                        list = it as ArrayList<ListItem>
                        refreshItems(true)
                    }
                })
            }
            DOCUMENTS_ID ->{
                DOCUMENTS_CLICK++
                model?.documents?.observe(baseSimpleActivity!!, androidx.lifecycle.Observer {
                    if (!it.isNullOrEmpty()) {
                        list = it as ArrayList<ListItem>
                        refreshItems(true)
                    }
                })
            }
            ZIP_FILES_ID ->{
                ZIP_FILES_CLICK++
                model?.zip_files?.observe(baseSimpleActivity!!, androidx.lifecycle.Observer {
                    if (!it.isNullOrEmpty()) {
                        list = it as ArrayList<ListItem>
                        refreshItems(true)
                    }
                })
            }
            INTERNAL_STORAGE -> {

               // currentFolderHeader = Environment.DIRECTORY_DOWNLOADS
                currentFolderHeader = "Internal"
                refreshItems(true)
            }
            EXTERNAL_STORAGE -> {

                // currentFolderHeader = Environment.DIRECTORY_DOWNLOADS
                currentFolderHeader = "External"
                refreshItems(true)
            }

        }


    }

    fun openPath(path: String, forceRefresh: Boolean = false) {

        getItems(path) { originalPath, listItems ->
            if (path != originalPath || !isAdded) {
                return@getItems
            }

            FileDirItem.sorting = requireContext().config.getFolderSorting(path)
            listItems.sort()
            activity?.runOnUiThread {
                activity?.invalidateOptionsMenu()
                addItems(listItems)
                if (context != null && currentViewType != requireContext().config.viewType) {
                    setupLayoutManager()
                }
            }
        }
    }
    fun setupLayoutManager() {
        if (requireContext().config.viewType == VIEW_TYPE_GRID) {
            currentViewType = VIEW_TYPE_GRID
            // setupGridLayoutManager()
        } else {
            currentViewType = VIEW_TYPE_LIST
            // setupListLayoutManager()
        }

        mView.item_list_rv.adapter = null
        val layoutManager = mView.item_list_rv.layoutManager as LinearLayoutManager
        addItems(storedItems)
    }

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
                    RootHelpers(requireActivity()).getFiles(path, callback)
                }
            }
        }
    }

    private fun getListItemsFromFileDirItems(fileDirItems: ArrayList<FileDirItem>): ArrayList<ListItem> {
        val listItems = ArrayList<ListItem>()
        fileDirItems.forEach {
            val listItem = ListItem(
                it.path,
                it.name,
                it.isDirectory,
                it.children,
                it.size,
                it.modified,
                false,
                null,
                ""
            )
            listItems.add(listItem)
        }
        return listItems
    }
    private fun getRegularItemsOf(path: String, callback: (originalPath: String, items: ArrayList<ListItem>) -> Unit) {
        val items = ArrayList<ListItem>()
        val files = File(path).listFiles()?.filterNotNull()
        if (context == null) {
            callback(path, items)
            return
        }

        val lastModifieds = if (isRPlus()) requireContext().getFolderLastModifieds(path) else HashMap()
        val isSortingBySize = requireContext().config.getFolderSorting(currentPath) and SORT_BY_SIZE != 0
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

        return ListItem(
            curPath,
            curName,
            isDirectory,
            children,
            size,
            lastModified,
            false,
            null,
            ""
        )
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
    private fun itemClicked(item: FileDirItem) {
        if (item.isDirectory) {
            (activity as FileManagerMainActivity).pathList.add(item.path)
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

                    requireActivity().tryOpenPathIntent(path, false)
                }

        }
    }
    fun itemClick(list : ListItem ,position: Int, forceRefresh: Boolean)
    {
       /* var mList : ArrayList<ListItem>? = null
        if(list is List<*>)
        {
           mList =  list as ArrayList<ListItem>
        }*/
        itemClicked(list as FileDirItem)
//            Log.d("@openPath","called")
//            val openFolder= list.mPath!!
//            openPath(openFolder)

//                        var adad = folderItems
//                        mainAdapter = AdapterForFolders(
//                            folderItems,
//                            { folder -> headerFolderClick(folder) },
//                            requireActivity()
//                        )
//                        item_list_rv?.layoutManager =
//                            LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
//                        item_list_rv?.adapter = mainAdapter
          }

    private fun addItems(items: ArrayList<ListItem>,forceRefresh: Boolean = false) {
        skipItemUpdating = false

        mView.apply {
            activity?.runOnUiThread {
                if (!forceRefresh && items.hashCode() == storedItems.hashCode()) {
                    return@runOnUiThread
                }
                dismissDialog()
                storedItems = items
                if(firstTime) {
                    (activity as FileManagerMainActivity).pathList.add(currentPath)
                    firstTime = false
                }

                if(adapterForPath == null) {
                    adapterForPath = AdapterForPath((activity as FileManagerMainActivity).pathList, this@ItemsListFragment, requireActivity())
//                    my_recyclerView?.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
//                    my_recyclerView?.adapter = adapterForPath
                }else{
                    adapterForPath?.updateDataAndNotify((activity as FileManagerMainActivity).pathList)
                }
                if(storedItems.isNotEmpty()) {

                    adapterForPath
                    getRecyclerAdapter()?.updateListItems(storedItems)
                    mView.item_list_rv.adapter = ItemsListAdapter(
                        activity as BaseSimpleActivity,
                         folderItems,
                        bottomnavigation,
                        storedItems,
                        this@ItemsListFragment,
                        null,
                        item_list_rv
                    ) { list, position -> itemClick(list as ListItem, position, false) }/*{
                        Log.d("@openPath","called")
                        var openFolder=items[position].mPath
                        openPath(openFolder)

//                        var adad = folderItems
//                        mainAdapter = AdapterForFolders(
//                            folderItems,
//                            { folder -> headerFolderClick(folder) },
//                            requireActivity()
//                        )
//                        item_list_rv?.layoutManager =
//                            LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
//                        item_list_rv?.adapter = mainAdapter
                    }*/
                }


                //  items_fastscroller.setViews(item_list_rv, null) {}
              //  getRecyclerLayoutManager().onRestoreInstanceState(scrollStates[currentPath])

            }
        }
    }


    private fun getRecyclerAdapter() = mView.item_list_rv.adapter as? ItemsListAdapter
//    private fun getRecyclerLayoutManager() = (mView.items_list.layoutManager as MyLinearLayoutManager)


    private fun dismissDialog() {
        if (ThemeUtils.getActivityIsAlive(activity) && mProgressDialog != null && mProgressDialog!!.isShowing) {
            mProgressDialog!!.dismiss()
        }
    }



    override fun refreshItems(isHeaderFolder: Boolean) {
        val internalStoragePath = context?.config?.internalStoragePath
        val externalStoragePath = context?.config?.sdCardPath
        if(isHeaderFolder){
            //currentFolderHeader= Environment.DIRECTORY_DOWNLOADS
            currentPath = "$internalStoragePath/$currentFolderHeader"
            if(currentFolderHeader == "Download"){
                (activity as FileManagerMainActivity).pathList.add(currentPath)
                openPath(currentPath)
            }else if(currentFolderHeader == "Internal"){
                currentPath = "$internalStoragePath"
                (activity as FileManagerMainActivity).pathList.add(currentPath)
                openPath(currentPath)
            }else if(currentFolderHeader == "External"){
                currentPath = "$externalStoragePath"
                (activity as FileManagerMainActivity).pathList.add(currentPath)
                openPath(currentPath)
            }
            else {
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

        if (requireContext().isPathOnRoot(firstPath)) {
            RootHelpers(requireActivity()).deleteFiles(files)
        } else {
            (activity as BaseSimpleActivity).deleteFiles(files, hasFolder) {
                if (!it) {
                    requireActivity().runOnUiThread {
                        requireActivity().toast(R.string.unknown_error_occurred)
                    }
                }
            }
        }
    }
    override fun selectedPaths(paths: ArrayList<String>) {
        (activity as FileManagerMainActivity).pickedPaths(paths)
    }

    override fun headerFolderClick(folder: FolderItem) {
//        when (folder.id) {
//            AUDIO_ID -> {
//                AUDIO_CLICK++
//                model?.audios?.observe(baseSimpleActivity!!, androidx.lifecycle.Observer {
//                    if (!it.isNullOrEmpty()) {
//                        list = it as ArrayList<ListItem>
//                        refreshItems(true)
//                    }
//                })
//            }
//            VIDEOS_ID -> {
//                VIDEOS_CLICK++
//                model?.videos?.observe(baseSimpleActivity!!, androidx.lifecycle.Observer {
//                    if (!it.isNullOrEmpty()) {
//                        list = it as ArrayList<ListItem>
//                        refreshItems(true)
//                    }
//                })
//            }
//            PHOTOS_ID -> {
//                PHOTOS_CLICK++
//                model?.photos?.observe(baseSimpleActivity!!, androidx.lifecycle.Observer {
//                    if (!it.isNullOrEmpty()) {
//                        list = it as ArrayList<ListItem>
//                        refreshItems(true)
//                    }
//                })
//            }
//            DOWNLOAD_ID -> {
//                DOWNLOAD_CLICK++
//                currentFolderHeader = Environment.DIRECTORY_DOWNLOADS
//                refreshItems(true)
//            }
//            APPLICATIONS_ID ->{
//                APPLICATION_CLICK++
//                model?.apps?.observe(baseSimpleActivity!!, androidx.lifecycle.Observer {
//                    if (!it.isNullOrEmpty()) {
//                        list = it as ArrayList<ListItem>
//                        refreshItems(true)
//                    }
//                })
//            }
//            DOCUMENTS_ID ->{
//                DOCUMENTS_CLICK++
//                model?.documents?.observe(baseSimpleActivity!!, androidx.lifecycle.Observer {
//                    if (!it.isNullOrEmpty()) {
//                        list = it as ArrayList<ListItem>
//                        refreshItems(true)
//                    }
//                })
//            }
//            ZIP_FILES_ID ->{
//                ZIP_FILES_CLICK++
//                model?.zip_files?.observe(baseSimpleActivity!!, androidx.lifecycle.Observer {
//                    if (!it.isNullOrEmpty()) {
//                        list = it as ArrayList<ListItem>
//                        refreshItems(true)
//                    }
//                })
//            }
////            FILTER_DUPLICATE_ID -> {
////                FILTER_DUPLICATE_CLICK++
////                val intent = Intent("com.rocks.music.hamburger.FilterDuplicateActivity")
////                startActivity(intent)
////            }
//        }
    }

    override fun storageFolderClick(storage: StorageItem) {
        TODO("Not yet implemented")
    }

    override fun breadcrumbClickedNew(path: String, position: Int) {
        val size = (activity as FileManagerMainActivity).pathList.size
        for(i in 0 until size){
            if(i>position) {
                (activity as FileManagerMainActivity).pathList.removeAt((activity as FileManagerMainActivity).pathList.size - 1)
            }
        }
        if (position == 0) {
            if(requireActivity().hasExternalSDCard() || requireActivity().hasOTGConnected()) {
                StoragePickerDialog(activity as BaseSimpleActivity, currentPath, false) {
                    getRecyclerAdapter()?.finishActMode()
//                    openPath(it)    //For SD Card And Otg
                    openPath(requireActivity().internalStoragePath)
                }
            }else{
                getRecyclerAdapter()?.finishActMode()
                openPath(requireActivity().internalStoragePath)
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