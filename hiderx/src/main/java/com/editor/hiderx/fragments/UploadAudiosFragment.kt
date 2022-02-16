package com.editor.hiderx.fragments

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.editor.hiderx.*
import com.editor.hiderx.StorageUtils.getAudiosHiderDirectory
import com.editor.hiderx.activity.AudiosActivity
import com.editor.hiderx.activity.CameraFolderActivity
import com.editor.hiderx.activity.FilemanagerActivity
import com.editor.hiderx.adapters.AdapterForAudios
import com.editor.hiderx.adapters.BottomViewFoldersAdapter
import com.editor.hiderx.database.HiddenFiles
import com.editor.hiderx.database.HiddenFilesDatabase
import com.editor.hiderx.dataclass.SimpleDataClass
import com.editor.hiderx.listeners.FragmentInteractionListener
import com.editor.hiderx.listeners.OnAudioSelectedListener
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.android.synthetic.main.bottom_layout.*
import kotlinx.android.synthetic.main.folder_bottom_sheet.view.*
import kotlinx.android.synthetic.main.fragment_upload_audios.*
import kotlinx.android.synthetic.main.fragment_upload_audios.bottom_view
import kotlinx.android.synthetic.main.fragment_upload_audios.btn_back
import kotlinx.android.synthetic.main.fragment_upload_audios.recycler_view
import kotlinx.android.synthetic.main.fragment_upload_audios.spinner
import kotlinx.android.synthetic.main.fragment_upload_audios.tv_selected_count
import kotlinx.android.synthetic.main.fragment_upload_videos.*
import kotlinx.android.synthetic.main.new_folder_dialog.view.*
import kotlinx.coroutines.*
import java.io.File


class UploadAudiosFragment : Fragment(), OnAudioSelectedListener,CoroutineScope by MainScope(), FragmentInteractionListener {

    private val currentPath : String = ""
    private var dialog: BottomSheetDialog? = null
    private var adapter: AdapterForAudios? = null
    private var model: DataViewModel? = null
    private  var audiosList : List<HiddenFiles> = ArrayList()
    private var folderData : HashMap<SimpleDataClass, ArrayList<HiddenFiles>> = HashMap()
    private var mContext : Context? = null
    var selectedAudios : ArrayList<HiddenFiles> = ArrayList()
    var xhiderDirectory = ""
    var myFolders : ArrayList<String>? = null
    var mSelectedFolder : String? = "Default"
    var mediaScanner : MediaScanner? = null
    var progressDialog : AppProgressDialog? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if(mContext == null)
        {
            mContext = context
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        model =  ViewModelProvider(requireActivity()).get(DataViewModel::class.java)
        progressDialog = AppProgressDialog(requireContext())
        model?.getAllAudios()
        model?.getMyAudiosFolders()
        xhiderDirectory = arguments?.getString(PARAM_HIDER_DIRECTORY, getAudiosHiderDirectory())!!
        mSelectedFolder = xhiderDirectory
        progressDialog?.show()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_upload_audios, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var arrayList : Array<SimpleDataClass>? = null
        val selectedValue = "Recent added"
        val folderNames = ArrayList<String>()
        folderNames.add(selectedValue)
        val listAdapter: ArrayAdapter<String> = ArrayAdapter(mContext!!, android.R.layout.simple_spinner_item, folderNames)
        listAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, i: Int, l: Long) {
                (spinner?.selectedView as TextView?)?.setTextColor(ResourcesCompat.getColor(resources, R.color.white, null))
                when (i) {
                    0 -> adapter?.audioList = audiosList
                    else -> adapter?.audioList = folderData[arrayList?.get(i - 1)]!!
                }
                adapter?.notifyDataSetChanged()
            }
            override fun onNothingSelected(adapterView: AdapterView<*>?) {
                (spinner?.selectedView as TextView?)?.setTextColor(ResourcesCompat.getColor(resources, R.color.white, null))
            }
        }
        spinner?.viewTreeObserver?.addOnGlobalLayoutListener {
            (spinner?.selectedView as TextView?)?.setTextColor(Color.WHITE) //change to your color
        }
        spinner?.adapter = listAdapter
        recycler_view?.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        adapter = AdapterForAudios(audiosList, recycler_view, requireContext(), this, null)
        recycler_view?.adapter = adapter

        tv_hide?.text =getString(R.string.hide_audios)
        selected_folder_text?.text = File(xhiderDirectory).name
        btn_back?.setOnClickListener()
        {
            destroyFragment()
        }

        model?.allAudios?.observe(requireActivity())
        {
            progressDialog?.hide()
            progressDialog = null
            if (it != null && it.isNotEmpty()) {
                audiosList = it
                adapter?.audioList = it
                adapter?.notifyDataSetChanged()
            }
        }

        model?.myAudiosFolders?.observe(requireActivity())
        {
            myFolders = it
        }

        model?.audioFolderData?.observe(requireActivity())
        {
            if (mContext != null) {
                folderData = it
                arrayList = it.keys.toTypedArray()
                if(arrayList != null && arrayList?.isNotEmpty()!!)
                {
                    for (i in arrayList!!)
                        folderNames.add(i.name!!)
                    listAdapter?.notifyDataSetChanged()
                }
            }
        }
        prepareBottomSheetView()
    }

    private fun prepareBottomSheetView() {
        ll_hide_items?.setOnClickListener()
        {
            if(mediaScanner == null)
                mediaScanner = MediaScanner(context)
            launch {
                val operation = async(Dispatchers.IO)
                {
                    for(i in selectedAudios)
                    {
                        moveAudioToPrivateFolder(i, xhiderDirectory)
                    }
                    HiderUtils.setLongSharedPreference(requireContext(), HiderUtils.Last_File_Insert_Time, System.currentTimeMillis())
                }
                operation.await()
                withContext(Dispatchers.Main)
                {
                    cancelActionMode()
                    destroyFragment()
                }
            }
        }

        ll_change_location?.setOnClickListener()
        {
            val dialogView: View = layoutInflater.inflate(R.layout.folder_bottom_sheet, null)
            dialogView.rv_folders_to_hide?.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            dialogView.rv_folders_to_hide?.adapter = BottomViewFoldersAdapter(getAudiosHiderDirectory(), myFolders, this)
            dialogView.img_cross?.setOnClickListener()
            {
                dismissDialog()
            }
            dialog = BottomSheetDialog(requireContext())
            dialog?.setContentView(dialogView)
            dialog?.setCanceledOnTouchOutside(true)
            dialog?.show()
        }

        img_cross?.setOnClickListener()
        {
            cancelActionMode()
        }

    }

    fun cancelActionMode() {
        bottom_view?.doGone()
        adapter?.deselectAll()
        selectedAudios.clear()
        deSelectAll()
        tv_selected_count?.text = selectedAudios?.size?.toString() +" Selected"
    }

    private fun deSelectAll() {
        for(i in audiosList)
        {
            i.isSelected = false
        }
        for(i in folderData.keys)
        {
            val audios = folderData[i]
            if(audios!=null)
            {
                for(i in audios)
                {
                    i.isSelected = false
                }
            }
        }
    }

    override fun getSelectedFolder(): String? {
        return mSelectedFolder
    }

    override fun showNewFolderDialog() {
        dismissDialog()
        val view1 = layoutInflater.inflate(R.layout.new_folder_dialog, null)
        var container : AlertDialog? = null
        val dialog = AlertDialog.Builder(context)
       view1?.tv_ok?.setOnClickListener()
        {
            val temp : File?=  File(getAudiosHiderDirectory() + "/" + view1.folder_name?.text?.toString())
            if(temp?.exists()!!)
            {
                view1.tv_already_exists?.visibility = View.VISIBLE
            }
            else
            {
                temp.mkdir()
                xhiderDirectory = temp.path
                myFolders?.add(temp.path)
                container?.dismiss()
                setDirectoryToFolder(temp.path,0)
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


    private fun destroyFragment() {
            launch {
                withContext(Dispatchers.Main)
                {
                    when (activity) {
                        is AudiosActivity -> (activity as AudiosActivity).onBackPressed()
                        is CameraFolderActivity -> (activity as CameraFolderActivity).onBackPressed()
                        is FilemanagerActivity -> (activity as FilemanagerActivity).onBackPressed()
                    }
                }
            }
    }


    private fun moveAudioToPrivateFolder(i: HiddenFiles, xhiderDirectory: String) {
            val filename: String? = getFileNameFromPath(i.path)
            val newFilename = StorageUtils.encode(filename!!, StorageUtils.offset)
            val newPath: String = "$xhiderDirectory/$newFilename"
            var moved = false
            try {
                moved = StorageUtils.move(i.path, newPath)
                if (moved) {
                    mediaScanner?.scan(newPath)
                    val item : HiddenFiles = HiddenFiles(
                            newPath,
                            filename,
                            i.path,
                            i.size!!,
                            "audio/*",
                            System.currentTimeMillis(),
                        false,true,0
                    )
                    HiddenFilesDatabase.getInstance(requireContext()).hiddenFilesDao.insertFile(item)
                }
            } catch (e: Exception) {
                FirebaseCrashlytics.getInstance().log(e.toString())
                FirebaseCrashlytics.getInstance().recordException(e)
            }
    }

    fun getFileNameFromPath(path: String): String? {
        return try {
            path.substring(path.lastIndexOf("/") + 1)
        } catch (e: java.lang.Exception) {
            "audio_file" + System.currentTimeMillis()
        }
    }


    override fun onAudioDeselected(audio: HiddenFiles)     {
        var index = -1
        for(i in 0 until selectedAudios.size)
        {
            if(selectedAudios[i].path == audio.path)
                index = i
        }
        selectedAudios.removeAt(index)
        if(selectedAudios.isEmpty())
            bottom_view?.doGone()
        tv_selected_count?.text = selectedAudios?.size?.toString() +" Selected"
        launch {
            withContext(Dispatchers.IO)
            {
                for(i in audiosList)
                {
                    if(i.path == audio.path)
                        i.isSelected = false
                }
                for(i in folderData.values)
                {
                    for(j in i)
                    {
                        if(j.path == audio.path)
                            j.isSelected = false
                    }
                }
            }
        }
    }


    override fun onAudioSelected(audio: HiddenFiles) {
        selectedAudios.add(audio)
        bottom_view?.visibility = View.VISIBLE
        tv_selected_count?.text = selectedAudios?.size?.toString() +" Selected"
        launch {
            withContext(Dispatchers.IO)
            {
                for(i in audiosList)
                {
                    if(i.path == audio.path)
                        i.isSelected = true
                }
                for(i in folderData.values)
                {
                    for(j in i)
                    {
                        if(j.path == audio.path)
                            j.isSelected = true
                    }
                }
            }
        }
    }

    override fun onAudioClicked(audio: List<HiddenFiles>, adapterPosition: Int) {

    }

    override fun onAudioFolderClicked(audio: HiddenFiles) {

    }

    override fun dismissDialog() {
        dialog?.dismiss()
    }

    override fun setDirectoryToDefault() {
        xhiderDirectory = getAudiosHiderDirectory()
        dismissDialog()
        mSelectedFolder = xhiderDirectory
        selected_folder_text?.text = xhiderDirectory.substringAfterLast("/")
    }

    override fun setDirectoryToFolder(folderPath : String?, position: Int?) {
        val temp : File?= File(folderPath!!)
        if(!temp?.exists()!!)
            temp.mkdir()
        xhiderDirectory = temp.path
        mSelectedFolder = xhiderDirectory
        selected_folder_text?.text = xhiderDirectory.substringAfterLast("/")
    }

    companion object
    {
        fun getInstance(hiderDirectory : String) : UploadAudiosFragment
        {
            return UploadAudiosFragment().apply {
                arguments = Bundle().apply { putString(PARAM_HIDER_DIRECTORY,hiderDirectory) }
            }
        }
    }

}


