package com.editor.hiderx.fragments

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.editor.hiderx.*
import com.editor.hiderx.StorageUtils.getPhotosHiderDirectory
import com.editor.hiderx.activity.CameraFolderActivity
import com.editor.hiderx.activity.FilemanagerActivity
import com.editor.hiderx.activity.PhotosActivity
import com.editor.hiderx.adapters.AdapterForPhotosOrVideos
import com.editor.hiderx.adapters.BottomViewFoldersAdapter
import com.editor.hiderx.database.HiddenFiles
import com.editor.hiderx.database.HiddenFilesDatabase
import com.editor.hiderx.dataclass.SimpleDataClass
import com.editor.hiderx.listeners.FragmentInteractionListener
import com.editor.hiderx.listeners.OnItemSelectedListener
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.bottom_layout.*
import kotlinx.android.synthetic.main.bottom_layout.img_cross
import kotlinx.android.synthetic.main.folder_bottom_sheet.view.*
import kotlinx.android.synthetic.main.folder_bottom_sheet.view.img_cross
import kotlinx.android.synthetic.main.fragment_upload_photos.*
import kotlinx.android.synthetic.main.layout_photos.recycler_view
import kotlinx.android.synthetic.main.new_folder_dialog.view.*
import kotlinx.coroutines.*
import java.io.File
import android.view.ViewTreeObserver
import android.view.ViewTreeObserver.OnGlobalLayoutListener


const val PARAM_HIDER_DIRECTORY = "PARAM_HIDER_DIRECTORY"
class UploadPhotosFragment : Fragment(), OnItemSelectedListener,CoroutineScope by MainScope(), FragmentInteractionListener {

    private var dialog: BottomSheetDialog? = null
    private var photosAdapter: AdapterForPhotosOrVideos? = null
    private var model: DataViewModel? = null
    private  var photosList : List<SimpleDataClass> = ArrayList()
    private var folderData : HashMap<SimpleDataClass, ArrayList<SimpleDataClass>> = HashMap()
    private var mContext : Context? = null
    var selectedImages : ArrayList<SimpleDataClass> = ArrayList()
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
        model?.getAllPhotos()
        model?.getMyPhotosFolders()
        xhiderDirectory = arguments?.getString(PARAM_HIDER_DIRECTORY,getPhotosHiderDirectory())!!
        mSelectedFolder = xhiderDirectory
        progressDialog?.show()
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_upload_photos, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        selected_folder_text?.text = File(xhiderDirectory).name
        btn_back?.setOnClickListener()
        {
           destroyFragment()
        }
        var arrayList : Array<SimpleDataClass>? = null
        val selectedValue = "Recent added"
        val folderNames = ArrayList<String>()
        folderNames.add(selectedValue)
        val adapter: ArrayAdapter<String> = ArrayAdapter(mContext!!, android.R.layout.simple_spinner_item, folderNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, i: Int, l: Long) {
                (spinner?.selectedView as TextView?)?.setTextColor(ResourcesCompat.getColor(resources, R.color.white, null))
                when (i) {
                    0 -> photosAdapter?.itemsList = photosList
                    else -> photosAdapter?.itemsList = folderData[arrayList?.get(i - 1)]!!
                }
                photosAdapter?.notifyDataSetChanged()
            }
            override fun onNothingSelected(adapterView: AdapterView<*>?) {
                (spinner?.selectedView as TextView?)?.setTextColor(ResourcesCompat.getColor(resources, R.color.white, null))
            }
        }
        spinner?.viewTreeObserver?.addOnGlobalLayoutListener {
            (spinner?.selectedView as TextView?)?.setTextColor(Color.WHITE) //change to your color
        }
        spinner?.adapter = adapter
        recycler_view?.layoutManager = GridLayoutManager(
                context, 2)
        photosAdapter = AdapterForPhotosOrVideos(photosList, recycler_view, requireContext(), this,null)
        recycler_view?.adapter = photosAdapter

        model?.allPhotos?.observe(requireActivity())
        {
            progressDialog?.hide()
            progressDialog = null
            if(it!=null && it.isNotEmpty())
            {
                photosList = it
                photosAdapter?.itemsList = it
                photosAdapter?.notifyDataSetChanged()
            }
        }
        model?.myPhotosFolders?.observe(requireActivity())
        {
            myFolders = it
        }

        model?.imageFolderData?.observe(requireActivity())
        {
            if (mContext != null) {
                folderData = it
                arrayList = it.keys.toTypedArray()
                if(arrayList != null && arrayList?.isNotEmpty()!!)
                {
                    for(i in arrayList!!)
                        folderNames.add(i.name!!)
                    adapter.notifyDataSetChanged()
                  //  (spinner?.selectedView as TextView?)?.setTextColor(ResourcesCompat.getColor(resources, R.color.white, null))
                }
            }
        }
        prepareBottomSheetView()
    }

    override fun onItemSelected(image: SimpleDataClass) {
        selectedImages.add(image)
        bottom_view?.visibility = View.VISIBLE
        tv_selected_count?.text = selectedImages?.size?.toString() +" Selected"
        launch {
            withContext(Dispatchers.IO)
            {
                for(i in photosList)
                {
                    if(i.path == image.path)
                        i.isSelected = true
                }
                for(i in folderData.values)
                {
                    for(j in i)
                    {
                        if(j.path == image.path)
                            j.isSelected = true
                    }
                }
            }
        }
    }

    private fun prepareBottomSheetView() {
        ll_hide_items?.setOnClickListener()
        {
            if(mediaScanner == null)
                mediaScanner = MediaScanner(context)
            launch {
                val operation = async(Dispatchers.IO)
                {
                    for(i in selectedImages)
                    {
                        moveFileToPrivateFolder(i, xhiderDirectory)
                    }
                    HiderUtils.setLongSharedPreference(requireContext(),HiderUtils.Last_File_Insert_Time,System.currentTimeMillis())
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
            dialogView.rv_folders_to_hide?.adapter = BottomViewFoldersAdapter(getPhotosHiderDirectory(),myFolders, this)
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
        photosAdapter?.deselectAll()
        selectedImages.clear()
        deSelectAll()
        tv_selected_count?.text = selectedImages?.size?.toString() +" Selected"
    }

    private fun deSelectAll() {
        for(i in photosList)
        {
            i.isSelected = false
        }
        for(i in folderData.keys)
        {
            var photos = folderData[i]
            if(photos!=null)
            {
                for(i in photos)
                {
                    i.isSelected = false
                }
            }
        }
    }

    override fun showNewFolderDialog() {
        dismissDialog()
        val view1 = layoutInflater.inflate(R.layout.new_folder_dialog, null)
        var container : AlertDialog? = null
        val dialog = AlertDialog.Builder(context)
       view1?.tv_ok?.setOnClickListener()
        {
            val temp : File?=  File(getPhotosHiderDirectory()+"/"+view1.folder_name?.text?.toString())
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
                    is PhotosActivity -> (activity as PhotosActivity).onBackPressed()
                    is CameraFolderActivity -> (activity as CameraFolderActivity).onBackPressed()
                    is FilemanagerActivity -> (activity as FilemanagerActivity).onBackPressed()
                }
            }
        }
    }

    private fun moveFileToPrivateFolder(i: SimpleDataClass, xhiderDirectory: String) {
            val filename: String? = getFileNameFromPath(i.path!!)
            val newFilename = StorageUtils.encode(filename!!, StorageUtils.offset)
            val newPath: String = "$xhiderDirectory/$newFilename"
            var moved = false
            try {
                moved = StorageUtils.move(i.path!!, newPath)
                if (moved) {
                    mediaScanner?.scan(newPath)
                    val item : HiddenFiles = HiddenFiles(newPath, filename, i.path!!, StorageUtils.format(File(i.path!!).length().toDouble(),2), "image/*", System.currentTimeMillis(), false,true,0)
                    HiddenFilesDatabase.getInstance(requireContext()).hiddenFilesDao.insertFile(item)
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Log.d("@ASHISH", e.toString())
            }
    }

    fun getFileNameFromPath(path: String): String? {
        return try {
            path.substring(path.lastIndexOf("/") + 1)
        } catch (e: java.lang.Exception) {
            "video_file" + System.currentTimeMillis()
        }
    }

    override fun onItemDeselected(image: SimpleDataClass)
    {
        var index = -1
        for(i in 0 until selectedImages.size)
        {
            if(selectedImages[i].path == image.path)
            {
                index = i
                break
            }
        }
        if(index>=0)
        selectedImages.removeAt(index)
        if(selectedImages.isEmpty())
                    bottom_view?.doGone()
        tv_selected_count?.text = selectedImages?.size?.toString() +" Selected"
        launch {
            withContext(Dispatchers.IO)
            {
                for(i in photosList)
                {
                    if(i.path == image.path)
                        i.isSelected = false
                }
                for(i in folderData.values)
                {
                    for(j in i)
                    {
                        if(j.path == image.path)
                            j.isSelected = false
                    }
                }
            }
        }
    }

    override fun onItemClicked(listOfFiles: List<SimpleDataClass>,position:Int) {

    }


    override fun dismissDialog() {
        dialog?.dismiss()
    }

    override fun getSelectedFolder(): String? {
        return mSelectedFolder
    }

    override fun setDirectoryToDefault() {
        xhiderDirectory = getPhotosHiderDirectory()
        dismissDialog()
        mSelectedFolder = xhiderDirectory
        selected_folder_text?.text = xhiderDirectory.substringAfterLast("/")
    }

    override fun setDirectoryToFolder(folderPath : String?, position : Int?) {
        val temp : File?= File(folderPath!!)
        if(!temp?.exists()!!)
            temp.mkdir()
        xhiderDirectory = temp.path
        mSelectedFolder = xhiderDirectory
        selected_folder_text?.text = xhiderDirectory.substringAfterLast("/")
    }



    companion object
    {
        fun getInstance(hiderDirectory : String) : UploadPhotosFragment
        {
            return UploadPhotosFragment().apply {
                arguments = Bundle().apply { putString(PARAM_HIDER_DIRECTORY,hiderDirectory) }
            }
        }
    }
}


