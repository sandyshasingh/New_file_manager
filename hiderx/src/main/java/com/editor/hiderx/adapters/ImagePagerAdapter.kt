package com.editor.hiderx.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.PagerAdapter
import com.editor.hiderx.database.HiddenFiles
import com.editor.hiderx.dataclass.FileDataClass
import com.editor.hiderx.fragments.ViewPagerFragment
import com.editor.hiderx.listeners.OnPagerItemsClickLister

class ImagePagerAdapter(fragmentManager: FragmentManager,var listOfFiles : List<HiddenFiles>?,var listOfFilePaths : ArrayList<FileDataClass>?,var onPagerItemsClickLister: OnPagerItemsClickLister)  : FragmentStatePagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT){

    override fun getCount(): Int {
        return if(listOfFiles !=null && listOfFiles?.size!!>0)
            listOfFiles?.size?:0
        else if(listOfFilePaths != null && listOfFilePaths?.size!!>0)
            listOfFilePaths?.size?:0
        else 0
    }

    override fun getItem(position: Int): Fragment {
        val fragment = if(listOfFiles !=null && listOfFiles?.size!!>0)
            ViewPagerFragment.newInstance(listOfFiles?.get(position), null)
        else
            ViewPagerFragment.newInstance(
                null,
                listOfFilePaths?.get(position),
            )
        fragment.onPagerItemsClickLister = onPagerItemsClickLister
        return fragment
    }

    override fun getItemPosition(any : Any): Int {
        return POSITION_NONE
    }
}