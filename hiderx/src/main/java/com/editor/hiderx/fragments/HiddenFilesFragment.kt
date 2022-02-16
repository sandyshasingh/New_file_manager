package com.editor.hiderx.fragments

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.editor.hiderx.*
import com.editor.hiderx.activity.FilemanagerActivity
import com.editor.hiderx.activity.REQUEST_CODE_FOR_SHARE
import com.editor.hiderx.adapters.BottomViewFoldersAdapter
import com.editor.hiderx.adapters.HiddenFilesAdapter
import com.editor.hiderx.database.HiddenFilesDatabase
import com.editor.hiderx.dataclass.FileDataClass
import com.editor.hiderx.dataclass.TreeNode
import com.editor.hiderx.listeners.ActionModeListener
import com.editor.hiderx.listeners.FragmentInteractionListener
import com.editor.hiderx.listeners.OnFileClickedListener
import com.editor.hiderx.listeners.ActivityFragmentListener
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.bottom_layout_hidden_items.*
import kotlinx.android.synthetic.main.delete_confirmation_dialog.view.*
import kotlinx.android.synthetic.main.folder_bottom_sheet.view.*
import kotlinx.android.synthetic.main.fragment_hidden_files.bottom_view
import kotlinx.android.synthetic.main.fragment_hidden_files.btn_back
import kotlinx.android.synthetic.main.fragment_hidden_files.btn_upload
import kotlinx.android.synthetic.main.fragment_hidden_files.img_select_all
import kotlinx.android.synthetic.main.fragment_hidden_files.img_title
import kotlinx.android.synthetic.main.fragment_hidden_files.recycler_view
import kotlinx.android.synthetic.main.fragment_hidden_files.rl_select_all
import kotlinx.android.synthetic.main.fragment_hidden_files.tv_folder_title
import kotlinx.android.synthetic.main.new_folder_dialog.view.*
import kotlinx.android.synthetic.main.unhide_path_dialog.view.*
import kotlinx.coroutines.*
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.*
import kotlin.collections.ArrayList


const val FOLDER_TYPE_DEFAULT = 0
const val FOLDER_TYPE_OTHERS = 5
const val FOLDER_TYPE_PHOTOS = 1
const val FOLDER_TYPE_VIDEOS = 2
const val FOLDER_TYPE_AUDIOS = 3
const val FOLDER_TYPE_DOCUMENTS = 4

const val APPLICATION_ID = "com.example.new_file_manager"

class HiddenFilesFragment : Fragment(), OnFileClickedListener, ActionModeListener, CoroutineScope by MainScope(), FragmentInteractionListener {

    var progressWheel: AppProgressDialog? = null
    private val tempPathList: ArrayList<String> = ArrayList()
    var myFolders: ArrayList<String>? = null
    private var dialog: BottomSheetDialog? = null
    private var bottomAdapter: BottomViewFoldersAdapter? = null
    private var selectAll: Boolean = false
    private var model: DataViewModel? = null
    var onUploadClickListener: ActivityFragmentListener? = null
    var adapter: HiddenFilesAdapter? = null
    var fileList: ArrayList<FileDataClass>? = null
    private var isActionMode: Boolean = false
    var mediaScanner: MediaScanner? = null
    var doExit: Boolean = true
    var foldersList: ArrayList<TreeNode<FileDataClass>>? = ArrayList()
    var selectedFiles: ArrayList<FileDataClass> = ArrayList()
    var currentType = -1
    var photosFolders: ArrayList<String>? = null
    var videoFolders: ArrayList<String>? = null
    var audioFolders: ArrayList<String>? = null
    var documentFolders: ArrayList<String>? = null
    var othersFolders: ArrayList<String>? = null
    var currentPath : String? = null
    var listToAdd: ArrayList<FileDataClass>? = null
    var listToRemove: ArrayList<FileDataClass>? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        model = ViewModelProvider(requireActivity()).get(DataViewModel::class.java)
       // model?.getFilemanagerData()
        model?.getMyPhotosFolders()
        model?.getMyAudiosFolders()
        model?.getMyVideosFolders()
        model?.getMyDocumentsFolders()
        model?.getMyOthersFolders()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_hidden_files, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btn_upload?.doGone()
        launch {
            val operation =  async(Dispatchers.IO)
            {
                currentPath = StorageUtils.getHiderDirectory().path
                fileList = ArrayList()
                getChildFiles()
            }
            operation.await()
            withContext(Dispatchers.Main)
            {
                currentType = FOLDER_TYPE_DEFAULT
                recycler_view?.layoutManager = LinearLayoutManager(context)
                img_title?.doVisible()
                tv_folder_title.text = getString(R.string.vault_file_manager)
                //    tv_folder_name?.text = "Root Folder"
                if(fileList!=null && fileList?.size!!>0)
                {
                    adapter = HiddenFilesAdapter(
                            fileList!!,
                            recycler_view = recycler_view,
                            context,
                            this@HiddenFilesFragment,
                            this@HiddenFilesFragment,
                            null
                    )
                    recycler_view?.adapter = adapter
                }
            }
        }

        btn_back?.setOnClickListener()
        {
            (activity as FilemanagerActivity).onBackPressed()
        }

        btn_upload?.setOnClickListener()
        {
            onUploadClickListener?.onUploadClick(currentPath!!)
        }

        ll_unhide?.setOnClickListener()
        {
            showUnhidePathDialog()
            //unHideSelectedFiles(true)
        }

        ll_move?.setOnClickListener()
        {
            moveSelectedFilesToFolder()
        }

        ll_share?.setOnClickListener()
        {
            (activity as FilemanagerActivity).backPressed = true
            shareSelectedFiles()
        }

        ll_delete?.setOnClickListener()
        {
            showConfirmationDialog()
        }

        img_cross?.setOnClickListener()
        {
            for (i in fileList!!)
                i.isSelected = false
            selectedFiles.clear()
            adapter?.notifyDataSetChanged()
            setActionModeValue(false)
        }

        img_select_all?.setOnClickListener()
        {
            if (selectAll) {
                selectAll = false
                img_select_all?.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.img_deselect, null))
                selectedFiles?.clear()
                for (i in fileList!!)
                    i.isSelected = false
                adapter?.notifyDataSetChanged()
            } else {
                selectAll = true
                img_select_all?.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.ic_select, null))
                selectedFiles.clear()
                for (i in fileList!!) {
                    if(i.isFile)
                    {
                        i.isSelected = true
                        selectedFiles.add(i)
                    }
                }
                adapter?.notifyDataSetChanged()
            }
            setActionModeValue(selectAll)
        }

        recycler_view?.layoutManager = LinearLayoutManager(context)

        /*model?.myHiddenFiles?.observe(requireActivity())
        {
            if (it != null && it.children.size > 0) {
                selectedFiles.clear()
                setActionModeValue(false)
                fileData = it
                currentNode = it
                foldersList?.clear()
                foldersList?.add(it)
                img_title?.doVisible()
                tv_folder_title.text = getString(R.string.vault_file_manager)
               // tv_folder_name?.text = "Root Folder"
                val data = it.children
                fileList = ArrayList()
                for (i in data) {
                    fileList?.add(i.value)
                }
                if (adapter == null) {
                    adapter = HiddenFilesAdapter(fileList!!, recycler_view = recycler_view, context, this, this, FOLDER_TYPE_DEFAULT)
                    recycler_view?.adapter = adapter
                } else {
                    adapter?.currentType = FOLDER_TYPE_DEFAULT
                    adapter?.filesList = fileList!!
                    adapter?.notifyDataSetChanged()
                }
            }
        }*/

        model?.myVideosFolders?.observe(requireActivity())
        {
            if (it != null && it.isNotEmpty()) {
                videoFolders = it
            }
        }

        model?.myAudiosFolders?.observe(requireActivity())
        {
            if (it != null && it.isNotEmpty()) {
                audioFolders = it
            }
        }
        model?.myPhotosFolders?.observe(requireActivity())
        {
            if (it != null && it.isNotEmpty()) {
                photosFolders = it
            }
        }
        model?.myOthersFolders?.observe(requireActivity())
        {
            if (it != null && it.isNotEmpty()) {
                othersFolders = it
            }
        }
        model?.myDocumentsFolders?.observe(requireActivity())
        {
            if (it != null && it.isNotEmpty()) {
                documentFolders = it
            }
        }
    }

    private suspend fun getChildFiles() {
                val dir = File(currentPath!!)
                val newList = dir.listFiles()
                val newFolderList : ArrayList<FileDataClass> = ArrayList()
                val newFileList : ArrayList<FileDataClass> = ArrayList()
                /*newList?.sortedBy{ file : File -> file.lastModified() }?.reversed().let{
                    if(it!=null && it.isNotEmpty())
                        newList = it.toTypedArray()
                }*/
                if( newList != null)
                {
                    val hiddenFilesDao = HiddenFilesDatabase.getInstance(requireContext()).hiddenFilesDao
                    for(i in newList)
                    {
                        if(i.path == StorageUtils.getPasswordFilePath())
                            continue
                        if(i.isFile)
                        {
                            val fileName = StorageUtils.decode(i.name,StorageUtils.offset)
                            val ext =   fileName?.substringAfterLast(".")
                            val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext?.toLowerCase())
                            val data = FileDataClass(i.path,fileName, StorageUtils.format(i.length().toDouble(), 1), i.isFile, 0, mimeType, false,0)
                            data.updateTimeStamp = hiddenFilesDao.getUpdateTimeForFile(i.path)?:0
                            newFileList.add(data)
                        }
                        else
                        {
                            val count = i?.listFiles()?.let{it.count()}
                                if(count != null && count > 0)
                                    newFolderList.add(0,FileDataClass(i.path, i.name, null, i.isFile, count, "", false, 0))
                        }
                    }
                    fileList?.addAll(newFolderList)
                    if(newFileList.size>0)
                    {
                        newFileList.sortedBy { fileDataClass -> fileDataClass.updateTimeStamp }.reversed().let {
                            if(it.isNotEmpty())
                                fileList?.addAll(newFileList)
                        }
                    }
        }
    }

    private fun showUnhidePathDialog() {
        val view1 = layoutInflater.inflate(R.layout.unhide_path_dialog, null)
        var container : AlertDialog? = null
        val dialog = AlertDialog.Builder(context)
        val listType = currentPath?.substringAfterLast("/")
        when(listType)
        {
            "Audios" -> view1?.tv_unhide_path?.text = getString(R.string.unhide_path_prefix)+ PUBLIC_DIRECTORY_HIDERBACKUP_FOR_AUDIOS
            "Videos" -> view1?.tv_unhide_path?.text = getString(R.string.unhide_path_prefix)+ PUBLIC_DIRECTORY_HIDERBACKUP_FOR_VIDEOS
            "Photos" -> view1?.tv_unhide_path?.text = getString(R.string.unhide_path_prefix)+ PUBLIC_DIRECTORY_HIDERBACKUP_FOR_PHOTOS
            "Others" -> view1?.tv_unhide_path?.text = getString(R.string.unhide_path_prefix)+ PUBLIC_DIRECTORY_HIDERBACKUP_FOR_OTHERS
            "Documents" -> view1?.tv_unhide_path?.text = getString(R.string.unhide_path_prefix)+ PUBLIC_DIRECTORY_HIDERBACKUP_FOR_DOCUMENTS
        }
        view1?.tv_cancel_unhide?.setOnClickListener()
        {
            container?.dismiss()
        }
        view1?.tv_unhide?.setOnClickListener()
        {
            if(view1.path_to_unhide?.checkedRadioButtonId == R.id.path_default)
            {
                unHideSelectedFiles(false)
            }
            else if(view1.path_to_unhide?.checkedRadioButtonId == R.id.path_original)
            {
                if(selectedFiles.isNotEmpty())
                    unHideSelectedFiles(true)
                else
                    Toast.makeText(context,"No files selected",Toast.LENGTH_SHORT).show()
            }
            container?.dismiss()
        }
        dialog.setView(view1)
        container=dialog.show()
    }

    private fun shareSelectedFiles() {

        progressWheel = AppProgressDialog(requireContext())
        progressWheel?.show()
        launch {
            val operation = async(Dispatchers.IO) {

                listToRemove = ArrayList()
                listToAdd = ArrayList()
                    parseSelectedFiles(selectedFiles,true)
                selectedFiles.removeAll(listToRemove!!)
                selectedFiles.addAll(listToAdd!!)
                listToAdd = null
                listToRemove = null

                when {
                    selectedFiles.size == 0 -> Toast.makeText(context, getString(R.string.no_files_selected), Toast.LENGTH_SHORT).show()
                    selectedFiles.size > 8 -> Toast.makeText(context, getString(R.string.limit_share_items), Toast.LENGTH_SHORT).show()
                    else -> {
                        val uriList: ArrayList<Uri>? = ArrayList()
                        for (i in selectedFiles) {
                            val tmpFile = File(i.path.substringBeforeLast("/") + "/" + i.name)
                            val inputStream = FileInputStream(File(i.path))
                            val out = FileOutputStream(tmpFile, false)
                            val buff = ByteArray(1024)
                            var read = 0
                            try {
                                while (inputStream.read(buff).also { read = it } > 0) {
                                    out.write(buff, 0, read)
                                }

                            } finally {
                                inputStream.close()
                                out.close()
                            }

                            val uri: Uri = FileProvider.getUriForFile(requireContext(), "$APPLICATION_ID.provider", tmpFile.absoluteFile)
                            uriList?.add(uri)
                            tempPathList.add(tmpFile.absolutePath)
                        }
                        val share = Intent(Intent.ACTION_SEND_MULTIPLE)
                        share.type = "*/*"
                        share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        share.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uriList)
                        startActivityForResult(share, REQUEST_CODE_FOR_SHARE)
                    }
                }
            }
            operation.await()
            withContext(Dispatchers.Main)
            {
                progressWheel?.dismiss()
                progressWheel = null
                setActionModeValue(false)
            }
        }
    }

    private fun parseSelectedFiles(list: ArrayList<FileDataClass>, isRoot : Boolean) {
        for (i in list) {
            if (!i.isFile) {
                if(isRoot)
                listToRemove?.add(i)
                val data = File(i.path).listFiles()
                parseSelectedFiles(getHiddenDataListFromFileList(data),false)
            }
            else
            {
                listToAdd?.add(i)
            }
        }
    }

    private fun getHiddenDataFromFile(i: File) : FileDataClass{
        return if(i.isFile)
        {
            val fileName = StorageUtils.decode(i.name,StorageUtils.offset)
            val ext =   fileName?.substringAfterLast(".")
            val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext?.toLowerCase())
            FileDataClass(i.path, fileName, StorageUtils.format(i.length().toDouble(), 1), i.isFile, 0, mimeType, false, 0)
        }
        else
        {
            val count = i?.listFiles()?.let{it.count()}
            FileDataClass(i.path, i.name, null, i.isFile, count, "", false, 0)
        }
    }

    private fun getHiddenDataListFromFileList(list: Array<File>) : ArrayList<FileDataClass>{
        val listToReturn : ArrayList<FileDataClass> = ArrayList()
        for(file in list)
        {
            val data: FileDataClass? = getHiddenDataFromFile(file)
            listToReturn.add(data!!)
        }
        return listToReturn
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_FOR_SHARE) {
            for (i in tempPathList) {
                if (File(i).exists())
                    File(i).delete()
            }
            tempPathList.clear()
            for (i in selectedFiles)
                i.isSelected = false
            selectedFiles.clear()
            adapter?.filesList = fileList!!
            adapter?.notifyDataSetChanged()
        }
    }

    private fun showConfirmationDialog() {
        val view1 = layoutInflater.inflate(R.layout.delete_confirmation_dialog, null)
        var container: AlertDialog? = null
        val dialog = AlertDialog.Builder(context)
        view1?.tv_cancel_delete?.setOnClickListener()
        {
            container?.dismiss()
        }
        view1?.tv_confirm?.setOnClickListener()
        {
            if (view1?.action_to_delete?.checkedRadioButtonId == R.id.action_unhide) {
                unHideSelectedFiles(true)
            } else if (view1?.action_to_delete?.checkedRadioButtonId == R.id.action_delete) {
                if (selectedFiles.isNotEmpty())
                    deleteSelectedFiles()
                else
                    Toast.makeText(context, "No files selected", Toast.LENGTH_SHORT).show()
            }
            container?.dismiss()
        }
        dialog.setView(view1)
        container = dialog.show()
    }

    private fun deleteSelectedFiles() {
        if (mediaScanner == null)
            mediaScanner = MediaScanner(context)
        launch {
            val operation = async(Dispatchers.IO) {
                for (i in selectedFiles) {
                    File(i.path).delete()
                    val hiddenFilesDatabase: HiddenFilesDatabase? = HiddenFilesDatabase.getInstance(requireContext())
                    hiddenFilesDatabase?.hiddenFilesDao?.deleteFile(i.path)
                }
            }
            operation.await()
            withContext(Dispatchers.Main)
            {
                fileList?.removeAll(selectedFiles)
                adapter?.filesList = fileList!!
                adapter?.notifyDataSetChanged()
                Toast.makeText(context, selectedFiles.size.toString() + " files deleted", Toast.LENGTH_SHORT).show()
                selectedFiles.clear()
                adapter?.notifyDataSetChanged()
                setActionModeValue(false)
            }
        }
    }


    private fun moveSelectedFilesToFolder() {
        var defaultFolder = ""
        when (currentType) {
            FOLDER_TYPE_AUDIOS -> {
                myFolders = audioFolders
                defaultFolder = StorageUtils.getAudiosHiderDirectory()
            }
            FOLDER_TYPE_VIDEOS -> {
                myFolders = videoFolders
                defaultFolder = StorageUtils.getVideosHiderDirectory()
            }
            FOLDER_TYPE_PHOTOS -> {
                myFolders = photosFolders
                defaultFolder = StorageUtils.getPhotosHiderDirectory()
            }
            FOLDER_TYPE_DOCUMENTS -> {
                myFolders = documentFolders
                defaultFolder = StorageUtils.getDocumentsHiderDirectory()
            }
            FOLDER_TYPE_OTHERS -> {
                myFolders = othersFolders
                defaultFolder = StorageUtils.getOthersHiderDirectory()
            }
        }
        myFolders?.remove(currentPath)
        bottomAdapter = BottomViewFoldersAdapter(defaultFolder,myFolders, this)
        val dialogView: View = layoutInflater.inflate(R.layout.folder_bottom_sheet, null)
        dialogView.rv_folders_to_hide?.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        dialogView.rv_folders_to_hide?.adapter = bottomAdapter
        dialogView.img_cross?.setOnClickListener()
        {
            dismissDialog()
        }
        dialog = BottomSheetDialog(requireContext())
        dialog?.setContentView(dialogView)
        dialog?.setCanceledOnTouchOutside(true)
        dialog?.show()
    }


    private fun unHideSelectedFiles(isOriginalPath: Boolean) {
        if (mediaScanner == null)
            mediaScanner = MediaScanner(context)
        launch {
            val operation = async(Dispatchers.IO) {
                for (i in selectedFiles) {
                    var originalPath: String? = ""
                    if (HiddenFilesDatabase.getInstance(requireActivity()).hiddenFilesDao.isFileExists(i.path)?:false) {
                        originalPath = HiddenFilesDatabase.getInstance(requireActivity()).hiddenFilesDao.getOriginalPathForFile(i.path!!)
                        HiddenFilesDatabase.getInstance(requireActivity()).hiddenFilesDao.deleteFile(i.path)
                    }
                    if (originalPath == null || originalPath == "" || !isOriginalPath)
                        originalPath = StorageUtils.getDefaultDirectoryForType(currentType) + "/" + i.name
                    val moved = StorageUtils.move(i.path, originalPath)
                    if (moved)
                        mediaScanner?.scan(originalPath)
                }

            }
            operation.await()
            withContext(Dispatchers.Main)
            {
                fileList?.removeAll(selectedFiles)
                selectedFiles.clear()
                adapter?.filesList = fileList!!
                adapter?.notifyDataSetChanged()
                setActionModeValue(false)
            }
        }
    }


    override fun onFileDeselected(fileDataClass: FileDataClass) {
        selectedFiles.remove(fileDataClass)
        if (selectedFiles.size == 0) {
            setActionModeValue(false)
        }
        img_select_all?.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.img_deselect, null))
    }

    override fun setDirectoryToFolder(path: String?, position: Int?) {
        launch {
            val operation = async(Dispatchers.IO) {
                val databaseDao = HiddenFilesDatabase.getInstance(requireContext()).hiddenFilesDao
                for (i in selectedFiles) {
                    val newFileDestination = path + "/" + File(i.path).name
                    StorageUtils.move(i.path, newFileDestination)
                    databaseDao.updateFilePath(i.path, newFileDestination, System.currentTimeMillis())
                }
                selectedFiles.clear()
                currentPath = path
            }
            operation.await()
            withContext(Dispatchers.Main)
            {
                dismissDialog()
                refreshData()
                setActionModeValue(false)
                Toast.makeText(context, "files moved successfully", Toast.LENGTH_SHORT).show()
            }
        }
    }


    override fun dismissDialog() {
        dialog?.dismiss()
    }

    override fun getSelectedFolder(): String? {
        return null
    }

    override fun setDirectoryToDefault() {
        launch {
            val newPath = when (currentType) {
                FOLDER_TYPE_AUDIOS -> StorageUtils.getAudiosHiderDirectory()
                FOLDER_TYPE_VIDEOS -> StorageUtils.getVideosHiderDirectory()
                FOLDER_TYPE_PHOTOS -> StorageUtils.getPhotosHiderDirectory()
                FOLDER_TYPE_DOCUMENTS -> StorageUtils.getDocumentsHiderDirectory()
                FOLDER_TYPE_OTHERS -> StorageUtils.getOthersHiderDirectory()
                else -> return@launch
            }
            val operation = async(Dispatchers.IO)
            {
                val databaseDao = HiddenFilesDatabase.getInstance(requireContext()).hiddenFilesDao
                for (i in selectedFiles) {
                    val newFileDestination = newPath + "/" + File(i.path).name
                    StorageUtils.move(i.path, newFileDestination)
                    databaseDao.updateFilePath(i.path, newFileDestination, System.currentTimeMillis())
                }
                selectedFiles.clear()
                currentPath = newPath
            }
            operation.await()
            withContext(Dispatchers.Main)
            {
                dismissDialog()
                refreshData()
                setActionModeValue(false)
                Toast.makeText(context, "files moved successfully", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun showNewFolderDialog() {
        val view1 = layoutInflater.inflate(R.layout.new_folder_dialog, null)
        var container: AlertDialog? = null
        val dialog = AlertDialog.Builder(context)
        view1?.tv_ok?.setOnClickListener()
        {
            val newPath = when (currentType) {
                FOLDER_TYPE_AUDIOS -> StorageUtils.getAudiosHiderDirectory()
                FOLDER_TYPE_VIDEOS -> StorageUtils.getVideosHiderDirectory()
                FOLDER_TYPE_PHOTOS -> StorageUtils.getPhotosHiderDirectory()
                FOLDER_TYPE_DOCUMENTS -> StorageUtils.getDocumentsHiderDirectory()
                FOLDER_TYPE_OTHERS -> StorageUtils.getOthersHiderDirectory()
                else -> StorageUtils.getHiderDirectory().path
            }
            val temp: File? = File(newPath + "/" + view1.folder_name?.text?.toString())
            if (temp?.exists()!!) {
                view1.tv_already_exists?.visibility = View.VISIBLE
            } else {
                temp.mkdir()
                if (myFolders == null) {
                    myFolders = ArrayList()
                }
                myFolders?.add(temp.path)
                setFolderList(myFolders)
                container?.dismiss()
                bottomAdapter?.folders = myFolders
                bottomAdapter?.notifyDataSetChanged()
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

    private fun setFolderList(myFolders: java.util.ArrayList<String>?) {
        when (currentType) {
            FOLDER_TYPE_AUDIOS -> audioFolders = myFolders
            FOLDER_TYPE_VIDEOS -> videoFolders = myFolders
            FOLDER_TYPE_PHOTOS -> photosFolders = myFolders
            FOLDER_TYPE_DOCUMENTS -> documentFolders = myFolders
            FOLDER_TYPE_OTHERS -> othersFolders = myFolders
        }

    }

    override fun onFileSelected(fileDataClass: FileDataClass) {
        selectedFiles?.add(fileDataClass)
        if(selectedFiles.size == fileList?.size)
        {
            selectAll = true
            img_select_all?.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.ic_select, null))
        }
        bottom_view?.doVisible()
        btn_upload?.doGone()
        rl_select_all?.doVisible()
    }

    override fun onFolderClicked(fileDataClass: FileDataClass) {
        doExit = false
        fileList?.clear()
        btn_upload?.doVisible()
        currentPath = fileDataClass.path
            launch {
                val operation = async(Dispatchers.IO)
                {
                    getChildFiles()
                }
                operation.await()
                withContext(Dispatchers.Main)
                {
                    setType()
                    img_title?.doGone()
                    tv_folder_title.text = fileDataClass.name
                    adapter?.filesList = fileList!!
                    adapter?.notifyDataSetChanged()
                }
            }
       /* val treeNode = foldersList?.get(foldersList?.size!! - 1)
        var data = treeNode?.children
        var nextRoot: TreeNode<FileDataClass>? = null
        for (i in data!!) {
            if (i.value == fileDataClass) {
                nextRoot = i
                break
            }
        }
        data = nextRoot?.children!!
        fileList?.clear()
        for (i in data) {
            fileList?.add(i.value)
        }
        if (foldersList?.size == 1) {
            when (nextRoot.value.name) {
                "Photos" -> {
                    currentType = FOLDER_TYPE_PHOTOS
                    adapter?.currentType = FOLDER_TYPE_PHOTOS
                }
                "Audios" -> {
                    currentType = FOLDER_TYPE_AUDIOS
                    adapter?.currentType = FOLDER_TYPE_AUDIOS
                }
                "Videos" -> {
                    currentType = FOLDER_TYPE_VIDEOS
                    adapter?.currentType = FOLDER_TYPE_VIDEOS
                }
                "Documents" -> {
                    currentType = FOLDER_TYPE_DOCUMENTS
                    adapter?.currentType = FOLDER_TYPE_DOCUMENTS
                }
                else -> {
                    currentType = FOLDER_TYPE_OTHERS
                    adapter?.currentType = FOLDER_TYPE_OTHERS
                }
            }
        }
        foldersList?.add(nextRoot)
        currentNode = nextRoot*/

       /* img_title?.doGone()
        tv_folder_title.text = currentNode?.value?.name*/
      //  tv_folder_name?.text = currentNode?.value?.name
       /* adapter?.filesList = fileList!!
        adapter?.notifyDataSetChanged()*/
    }

    private fun setType() {
        currentType = when
                        {
                            currentPath?.startsWith(StorageUtils.getPhotosHiderDirectory())!! -> FOLDER_TYPE_PHOTOS
                            currentPath?.startsWith(StorageUtils.getVideosHiderDirectory())!! -> FOLDER_TYPE_VIDEOS
                            currentPath?.startsWith(StorageUtils.getAudiosHiderDirectory())!! -> FOLDER_TYPE_AUDIOS
                            currentPath?.startsWith(StorageUtils.getDocumentsHiderDirectory())!! -> FOLDER_TYPE_DOCUMENTS
                            currentPath?.startsWith(StorageUtils.getOthersHiderDirectory())!! -> FOLDER_TYPE_OTHERS
                            else -> FOLDER_TYPE_DEFAULT
                        }
    }

    override fun onFileClicked(listOfFiles: ArrayList<FileDataClass>, position: Int) {
        val tempList : ArrayList<FileDataClass> = ArrayList()
        var count = 0
        for(i in listOfFiles)
        {
            if(i.isFile)
            tempList.add(i)
            else
                count++
        }
        var tempPosition : Int = position-count
        if(tempPosition<0)
                tempPosition = 0
        (activity as FilemanagerActivity).viewFile(tempList,tempPosition)
    }


    override fun getActionMode(): Boolean {
        return isActionMode
    }

    override fun setActionModeValue(actionMode: Boolean) {
        isActionMode = actionMode
        if (!actionMode) {
            selectAll = false
            img_select_all?.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.img_deselect, null))
            rl_select_all?.doGone()
            btn_upload?.doVisible()
            bottom_view?.doGone()
        }
    }

    fun onPressedBack() {

        if(selectedFiles.isNotEmpty())
        {
            for (i in fileList!!) {
                if (i.isSelected)
                    i.isSelected = false
            }
            selectedFiles.clear()
            setActionModeValue(false)
            adapter?.notifyDataSetChanged()
            selectAll = false
            img_select_all?.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.img_deselect, null))
        }
        else
        {
            fileList?.clear()
            val parentFile = File(currentPath!!).parentFile
            var newList = parentFile?.listFiles()
            newList?.sortedBy{ file : File -> file.lastModified() }?.reversed().let{
                if(it!=null && it.isNotEmpty())
                    newList = it.toTypedArray()
            }
            if(newList!=null)
            {
                for(i in newList!!)
                {
                    if(i.path == StorageUtils.getPasswordFilePath())
                        continue
                        if(i.isFile)
                        {
                            val filename = StorageUtils.decode(i.name,StorageUtils.offset)
                            val ext =   filename?.substringAfterLast(".")
                            val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext?.toLowerCase())
                            fileList?.add(FileDataClass(i.path, filename, StorageUtils.format(i.length().toDouble(), 1), i.isFile, 0, mimeType, false, 0))
                        }
                        else
                        {
                            val count = i?.listFiles()?.let{it.count()}
                            if(count!=null && count>0)
                            fileList?.add(FileDataClass(i.path, i.name, null, i.isFile, count, "", false, 0))
                        }
                }
            }
            currentPath = parentFile?.path!!
            if(currentPath == StorageUtils.getHiderDirectory().absolutePath)
            {
                doExit = true
                img_title?.doVisible()
                tv_folder_title.text = getString(R.string.vault_file_manager)
            }
            else
            {
                img_title?.doGone()
                tv_folder_title.text = parentFile.name
            }
            selectedFiles.clear()
            setActionModeValue(false)
            adapter?.filesList = fileList!!
            adapter?.notifyDataSetChanged()
            setType()
        }
        if(currentPath == StorageUtils.getHiderDirectory().absolutePath)
            btn_upload?.doGone()
        else
            btn_upload?.doVisible()
    }

    fun refreshData() {
        doExit = false
        fileList?.clear()
        btn_upload?.doVisible()
        when(currentType)
        {
            FOLDER_TYPE_AUDIOS -> model?.getMyAudiosFolders()
            FOLDER_TYPE_VIDEOS -> model?.getMyVideosFolders()
            FOLDER_TYPE_PHOTOS -> model?.getMyPhotosFolders()
            FOLDER_TYPE_DOCUMENTS -> model?.getMyDocumentsFolders()
            FOLDER_TYPE_OTHERS -> model?.getMyOthersFolders()
        }
        launch {
            val operation = async(Dispatchers.IO)
            {
                getChildFiles()
            }
            operation.await()
            withContext(Dispatchers.Main)
            {
                img_title?.doGone()
                tv_folder_title.text = File(currentPath!!).name
                adapter?.filesList = fileList!!
                adapter?.notifyDataSetChanged()
            }
        }
    }

}