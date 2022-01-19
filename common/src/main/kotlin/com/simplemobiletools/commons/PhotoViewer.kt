package com.simplemobiletools.commons

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import kotlinx.android.synthetic.main.fragment_photo_viewer.*


private const val ARG_IMAGE_LIST = "IMAGE_LIST"
private const val ARG_FILES_LIST = "HIDDEN_FILES_LIST"
private const val ARG_POS = "POSITION_EXTRA"


class PhotoViewer : Fragment(),OnPagerItemsClickLister {

    private var listOfPaths : ArrayList<ListItem>? = null
    private var position : Int = 0
    private var imagePagerAdapter : ImagePagerAdapter?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            if(arguments?.containsKey(ARG_IMAGE_LIST) == true)
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
        listOfPaths = DataHolderforImageViewer.mfinalValues

            imagePagerAdapter =
                activity?.supportFragmentManager?.let { ImagePagerAdapter(it,listOfPaths,this) }
            photo_view_pager?.adapter = imagePagerAdapter
            photo_view_pager?.currentItem = position


    }


    companion object {

        @JvmStatic
        fun newInstance(
            position: Int
        ) =
            PhotoViewer().apply {
                arguments = Bundle().apply {
//                    hiddenFiles?.let{putSerializable(ARG_FILES_LIST,hiddenFiles)}
                    putInt(ARG_POS,position)
                }

            }

    }

    override fun onFileRemoved(fileDataClass: ListItem) {
        listOfPaths?.let{
            it.remove(fileDataClass)
            if(it.size == 0)
            {
                when{

                    else -> activity?.onBackPressed()
                }
            }
            else
            {
                imagePagerAdapter?.listOfFiles = listOfPaths
                imagePagerAdapter?.notifyDataSetChanged()
                photo_view_pager?.invalidate()
            }
        }
    }

//    override fun onFileRemoved(fileDataClass: FileDataClass) {
//        listOfFilePaths?.let{
//            it.remove(fileDataClass)
//            if(it.size == 0)
//            {
//                when{
//                    (activity is PhotosActivity) -> (activity as PhotosActivity).onBackPressed()
//                    (activity is CameraFolderActivity) -> (activity as CameraFolderActivity).onBackPressed()
//                    (activity is FilemanagerActivity) -> (activity as FilemanagerActivity).onBackPressed()
//                    else -> activity?.onBackPressed()
//                }
//            }
//            else
//            {
//                imagePagerAdapter?.listOfFilePaths = listOfFilePaths
//                imagePagerAdapter?.notifyDataSetChanged()
//                photo_view_pager?.invalidate()
//            }
//        }    }

}