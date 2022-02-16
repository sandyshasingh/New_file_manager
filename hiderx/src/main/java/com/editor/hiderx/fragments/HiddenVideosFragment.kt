package com.editor.hiderx.fragments

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.editor.hiderx.*
import com.editor.hiderx.activity.REQUEST_CODE_FOR_SHARE
import com.editor.hiderx.activity.VideosActivity
import com.editor.hiderx.adapters.BottomViewFoldersAdapter
import com.editor.hiderx.adapters.HiddenVideosAdapter
import com.editor.hiderx.database.HiddenFiles
import com.editor.hiderx.database.HiddenFilesDatabase
import com.editor.hiderx.listeners.ActionModeListener
import com.editor.hiderx.listeners.FragmentInteractionListener
import com.editor.hiderx.listeners.ActivityFragmentListener
import com.editor.hiderx.listeners.OnVideoSelectedListener
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.bottom_layout_hidden_items.*
import kotlinx.android.synthetic.main.delete_confirmation_dialog.view.*
import kotlinx.android.synthetic.main.folder_bottom_sheet.view.*
import kotlinx.android.synthetic.main.fragment_hidden_files.*
import kotlinx.android.synthetic.main.layout_videos.*
import kotlinx.android.synthetic.main.layout_videos.bottom_view
import kotlinx.android.synthetic.main.layout_videos.btn_back
import kotlinx.android.synthetic.main.layout_videos.btn_upload
import kotlinx.android.synthetic.main.layout_videos.img_select_all
import kotlinx.android.synthetic.main.layout_videos.img_title
import kotlinx.android.synthetic.main.layout_videos.recycler_view
import kotlinx.android.synthetic.main.layout_videos.rl_select_all
import kotlinx.android.synthetic.main.new_folder_dialog.view.*
import kotlinx.android.synthetic.main.unhide_path_dialog.view.*
import kotlinx.coroutines.*
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream


class HiddenVideosFragment : Fragment(),CoroutineScope by MainScope(), FragmentInteractionListener, OnVideoSelectedListener,ActionModeListener {

    var doExit: Boolean = true
    var currentPath : String = ""
    private var dialog: BottomSheetDialog? = null
    private var bottomAdapter: BottomViewFoldersAdapter?  = null
    var myFolders : ArrayList<String>? = null
    private var model: DataViewModel? = null
    var videosList : ArrayList<HiddenFiles> = ArrayList()
    var videosAdapter : HiddenVideosAdapter? = null
    var activityFragmentListener : ActivityFragmentListener? = null
    var selectedVideos : ArrayList<HiddenFiles> = ArrayList()
    var selectAll : Boolean = false
    var mediaScanner : MediaScanner? = null
    var isActionMode : Boolean = false
    val tempPathList : ArrayList<String> = ArrayList()
    var progressWheel: AppProgressDialog? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        model =  ViewModelProvider(requireActivity()).get(DataViewModel::class.java)
       // model?.getHiddenVideos()
        model?.getMyVideosFolders()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.layout_videos, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btn_back?.setOnClickListener()
        {
            (activity as VideosActivity).onBackPressed()
        }

        launch {
            val operation =  async(Dispatchers.IO)
            {
                currentPath = StorageUtils.getVideosHiderDirectory()
                videosList = ArrayList()
                getChildFiles()
            }
            operation.await()
            withContext(Dispatchers.Main)
            {
                recycler_view?.layoutManager = GridLayoutManager(
                    context,
                    2)
                img_title?.doVisible()
                folder_name?.text = getString(R.string.videos)
                if(videosList!=null && videosList.size>0)
                {
                    videosAdapter = HiddenVideosAdapter(
                        videosList,
                        recycler_view = recycler_view,
                        context,
                        this@HiddenVideosFragment,
                        this@HiddenVideosFragment,
                    )
                    recycler_view?.adapter = videosAdapter
                }
            }
        }

       /* videosAdapter = HiddenVideosAdapter(videosList, recycler_view, context, this, this)
        recycler_view?.adapter = videosAdapter*/
        model?.hiddenVideos?.observe(requireActivity())
        {
            if(it!=null && it.isNotEmpty())
            {
                videosList = it as ArrayList<HiddenFiles>
                videosAdapter?.videosList = it
                videosAdapter?.notifyDataSetChanged()
            }
        }

        ll_share?.setOnClickListener()
        {
            (activity as VideosActivity).backPressed = true
                shareSelectedVideo()
        }

        model?.myVideosFolders?.observe(requireActivity())
        {
            myFolders = it
        }

        btn_upload?.setOnClickListener()
        {
                activityFragmentListener?.onUploadClick(currentPath)
        }

        img_cross?.setOnClickListener()
        {
            setActionModeValue(false)
        }

        img_select_all?.setOnClickListener()
        {
            if(selectAll)
            {
                selectAll = false
                img_select_all?.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.img_deselect, null))
            }
            else
            {
                selectAll = true
                img_select_all?.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.ic_select, null))
                selectedVideos.clear()
                for(i in videosList)
                {
                    if(i.isFile!!)
                    i.isSelected = true
                }
                selectedVideos.addAll(videosList)
                videosAdapter?.notifyDataSetChanged()
            }
            setActionModeValue(selectAll)
        }

        ll_move?.setOnClickListener()
        {
            myFolders?.remove(currentPath)
            val dialogView: View = layoutInflater.inflate(R.layout.folder_bottom_sheet, null)
            dialogView.rv_folders_to_hide?.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            bottomAdapter = BottomViewFoldersAdapter("Videos", myFolders, this)
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

        ll_unhide?.setOnClickListener()
        {
            showUnhidePathDialog()
        }

        ll_delete?.setOnClickListener()
        {
                showConfirmationDialog()
        }
    }

    private suspend fun getChildFiles() {
        val dir = File(currentPath)
        val newList = dir.listFiles()
        val newFolderList : ArrayList<HiddenFiles> = ArrayList()
        var newFileList : ArrayList<HiddenFiles> = ArrayList()
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
                    val data = HiddenFiles(i.path,fileName!!, StorageUtils.format(i.length().toDouble(), 1),"",  mimeType,0,false,true,0)
                    data.updateTime = hiddenFilesDao.getUpdateTimeForFile(i.path)?:0
                    newFileList.add(data)
                }
                else
                {
                    val list = i?.listFiles()
                    val count = list?.let{it.count()}
                    if(count!!>0)
                    {
                        val thumbnail = list?.get(0)?.path
                        newFolderList.add(HiddenFiles(i.path,i.name,thumbnail,"","video/*",0,false,false,count))
                    }
                }
            }
            videosList.addAll(newFolderList)
            if(newFileList.size>0)
            {
                newFileList.sortedBy { fileDataClass -> fileDataClass.updateTime }.reversed().let {
                    if(it.isNotEmpty())
                        videosList.addAll(it)
                }
            }
        }
    }

    private fun showUnhidePathDialog() {
        val view1 = layoutInflater.inflate(R.layout.unhide_path_dialog, null)
        var container : AlertDialog? = null
        val dialog = AlertDialog.Builder(context)
        view1?.tv_unhide_path?.text = getString(R.string.unhide_path_prefix)+ PUBLIC_DIRECTORY_HIDERBACKUP_FOR_VIDEOS
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
                if(selectedVideos.isNotEmpty())
                    unhideSelectedFiles(true)
                else
                    Toast.makeText(context,"No files selected",Toast.LENGTH_SHORT).show()
            }
            container?.dismiss()
        }
        dialog.setView(view1)
        container=dialog.show()
    }

    private fun shareSelectedVideo() {

        if(selectedVideos.isNotEmpty())
        {
            if(selectedVideos.size > 8)
            {
                Toast.makeText(context, "You can share upto 8 files at a time", Toast.LENGTH_SHORT).show()
            }
            else
            {
                progressWheel = AppProgressDialog(requireContext())
                progressWheel?.show()
                launch{
                    val operation = async(Dispatchers.IO) {
                        val uriList : ArrayList<Uri>? = ArrayList()
                        for(i in selectedVideos)
                        {
                            val tmpFile: File = File(i.path.substringBeforeLast("/")+"/"+i.name)
                            val inputStream : InputStream = FileInputStream(File(i.path))
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
                        startActivityForResult(share,REQUEST_CODE_FOR_SHARE)
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
        }
        else
        {
            Toast.makeText(context, "No files selected", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == REQUEST_CODE_FOR_SHARE)
        {
                for(i in tempPathList)
                {
                    if(File(i).exists())
                        File(i).delete()
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
            if(view1?.action_to_delete?.checkedRadioButtonId == R.id.action_unhide)
            {
                unhideSelectedFiles(true)
            }
            else if(view1?.action_to_delete?.checkedRadioButtonId == R.id.action_delete)
            {
                if(selectedVideos.isNotEmpty())
                deleteSelectedFiles()
                else
                    Toast.makeText(context, "No files selected", Toast.LENGTH_SHORT).show()
            }
            container?.dismiss()
        }
        dialog.setView(view1)
        container=dialog.show()
    }

    private fun deleteSelectedFiles() {
        launch{
            val operation = async(Dispatchers.IO) {
                for(i in selectedVideos)
                {
                    File(i.path).delete()
                    val hiddenFilesDatabase: HiddenFilesDatabase? = HiddenFilesDatabase.getInstance(requireContext())
                    hiddenFilesDatabase?.hiddenFilesDao?.deleteFile(i.path)
                }
            }
            operation.await()
            withContext(Dispatchers.Main)
            {
                for(i in selectedVideos)
                {
                    videosList.remove(i)
                }
                videosAdapter?.videosList = videosList
                Toast.makeText(context, selectedVideos.size.toString() + " files deleted", Toast.LENGTH_SHORT).show()
                selectedVideos.clear()
                videosAdapter?.notifyDataSetChanged()
                bottom_view?.doGone()
                btn_upload?.doVisible()
                rl_select_all?.doGone()
            }
        }
    }

    private fun unhideSelectedFiles(isOriginalPath: Boolean) {
        if(selectedVideos.isNotEmpty())
        {
            if(mediaScanner == null)
                mediaScanner = MediaScanner(context)
            launch{
                val operation = async(Dispatchers.IO) {
                    val externalStoragePublic: File? = StorageUtils.getPublicAlbumStorageDirForVideos()
                    val hiddenFilesDatabase: HiddenFilesDatabase? = HiddenFilesDatabase.getInstance(requireContext())
                    for(i in selectedVideos)
                    {
                        var moved = false
                        var newExternalPath : String? = null
                        newExternalPath = if(hiddenFilesDatabase == null || !isOriginalPath) {
                            externalStoragePublic?.path + "/" + i.name
                        }
                        else {
                            val originalPath = hiddenFilesDatabase.hiddenFilesDao.getOriginalPathForFile(i.path)
                            if(TextUtils.isEmpty(originalPath))
                                externalStoragePublic?.path + "/" + i.name
                            else
                                originalPath
                        }
                        moved = StorageUtils.move(i.path, newExternalPath!!)
                        if(moved)
                        {
                            hiddenFilesDatabase?.hiddenFilesDao?.deleteFile(i.path)
                            mediaScanner?.scan(newExternalPath)
                        }
                    }
                }
                operation.await()
                withContext(Dispatchers.Main)
                {
                    for(i in selectedVideos)
                    {
                        videosList.remove(i)
                    }
                    selectedVideos.clear()
                    videosAdapter?.videosList = videosList
                    videosAdapter?.notifyDataSetChanged()
                    setActionModeValue(false)
                }
            }
        }
        else
        {
            Toast.makeText(context,"No files selected",Toast.LENGTH_SHORT).show()
        }
    }

    override fun onVideoSelected(video: HiddenFiles) {
        selectedVideos.add(video)
        if(selectedVideos.size == videosList.size)
        {
            selectAll = true
            img_select_all?.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.ic_select, null))
        }
        bottom_view?.doVisible()
        btn_upload?.doGone()
        rl_select_all?.doVisible()
    }

    override fun onVideoDeselected(video: HiddenFiles)
    {
        selectedVideos.remove(video)
        if(selectedVideos.size == 0)
        {
          setActionModeValue(false)
        }
        img_select_all?.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.img_deselect, null))
    }

    override fun onVideoClicked(hiddenVideos: List<HiddenFiles>,position : Int) {
        (activity as VideosActivity).viewFile(hiddenVideos,position)
    }

    override fun onVideoFolderClicked(hiddenVideos : HiddenFiles) {
        doExit = false
        videosList.clear()
        currentPath = hiddenVideos.path
        launch {
            val operation = async(Dispatchers.IO)
            {
                getChildFiles()
            }
            operation.await()
            withContext(Dispatchers.Main)
            {
                img_title?.doGone()
                folder_name.text = hiddenVideos.name
                videosAdapter?.videosList = videosList
                videosAdapter?.notifyDataSetChanged()
            }
        }
    }

    override fun setDirectoryToFolder(newPath: String?, position: Int?) {
        launch {
            val operation = async(Dispatchers.IO)
            {
                for(i in selectedVideos)
                {
                    val path = newPath+"/"+File(i.path).name
                    val moved = StorageUtils.move(i.path, path)
                    if(moved)
                    {
                        HiddenFilesDatabase.getInstance(requireContext()).hiddenFilesDao.updateFilePath(i.path,path,System.currentTimeMillis())
                    }
                }
                selectedVideos.clear()
            }
            operation.await()
            withContext(Dispatchers.Main)
            {
                dismissDialog()
                refreshData()
                img_select_all?.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.img_deselect, null))
                rl_select_all?.doGone()
                btn_upload?.doVisible()
                bottom_view?.doGone()
                Toast.makeText(context,"files moved successfully",Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun refreshData()     {
        isActionMode = false
        videosList.clear()
        selectAll = false
        btn_upload?.doVisible()
        model?.getMyVideosFolders()
        launch {
            val operation = async(Dispatchers.IO)
            {
                getChildFiles()
            }
            operation.await()
            withContext(Dispatchers.Main)
            {
                if(currentPath == StorageUtils.getVideosHiderDirectory())
                {
                    doExit = true
                    img_title?.doVisible()
                }
                else
                {
                    doExit = false
                    img_title?.doGone()
                }
                folder_name.text = File(currentPath).name
                if(videosAdapter == null)
                {
                    if(videosList!=null && videosList.size>0)
                    {
                        videosAdapter = HiddenVideosAdapter(
                                videosList,
                                recycler_view = recycler_view,
                                requireContext(),
                                this@HiddenVideosFragment,
                                this@HiddenVideosFragment,
                        )
                        recycler_view?.adapter = videosAdapter
                    }
                }
                else
                {
                    videosAdapter?.videosList = videosList
                    videosAdapter?.notifyDataSetChanged()
                }
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
            val newPath = StorageUtils.getVideosHiderDirectory()
            val operation = async(Dispatchers.IO)
            {
                for(i in selectedVideos)
                {
                    val path = newPath+"/"+File(i.path).name
                    val moved = StorageUtils.move(i.path, path)
                    if(moved)
                    {
                        HiddenFilesDatabase.getInstance(requireContext()).hiddenFilesDao.updateFilePath(i.path, path, System.currentTimeMillis())
                    }
                }
                selectedVideos.clear()
            }
            operation.await()
            withContext(Dispatchers.Main)
            {
                dismissDialog()
                refreshData()
                img_select_all?.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.img_deselect, null))
                rl_select_all?.doGone()
                btn_upload?.doVisible()
                bottom_view?.doGone()
                Toast.makeText(context,"files moved successfully",Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun showNewFolderDialog() {
        val view1 = layoutInflater.inflate(R.layout.new_folder_dialog, null)
        var container : AlertDialog? = null
        val dialog = AlertDialog.Builder(context)
        view1?.tv_ok?.setOnClickListener()
        {
            val temp : File?=  File(StorageUtils.getVideosHiderDirectory() + "/" + view1.folder_name?.text?.toString())
            if(temp?.exists()!!)
            {
                view1.tv_already_exists?.visibility = View.VISIBLE
            }
            else
            {
                temp.mkdir()
                myFolders?.add(temp.path)
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
        container=dialog.show()
    }

    override fun getActionMode(): Boolean {
        return isActionMode
    }

    override fun setActionModeValue(actionMode: Boolean) {
        isActionMode = actionMode
        if(!actionMode)
        {
            selectAll = false
            img_select_all?.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.img_deselect, null))
            rl_select_all?.doGone()
            btn_upload?.doVisible()
            bottom_view?.doGone()
            if(selectedVideos.size>0)
            {
                for(i in videosList)
                    i.isSelected = false
                selectedVideos.clear()
                videosAdapter?.notifyDataSetChanged()
            }
        }
    }

    fun onPressedBack()
    {

        if(selectedVideos.isNotEmpty())
        {
            for (i in selectedVideos!!) {
                if (i.isSelected)
                    i.isSelected = false
            }
            selectedVideos.clear()
            setActionModeValue(false)
            videosAdapter?.notifyDataSetChanged()
            selectAll = false
            img_select_all?.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.img_deselect, null))
        }
        else
        {
            videosList.clear()
            val parentFile = File(currentPath).parentFile
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
                        videosList.add(HiddenFiles(i.path,filename!!, StorageUtils.format(i.length().toDouble(), 1),"",  mimeType,0,false,true,0))
                    }
                    else
                    {
                        val list = i?.listFiles()
                        val count = list?.let{it.count()}
                        if(count!! > 0 )
                        {
                            val thumbnail = list[0]?.path
                            videosList.add(HiddenFiles(i.path,i.name,thumbnail,"","video/*",0,false,false,count))
                        }
                    }
                }
            }
            currentPath = parentFile?.path!!
            if(currentPath == StorageUtils.getVideosHiderDirectory())
            {
                doExit = true
                img_title?.doVisible()
                folder_name.text = getString(R.string.videos)
            }
            else
            {
                img_title?.doGone()
                folder_name.text = parentFile.name
            }
            selectedVideos.clear()
            setActionModeValue(false)
            videosAdapter?.videosList = videosList
            videosAdapter?.notifyDataSetChanged()
        }
    }
}