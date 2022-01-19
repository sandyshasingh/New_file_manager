package com.simplemobiletools.commons

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter

class ImagePagerAdapter(fragmentManager: FragmentManager,var listOfFiles : ArrayList<ListItem>?,var onPagerItemsClickLister: OnPagerItemsClickLister)  : FragmentStatePagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT){

    override fun getCount(): Int {
        return if(listOfFiles !=null && listOfFiles?.size!!>0)
            listOfFiles?.size?:0

        else 0
    }

    override fun getItem(position: Int): Fragment {
        DataHolderforImageViewer.mfinalValues = listOfFiles
        val fragment =
            ViewPagerFragment.newInstance(position)

        fragment.onPagerItemsClickLister = onPagerItemsClickLister
        return fragment
    }

    override fun getItemPosition(any : Any): Int {
        return POSITION_NONE
    }
}