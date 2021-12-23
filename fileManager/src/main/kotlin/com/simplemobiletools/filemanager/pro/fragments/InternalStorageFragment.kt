package com.simplemobiletools.filemanager.pro.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.simplemobiletools.commons.activities.BaseSimpleActivity
import com.simplemobiletools.commons.models.FileDirItem
import com.simplemobiletools.filemanager.pro.R
import com.simplemobiletools.filemanager.pro.extensions.config

class InternalStorageFragment: Fragment() {

    var currentPath = ""


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_internal_storage, container, false)
    }

//    fun openPath(path: String, forceRefresh: Boolean = false) {
//        if (!isAdded || (activity as BaseSimpleActivity).isAskingPermissions) {
//            return
//        }
//        showDialog()
//        var realPath = path.trimEnd('/')
//        if (realPath.isEmpty()) {
//            realPath = "/"
//        }
//
//        isHeaderShow = realPath == activity?.config?.internalStoragePath
//
//        scrollStates[currentPath] = getScrollState()!!
//        currentPath = realPath
//        showHidden = requireContext().config.shouldShowHidden
//        getItems(currentPath) { originalPath, listItems ->
//            if (currentPath != originalPath || !isAdded) {
//                return@getItems
//            }
//
//            FileDirItem.sorting = requireContext().config.getFolderSorting(currentPath)
//            listItems.sort()
//            activity?.runOnUiThread {
//                activity?.invalidateOptionsMenu()
//                addItems(listItems, isHeaderShow)
//                if (context != null && currentViewType != requireContext().config.viewType) {
//                    setupLayoutManager()
//                }
//            }
//        }
//    }
}