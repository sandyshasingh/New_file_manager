package com.editor.hiderx.adapters



import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.editor.hiderx.fragments.PlaceholderFragment
import com.editor.hiderx.listeners.onUploadClickListenerForCamera

class SectionsPagerAdapter(
        fm: FragmentManager?,
        var mNumOfTabs: Int,
        var onUploadClickListenerForCamera: onUploadClickListenerForCamera
) : FragmentStatePagerAdapter(fm!!, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> PlaceholderFragment.newInstance(
                    0, onUploadClickListenerForCamera)
            1 -> PlaceholderFragment.newInstance(
                    1,
                    onUploadClickListenerForCamera)
            2 -> PlaceholderFragment.newInstance(
                    2,
                    onUploadClickListenerForCamera)
            else -> {
                null!!
            }
        }
    }

    override fun getCount(): Int {
        return mNumOfTabs
    }
}