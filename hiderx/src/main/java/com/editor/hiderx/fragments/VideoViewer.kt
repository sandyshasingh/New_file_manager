package com.editor.hiderx.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.editor.hiderx.R
import com.editor.hiderx.activity.CameraFolderActivity
import com.editor.hiderx.activity.FilemanagerActivity
import com.editor.hiderx.activity.PhotosActivity
import com.editor.hiderx.activity.VideosActivity
import com.editor.hiderx.adapters.VideoPagerAdapter
import com.editor.hiderx.database.HiddenFiles
import com.editor.hiderx.dataclass.FileDataClass
import com.editor.hiderx.listeners.OnPagerItemsClickLister
import kotlinx.android.synthetic.main.fragment_photo_viewer.*


private const val ARG_IMAGE_LIST = "HIDDEN_IMAGE_LIST"
private const val ARG_FILES_LIST = "HIDDEN_FILES_LIST"
private const val ARG_POS = "POSITION_EXTRA"


class VideoViewer : Fragment(),OnPagerItemsClickLister {

    private var listOfPaths : ArrayList<HiddenFiles>? = null
    private var listOfFilePaths : ArrayList<FileDataClass>? = null
    private var position : Int = 0
    private var videoPagerAdapter : VideoPagerAdapter?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            if(arguments?.containsKey(ARG_IMAGE_LIST) == true)
            listOfPaths = it.getSerializable(ARG_IMAGE_LIST) as ArrayList<HiddenFiles>
            else if(arguments?.containsKey(ARG_FILES_LIST) == true)
            listOfFilePaths = it.getSerializable(ARG_FILES_LIST) as ArrayList<FileDataClass>
            position = it.getInt(ARG_POS)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_photo_viewer, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if(listOfPaths!=null && listOfPaths?.size!!>0)
        {
            videoPagerAdapter = VideoPagerAdapter(activity?.supportFragmentManager!!,listOfPaths!!,null,this)
            photo_view_pager?.adapter = videoPagerAdapter
            photo_view_pager?.currentItem = position
        }
        else if(listOfFilePaths!=null && listOfFilePaths?.size!!>0)
        {
            videoPagerAdapter = VideoPagerAdapter(activity?.supportFragmentManager!!,null,listOfFilePaths,this)
            photo_view_pager?.adapter = videoPagerAdapter
            photo_view_pager?.currentItem = position
        }
    }


    companion object {

        @JvmStatic
        fun newInstance(
            data: ArrayList<HiddenFiles>?,
            position: Int,
            hiddenFiles: ArrayList<FileDataClass>?,
        ) =
            VideoViewer().apply {
                arguments = Bundle().apply {
                    data?.let{putSerializable(ARG_IMAGE_LIST,data)}
                    hiddenFiles?.let{putSerializable(ARG_FILES_LIST,hiddenFiles)}
                    putInt(ARG_POS,position)
                }
            }

    }

    override fun onItemRemoved(hiddenFiles: HiddenFiles) {
        listOfPaths?.let{
            it.remove(hiddenFiles)
            if(it.size == 0)
            {
                when{
                    (activity is PhotosActivity) -> (activity as PhotosActivity).onBackPressed()
                    (activity is CameraFolderActivity) -> (activity as CameraFolderActivity).onBackPressed()
                    (activity is FilemanagerActivity) -> (activity as FilemanagerActivity).onBackPressed()
                    else -> activity?.onBackPressed()
                }
            }
            else
            {
                videoPagerAdapter?.listOfFiles = listOfPaths
                videoPagerAdapter?.notifyDataSetChanged()
                photo_view_pager?.invalidate()
            }
        }
    }

    override fun onFileRemoved(fileDataClass: FileDataClass) {
        listOfFilePaths?.let{
            it.remove(fileDataClass)
            if(it.size == 0)
            {
                when{
                    (activity is VideosActivity) -> (activity as VideosActivity).onBackPressed()
                    (activity is CameraFolderActivity) -> (activity as CameraFolderActivity).onBackPressed()
                    (activity is FilemanagerActivity) -> (activity as FilemanagerActivity).onBackPressed()
                    else -> activity?.onBackPressed()
                }
            }
            else
            {
                videoPagerAdapter?.listOfFilePaths = listOfFilePaths
                videoPagerAdapter?.notifyDataSetChanged()
                photo_view_pager?.invalidate()
            }
        }    }

}