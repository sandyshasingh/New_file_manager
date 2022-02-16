package com.editor.hiderx.fragments

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.editor.hiderx.*
import com.editor.hiderx.activity.CameraFolderActivity
import com.editor.hiderx.adapters.*
import com.editor.hiderx.database.HiddenFiles
import com.editor.hiderx.database.HiddenFilesDatabase
import com.editor.hiderx.dataclass.FileDataClass
import com.editor.hiderx.dataclass.SimpleDataClass
import com.editor.hiderx.listeners.*
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.bottom_layout_hidden_items.*
import kotlinx.android.synthetic.main.delete_confirmation_dialog.view.*
import kotlinx.android.synthetic.main.folder_bottom_sheet.view.*
import kotlinx.android.synthetic.main.fragment_placeholder.*
import kotlinx.android.synthetic.main.fragment_placeholder.bottom_view
import kotlinx.android.synthetic.main.fragment_placeholder.btn_upload
import kotlinx.android.synthetic.main.new_folder_dialog.view.*
import kotlinx.android.synthetic.main.unhide_path_dialog.view.*
import kotlinx.coroutines.*
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream

const val REQUEST_CODE_TO_SHARE_VIDEO = 123
const val REQUEST_CODE_TO_SHARE_IMAGES = 321
const val REQUEST_CODE_TO_SHARE_VIDEO_FROM_FOLDERS = 456
const val REQUEST_CODE_TO_SHARE_IMAGES_FROM_FOLDERS = 654
const val LIST_TYPE_PHOTOS = 0
const val LIST_TYPE_VIDEOS = 1
const val LIST_TYPE_CAMERA_PHOTOS = 2
const val LIST_TYPE_CAMERA_VIDEOS = 3


class PlaceholderFragment : Fragment(), OnImageSelectionListener, OnVideoSelectedListener, FragmentInteractionListener, OnFolderClickListener, OnItemSelectedListener, CoroutineScope by MainScope(), ActionModeListener {

    var currentFolderData : FileDataClass? = null
    var currentPath : String = ""
    var progressWheel: AppProgressDialog? = null
    private val tempPathList: ArrayList<String> = ArrayList()
    private var adapter: AdapterForPhotosOrVideos? = null
    private var bottomAdapter: BottomViewFoldersAdapter? = null
    private var dialog: BottomSheetDialog? = null
    private var photosList: ArrayList<HiddenFiles> = ArrayList()
    private var videosList: ArrayList<HiddenFiles> = ArrayList()
    private var folderChildList: ArrayList<SimpleDataClass>? = ArrayList()
    var selectedImages: ArrayList<HiddenFiles> = ArrayList()
    var selectedVideos: ArrayList<HiddenFiles> = ArrayList()
    private var photosAdapter: HiddenPhotosAdapter? = null
    private var videosAdapter: HiddenVideosAdapter? = null
    private var folderAdapter: CameraFolderAdapter? = null
    var onUploadClickListener: onUploadClickListenerForCamera? = null
    private var pagerPosition: Int? = null
    private var model: DataViewModel? = null
    var myFolders: ArrayList<String>? = null
    var videoFolders: ArrayList<String>? = null
    var photoFolders: ArrayList<String>? = null
    var mediaScanner: MediaScanner? = null
    var folderData: HashMap<FileDataClass, ArrayList<SimpleDataClass>>? = null
    var folderList: ArrayList<FileDataClass>? = null
    var isVideo: Boolean? = null
    var isActionMode: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            pagerPosition = it.getInt(HiderUtils.PAGER_POSITION)
        }
        model = ViewModelProvider(requireActivity()).get(DataViewModel::class.java)
        when (pagerPosition) {
            0 -> {
                model?.getHiddenPhotos(requireContext())
                model?.getMyPhotosFolders()
            }
            1 -> {
                model?.getHiddenVideos(requireContext())
                model?.getMyVideosFolders()
            }
            2 -> {
                model?.getCameraFolders()
                model?.getMyPhotosFolders()
                model?.getMyVideosFolders()
            }
        }
    }



    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_placeholder, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        when (pagerPosition) {
            0 -> {
                initializePhotosView()
                isVideo = false
            }
            1 -> {
                initializeVideosView()
                isVideo  = true
            }
            2 -> {
                initializeFoldersView()
            }
        }
        ll_move?.setOnClickListener()
        {
            when (pagerPosition) {
                0 -> bottomAdapter = BottomViewFoldersAdapter(StorageUtils.getPhotosHiderDirectory(), myFolders, this)
                1 -> bottomAdapter = BottomViewFoldersAdapter(StorageUtils.getVideosHiderDirectory(), myFolders, this)
                2 -> {
                    bottomAdapter = if (isVideo!!)
                    {
                        videoFolders?.remove(currentFolderData?.path)
                        BottomViewFoldersAdapter(StorageUtils.getVideosHiderDirectory(), videoFolders, this)
                    }
                    else
                    {
                        photoFolders?.remove(currentFolderData?.path)
                        BottomViewFoldersAdapter(StorageUtils.getPhotosHiderDirectory(), photoFolders, this)
                    }
                }
            }
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
        ll_delete?.setOnClickListener()
        {
            showConfirmationDialog()
        }
    }

    /* private fun selectAllVideos() {
         if(selectAll)
         {
             selectAll = false
             //img_select_all?.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.img_deselect, null))
             selectedVideos.clear()
             for(i in videosList)
                 i.isSelected = false
             videosAdapter?.notifyDataSetChanged()
         }
         else
         {
             selectAll = trueREQUEST_CODE_TO_SHARE_VIDEO
             //img_select_all?.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.ic_select, null))
             selectedVideos.clear()
             for(i in videosList)
             {
                 i.isSelected = true
             }
             selectedVideos.addAll(videosList)
             videosAdapter?.notifyDataSetChanged()
         }
     }

     private fun selectAllPhotos() {
         if(selectAll)
         {
             selectAll = false
             //img_select_all?.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.img_deselect, null))
             selectedImages.clear()
             for(i in photosList)
                 i.isSelected = false
             photosAdapter?.notifyDataSetChanged()
         }
         else
         {
             selectAll = true
             //img_select_all?.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.ic_select, null))
             selectedImages.clear()
             for(i in photosList)
             {
                 i.isSelected = true
             }
             selectedImages.addAll(photosList)
             photosAdapter?.notifyDataSetChanged()
         }
     }
 */

    private fun initializeFoldersView() {
        ll_parent_dir?.doVisible()
        btn_upload?.doGone()
        btn_upload?.setOnClickListener()
        {
            if (isVideo!!)
                onUploadClickListener?.onUploadVideoClicked(currentPath)
            else
                onUploadClickListener?.onUploadPhotoClicked(currentPath)
        }
        ll_parent_dir?.setOnClickListener()
        {
            ll_child_dir?.doGone()
            rv_photos_videos?.doGone()
            rv_camera_folders?.doVisible()
            isVideo = null
            rv_camera_folders?.layoutManager = LinearLayoutManager(context)
            folderAdapter = CameraFolderAdapter(folderList, this)
            rv_camera_folders?.adapter = folderAdapter
            selectedVideos.clear()
            selectedImages.clear()
            if (folderChildList != null) {
                for (i in folderChildList!!) {
                    i.isSelected = false
                }
            }
            setActionModeValue(false)
        }
        rv_camera_folders?.layoutManager = LinearLayoutManager(context)
        folderAdapter = CameraFolderAdapter(folderList, this)
        rv_camera_folders?.adapter = folderAdapter
        model?.myCameraFolders?.observe(requireActivity())
        {
            if (it != null && it.isNotEmpty()) {
                folderData = it
                val folders = it.keys.toTypedArray()
                if (folderList != null)
                    folderList?.clear()
                else
                    folderList = ArrayList()
                for (i in folders) {
                    folderList?.add(i)
                }
                folderAdapter?.folderList = folderList
                folderAdapter?.notifyDataSetChanged()
                ll_child_dir?.doGone()
                rv_photos_videos?.doGone()
                rv_camera_folders?.doVisible()
                isVideo = null
                selectedVideos.clear()
                selectedImages.clear()
                bottom_view?.doGone()
                btn_upload?.doGone()
            }
        }

        model?.myPhotosFolders?.observe(requireActivity())
        {
            photoFolders = it
        }

        model?.myVideosFolders?.observe(requireActivity())
        {
            videoFolders = it
        }


        img_cross?.setOnClickListener()
        {
            cancelActionModeForFolders()
        }

        ll_unhide?.setOnClickListener()
        {
            if (isVideo!!)
                    showUnhidePathDialog(LIST_TYPE_CAMERA_VIDEOS)
            else
                    showUnhidePathDialog(LIST_TYPE_CAMERA_PHOTOS)
        }

        ll_share?.setOnClickListener()
        {
            (activity as CameraFolderActivity).backPressed = true
            if (isVideo!!)
                shareSelectedVideos(true)
            else
                shareSelectedPhotos(true)
        }

    }

    fun cancelActionModeForFolders() {
        for (i in folderChildList!!)
            i.isSelected = false
        if (isVideo!!)
            selectedVideos.clear()
        else
            selectedImages.clear()
        adapter?.notifyDataSetChanged()
        setActionModeValue(false)
    }


    private fun unHideSelectedPhotosForCamera(isOriginalPath: Boolean) {
        if (selectedImages.isNotEmpty()) {
            if (mediaScanner == null)
                mediaScanner = MediaScanner(context)
            launch {
                val operation = async(Dispatchers.IO) {
                    val externalStoragePublic: File? = StorageUtils.getPublicAlbumStorageDirForPhotos()
                    val hiddenFilesDatabase: HiddenFilesDatabase? = HiddenFilesDatabase.getInstance(requireContext())
                    for (i in selectedImages) {
                        var moved = false
                        var newExternalPath: String? = null
                        newExternalPath = if (hiddenFilesDatabase == null || i.originalPath == "") {
                            externalStoragePublic?.path + "/" + StorageUtils.decode(i.name!!, StorageUtils.offset)
                        } else {
                            i.originalPath
                        }
                        hiddenFilesDatabase?.hiddenFilesDao?.deleteFile(i.path)
                        moved = StorageUtils.move(i.path, newExternalPath!!)
                        if (moved)
                            mediaScanner?.scan(newExternalPath)
                    }
                }
                operation.await()
                withContext(Dispatchers.Main)
                {
                    for (i in selectedImages) {
                        folderChildList?.remove(SimpleDataClass(i.path, i.name, true))
                    }
                    val list = File(currentFolderData?.path).listFiles()
                    val count = list?.size
                    if(count!=null)
                    {
                        var size = 0L
                        for(i in list)
                        {
                            if(i.isFile && i.length()>0)
                            {
                                size += i.length()
                            }
                        }
                        currentFolderData?.size = StorageUtils.format(size.toDouble(),1)
                        currentFolderData?.noOfItems = count
                    }
                    selectedImages.clear()
                    adapter?.itemsList = folderChildList!!
                    currentFolderData?.noOfItems = folderChildList?.size!!
                    adapter?.notifyDataSetChanged()
                    setActionModeValue(false)
                    //rl_select_all?.doGone()
                }
            }
        } else {
            Toast.makeText(context, "No files selected", Toast.LENGTH_SHORT).show()
        }
    }

    private fun unHideSelectedVideosForCamera(isOriginalPath: Boolean) {
        if (selectedVideos.isNotEmpty()) {
            if (mediaScanner == null)
                mediaScanner = MediaScanner(context)
            launch {
                val operation = async(Dispatchers.IO) {
                    val externalStoragePublic: File? = StorageUtils.getPublicAlbumStorageDirForVideos()
                    val hiddenFilesDatabase: HiddenFilesDatabase? = HiddenFilesDatabase.getInstance(requireContext())
                    for (i in selectedVideos) {
                        var moved = false
                        var newExternalPath: String? = null
                        newExternalPath = if (hiddenFilesDatabase == null || i.originalPath == "") {
                            externalStoragePublic?.path + "/" + StorageUtils.decode(i.name!!, StorageUtils.offset)
                        } else {
                            i.originalPath
                        }
                        hiddenFilesDatabase?.hiddenFilesDao?.deleteFile(i.path)
                        moved = StorageUtils.move(i.path!!, newExternalPath!!)
                        if (moved)
                            mediaScanner?.scan(newExternalPath)
                    }
                }
                operation.await()
                withContext(Dispatchers.Main)
                {
                    for (i in selectedVideos) {
                        folderChildList?.remove(SimpleDataClass(i.path, i.name!!, true))
                    }
                    val list = File(currentFolderData?.path).listFiles()
                    val count = list?.size
                    if(count!=null)
                    {
                        var size = 0L
                        for(i in list)
                        {
                            if(i.isFile && i.length()>0)
                            {
                                size += i.length()
                            }
                        }
                        currentFolderData?.size = StorageUtils.format(size.toDouble(),1)
                        currentFolderData?.noOfItems = count
                    }
                    selectedVideos.clear()
                    adapter?.itemsList = folderChildList!!
                    adapter?.notifyDataSetChanged()
                    setActionModeValue(false)
                    //rl_select_all?.doGone()
                }
            }
        } else {
            Toast.makeText(context, "No files selected", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initializeVideosView() {
        folder_indicator?.doGone()
        rv_camera_folders?.layoutManager = GridLayoutManager(
                context,
                2)
        videosAdapter = HiddenVideosAdapter(videosList, rv_camera_folders, context, this, this)
        rv_camera_folders?.adapter = videosAdapter
        model?.hiddenVideos?.observe(requireActivity())
        {
            if (it != null && it.isNotEmpty()) {
                videosList = it as ArrayList<HiddenFiles>
                videosAdapter?.videosList = it
                videosAdapter?.notifyDataSetChanged()
            }
        }
        btn_upload?.setOnClickListener()
        {
            onUploadClickListener?.onUploadVideoClicked(StorageUtils.getVideosHiderDirectory())
        }
        model?.myVideosFolders?.observe(requireActivity())
        {
            myFolders = it
        }
        img_cross?.setOnClickListener()
        {
            cancelActionModeForVideos()
        }

        ll_unhide?.setOnClickListener()
        {
            showUnhidePathDialog(LIST_TYPE_VIDEOS)
        }

        ll_share?.setOnClickListener()
        {
            (activity as CameraFolderActivity).backPressed = true
            shareSelectedVideos(false)
        }

    }

    fun cancelActionModeForVideos() {
        for (i in videosList)
            i.isSelected = false
        selectedVideos.clear()
        //rl_select_all?.doGone()
        videosAdapter?.notifyDataSetChanged()
        setActionModeValue(false)
    }

    private fun shareSelectedVideos(fromFolders: Boolean) {
        if (selectedVideos.isNotEmpty()) {
            if (selectedVideos.size <= 8) {
                launch {
                    progressWheel = AppProgressDialog(requireContext())
                    progressWheel?.show()
                    val operation = async(Dispatchers.IO) {
                        val uriList: ArrayList<Uri>? = ArrayList()
                        for (i in selectedVideos) {
                            val tmpFile: File = if (fromFolders)
                                File(i.path?.substringBeforeLast("/") + "/" + StorageUtils.decode(i.name!!, StorageUtils.offset))
                            else
                                File(i.path?.substringBeforeLast("/") + "/" +i.name!!)
                            val inputStream: InputStream = FileInputStream(File(i.path))
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
                        share.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uriList)
                        share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        if(fromFolders)
                            startActivityForResult(share, REQUEST_CODE_TO_SHARE_VIDEO_FROM_FOLDERS)
                        else
                        startActivityForResult(share, REQUEST_CODE_TO_SHARE_VIDEO)
                    }
                    operation.await()
                    withContext(Dispatchers.Main)
                    {
                        progressWheel?.dismiss()
                        progressWheel = null
                        setActionModeValue(false)
                        //rl_select_all?.doGone()
                    }
                }
            } else {
                Toast.makeText(requireContext(), "you can send upto 8 files at a time", Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(requireContext(), "no files selected", Toast.LENGTH_LONG).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        deleteTemporaryfiles()

        when(requestCode)
        {
            REQUEST_CODE_TO_SHARE_VIDEO ->
            {
                for (i in selectedVideos) {
                    i.isSelected = false
                }
                selectedVideos.clear()
                videosAdapter?.videosList = videosList
                videosAdapter?.notifyDataSetChanged()
            }
            REQUEST_CODE_TO_SHARE_IMAGES ->
            {
                for (i in selectedImages) {
                    i.isSelected = false
                }
                selectedImages.clear()
                photosAdapter?.photosList = photosList
                photosAdapter?.notifyDataSetChanged()
            }
            REQUEST_CODE_TO_SHARE_IMAGES_FROM_FOLDERS ->
            {
                for (i in selectedImages) {
                    i.isSelected = false
                }
                selectedImages.clear()
                for(i in folderChildList!!)
                    i.isSelected = false
                adapter?.notifyDataSetChanged()
            }
            REQUEST_CODE_TO_SHARE_VIDEO_FROM_FOLDERS ->
            {
                for (i in selectedVideos) {
                    i.isSelected = false
                }
                selectedVideos.clear()
                for(i in folderChildList!!)
                    i.isSelected = false
                adapter?.notifyDataSetChanged()
            }
        }
    }

    private fun deleteTemporaryfiles() {
        for (i in tempPathList) {
            if (File(i).exists()) {
                File(i).delete()
            }
        }
        tempPathList.clear()
    }

    private fun unHideSelectedVideos(isOriginalPath: Boolean) {
        if (selectedVideos.isNotEmpty()) {
            if (mediaScanner == null)
                mediaScanner = MediaScanner(context)
            launch {
                val operation = async(Dispatchers.IO) {
                    val externalStoragePublic: File? = StorageUtils.getPublicAlbumStorageDirForVideos()
                    val hiddenFilesDatabase: HiddenFilesDatabase? = HiddenFilesDatabase.getInstance(requireContext())
                    for (i in selectedVideos) {
                        var moved = false
                        var newExternalPath: String? = null
                        newExternalPath = if (hiddenFilesDatabase == null || i.originalPath == "") {
                            externalStoragePublic?.path + "/" + StorageUtils.decode(i.name!!, StorageUtils.offset)
                        } else {
                            i.originalPath
                        }
                        hiddenFilesDatabase?.hiddenFilesDao?.deleteFile(i.path)
                        moved = StorageUtils.move(i.path!!, newExternalPath!!)
                        if (moved)
                            mediaScanner?.scan(newExternalPath)
                    }
                }
                operation.await()
                withContext(Dispatchers.Main)
                {
                    for (i in selectedVideos) {
                        videosList.remove(i)
                    }
                    selectedVideos.clear()
                    videosAdapter?.videosList = videosList
                    videosAdapter?.notifyDataSetChanged()
                    setActionModeValue(false)
                    //rl_select_all?.doGone()
                }
            }
            model?.getAllVideos()
        } else {
            Toast.makeText(context, "No files selected", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initializePhotosView() {
        folder_indicator?.doGone()
        rv_camera_folders?.layoutManager = GridLayoutManager(
                context,
                2)
        photosAdapter = HiddenPhotosAdapter(photosList, rv_camera_folders, context, this, this)
        rv_camera_folders?.adapter = photosAdapter
        model?.hiddenPhotos?.observe(requireActivity())
        {
            if (it != null && it.isNotEmpty()) {
                photosList = it as ArrayList<HiddenFiles>
                photosAdapter?.photosList = it
                photosAdapter?.notifyDataSetChanged()
            }
        }
        btn_upload?.setOnClickListener()
        {
            onUploadClickListener?.onUploadPhotoClicked(StorageUtils.getPhotosHiderDirectory())
        }
        model?.myPhotosFolders?.observe(requireActivity())
        {
            myFolders = it
        }
        img_cross?.setOnClickListener()
        {
            cancelActionModeForPhotos()
        }

        ll_unhide?.setOnClickListener()
        {
            showUnhidePathDialog(LIST_TYPE_PHOTOS)
        }

        ll_share?.setOnClickListener()
        {
            (activity as CameraFolderActivity).backPressed = true
            shareSelectedPhotos(false)
        }
    }

     fun cancelActionModeForPhotos() {
        for (i in photosList)
            i.isSelected = false
        selectedImages.clear()
        photosAdapter?.notifyDataSetChanged()
         setActionModeValue(false)
     }

    private fun showUnhidePathDialog(listType : Int) {
        val view1 = layoutInflater.inflate(R.layout.unhide_path_dialog, null)
        var container : AlertDialog? = null
        val dialog = AlertDialog.Builder(context)
        when (listType) {
            LIST_TYPE_PHOTOS, LIST_TYPE_CAMERA_PHOTOS -> view1?.tv_unhide_path?.text = getString(R.string.unhide_path_prefix)+PUBLIC_DIRECTORY_HIDERBACKUP_FOR_PHOTOS
            LIST_TYPE_VIDEOS, LIST_TYPE_CAMERA_VIDEOS -> view1?.tv_unhide_path?.text = getString(R.string.unhide_path_prefix)+PUBLIC_DIRECTORY_HIDERBACKUP_FOR_VIDEOS
        }
        view1?.tv_cancel_unhide?.setOnClickListener()
        {
            container?.dismiss()
        }
        view1?.tv_unhide?.setOnClickListener()
        {
            if(view1.path_to_unhide?.checkedRadioButtonId == R.id.path_default)
            {
                when(listType)
                {
                    LIST_TYPE_PHOTOS -> unHideSelectedPhotos(false)
                    LIST_TYPE_VIDEOS -> unHideSelectedVideos(false)
                    LIST_TYPE_CAMERA_PHOTOS -> unHideSelectedPhotosForCamera(false)
                    LIST_TYPE_CAMERA_VIDEOS -> unHideSelectedVideosForCamera(false)
                }
            }
            else if(view1.path_to_unhide?.checkedRadioButtonId == R.id.path_original)
            {
                when(listType)
                {
                    LIST_TYPE_PHOTOS -> unHideSelectedPhotos(true)
                    LIST_TYPE_VIDEOS -> unHideSelectedVideos(true)
                    LIST_TYPE_CAMERA_PHOTOS -> unHideSelectedPhotosForCamera(true)
                    LIST_TYPE_CAMERA_VIDEOS -> unHideSelectedVideosForCamera(true)
                }
            }
            container?.dismiss()
        }
        dialog.setView(view1)
        container=dialog.show()
    }


    private fun shareSelectedPhotos(fromFolders: Boolean) {
        if (selectedImages.isNotEmpty()) {
            if (selectedImages.size <= 8) {
                progressWheel = AppProgressDialog(requireContext())
                progressWheel?.show()
                launch {
                    val operation = async(Dispatchers.IO) {
                        val uriList: ArrayList<Uri>? = ArrayList()
                        for (i in selectedImages) {
                            val tmpFile: File = if (fromFolders)
                                File(i.path?.substringBeforeLast("/") + "/" + StorageUtils.decode(i.name!!, StorageUtils.offset))
                            else
                                File(i.path?.substringBeforeLast("/") + "/" +i.name!!)
                            val inputStream: InputStream = FileInputStream(File(i.path))
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
                        share.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uriList)
                        if(fromFolders)
                        startActivityForResult(share, REQUEST_CODE_TO_SHARE_IMAGES_FROM_FOLDERS)
                        else
                        startActivityForResult(share, REQUEST_CODE_TO_SHARE_IMAGES)
                    }
                    operation.await()
                    withContext(Dispatchers.Main)
                    {
                        progressWheel?.dismiss()
                        progressWheel = null
                        setActionModeValue(false)
                        //rl_select_all?.doGone()
                    }
                }
            } else {
                Toast.makeText(requireContext(), getString(R.string.limit_share_items), Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(requireContext(), getString(R.string.no_files_selected), Toast.LENGTH_LONG).show()
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
            if (view1.action_to_delete?.checkedRadioButtonId == R.id.action_unhide) {
                when (pagerPosition) {
                    0 -> unHideSelectedPhotos(true)
                    1 -> unHideSelectedVideos(true)
                    2 -> {
                        if (isVideo!!)
                            unHideSelectedVideosForCamera(true)
                        else
                            unHideSelectedPhotosForCamera(true)
                    }
                }
            } else if (view1.action_to_delete?.checkedRadioButtonId == R.id.action_delete) {
                when (pagerPosition) {
                    0 -> deleteSelectedPhotos()
                    1 -> deleteSelectedVideos()
                    2 -> {
                        if (isVideo!!)
                            deleteSelectedVideos()
                        else
                            deleteSelectedPhotos()
                    }
                }
            }
            container?.dismiss()
        }
        dialog.setView(view1)
        container = dialog.show()
    }

    private fun deleteSelectedVideos() {
        if (selectedVideos.isNotEmpty()) {
            launch {
                val operation = async(Dispatchers.IO) {
                    for (i in selectedVideos) {
                        File(i.path).delete()
                        val hiddenFilesDatabase: HiddenFilesDatabase? = HiddenFilesDatabase.getInstance(requireContext())
                        hiddenFilesDatabase?.hiddenFilesDao?.deleteFile(i.path)
                    }
                }
                operation.await()
                withContext(Dispatchers.Main)
                {
                    if (pagerPosition == 2) {
                        for (i in selectedVideos) {
                            folderChildList?.remove(SimpleDataClass(i.path, i.name!!, true))
                            currentFolderData?.noOfItems = currentFolderData?.noOfItems?.minus(1)
                        }
                        selectedVideos.clear()
                        adapter?.itemsList = folderChildList!!
                        adapter?.notifyDataSetChanged()
                    } else {
                        for (i in selectedVideos) {
                            videosList.remove(i)
                        }
                        selectedVideos.clear()
                        videosAdapter?.videosList = videosList
                        videosAdapter?.notifyDataSetChanged()
                    }
                    setActionModeValue(false)
                    //rl_select_all?.doGone()
                }
            }
        } else {
            Toast.makeText(context, "No files selected", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteSelectedPhotos() {
        if (selectedImages.isNotEmpty()) {
            launch {
                val operation = async(Dispatchers.IO) {
                    for (i in selectedImages) {
                        File(i.path).delete()
                        val hiddenFilesDatabase: HiddenFilesDatabase? = HiddenFilesDatabase.getInstance(requireContext())
                        hiddenFilesDatabase?.hiddenFilesDao?.deleteFile(i.path)
                    }
                }
                operation.await()
                withContext(Dispatchers.Main)
                {
                    if (pagerPosition == 2) {
                        for (i in selectedImages) {
                            folderChildList?.remove(SimpleDataClass(i.path, i.name!!, true))
                            currentFolderData?.noOfItems = currentFolderData?.noOfItems?.minus(1)
                        }
                        selectedImages.clear()
                        adapter?.itemsList = folderChildList!!
                        adapter?.notifyDataSetChanged()
                    } else {
                        for (i in selectedImages) {
                            photosList.remove(i)
                        }
                        selectedImages?.clear()
                        photosAdapter?.photosList = photosList
                        photosAdapter?.notifyDataSetChanged()
                    }
                    setActionModeValue(false)
                    //rl_select_all?.doGone()
                }
            }
        } else {
            Toast.makeText(context, "No files selected", Toast.LENGTH_SHORT).show()
        }
    }

    private fun unHideSelectedPhotos(isOriginalPath: Boolean) {
        if (selectedImages.isNotEmpty()) {
            if (mediaScanner == null)
                mediaScanner = MediaScanner(context)
            launch {
                val operation = async(Dispatchers.IO) {
                    val externalStoragePublic: File? = StorageUtils.getPublicAlbumStorageDirForPhotos()
                    val hiddenFilesDatabase: HiddenFilesDatabase? = HiddenFilesDatabase.getInstance(requireContext())
                    for (i in selectedImages) {
                        var moved = false
                        var newExternalPath: String? = null
                        newExternalPath = if (hiddenFilesDatabase == null || i.originalPath == null || i.originalPath == "" || !isOriginalPath) {
                            externalStoragePublic?.path + "/" +i.name!!
                        } else {
                            i.originalPath
                        }
                        hiddenFilesDatabase?.hiddenFilesDao?.deleteFile(i.path)
                        moved = StorageUtils.move(i.path!!, newExternalPath!!)
                        if (moved)
                            mediaScanner?.scan(newExternalPath)
                    }
                }
                operation.await()
                withContext(Dispatchers.Main)
                {
                    for (i in selectedImages) {
                        photosList.remove(i)
                    }
                    selectedImages?.clear()
                    photosAdapter?.photosList = photosList
                    photosAdapter?.notifyDataSetChanged()
                    setActionModeValue(false)
                    //rl_select_all?.doGone()
                }
            }
            model?.getAllPhotos()
        } else {
            Toast.makeText(context, "No files selected", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: Int, onUploadClickListenerForCamera: onUploadClickListenerForCamera) =
                PlaceholderFragment().apply {
                    arguments = Bundle().apply {
                        putInt(HiderUtils.PAGER_POSITION, param1)
                    }
                    onUploadClickListener = onUploadClickListenerForCamera
                }
    }

    override fun onImageDeselected(photo: HiddenFiles) {
        selectedImages.remove(photo)
        if (selectedImages.size == 0) {
            setActionModeValue(false)
        }
        //img_select_all?.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.img_deselect, null))
    }

    override fun onImageSelected(photo: HiddenFiles) {
        selectedImages.add(photo)
        bottom_view?.doVisible()
        btn_upload?.doGone()
        //rl_select_all?.doVisible()
    }

    override fun onImageClicked(hiddenPhotos: List<HiddenFiles>, position: Int) {
        onUploadClickListener?.onFileClicked(hiddenPhotos,position)
    }


    override fun onImageFolderClicked(hiddenPhotos: HiddenFiles) {

    }


    override fun setDirectoryToFolder(folderName: String?, position: Int?) {
        when (pagerPosition) {
            0 -> setPhotosDirectoryToFolder(folderName)
            1 -> setVideosDirectoryToFolder(folderName)
            2 -> {
                if (isVideo!!)
                    setVideosDirectoryToFolder(folderName)
                else
                    setPhotosDirectoryToFolder(folderName)
            }
        }
    }


    private fun setVideosDirectoryToFolder(newPath : String?) {
        launch {
            val operation = async(Dispatchers.IO)
            {
                for (i in selectedVideos) {
                    val path = newPath + "/" + File(i.path).name
                    val moved = StorageUtils.move(i.path!!, path)
                    if (moved) {
                        HiddenFilesDatabase.getInstance(requireContext()).hiddenFilesDao.updateFilePath(i.path!!, path, System.currentTimeMillis())
                    }
                }
                selectedVideos.clear()
                if (pagerPosition == 2) {

                  /*  val listOfFiles : ArrayList<SimpleDataClass> = ArrayList()
                    var list = File(currentFolderData?.path!!).listFiles()
                    var count = list?.count()
                    if(count!! > 0)
                    {
                        var size = 0L
                        for(i in list)
                        {
                            if(i.isFile && i.length()>0)
                            {
                                listOfFiles.add(SimpleDataClass(i.path,i.name,false))
                                size += i.length()
                            }
                            else
                            {
                                count = count!!-1
                            }
                        }
                        currentFolderData?.noOfItems = count
                        currentFolderData?.size = StorageUtils.format(size.toDouble(),2)
                        folderChildList = listOfFiles
                    }
                    listOfFiles.clear()
                    list = File(newPath).listFiles()
                    count = list?.count()
                    if(count!! > 0)
                    {
                        var size = 0L
                        for(i in list)
                        {
                            if(i.isFile && i.length()>0)
                            {
                                listOfFiles.add(SimpleDataClass(i.path,i.name,false))
                                size += i.length()
                            }
                            else
                            {
                                count = count!!-1
                            }
                        }
                        val folder = File(newPath)
                        var fileDataClass = FileDataClass(folder.path,folder.name,StorageUtils.format(size.toDouble(),1),false,count,"",false)
                        var dataToRemove  : FileDataClass? = null
                        for(i in folderList!!)
                        {
                            if(i.path == newPath)
                            {
                                folderData?.remove(i)
                                dataToRemove = i
                            }
                        }
                        if(dataToRemove!=null)
                        {
                            folderList?.remove(dataToRemove)
                        }
                        folderList?.add(0,fileDataClass)
                        folderData?.set(fileDataClass,listOfFiles)
                    }*/
                    model?.getCameraFolders()
                } else {
                    for (i in videosList) {
                        if (i.isSelected) {
                            i.path = (newPath + "/" + File(i.path).name)
                            i.isSelected = false
                        }
                    }
                }
            }
            operation.await()
            withContext(Dispatchers.Main)
            {
                dismissDialog()
                setActionModeValue(false)
               /* if(pagerPosition == 2)
                {
                    folder_name?.text = currentFolderData?.name
                    adapter?.itemsList = folderChildList!!
                    adapter?.notifyDataSetChanged()
                    model?.getCameraFolders()
                }
                else*/
                if(pagerPosition != 2)
                {
                    videosAdapter?.videosList = videosList
                    videosAdapter?.notifyDataSetChanged()
                }
                model?.getMyVideosFolders()
                //rl_select_all?.doGone()
                Toast.makeText(context, "files moved successfully", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setPhotosDirectoryToFolder(newPath: String?) {
        launch {
            val operation = async(Dispatchers.IO)
            {
                for (i in selectedImages) {
                    val path = newPath + "/" + File(i.path).name
                    val moved = StorageUtils.move(i.path!!, path)
                    if (moved) {
                        HiddenFilesDatabase.getInstance(requireContext()).hiddenFilesDao.updateFilePath(i.path!!, path, System.currentTimeMillis())
                    }
                }
                selectedImages.clear()
                if (pagerPosition == 2) {
/*
                    val listOfFiles : ArrayList<SimpleDataClass> = ArrayList()
                    var list = File(currentFolderData?.path!!).listFiles()
                    var count = list?.count()
                    if(count!! > 0)
                    {
                        var size = 0L
                        for(i in list)
                        {
                            if(i.isFile && i.length()>0)
                            {
                                listOfFiles.add(SimpleDataClass(i.path,i.name,false))
                                size += i.length()
                            }
                            else
                            {
                                count = count!!-1
                            }
                        }
                        currentFolderData?.noOfItems = count
                        currentFolderData?.size = StorageUtils.format(size.toDouble(),2)
                        folderChildList = listOfFiles
                    }
                    listOfFiles.clear()
                    list = File(newPath).listFiles()
                    count = list?.count()
                    if(count!! > 0)
                    {
                        var size = 0L
                        for(i in list)
                        {
                            if(i.isFile && i.length()>0)
                            {
                                listOfFiles.add(SimpleDataClass(i.path,i.name,false))
                                size += i.length()
                            }
                            else
                            {
                                count = count!!-1
                            }
                        }
                        val folder = File(newPath)
                        val fileDataClass = FileDataClass(folder.path,folder.name,StorageUtils.format(size.toDouble(),1),false,count,"",false)
                        var dataToRemove  : FileDataClass? = null
                        for(i in folderList!!)
                        {
                            if(i.path == newPath)
                            {
                                folderData?.remove(i)
                                dataToRemove = i
                            }
                        }
                        if(dataToRemove!=null)
                        {
                            folderList?.remove(dataToRemove)
                        }
                        folderList?.add(0,fileDataClass)
                        folderData?.set(fileDataClass,listOfFiles)
                    }*/
                    model?.getCameraFolders()
                } else {
                    for (i in photosList) {
                        if (i.isSelected) {
                            i.path = (newPath + "/" + File(i.path).name)
                            i.isSelected = false
                        }
                    }
                }
            }
            operation.await()
            withContext(Dispatchers.Main)
            {
                dismissDialog()
                /*if(pagerPosition == 2)
                {
                    folder_name?.text = currentFolderData?.name
                    adapter?.itemsList = folderChildList!!
                    adapter?.notifyDataSetChanged()
                    model?.getCameraFolders()
                }
                else*/
                if(pagerPosition != 2)
                {
                    photosAdapter?.photosList = photosList
                    photosAdapter?.notifyDataSetChanged()
                }
                model?.getMyPhotosFolders()
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
        when (pagerPosition) {
            0 -> setPhotosDirectoryToDefault()
            1 -> setVideosDirectoryToDefault()
            2 ->
                if (isVideo!!)
                    setVideosDirectoryToDefault()
                else
                    setPhotosDirectoryToDefault()

        }
    }

    private fun setVideosDirectoryToDefault() {
        launch {
            val newPath = StorageUtils.getVideosHiderDirectory()
            val operation = async(Dispatchers.IO)
            {
                for (i in selectedVideos) {
                    val path = newPath + "/" + File(i.path).name
                    val moved = StorageUtils.move(i.path!!, path)
                    if (moved) {
                        HiddenFilesDatabase.getInstance(requireContext()).hiddenFilesDao.updateFilePath(i.path!!, path, System.currentTimeMillis())
                    }
                }
                selectedVideos.clear()
                if (pagerPosition == 2) {

                   /* val listOfFiles : ArrayList<SimpleDataClass> = ArrayList()
                    var list = File(currentFolderData?.path!!).listFiles()
                    var count = list?.count()
                    if(count!! > 0)
                    {
                        var size = 0L
                        for(i in list)
                        {
                            if(i.isFile && i.length()>0)
                            {
                                listOfFiles.add(SimpleDataClass(i.path,i.name,false))
                                size += i.length()
                            }
                            else
                            {
                                count = count!!-1
                            }
                        }
                        currentFolderData?.noOfItems = count
                        currentFolderData?.size = StorageUtils.format(size.toDouble(),2)
                        folderChildList = listOfFiles
                    }
                    listOfFiles.clear()
                    list = File(newPath).listFiles()
                    count = list?.count()
                    if(count!! > 0)
                    {
                        var size = 0L
                        for(i in list)
                        {
                            if(i.isFile && i.length()>0)
                            {
                                listOfFiles.add(SimpleDataClass(i.path,i.name,false))
                                size += i.length()
                            }
                            else
                            {
                                count = count!!-1
                            }
                        }
                        val folder = File(newPath)
                        var fileDataClass = FileDataClass(folder.path,folder.name,StorageUtils.format(size.toDouble(),1),false,count,"",false)
                        var dataToRemove  : FileDataClass? = null
                        for(i in folderList!!)
                        {
                            if(i.path == newPath)
                            {
                                folderData?.remove(i)
                                dataToRemove = i
                            }
                        }
                        if(dataToRemove!=null)
                        {
                            folderList?.remove(dataToRemove)
                        }
                        folderList?.add(0,fileDataClass)
                        folderData?.set(fileDataClass,listOfFiles)
                    }*/
                    model?.getCameraFolders()
                } else {
                    for (i in videosList) {
                        if (i.isSelected) {
                            i.path = (newPath + "/" + File(i.path).name)
                            i.isSelected = false
                        }
                    }
                }
            }
            operation.await()
            withContext(Dispatchers.Main)
            {
                dismissDialog()
               /* if(pagerPosition == 2)
                {
                    folder_name?.text = currentFolderData?.name
                    adapter?.itemsList = folderChildList!!
                    adapter?.notifyDataSetChanged()
                    model?.getCameraFolders()
                }
                else*/
                if(pagerPosition != 2)
                {
                    videosAdapter?.videosList = videosList
                    videosAdapter?.notifyDataSetChanged()
                }
                model?.getMyVideosFolders()
                setActionModeValue(false)
                //rl_select_all?.doGone()
                Toast.makeText(context, "files moved successfully", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setPhotosDirectoryToDefault() {
        launch {
            val newPath = StorageUtils.getPhotosHiderDirectory()
            val operation = async(Dispatchers.IO)
            {
                for (i in selectedImages) {
                    val path = newPath + "/" + File(i.path).name
                    val moved = StorageUtils.move(i.path!!, path)
                    if (moved) {
                        HiddenFilesDatabase.getInstance(requireContext()).hiddenFilesDao.updateFilePath(i.path!!, path, System.currentTimeMillis())
                    }
                }
                selectedImages.clear()
                if (pagerPosition == 2) {

                   /* val listOfFiles: ArrayList<SimpleDataClass> = ArrayList()
                    var list = File(currentFolderData?.path!!).listFiles()
                    var count = list?.count()
                    if (count!! > 0) {
                        var size = 0L
                        for (i in list) {
                            if (i.isFile && i.length() > 0) {
                                listOfFiles.add(SimpleDataClass(i.path, i.name, false))
                                size += i.length()
                            } else {
                                count = count!! - 1
                            }
                        }
                        currentFolderData?.noOfItems = count
                        currentFolderData?.size = StorageUtils.format(size.toDouble(), 2)
                        folderChildList = listOfFiles
                    }
                    listOfFiles.clear()
                    list = File(newPath).listFiles()
                    count = list?.count()
                    if (count!! > 0) {
                        var size = 0L
                        for (i in list) {
                            if (i.isFile && i.length() > 0) {
                                listOfFiles.add(SimpleDataClass(i.path, i.name, false))
                                size += i.length()
                            } else {
                                count = count!! - 1
                            }
                        }
                        val folder = File(newPath)
                        val fileDataClass = FileDataClass(folder.path, folder.name, StorageUtils.format(size.toDouble(), 1), false, count, "", false)
                        var dataToRemove: FileDataClass? = null
                        for (i in folderList!!) {
                            if (i.path == newPath) {
                                folderData?.remove(i)
                                dataToRemove = i
                            }
                        }
                        if (dataToRemove != null) {
                            folderList?.remove(dataToRemove)
                        }
                        folderList?.add(0, fileDataClass)
                        folderData?.set(fileDataClass, listOfFiles)
                    }*/
                    model?.getCameraFolders()
                } else {
                    for (i in photosList) {
                        if (i.isSelected) {
                            i.path = (newPath + "/" + File(i.path).name)
                            i.isSelected = false
                        }
                    }
                }
            }
            operation.await()
            withContext(Dispatchers.Main)
            {
                dismissDialog()
               /* if(pagerPosition == 2)
                {
                    folder_name?.text = currentFolderData?.name
                    adapter?.itemsList = folderChildList!!
                    adapter?.notifyDataSetChanged()
                    model?.getCameraFolders()
                }
                else*/
                if(pagerPosition != 2)
                {
                    photosAdapter?.photosList = photosList
                    photosAdapter?.notifyDataSetChanged()
                }
                model?.getMyPhotosFolders()
                setActionModeValue(false)
                //rl_select_all?.doGone()
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
            var temp: File? = null
            when (pagerPosition) {
                0 -> temp = File(StorageUtils.getPhotosHiderDirectory() + "/" + view1.folder_name?.text?.toString())
                1 -> temp = File(StorageUtils.getVideosHiderDirectory() + "/" + view1.folder_name?.text?.toString())
                2 -> {
                    temp = if (isVideo!!)
                        File(StorageUtils.getVideosHiderDirectory() + "/" + view1.folder_name?.text?.toString())
                    else
                        File(StorageUtils.getPhotosHiderDirectory() + "/" + view1.folder_name?.text?.toString())
                }
            }
            if (temp?.exists()!!) {
                view1.tv_already_exists?.visibility = View.VISIBLE
            } else {
                temp.mkdir()
                container?.dismiss()
                if (pagerPosition != 2) {
                    myFolders?.add(temp.path)
                    bottomAdapter?.folders = myFolders
                } else {
                    if (isVideo!!) {
                        videoFolders?.add(temp.path)
                        bottomAdapter?.folders = videoFolders
                    } else {
                        photoFolders?.add(temp.path)
                        bottomAdapter?.folders = photoFolders
                    }
                }
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

    override fun onVideoSelected(hiddenFiles: HiddenFiles) {
        selectedVideos.add(hiddenFiles)
        bottom_view?.doVisible()
        btn_upload?.doGone()
        //rl_select_all?.doVisible()
    }

    override fun onVideoDeselected(hiddenVideos: HiddenFiles) {
        selectedVideos.remove(hiddenVideos)
        if (selectedVideos.size == 0) {
            setActionModeValue(false)
        }
        //img_select_all?.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.img_deselect, null))
    }

    override fun onVideoClicked(hiddenVideos: List<HiddenFiles>,position: Int) {
        onUploadClickListener?.onFileClicked(hiddenVideos,position)
    }

    override fun onVideoFolderClicked(hiddenVideos: HiddenFiles) {

    }

    override fun onFolderClicked(fileDataClass: FileDataClass) {
        btn_upload?.doVisible()
        ll_child_dir?.doVisible()
        folder_name?.text = fileDataClass.name
        currentPath = fileDataClass.path
        currentFolderData = fileDataClass
        rv_camera_folders?.doGone()
        rv_photos_videos?.doVisible()
        rv_photos_videos?.layoutManager = GridLayoutManager(context, 2)
        isVideo = fileDataClass.path.startsWith(StorageUtils.getVideosHiderDirectory())
        folderChildList = folderData?.get(fileDataClass)
        if (folderChildList != null) {
            adapter = AdapterForPhotosOrVideos(folderChildList!!, rv_photos_videos, requireContext(), this, this)
            rv_photos_videos?.adapter = adapter
        }
    }

    override fun onItemSelected(item: SimpleDataClass) {
        if (isVideo!!)
            selectedVideos.add(HiddenFiles(item.path!!, item.name!!, "", StorageUtils.format(File(item.path!!).length().toDouble(), 2), "video/*", System.currentTimeMillis(), false,true,0))
        else
            selectedImages.add(HiddenFiles(item.path!!, item.name!!, "", StorageUtils.format(File(item.path!!).length().toDouble(), 2), "image/*", System.currentTimeMillis(), false,true,0))
        bottom_view?.doVisible()
        btn_upload?.doGone()
    }

    override fun onItemDeselected(item: SimpleDataClass) {
        if (isVideo!!) {
            var index = 0
            for (i in selectedVideos) {
                if (i.path == item.path)
                    break
                index++
            }
            selectedVideos.removeAt(index)
            if (selectedVideos.size == 0) {
                setActionModeValue(false)
            }
        } else {
            var index = 0
            for (i in selectedImages) {
                if (i.path == item.path)
                    break
                index++
            }
            selectedImages.removeAt(index)
            if (selectedImages.size == 0) {
                setActionModeValue(false)
            }
        }
    }

    override fun onItemClicked(listOfFiles: List<SimpleDataClass>,position: Int) {
        val filesList : ArrayList<HiddenFiles>? = ArrayList()
        val hiddenFilesDao = HiddenFilesDatabase.getInstance(requireContext()).hiddenFilesDao
        launch {
            val operation = async(Dispatchers.IO)
            {
                for(i in listOfFiles)
                {
                    val filename = StorageUtils.decode(i.name!!, StorageUtils.offset)
                    val ext = filename?.substringAfterLast(".")
                    val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext?.toLowerCase())
                    if (mimeType != null && !TextUtils.isEmpty(mimeType))
                    {
                        val hiddenFile = HiddenFiles(i.path!!, filename!!, "", StorageUtils.format(File(i.path).length().toDouble(), 1), mimeType, null, false,true,0)
                        hiddenFile.originalPath = hiddenFilesDao.getOriginalPathForFile(i.path!!)
                            filesList?.add(hiddenFile)
                    }
                }
            }
            operation.await()
            withContext(Dispatchers.Main)
            {
                onUploadClickListener?.onFileClicked(filesList!!,position)
            }
        }
    }

    override fun getActionMode(): Boolean {
        return isActionMode
    }

    override fun setActionModeValue(actionMode: Boolean) {
        isActionMode = actionMode
        if (!actionMode) {
            if (isVideo != null)
            {
                btn_upload?.doVisible()
            }
            else
                btn_upload?.doGone()
            bottom_view?.doGone()
        }
    }

    fun refreshData(xhiderDirectory : String) {
        when(pagerPosition)
        {
            0 -> {
                model?.getHiddenPhotos(requireContext())
                model?.getMyPhotosFolders()
            }
            1 ->{
                model?.getHiddenVideos(requireContext())
                model?.getMyVideosFolders()
            }
            2 ->{
                    launch {
                        val operation = async(Dispatchers.IO)
                        {
                            val listOfFiles : ArrayList<SimpleDataClass> = ArrayList()
                            val list = File(xhiderDirectory).listFiles()
                            var count = list?.count()
                            if(count != null && count > 0)
                            {
                                var size = 0L
                                for(i in list)
                                {
                                    if(i.isFile && i.length()>0)
                                    {
                                        listOfFiles.add(SimpleDataClass(i.path,i.name,false))
                                        size += i.length()
                                    }
                                    else
                                    {
                                        count = count!!-1
                                    }
                                }
                                if(currentFolderData?.path == xhiderDirectory)
                                {
                                    currentFolderData?.noOfItems = count
                                    currentFolderData?.size = StorageUtils.format(size.toDouble(),2)
                                }
                                else
                                {
                                    val folder = File(xhiderDirectory)
                                    currentFolderData = FileDataClass(folder.path, folder.name, StorageUtils.format(size.toDouble(),1), false, count, "", false, 0)
                                }
                                folderChildList = listOfFiles
                                for(i in folderList!!)
                                {
                                    if(i.path == xhiderDirectory)
                                    {
                                        folderData?.remove(i)
                                        folderList?.remove(i)
                                        break
                                    }
                                }
                                folderList?.add(0,currentFolderData!!)
                                folderData?.set(currentFolderData!!,folderChildList!!)
                            }
                        }
                        operation.await()
                        withContext(Dispatchers.Main)
                        {
                            folder_name?.text = currentFolderData?.name
                            adapter?.itemsList = folderChildList!!
                                adapter?.notifyDataSetChanged()
                            if(isVideo == true)
                                model?.getMyVideosFolders()
                            else
                                model?.getMyPhotosFolders()
                        }
                    }
            }
        }
    }
}