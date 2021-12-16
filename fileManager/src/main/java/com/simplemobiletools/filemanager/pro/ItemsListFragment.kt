package com.simplemobiletools.filemanager.pro

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.simplemobiletools.commons.adapters.AdapterForStorage
import com.simplemobiletools.filemanager.pro.adapters.ItemsAdapter
import kotlinx.android.synthetic.main.fragment_items_list.*
import kotlinx.android.synthetic.main.this_is_it.*


class ItemsListFragment : Fragment() {

    var itemsAdapter : ItemsAdapter? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_items_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        main_items_rv?.layoutManager = LinearLayoutManager(activity,
//            LinearLayoutManager.VERTICAL,false)
//        itemsAdapter = ItemsAdapter(storageItems,requireActivity() )
//        rv_storage?.adapter = itemsAdapter
    }


}