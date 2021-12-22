package com.simplemobiletools.filemanager.pro

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.simplemobiletools.filemanager.pro.adapters.AdapterForRecentFiles
import com.simplemobiletools.filemanager.pro.adapters.MoreItemAdapter
import com.simplemobiletools.filemanager.pro.models.ListItem
import kotlinx.android.synthetic.main.fragment_more_item.*
import kotlinx.android.synthetic.main.this_is_it.*


class MoreItemFragment : Fragment() {

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

        moreItemAdapter = MoreItemAdapter(requireActivity() ,arrayList)
        more_items_rv?.adapter = moreItemAdapter
    }


}