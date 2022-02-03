package com.simplemobiletools.filemanager.pro

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.simplemobiletools.commons.DataHolderforImageViewer
import com.simplemobiletools.commons.ListItem
import com.simplemobiletools.commons.VideoDataHolder
import com.simplemobiletools.commons.extensions.getMimeTypeFromUri
import com.simplemobiletools.filemanager.pro.activities.FileManagerMainActivity
import com.simplemobiletools.filemanager.pro.adapters.MoreItemAdapter
import kotlinx.android.synthetic.main.fragment_more_item.*
import kotlinx.android.synthetic.main.this_is_it.*


class MoreItemFragment : Fragment(),MoreItemAdapter.MoreItemsListener {

    var moreItemAdapter:MoreItemAdapter?=null
    var arrayList : List<ListItem>? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {



        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_more_item, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        moreItemAdapter = MoreItemAdapter(requireActivity(),arrayList,this@MoreItemFragment)
        more_items_rv?.adapter = moreItemAdapter
    }

    override fun moreItems(item: ListItem, position: Int) {
        var mime =  context?.getMimeTypeFromUri(Uri.parse(item.path))
        var imageslist:ArrayList<ListItem> = ArrayList()
        var videoslist:ArrayList<ListItem> = ArrayList()
        if(mime?.contains("image") == true){
            for (value in arrayList!!){
                if(context?.getMimeTypeFromUri(Uri.parse(value.mPath))?.contains("image") == true)
                    imageslist.add(value)
            }
            DataHolderforImageViewer.mfinalValues = imageslist
            (activity as? FileManagerMainActivity)?.loadPhotoViewerFragment(imageslist,position )
        }
        else if (mime?.contains("video") == true){
            for (value in arrayList!!){
                if(context?.getMimeTypeFromUri(Uri.parse(value.mPath))?.contains("video") == true)
                    videoslist.add(value)
            }
            VideoDataHolder.data = videoslist
            (activity as? FileManagerMainActivity)?.startVideoPlayer(position)
        }
    }


}