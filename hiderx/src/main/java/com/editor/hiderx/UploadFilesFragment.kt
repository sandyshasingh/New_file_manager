package com.editor.hiderx

import android.app.AlertDialog
import android.os.Bundle
import android.os.Environment
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.editor.hiderx.activity.FilemanagerActivity
import com.editor.hiderx.adapters.BottomViewFoldersAdapter
import com.editor.hiderx.adapters.HiddenFilesAdapter
import com.editor.hiderx.database.HiddenFiles
import com.editor.hiderx.database.HiddenFilesDatabase
import com.editor.hiderx.dataclass.FileDataClass
import com.editor.hiderx.filefilters.DocumentsFileFilter
import com.editor.hiderx.filefilters.OthersFileFilter
import com.editor.hiderx.fragments.PARAM_HIDER_DIRECTORY
import com.editor.hiderx.listeners.ActionModeListener
import com.editor.hiderx.listeners.FragmentInteractionListener
import com.editor.hiderx.listeners.OnFileClickedListener
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.android.synthetic.main.bottom_layout.*
import kotlinx.android.synthetic.main.folder_bottom_sheet.view.*
import kotlinx.android.synthetic.main.fragment_upload_files.*
import kotlinx.android.synthetic.main.fragment_upload_files.recycler_view
import kotlinx.android.synthetic.main.new_folder_dialog.view.*
import kotlinx.coroutines.*
import java.io.File

const val UPLOAD_DOCUMENTS = 0
const val UPLOAD_OTHERS = 1
const val UPLOAD_ALL = 2

class UploadFilesFragment : Fragment(), OnFileClickedListener, ActionModeListener, FragmentInteractionListener, CoroutineScope by MainScope() {

    var defaultHiderDirectory: String = ""
    var currentType = -1
    var xhiderDirectory = ""
    private var mediaScanner: MediaScanner? = null
    private var dialog: BottomSheetDialog? = null
    var doExit: Boolean = true
    private var adapter: HiddenFilesAdapter? = null
    private var model: DataViewModel? = null
    var selectedFiles: ArrayList<FileDataClass> = ArrayList()
    var fileList: ArrayList<FileDataClass>? = null
    var isActionMode: Boolean = false
    var myFoldersList: ArrayList<String>? = ArrayList()
    var folderToHide: String = "Default"
    var listToAdd: ArrayList<FileDataClass>? = null
    var listToRemove: ArrayList<FileDataClass>? = null
    var currentParentDirectory: File? = null
    var currentPath: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        model = ViewModelProvider(requireActivity()).get(DataViewModel::class.java)
        xhiderDirectory = arguments?.getString(PARAM_HIDER_DIRECTORY, StorageUtils.getVideosHiderDirectory())!!
        when {
            xhiderDirectory.startsWith(StorageUtils.getDocumentsHiderDirectory()) -> {
                model?.getMyDocumentsFolders()
                folderToHide = xhiderDirectory
                currentType = UPLOAD_DOCUMENTS
                defaultHiderDirectory = StorageUtils.getDocumentsHiderDirectory()
            }
            xhiderDirectory.startsWith(StorageUtils.getOthersHiderDirectory()) -> {
                model?.getMyOthersFolders()
                folderToHide = xhiderDirectory
                currentType = UPLOAD_OTHERS
                defaultHiderDirectory = StorageUtils.getOthersHiderDirectory()
            }
            else -> {
                currentType = UPLOAD_ALL
            }

        }
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_upload_files, container, false)
    }

    fun clearData() {
        model?.myFilesFolders?.value = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (currentType == UPLOAD_ALL)
            ll_change_location?.doGone()
        else
            selected_folder_text?.text = File(folderToHide).name
        loadData()
        when (currentType) {
            UPLOAD_OTHERS -> {
                model?.myOthersFolders?.observe(requireActivity())
                {
                    if (it != null) {
                        myFoldersList?.clear()
                        for (i in it) {
                            myFoldersList?.add(i)
                        }
                    }
                }
            }
            UPLOAD_DOCUMENTS -> {
                model?.myDocumentsFolders?.observe(requireActivity())
                {
                    if (it != null) {
                        myFoldersList?.clear()
                        for (i in it) {
                            myFoldersList?.add(i)
                        }
                    }
                }
            }
        }
        btn_back?.setOnClickListener()
        {
            (activity as FilemanagerActivity).onBackPressed()
        }
        img_cross?.setOnClickListener()
        {
            cancelActionMode()
        }

        ll_hide?.setOnClickListener()
        {
            if (mediaScanner == null)
                mediaScanner = MediaScanner(context)
            launch {
                val operation = async(Dispatchers.IO)
                {
                    /*listToRemove = ArrayList()
                    listToAdd = ArrayList()
                    for(i in selectedFiles)
                    {
                        if(!i.isFile)
                        {
                            parseFiles(i)
                        }
                    }
                    selectedFiles.removeAll(listToRemove!!)
                    selectedFiles.addAll(listToAdd!!)
                    listToAdd = null
                    listToRemove = null*/
                    for (i in selectedFiles) {
                        moveFileToPrivateFolder(i)
                    }
                    HiderUtils.setLongSharedPreference(
                            requireContext(),
                            HiderUtils.Last_File_Insert_Time,
                            System.currentTimeMillis()
                    )
                }
                operation.await()
                withContext(Dispatchers.Main)
                {
                    clearData()
                    cancelActionMode()
                    destroyFragment()
                }
            }
        }

        ll_change_location?.setOnClickListener()
        {
            val dialogView: View = layoutInflater.inflate(R.layout.folder_bottom_sheet, null)
            dialogView.rv_folders_to_hide?.layoutManager = LinearLayoutManager(
                    context,
                    RecyclerView.VERTICAL,
                    false
            )
            dialogView.rv_folders_to_hide?.adapter = BottomViewFoldersAdapter(defaultHiderDirectory, myFoldersList, this)
            dialogView.img_cross?.setOnClickListener()
            {
                dismissDialog()
            }
            dialog = BottomSheetDialog(requireContext())
            dialog?.setContentView(dialogView)
            dialog?.setCanceledOnTouchOutside(true)
            dialog?.show()
        }
    }

    fun cancelActionMode() {
        for (i in fileList!!) {
            i.isSelected = false
        }
        adapter?.notifyDataSetChanged()
        selectedFiles.clear()
        tv_selected_count?.text = selectedFiles.count().toString() + " Selected"
        setActionModeValue(false)
    }

    fun getDefaultDirectoryForType(currentType: String?): String? {
        when {
            currentType == null -> {
                return StorageUtils.getOthersHiderDirectory()
            }
            currentType.startsWith("audio")!! -> {
                return StorageUtils.getAudiosHiderDirectory()
            }
            currentType.startsWith("image") -> {
                return StorageUtils.getPhotosHiderDirectory()
            }
            currentType.startsWith("video") -> {
                return StorageUtils.getVideosHiderDirectory()
            }
            currentType.endsWith("pdf") || currentType.contains("document") || currentType.endsWith(
                    "msword"
            ) -> {
                return StorageUtils.getDocumentsHiderDirectory()
            }
            else -> {
                return StorageUtils.getOthersHiderDirectory()
            }
        }
    }

    private fun parseFiles(i: FileDataClass) {
        listToRemove?.add(i)
        /*val data = node?.children
        var mNode : TreeNode<FileDataClass>? = null
        for(j in data!!)
        {
            if(j.value.path == i.path)
            {
                mNode = j
                break
            }
        }
        val list = mNode?.children
        for (i in list!!) {
            if (i.value.isFile) {
                listToAdd?.add(i.value)
            } else {
                parseFiles(i.value, mNode)
            }
        }*/
        val children = File(i.path).listFiles()
        if (children != null) {
            for (j in children) {
                if (j.isFile) {
                    val ext = j.name.substringAfterLast(".")
                    val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext.toLowerCase())
                    listToAdd?.add(FileDataClass(j.path, j.name, StorageUtils.format(j.length().toDouble(), 1), j.isFile, 0, mimeType, false, 0))
                } else {
                    val count = j?.listFiles()?.let { it.count() }
                    parseFiles(FileDataClass(j.path, j.name, null, j.isFile, count, "", false, 0))
                }
            }
        }
    }

    private fun destroyFragment() {
        launch {
            withContext(Dispatchers.Main)
            {
                doExit = true
                (activity as FilemanagerActivity).onBackPressed()
            }
        }
    }

    private fun loadData() {
        launch {
            val operation = async(Dispatchers.IO)
            {
                currentParentDirectory = Environment.getExternalStorageDirectory()
                fileList = ArrayList()
                val filter = when (currentType) {
                    UPLOAD_OTHERS -> OthersFileFilter()
                    UPLOAD_DOCUMENTS -> DocumentsFileFilter()
                    else -> null
                }
                if (filter != null) {
                    val listOfFiles = currentParentDirectory?.listFiles(filter)
                    if (listOfFiles != null && listOfFiles.isNotEmpty()) {
                        for (i in listOfFiles) {
                            if (i.isFile) {
                                val ext = i.name.substringAfterLast(".")
                                val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext.toLowerCase())
                                fileList?.add(FileDataClass(i.path, i.name, StorageUtils.format(i.length().toDouble(), 1), i.isFile, 0, mimeType, false, 0))
                            } else {
                                val count = i?.listFiles(filter)?.let { it.count() }
                                fileList?.add(FileDataClass(i.path, i.name, null, i.isFile, count, "", false, 0))
                            }
                        }
                    }
                } else {
                    val listOfFiles = currentParentDirectory?.listFiles()
                    if (listOfFiles != null && listOfFiles.isNotEmpty()) {
                        for (i in listOfFiles) {
                            if (i.isFile) {
                                val ext = i.name.substringAfterLast(".")
                                val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext.toLowerCase())
                                fileList?.add(FileDataClass(i.path, i.name, StorageUtils.format(i.length().toDouble(), 1), i.isFile, 0, mimeType, false, 0))
                            } else {
                                val count = i?.listFiles()?.let { it.count() }
                                fileList?.add(FileDataClass(i.path, i.name, null, i.isFile, count, "", false, 0))
                            }
                        }
                    }
                }
            }
            operation.await()
            withContext(Dispatchers.Main)
            {
                recycler_view?.layoutManager = LinearLayoutManager(context)
                img_title?.doVisible()
                tv_folder_title.text = getString(R.string.select_files)
                //    tv_folder_name?.text = "Root Folder"
                if (fileList != null && fileList?.size!! > 0) {
                    adapter = HiddenFilesAdapter(
                            fileList!!,
                            recycler_view = recycler_view,
                            context,
                            this@UploadFilesFragment,
                            this@UploadFilesFragment,
                            null
                    )
                    recycler_view?.adapter = adapter
                }
            }
        }
        tv_hide?.text = getString(R.string.hide_files)
    }


    /*  private fun moveOtherFilesToPrivateFolder(i: FileDataClass) {
          val filename: String? = StorageUtils.getFileNameFromPath(i.path!!)
          val newFilename = StorageUtils.encode(filename!!, StorageUtils.offset)
          var newPath: String? = ""
          newPath = if(folderToHide != "Default")
              "$pathToHide/$newFilename"
          else
              StorageUtils.getOthersHiderDirectory()+"/"+newFilename
          var moved = false
          try {
              moved = StorageUtils.move(i.path, newPath)
              if (moved) {
                  mediaScanner?.scan(newPath)
                  val item : HiddenFiles = HiddenFiles(newPath,filename,i.path,i.size!!,i.mimeType,System.currentTimeMillis())
                  HiddenFilesDatabase.getInstance(requireContext()).hiddenFilesDao.insertFile(item)
              }
          } catch (e: Exception) {
              e.printStackTrace()
              Log.d("@ASHISH", e.toString())
          }
      }*/


    private fun moveFileToPrivateFolder(i: FileDataClass) {
        var newPath: String? = ""
        when (currentType) {
            UPLOAD_ALL -> {
                val type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                        File(i.path).extension.toLowerCase()
                )
                newPath = getDefaultDirectoryForType(type)
            }
            UPLOAD_DOCUMENTS, UPLOAD_OTHERS -> {
                newPath = folderToHide
            }
        }
        val filename: String? = StorageUtils.getFileNameFromPath(i.path)
        val newFilename = StorageUtils.encode(filename!!, StorageUtils.offset)
        val size = File(i.path).length()
        if (!File(newPath).exists())
            File(newPath).mkdir()
        newPath = "$newPath/$newFilename"
        var moved = false
        try {
            moved = StorageUtils.move(i.path, newPath)
            if (moved) {
                mediaScanner?.scan(newPath)
                val item = HiddenFiles(
                        newPath, filename, i.path, StorageUtils.format(
                        size.toDouble(),
                        2
                ), i.mimeType, System.currentTimeMillis(), false, i.isFile, 0
                )
                HiddenFilesDatabase.getInstance(requireContext()).hiddenFilesDao.insertFile(item)
            }
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().log(e.toString())
            FirebaseCrashlytics.getInstance().recordException(e)
            Log.d("@ASHISH", e.toString())
        }
    }

    override fun setDirectoryToFolder(folderPath: String?, position: Int?) {
        folderToHide = folderPath!!
        selected_folder_text?.text = File(folderPath).name
    }

    override fun dismissDialog() {
        dialog?.dismiss()
    }

    override fun getSelectedFolder(): String? {
        return folderToHide
    }

    override fun setDirectoryToDefault() {
        folderToHide = defaultHiderDirectory
        dismissDialog()
        selected_folder_text?.text = File(folderToHide).name
    }

    override fun showNewFolderDialog() {
        dismissDialog()
        val view1 = layoutInflater.inflate(R.layout.new_folder_dialog, null)
        var container: AlertDialog? = null
        val dialog = AlertDialog.Builder(context)
        view1?.tv_ok?.setOnClickListener()
        {
            val newFolderPath = defaultHiderDirectory + "/" + view1.folder_name?.text?.toString()
            if (myFoldersList?.contains(newFolderPath)!!) {
                view1.tv_already_exists?.visibility = View.VISIBLE
            } else if (!TextUtils.isEmpty(newFolderPath)) {
                myFoldersList?.add(newFolderPath)
                folderToHide = newFolderPath
                container?.dismiss()
                selected_folder_text?.text = File(newFolderPath).name
            }
        }

        view1?.folder_name?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {}
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                view1.tv_already_exists?.visibility = View.GONE
            }
        })

        view1?.tv_cancel?.setOnClickListener()
        {
            container?.dismiss()
        }

        dialog.setView(view1)
        container = dialog.show()
    }

    override fun onFileDeselected(fileDataClass: FileDataClass) {
        selectedFiles.remove(fileDataClass)
        tv_selected_count?.text = selectedFiles.count().toString() + " Selected"
        if (selectedFiles.size == 0) {
            setActionModeValue(false)
        }
    }

    override fun onFileSelected(fileDataClass: FileDataClass) {
        selectedFiles.add(fileDataClass)
        tv_selected_count?.text = selectedFiles.count().toString() + " Selected"
    }

    override fun onFolderClicked(fileDataClass: FileDataClass) {
        doExit = false
        /* val treeNode = foldersList?.get(foldersList?.size!! - 1)
         var data = treeNode?.children
         var nextRoot : TreeNode<FileDataClass>? = null
         for(i in data!!)
         {
             if(i.value == fileDataClass)
             {
                 nextRoot = i
                 break
             }
         }
         nextRoot?.children?.let{ data= it }*/
        fileList?.clear()
        val folderPath = fileDataClass.path
        val filter = when (currentType) {
            UPLOAD_OTHERS -> OthersFileFilter()
            UPLOAD_DOCUMENTS -> DocumentsFileFilter()
            else -> null
        }
        if (filter != null) {
            val listOfFiles = File(folderPath).listFiles(filter)
            if (listOfFiles != null && listOfFiles.isNotEmpty()) {
                for (i in listOfFiles) {
                    if (i.path != StorageUtils.getRootHiderDirectory().path) {
                        if (i.isFile) {
                            val ext = i.name.substringAfterLast(".")
                            val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext.toLowerCase())
                            fileList?.add(FileDataClass(i.path, i.name, StorageUtils.format(i.length().toDouble(), 1), i.isFile, 0, mimeType, false, 0))
                        } else {
                            fileList?.add(FileDataClass(i.path, i.name, null, i.isFile, i?.listFiles(filter)?.count()!!, "", false, 0))
                        }
                    }
                }
            }
        } else {
            val listOfFiles = File(folderPath).listFiles()
            if (listOfFiles != null && listOfFiles.isNotEmpty()) {
                for (i in listOfFiles) {
                    if (i.path != StorageUtils.getRootHiderDirectory().path) {
                        if (i.isFile) {
                            val ext = i.name.substringAfterLast(".")
                            val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext.toLowerCase())
                            fileList?.add(FileDataClass(i.path, i.name, StorageUtils.format(i.length().toDouble(), 1), i.isFile, 0, mimeType, false, 0))
                        } else {
                            fileList?.add(FileDataClass(i.path, i.name, null, i.isFile, i?.listFiles()?.count()!!, "", false, 0))
                        }
                    }
                }
            }
        }
        img_title?.doGone()
        tv_folder_title.text = fileDataClass?.name
        // tv_folder_name?.text = fileDataClass?.name
        adapter?.filesList = fileList!!
        adapter?.notifyDataSetChanged()
        currentPath = fileDataClass.path

        /* if(data != null && nextRoot!=null)
         {
             for(i in data!!)
             {
                 fileList?.add(i.value)
             }
             foldersList?.add(nextRoot)
             currentNode = nextRoot
             tv_folder_name?.text = currentNode?.value?.name
             adapter?.filesList = fileList!!
             adapter?.notifyDataSetChanged()
         }*/
    }

    override fun onFileClicked(fileDataClass: ArrayList<FileDataClass>, position: Int) {
        (activity as FilemanagerActivity).viewFile(fileDataClass, position)
    }

    fun onPressedBack() {
        /*for(i in fileList!!)
        {
            if(i.isSelected)
                i.isSelected = false
        }*/
        tv_selected_count?.text = "0 Selected"
        /*if(foldersList?.size!!>1)
        {
            foldersList?.removeLast()
        }*/
        //  val nextRoot =  foldersList?.get(foldersList?.size!! - 1)
        //val data = nextRoot?.children!!
        fileList?.clear()
        val parentFile = File(currentPath!!).parentFile
        val filter = when (currentType) {
            UPLOAD_OTHERS -> OthersFileFilter()
            UPLOAD_DOCUMENTS -> DocumentsFileFilter()
            else -> null
        }
        if (filter != null) {
            val listOfFiles = parentFile?.listFiles(filter)
            if (listOfFiles != null && listOfFiles.isNotEmpty()) {
                for (i in listOfFiles) {
                    if (i.path != StorageUtils.getRootHiderDirectory().path) {
                        if (i.isFile) {
                            val ext = i.name.substringAfterLast(".")
                            val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext.toLowerCase())
                            fileList?.add(FileDataClass(i.path, i.name, StorageUtils.format(i.length().toDouble(), 1), i.isFile, 0, mimeType, false, 0))
                        } else {
                            val count = i?.listFiles(filter)?.let { it.count() }
                            fileList?.add(FileDataClass(i.path, i.name, null, i.isFile, count, "", false, 0))
                        }
                    }
                }
            }
        } else {
            val listOfFiles = parentFile?.listFiles()
            if (listOfFiles != null && listOfFiles.isNotEmpty()) {
                for (i in listOfFiles) {
                    if (i.path != StorageUtils.getRootHiderDirectory().path) {
                        if (i.isFile) {
                            val ext = i.name.substringAfterLast(".")
                            val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext.toLowerCase())
                            fileList?.add(FileDataClass(i.path, i.name, StorageUtils.format(i.length().toDouble(), 1), i.isFile, 0, mimeType, false, 0))
                        } else {
                            val count = i?.listFiles()?.let { it.count() }
                            fileList?.add(FileDataClass(i.path, i.name, null, i.isFile, count, "", false, 0))
                        }
                    }
                }
            }
        }
        currentPath = parentFile?.path!!
        //val nextParent = parentFile.parentFile
        if (currentPath == Environment.getExternalStorageDirectory().path) {
            doExit = true
            img_title?.doVisible()
            tv_folder_title.text = getString(R.string.select_files)
            // tv_folder_name?.text = "Root Folder"
        } else {
            img_title?.doGone()
            tv_folder_title.text = parentFile.name
            //   tv_folder_name?.text = parentFile.name
        }
        selectedFiles.clear()
        setActionModeValue(false)
        adapter?.filesList = fileList!!
        adapter?.notifyDataSetChanged()
    }

    override fun getActionMode(): Boolean {
        return isActionMode
    }

    override fun setActionModeValue(actionMode: Boolean) {
        isActionMode = actionMode
        if (!isActionMode)
            bottom_view?.doGone()
        else
            bottom_view?.doVisible()
    }

    companion object {
        fun getInstance(hiderDirectory: String): UploadFilesFragment {
            return UploadFilesFragment().apply {
                arguments = Bundle().apply { putString(PARAM_HIDER_DIRECTORY, hiderDirectory) }
            }
        }
    }

}