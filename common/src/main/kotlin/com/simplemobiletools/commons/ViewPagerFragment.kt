package com.simplemobiletools.commons

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.FileProvider
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme

import kotlinx.android.synthetic.main.delete_confirmation_dialog.view.*
import kotlinx.android.synthetic.main.fragment_view_pager.*
import kotlinx.android.synthetic.main.fragment_view_pager.btn_back
import kotlinx.coroutines.*
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.util.*
import kotlin.collections.ArrayList


private const val ARG_POSITION = "POSITION"
private const val ARG_FILE_DATA = "FILE_DATA_EXTRA"
private const val APPLICATION_ID = "com.example.new_file_manager"
private const val REQUEST_CODE_FOR_SHARE = 89




class ViewPagerFragment : Fragment(),CoroutineScope by MainScope() {

    var tempPath : String =""
    private var hiddenFile: ListItem? = null
    private var isControllerShown : Boolean = true
    var onPagerItemsClickLister : OnPagerItemsClickLister? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            var position = it.getInt(ARG_POSITION)
            hiddenFile = DataHolderforImageViewer.mfinalValues?.get(position)
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

            image_viewer_image?.setOnClickListener()
            {
                if(isControllerShown)controller?.visibility = View.VISIBLE else controller?.visibility = View.GONE
                isControllerShown = !isControllerShown
            }
        btn_back?.setOnClickListener()
        {
            //(activity as? FileManagerMainActivity)?.loadPhotoViewerFragment(imageslist,position )

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
           showProperties(hiddenFile)
        }

    }


    private fun showProperties(hidden: ListItem?) {
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
                    size = MemorySizeUtils.formatSize(it.size)?:"0kb"
                }

                val file = File(filePath)
                val smsTime = Calendar.getInstance()


                val date = Date(file.lastModified())
                smsTime.time = date
                keyValueModelArrayList.add(KeyValueModel(activity?.resources?.getString(R.string.File_name), fileName))
                keyValueModelArrayList.add(KeyValueModel(activity?.resources?.getString(R.string.File_size), "" + size))
                keyValueModelArrayList.add(KeyValueModel(activity?.resources?.getString(R.string.location), filePath))
                keyValueModelArrayList.add(KeyValueModel(activity?.resources?.getString(R.string.Date), smsTime.toString()))
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

            if(view1.action_to_delete?.checkedRadioButtonId == R.id.action_delete)
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
                }

            }
            operation.await()
            withContext(Dispatchers.Main)
            {
                Toast.makeText(context,"file deleted successfully", Toast.LENGTH_SHORT).show()
                hiddenFile?.let{onPagerItemsClickLister?.onFileRemoved(it)}
            }
        }
    }

    private fun shareFile() {
        when
        {
//            (activity is PhotosActivity) -> (activity as PhotosActivity).backPressed = true
//            (activity is CameraFolderActivity) -> (activity as CameraFolderActivity).backPressed = true
//            (activity is FilemanagerActivity) -> (activity as FilemanagerActivity).backPressed = true
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




    companion object {
        @JvmStatic
        fun newInstance(
            hiddenFiles: Int?
        ) =
            ViewPagerFragment().apply {
                arguments = Bundle().apply {
                    hiddenFiles?.let{putInt(ARG_POSITION, hiddenFiles)}
                }
            }
    }
}