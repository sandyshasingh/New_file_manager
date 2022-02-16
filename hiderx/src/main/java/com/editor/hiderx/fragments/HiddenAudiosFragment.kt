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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.editor.hiderx.*
import com.editor.hiderx.activity.AudiosActivity
import com.editor.hiderx.activity.REQUEST_CODE_FOR_SHARE
import com.editor.hiderx.adapters.AdapterForAudios
import com.editor.hiderx.adapters.BottomViewFoldersAdapter
import com.editor.hiderx.database.HiddenFiles
import com.editor.hiderx.database.HiddenFilesDatabase
import com.editor.hiderx.listeners.ActionModeListener
import com.editor.hiderx.listeners.FragmentInteractionListener
import com.editor.hiderx.listeners.OnAudioSelectedListener
import com.editor.hiderx.listeners.ActivityFragmentListener
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.bottom_layout_hidden_items.*
import kotlinx.android.synthetic.main.bottom_layout_hidden_items.img_cross
import kotlinx.android.synthetic.main.delete_confirmation_dialog.view.*
import kotlinx.android.synthetic.main.folder_bottom_sheet.view.*
import kotlinx.android.synthetic.main.fragment_hidden_files.bottom_view
import kotlinx.android.synthetic.main.fragment_hidden_files.btn_back
import kotlinx.android.synthetic.main.fragment_hidden_files.btn_upload
import kotlinx.android.synthetic.main.fragment_hidden_files.img_select_all
import kotlinx.android.synthetic.main.fragment_hidden_files.img_title
import kotlinx.android.synthetic.main.fragment_hidden_files.recycler_view
import kotlinx.android.synthetic.main.fragment_hidden_files.rl_select_all
import kotlinx.android.synthetic.main.layout_audios.folder_name
import kotlinx.android.synthetic.main.new_folder_dialog.view.*
import kotlinx.android.synthetic.main.unhide_path_dialog.view.*
import kotlinx.coroutines.*
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream

class HiddenAudiosFragment : Fragment(),CoroutineScope by MainScope(), OnAudioSelectedListener, FragmentInteractionListener, ActionModeListener {

    var doExit: Boolean = true
    var currentPath : String = ""
    private val tempPathList: ArrayList<String> = ArrayList()
    private var dialog: BottomSheetDialog? = null
    private var bottomAdapter: BottomViewFoldersAdapter?  = null
    var myFolders : ArrayList<String>? = null
    private var model: DataViewModel? = null
    var audiosList : ArrayList<HiddenFiles> = ArrayList()
    var audiosAdapter : AdapterForAudios? = null
    var onUploadClickListener : ActivityFragmentListener? = null
    var selectedAudios : ArrayList<HiddenFiles> = ArrayList()
    var selectAll : Boolean = false
    var mediaScanner : MediaScanner? = null
    var isActionMode : Boolean = false
    var progressWheel : AppProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        model =  ViewModelProvider(requireActivity()).get(DataViewModel::class.java)
       // model?.getHiddenAudios()
        model?.getMyAudiosFolders()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.layout_audios, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btn_back?.setOnClickListener()
        {
            (activity as AudiosActivity).onBackPressed()
        }
        recycler_view?.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        launch {
            val operation =  async(Dispatchers.IO)
            {
                currentPath = StorageUtils.getAudiosHiderDirectory()
                audiosList = ArrayList()
                getChildFiles()
            }
            operation.await()
            withContext(Dispatchers.Main)
            {
                img_title?.doVisible()
                folder_name?.text = getString(R.string.audios)
                if(audiosList!=null && audiosList.size>0)
                {
                    audiosAdapter = AdapterForAudios(
                            audiosList,
                            recycler_view = recycler_view,
                            requireContext(),
                            this@HiddenAudiosFragment,
                            this@HiddenAudiosFragment,
                    )
                    recycler_view?.adapter = audiosAdapter
                }
            }
        }
        model?.myAudiosFolders?.observe(requireActivity())
        {
            myFolders = it
        }

        btn_upload?.setOnClickListener()
        {
                onUploadClickListener?.onUploadClick(currentPath)
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
                selectedAudios.clear()
                for(i in audiosList)
                {
                        i.isSelected = false
                }
                audiosAdapter?.notifyDataSetChanged()
            }
            else
            {
                selectAll = true
                img_select_all?.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.ic_select, null))
                selectedAudios.clear()
                for(i in audiosList)
                {
                    if(i.isFile!!)
                    i.isSelected = true
                }
                selectedAudios.addAll(audiosList)
                audiosAdapter?.notifyDataSetChanged()
            }
            setActionModeValue(selectAll)
        }

        ll_share?.setOnClickListener()
        {
            (activity as AudiosActivity).pressedBack = true
            shareSelectedAudios()
        }

        ll_move?.setOnClickListener()
        {
            myFolders?.remove(currentPath)
            val dialogView: View = layoutInflater.inflate(R.layout.folder_bottom_sheet, null)
            dialogView.rv_folders_to_hide?.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            bottomAdapter = BottomViewFoldersAdapter("Audios", myFolders, this)
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
        val dir = File(currentPath!!)
        val newList = dir.listFiles()
        val newFolderList : ArrayList<HiddenFiles> = ArrayList()
        var newFileList : ArrayList<HiddenFiles> = ArrayList()
       /* newList?.sortedBy{ file : File -> file.lastModified() }?.reversed().let{
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
                    val data = HiddenFiles(i.path,fileName!!,"" ,StorageUtils.format(i.length().toDouble(), 1),  mimeType,0,false,true,0)
                    data.updateTime = hiddenFilesDao.getUpdateTimeForFile(i.path)?:0
                    newFileList.add(data)
                }
                else
                {
                    val list = i?.listFiles()
                    val count = list?.let{it.count()}
                    if(count!! > 0)
                    {
                        val thumbnail = list?.get(0)?.path
                        newFolderList.add(HiddenFiles(i.path,i.name,thumbnail,"","audio/*",0,false,false,count))
                    }
                }
            }
            audiosList.addAll(newFolderList)
            if(newFileList.size>0)
            {
                newFileList.sortedBy { fileDataClass -> fileDataClass.updateTime }.reversed().let {
                    if(it.isNotEmpty())
                        newFileList = it as ArrayList<HiddenFiles>
                }
                audiosList.addAll(newFileList)
            }
        }
    }

    private fun showUnhidePathDialog() {
        val view1 = layoutInflater.inflate(R.layout.unhide_path_dialog, null)
        var container : AlertDialog? = null
        val dialog = AlertDialog.Builder(context)
        view1?.tv_unhide_path?.text = getString(R.string.unhide_path_prefix)+ PUBLIC_DIRECTORY_HIDERBACKUP_FOR_AUDIOS
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
                if(selectedAudios.isNotEmpty())
                    unhideSelectedFiles(true)
                else
                    Toast.makeText(context,"No files selected",Toast.LENGTH_SHORT).show()
            }
            container?.dismiss()
        }
        dialog.setView(view1)
        container=dialog.show()
    }

    private fun shareSelectedAudios() {
        if(selectedAudios.isNotEmpty())
        {
            if(selectedAudios.size > 8)
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
                        for(i in selectedAudios)
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
                        startActivityForResult(share, REQUEST_CODE_FOR_SHARE)
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
                if(selectedAudios.isNotEmpty())
                deleteSelectedFiles()
                else
                    Toast.makeText(context,"No files selected",Toast.LENGTH_SHORT).show()
            }
            container?.dismiss()
        }
        dialog.setView(view1)
        container=dialog.show()
    }

    private fun deleteSelectedFiles() {
        launch{
            val operation = async(Dispatchers.IO) {
                for(i in selectedAudios)
                {
                    File(i.path).delete()
                    val hiddenFilesDatabase: HiddenFilesDatabase? = HiddenFilesDatabase.getInstance(requireContext())
                    hiddenFilesDatabase?.hiddenFilesDao?.deleteFile(i.path)
                }
            }
            operation.await()
            withContext(Dispatchers.Main)
            {
                for(i in selectedAudios)
                {
                    audiosList.remove(i)
                }
                audiosAdapter?.audioList = audiosList
                Toast.makeText(context, selectedAudios.size.toString()+" files deleted", Toast.LENGTH_SHORT).show()
                selectedAudios.clear()
                audiosAdapter?.notifyDataSetChanged()
                bottom_view?.doGone()
                btn_upload?.doVisible()
                rl_select_all?.doGone()
            }
        }
    }

    fun onPressedBack() {

        if(selectedAudios.isNotEmpty())
        {
            for (i in selectedAudios!!) {
                if (i.isSelected)
                    i.isSelected = false
            }
            selectedAudios.clear()
            setActionModeValue(false)
            audiosAdapter?.notifyDataSetChanged()
            selectAll = false
            img_select_all?.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.img_deselect, null))
        }
        else
        {
            audiosList.clear()
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
                        audiosList.add(HiddenFiles(i.path,filename!!, StorageUtils.format(i.length().toDouble(), 1),"",  mimeType,0,false,true,0))
                    }
                    else
                    {
                        val list = i?.listFiles()
                        val count = list?.let{it.count()}
                        if(count!! > 0 )
                        {
                            val thumbnail = list[0]?.path
                            audiosList.add(HiddenFiles(i.path,i.name,thumbnail,"","audio/*",0,false,false,count))
                        }
                    }
                }
            }
            currentPath = parentFile?.path!!
            if(currentPath == StorageUtils.getAudiosHiderDirectory())
            {
                doExit = true
                img_title?.doVisible()
                folder_name.text = getString(R.string.audios)
            }
            else
            {
                img_title?.doGone()
                folder_name.text = parentFile.name
            }
            selectedAudios.clear()
            setActionModeValue(false)
            audiosAdapter?.audioList = audiosList!!
            audiosAdapter?.notifyDataSetChanged()
        }
    }


    private fun unhideSelectedFiles(isOriginalPath: Boolean) {
        if(selectedAudios.isNotEmpty())
        {
            if(mediaScanner == null)
                mediaScanner = MediaScanner(context)
            launch{
                val operation = async(Dispatchers.IO) {
                    val externalStoragePublic: File? = StorageUtils.getPublicAlbumStorageDirForAudios()
                    val hiddenFilesDatabase: HiddenFilesDatabase? = HiddenFilesDatabase.getInstance(requireContext())
                    for(i in selectedAudios)
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
                    for(i in selectedAudios)
                    {
                        audiosList.remove(i)
                    }
                    selectedAudios.clear()
                    audiosAdapter?.audioList = audiosList
                    audiosAdapter?.notifyDataSetChanged()
                    setActionModeValue(false)
                }
            }
        }
        else
        {
            Toast.makeText(context,"No files selected",Toast.LENGTH_SHORT).show()
        }
    }


    override fun onAudioClicked(audios: List<HiddenFiles>, adapterPosition: Int) {
        (activity as AudiosActivity).viewFile(audios,adapterPosition)
    }

    override fun onAudioFolderClicked(audio: HiddenFiles) {
        doExit = false
        audiosList.clear()
        currentPath = audio.path
        launch {
            val operation = async(Dispatchers.IO)
            {
                getChildFiles()
            }
            operation.await()
            withContext(Dispatchers.Main)
            {
                img_title?.doGone()
                folder_name.text = audio.name
                audiosAdapter?.audioList = audiosList
                audiosAdapter?.notifyDataSetChanged()
            }
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
            tempPathList.clear()
            for(i in selectedAudios)
                i.isSelected = false
            selectedAudios.clear()
            audiosAdapter?.audioList = audiosList
            audiosAdapter?.notifyDataSetChanged()
        }
    }

    override fun onAudioDeselected(audio: HiddenFiles) {
        selectedAudios.remove(audio)
        if(selectedAudios.size == 0)
        {
            bottom_view?.doGone()
            rl_select_all?.doGone()
            btn_upload?.doVisible()
            setActionModeValue(false)
        }
        img_select_all?.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.img_deselect, null))
    }

    override fun onAudioSelected(audio: HiddenFiles) {
        selectedAudios.add(audio)
        if(selectedAudios.size == audiosList.size)
        {
            selectAll = true
            img_select_all?.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.ic_select, null))
        }
        bottom_view?.doVisible()
        btn_upload?.doGone()
        rl_select_all?.doVisible()
    }


    override fun setDirectoryToFolder(folderName: String?,position : Int?) {
        launch {
            val operation = async(Dispatchers.IO)
            {
                for(i in selectedAudios)
                {
                    val path = folderName+"/"+File(i.path).name
                    val moved = StorageUtils.move(i.path, path)
                    if(moved)
                    {
                        HiddenFilesDatabase.getInstance(requireContext()).hiddenFilesDao.updateFilePath(i.path,path,System.currentTimeMillis())
                    }
                }
                selectedAudios.clear()
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

    fun refreshData() {
            isActionMode = false
            audiosList.clear()
            btn_upload?.doVisible()
            selectAll = false
            model?.getMyAudiosFolders()
            launch {
                val operation = async(Dispatchers.IO)
                {
                    getChildFiles()
                }
                operation.await()
                withContext(Dispatchers.Main)
                {
                    if(currentPath == StorageUtils.getAudiosHiderDirectory())
                    {
                        doExit = true
                        img_title?.doVisible()
                    }
                    else
                    {
                        doExit = false
                        img_title?.doGone()
                    }
                    folder_name.text = File(currentPath!!).name
                    if(audiosAdapter == null)
                    {
                        if(audiosList!=null && audiosList.size>0)
                        {
                            audiosAdapter = AdapterForAudios(
                                    audiosList,
                                    recycler_view = recycler_view,
                                    requireContext(),
                                    this@HiddenAudiosFragment,
                                    this@HiddenAudiosFragment,
                            )
                            recycler_view?.adapter = audiosAdapter
                        }
                    }
                    else
                    {
                        audiosAdapter?.audioList = audiosList
                        audiosAdapter?.notifyDataSetChanged()
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
            val newPath = StorageUtils.getAudiosHiderDirectory()
            val operation = async(Dispatchers.IO)
            {
                for(i in selectedAudios)
                {
                    val path = newPath+"/"+File(i.path).name
                    val moved = StorageUtils.move(i.path, path)
                    if(moved)
                    {
                        HiddenFilesDatabase.getInstance(requireContext()).hiddenFilesDao.updateFilePath(i.path,path,System.currentTimeMillis())
                    }
                }
                selectedAudios.clear()
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
            val temp : File?=  File(StorageUtils.getAudiosHiderDirectory() +"/"+view1.folder_name?.text?.toString())
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
            if(selectedAudios.size>0)
            {
                for(i in audiosList)
                    i.isSelected = false
                selectedAudios.clear()
                audiosAdapter?.notifyDataSetChanged()
            }
        }
    }

}