package com.editor.hiderx.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.editor.hiderx.R
import com.editor.hiderx.activity.CameraFolderActivity
import com.editor.hiderx.adapters.SectionsPagerAdapter
import com.editor.hiderx.database.HiddenFiles
import com.editor.hiderx.listeners.onUploadClickListenerForCamera
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.fragment_camera_folder.*
import java.text.FieldPosition

class CameraFolderFragment : Fragment(), onUploadClickListenerForCamera {

    private var pagerAdapter: SectionsPagerAdapter? = null
    var onUploadClickListenerForCamera : onUploadClickListenerForCamera? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_camera_folder, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btn_back?.setOnClickListener()
        {
            (activity as CameraFolderActivity).onBackPressed()
        }
        tabs?.addTab(tabs.newTab().setText("Photos"))
        tabs?.addTab(tabs.newTab().setText("Videos"))
        tabs?.addTab(tabs.newTab().setText("Folders"))
        tabs.tabGravity = TabLayout.GRAVITY_START
        pagerAdapter = SectionsPagerAdapter(activity?.supportFragmentManager,3,this)
        view_pager?.adapter = pagerAdapter
        view_pager?.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabs))
        tabs?.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {

            override fun onTabSelected(tab: TabLayout.Tab) {
                view_pager?.currentItem = tab.position
                val currentPosition =view_pager?.currentItem!!
                val placeHolderFragment =
                   view_pager?.adapter?.instantiateItem(
                        view_pager!!,
                        currentPosition
                    ) as PlaceholderFragment
                placeHolderFragment.refreshData(placeHolderFragment.currentPath)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {

            }

            override fun onTabReselected(tab: TabLayout.Tab) {

            }

        })

    }

    override fun onFileClicked(hiddenFiles: List<HiddenFiles>,position: Int) {
        onUploadClickListenerForCamera?.onFileClicked(hiddenFiles,position)
    }

    override fun onUploadPhotoClicked(path : String) {
        onUploadClickListenerForCamera?.onUploadPhotoClicked(path)
    }

    override fun onUploadVideoClicked(path : String) {
        onUploadClickListenerForCamera?.onUploadVideoClicked(path)
    }

}