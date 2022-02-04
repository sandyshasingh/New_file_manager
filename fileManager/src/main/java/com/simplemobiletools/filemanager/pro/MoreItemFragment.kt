package com.simplemobiletools.filemanager.pro

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.simplemobiletools.commons.*
import com.simplemobiletools.commons.extensions.beGone
import com.simplemobiletools.commons.extensions.getMimeTypeFromUri
import com.simplemobiletools.commons.helpers.SORT_BY_SIZE
import com.simplemobiletools.commons.models.FileDirItem
import com.simplemobiletools.filemanager.pro.activities.FileManagerMainActivity
import com.simplemobiletools.filemanager.pro.adapters.ItemsListAdapter
import com.simplemobiletools.filemanager.pro.adapters.MoreItemAdapter
import com.simplemobiletools.filemanager.pro.extensions.config
import kotlinx.android.synthetic.main.fragment_items_list.*
import kotlinx.android.synthetic.main.fragment_items_list.view.*
import kotlinx.android.synthetic.main.fragment_more_item.*
import kotlinx.android.synthetic.main.this_is_it.*
import java.io.File
import java.util.HashMap


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

    fun searchInMore(text: String){
        var uptext = text.toUpperCase()
        var listItem:ArrayList<ListItem> = ArrayList()
        for (value in arrayList!!)
        {
//            if (value.isDirectory){
//                val aa = searchFiles(text,value.mPath)
//                listItem.addAll(aa)
////                value.
////                        getfiledir
//            }
            if (value.mName.toUpperCase().contains("$uptext"))
                listItem.add(value)
        }
        more_items_rv?.doVisible()
        zrp?.beGone()
        if (listItem.isEmpty()){
            zrp?.doVisible()
            more_items_rv?.doGone()
        }

        else
        {
            getRecyclerAdapter()?.updateItems(listItem)
        }


    }

    private fun getRecyclerAdapter() = more_items_rv.adapter as? MoreItemAdapter

}