package com.editor.hiderx.fragments

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.FileProvider
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import com.editor.hiderx.*
import com.editor.hiderx.Utility.convertIntoDate
import com.editor.hiderx.activity.CameraFolderActivity
import com.editor.hiderx.activity.FilemanagerActivity
import com.editor.hiderx.activity.PhotosActivity
import com.editor.hiderx.activity.REQUEST_CODE_FOR_SHARE
import com.editor.hiderx.database.HiddenFiles
import com.editor.hiderx.database.HiddenFilesDatabase
import com.editor.hiderx.dataclass.FileDataClass
import com.editor.hiderx.listeners.OnPagerItemsClickLister
import kotlinx.android.synthetic.main.delete_confirmation_dialog.view.*
import kotlinx.android.synthetic.main.fragment_view_pager.*
import kotlinx.android.synthetic.main.fragment_view_pager.btn_back
import kotlinx.android.synthetic.main.unhide_path_dialog.view.*
import kotlinx.coroutines.*
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.util.*
import kotlin.collections.ArrayList


private const val ARG_HIDDEN_FILE = "HIDDEN_FILE_EXTRA"
private const val ARG_FILE_DATA = "FILE_DATA_EXTRA"


class ViewPagerFragment : Fragment(),CoroutineScope by MainScope() {

    var tempPath : String =""
    private var hiddenFile: HiddenFiles? = null
    private var fileDataClass: FileDataClass? = null
    private var isControllerShown : Boolean = true
    var onPagerItemsClickLister : OnPagerItemsClickLister? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            if(arguments?.containsKey(ARG_HIDDEN_FILE) == true)
            hiddenFile = it.getSerializable(ARG_HIDDEN_FILE) as HiddenFiles?
            else if(arguments?.containsKey(ARG_FILE_DATA) == true)
                fileDataClass = it.getParcelable(ARG_FILE_DATA) as FileDataClass?
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_view_pager, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        hiddenFile?.path?.let{ image_viewer_image?.loadUriForPhotoViewer(it) }
        fileDataClass?.path?.let{ image_viewer_image?.loadUriForPhotoViewer(it) }

            image_viewer_image?.setOnClickListener()
            {
                if(isControllerShown)controller?.visibility = View.VISIBLE else controller?.visibility = View.GONE
                isControllerShown = !isControllerShown
            }
        btn_back?.setOnClickListener()
        {
            when
            {
                (activity is PhotosActivity) -> (activity as PhotosActivity).onBackPressed()
                (activity is CameraFolderActivity) -> (activity as CameraFolderActivity).onBackPressed()
                (activity is FilemanagerActivity) -> (activity as FilemanagerActivity).onBackPressed()
            }

        }
        ll_unhide?.setOnClickListener()
        {
            showUnhidePathDialog()
        }
        ll_share?.setOnClickListener()
        {
            shareFile()
        }
        ll_delete?.setOnClickListener()
        {
            showConfirmationDialog()
        }
        btn_info?.setOnClickListener()
        {
           showProperties(hiddenFile,fileDataClass)
        }

    }

    private fun showProperties(hidden: HiddenFiles?, fileDataClass: FileDataClass?) {
        val keyValueModelArrayList: ArrayList<KeyValueModel> = ArrayList()
        launch {
            val operation = async(Dispatchers.IO)
            {
                var filePath: String =""
                var fileName: String = ""
                var originalPath : String = ""
                var size = ""
                hidden?.let{
                    filePath = it.path
                    fileName = it.name?:"Unknown"
                    size = it.size?:"0kb"
                    originalPath = it.originalPath?:"Unknown"
                }
                fileDataClass?.let{
                    filePath = it.path
                    fileName = it.name?:"Unknown"
                    size = it.size?:"0kb"
                    if (HiddenFilesDatabase.getInstance(requireActivity()).hiddenFilesDao.isFileExists(it.path)?:false) {
                        originalPath = HiddenFilesDatabase.getInstance(requireActivity()).hiddenFilesDao.getOriginalPathForFile(it.path)?:"UnKnown"
                    }
                }

                val file = File(filePath)
                val smsTime = Calendar.getInstance()

                val date = Date(file.lastModified())
                smsTime.time = date
                keyValueModelArrayList.add(KeyValueModel(activity?.resources?.getString(R.string.File_name), fileName))
                keyValueModelArrayList.add(KeyValueModel(activity?.resources?.getString(R.string.File_size), "" + size))
                keyValueModelArrayList.add(KeyValueModel(activity?.resources?.getString(R.string.location), filePath))
                keyValueModelArrayList.add(KeyValueModel(activity?.resources?.getString(R.string.Date), convertIntoDate(smsTime)))
                keyValueModelArrayList.add(KeyValueModel(activity?.resources?.getString(R.string.original_path),originalPath ))
            }
            operation.await()
            withContext(Dispatchers.Main)
            {
                val materialDialog = MaterialDialog.Builder(requireContext())
                    .title(R.string.properties)
                    .theme(Theme.LIGHT)
                    .positiveText(R.string.ok)
                    .onPositive { dialog, which -> } // second parameter is an optional layout manager. Must be a LinearLayoutManager or GridLayoutManager.
                    .adapter(KeyValueAdapter(keyValueModelArrayList), null).build()
                materialDialog.show()
            }
        }
    }

    private fun showConfirmationDialog() {
        val view1 = layoutInflater.inflate(R.layout.delete_confirmation_dialog, null)
        var container : AlertDialog? = null
        val dialog = AlertDialog.Builder(context)
        view1?.tv_cancel_delete?.setOnClickListener()
        {
            container?.dismiss()
        }
        view1?.tv_confirm?.setOnClickListener()
        {
            if(view1.action_to_delete?.checkedRadioButtonId == R.id.action_unhide)
            {
                unhideSelectedFiles(true)
            }
            else if(view1.action_to_delete?.checkedRadioButtonId == R.id.action_delete)
            {
                    deleteSelectedFiles()
            }
            container?.dismiss()
        }
        dialog.setView(view1)
        container=dialog.show()
    }

    private fun deleteSelectedFiles() {
        launch{
            val operation = async(Dispatchers.IO) {
                hiddenFile?.let{
                    File(it.path).delete()
                    val hiddenFilesDatabase: HiddenFilesDatabase? = HiddenFilesDatabase.getInstance(requireContext())
                    hiddenFilesDatabase?.hiddenFilesDao?.deleteFile(it.path)
                }
                fileDataClass?.let{
                    File(it.path).delete()
                    val hiddenFilesDatabase: HiddenFilesDatabase? = HiddenFilesDatabase.getInstance(requireContext())
                    hiddenFilesDatabase?.hiddenFilesDao?.deleteFile(it.path)
                }
            }
            operation.await()
            withContext(Dispatchers.Main)
            {
                Toast.makeText(context,"file deleted successfully", Toast.LENGTH_SHORT).show()
                fileDataClass?.let{onPagerItemsClickLister?.onFileRemoved(it)}
                hiddenFile?.let{onPagerItemsClickLister?.onItemRemoved(it)}
            }
        }
    }

    private fun shareFile() {
        when
        {
            (activity is PhotosActivity) -> (activity as PhotosActivity).backPressed = true
            (activity is CameraFolderActivity) -> (activity as CameraFolderActivity).backPressed = true
            (activity is FilemanagerActivity) -> (activity as FilemanagerActivity).backPressed = true
        }
        launch{
             withContext(Dispatchers.IO) {

                var name  : String = ""
                var originalPath : String = ""
                hiddenFile?.let{
                    name = it.name?:""
                    tempPath = it.path.substringBeforeLast("/")+"/"+name
                    originalPath = it.path
                }
                fileDataClass?.let{
                    name = it.name?:""
                    tempPath = it.path.substringBeforeLast("/")+"/"+name
                    originalPath = it.path
                }
                val tmpFile: File = File(tempPath)
                val inputStream : InputStream = FileInputStream(File(originalPath))
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
                val share = Intent(Intent.ACTION_SEND)
                share.type = "image/*"
                share.putExtra(Intent.EXTRA_STREAM, uri)
                share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                startActivityForResult(share, REQUEST_CODE_FOR_SHARE)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == REQUEST_CODE_FOR_SHARE)
        {
            val file : File = File(tempPath)
                if(file.exists())
                    file.delete()
        }
    }

    private fun showUnhidePathDialog() {
        val view1 = layoutInflater.inflate(R.layout.unhide_path_dialog, null)
        var container : AlertDialog? = null
        val dialog = AlertDialog.Builder(context)
        view1?.tv_unhide_path?.text = getString(R.string.unhide_path_prefix)+ PUBLIC_DIRECTORY_HIDERBACKUP_FOR_PHOTOS
        view1?.tv_cancel_unhide?.setOnClickListener()
        {
            container?.dismiss()
        }
        view1?.tv_unhide?.setOnClickListener()
        {
            if(view1.path_to_unhide?.checkedRadioButtonId == R.id.path_default)
            {
                unhideSelectedFiles(false)
            }
            else if(view1.path_to_unhide?.checkedRadioButtonId == R.id.path_original)
            {
                    unhideSelectedFiles(true)
            }
            container?.dismiss()
        }
        dialog.setView(view1)
        container=dialog.show()
    }

    private fun unhideSelectedFiles(isOriginalPath: Boolean) {
        launch{
            val operation = async(Dispatchers.IO){
                val mediaScanner = MediaScanner(context)
                var moved = false
                var newExternalPath : String? = null
                val externalStoragePublic: File? = StorageUtils.getPublicAlbumStorageDirForPhotos()
                val hiddenFilesDatabase: HiddenFilesDatabase? = HiddenFilesDatabase.getInstance(requireContext())
                if(hiddenFilesDatabase == null || !isOriginalPath) {
                     hiddenFile?.let {
                         newExternalPath = externalStoragePublic?.path + "/" + it.name }
                    fileDataClass?.let {
                         newExternalPath = externalStoragePublic?.path + "/" + it.name }
                }
                else {
                    var originalPath : String? = ""
                    hiddenFile?.let{originalPath= hiddenFilesDatabase.hiddenFilesDao.getOriginalPathForFile(it.path)
                        newExternalPath = if(TextUtils.isEmpty(originalPath))
                            externalStoragePublic?.path + "/" + it.name
                        else
                            originalPath
                    }
                    fileDataClass?.let{originalPath= hiddenFilesDatabase.hiddenFilesDao.getOriginalPathForFile(it.path)
                        newExternalPath = if(TextUtils.isEmpty(originalPath))
                            externalStoragePublic?.path + "/" + it.name
                        else
                            originalPath
                    }
                }
                hiddenFile?.let {moved = StorageUtils.move(it.path, newExternalPath!!)
                    if(moved)
                    {
                        hiddenFilesDatabase?.hiddenFilesDao?.deleteFile(it.path)
                        mediaScanner?.scan(newExternalPath)
                    }
                }
                fileDataClass?.let {moved = StorageUtils.move(it.path, newExternalPath!!)
                    if(moved)
                    {
                        hiddenFilesDatabase?.hiddenFilesDao?.deleteFile(it.path)
                        mediaScanner?.scan(newExternalPath)
                    }
                }
            }
            operation.await()
            withContext(Dispatchers.Main)
            {
                fileDataClass?.let{onPagerItemsClickLister?.onFileRemoved(it)}
                hiddenFile?.let{onPagerItemsClickLister?.onItemRemoved(it)}
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(
            hiddenFiles: HiddenFiles?,
            fileDataClass: FileDataClass?,
        ) =
            ViewPagerFragment().apply {
                arguments = Bundle().apply {
                    hiddenFiles?.let{putSerializable(ARG_HIDDEN_FILE, hiddenFiles)}
                    fileDataClass?.let{putParcelable(ARG_FILE_DATA, fileDataClass)}
                }
            }
    }
}