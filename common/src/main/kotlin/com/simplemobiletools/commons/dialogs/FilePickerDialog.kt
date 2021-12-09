package com.simplemobiletools.commons.dialogs

import android.os.Environment
import android.os.Parcelable
import android.view.KeyEvent
import androidx.appcompat.app.AlertDialog
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.activities.BaseSimpleActivity
import com.simplemobiletools.commons.adapters.FilepickerItemsAdapter
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.helpers.ensureBackgroundThread
import com.simplemobiletools.commons.models.FileDirItem
import com.simplemobiletools.commons.views.Breadcrumbs
import kotlinx.android.synthetic.main.dialog_filepicker.view.*
import java.io.File
import java.util.*


class FilePickerDialog(val activity: BaseSimpleActivity,
                       var currPath: String = Environment.getExternalStorageDirectory().toString(),
                       var positiveButtonText : String,
                       var negativeButtonText : String,
                       val pickFile: Boolean = true,
                       var showHidden: Boolean = false,
                       val showFAB: Boolean = false,
                       val canAddShowHiddenButton: Boolean = false,
                       val forceShowRoot: Boolean = false,
                       val callback: (pickedPath: String) -> Unit) : Breadcrumbs.BreadcrumbsListener {

    private var mFirstUpdate = true
    private var mPrevPath = ""
    private var mScrollStates = HashMap<String, Parcelable>()
    private val mDateFormat = activity.baseConfig.dateFormat
    private val mTimeFormat = activity.getTimeFormat()
    private val internalStoragePath = activity.internalStoragePath
    private var source = currPath
    private lateinit var mDialog: AlertDialog
    private var mDialogView = activity.layoutInflater.inflate(R.layout.dialog_filepicker, null)

    init {
        source = currPath
        if (!activity.getDoesFilePathExist(currPath)) {
            currPath = activity.internalStoragePath
        }

        if (!activity.getIsPathDirectory(currPath)) {
            currPath = currPath.getParentPath()
        }

        // do not allow copying files in the recycle bin manually
        if (currPath.startsWith(activity.filesDir.absolutePath)) {
            currPath = activity.internalStoragePath
        }

        mDialogView.filepicker_breadcrumbs.apply {
            listener = this@FilePickerDialog
        }

        tryUpdateItems()

        val builder = AlertDialog.Builder(activity,R.style.MyDialogTheme)
                .setNegativeButton(negativeButtonText, null)
                .setOnKeyListener { dialogInterface, i, keyEvent ->
                    if (keyEvent.action == KeyEvent.ACTION_UP && i == KeyEvent.KEYCODE_BACK) {
                        val breadcrumbs = mDialogView.filepicker_breadcrumbs
                        if (breadcrumbs.childCount > 1) {
                            breadcrumbs.removeBreadcrumb()
                            currPath = breadcrumbs.getLastItem().path.trimEnd('/')
                            tryUpdateItems()
                        } else {
                            mDialog.dismiss()
                        }
                    }
                    true
                }

        if (!pickFile)
            builder.setPositiveButton(positiveButtonText, null)

        if (showFAB) {
            mDialogView.filepicker_fab.apply {
                beVisible()
                setOnClickListener { createNewFolder() }
            }
        }

        val secondaryFabBottomMargin = activity.resources.getDimension(if (showFAB) R.dimen.secondary_fab_bottom_margin else R.dimen.activity_margin).toInt()
        mDialogView.filepicker_fabs_holder.apply {
            (layoutParams as CoordinatorLayout.LayoutParams).bottomMargin = secondaryFabBottomMargin
        }

        mDialogView.filepicker_fab_show_hidden.apply {
            beVisibleIf(!showHidden && canAddShowHiddenButton)
            setOnClickListener {
                beGone()
                showHidden = true
                tryUpdateItems()
            }
        }

        mDialog = builder.create().apply {
            activity.setupDialogStuff(mDialogView, this, getTitle())
        }

        if (!pickFile) {
            mDialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setOnClickListener {
                verifyPath()
            }
        }
    }

    private fun getTitle() = if (pickFile) R.string.select_file else R.string.select_folder

    private fun createNewFolder() {
        CreateNewFolderDialog(activity,"OK","Cancel", currPath) {
            callback(it)
            mDialog.dismiss()
        }
    }

    private fun tryUpdateItems() {
        ensureBackgroundThread {
            getItems(currPath) {
                activity.runOnUiThread {
                    updateItems(it as ArrayList<FileDirItem>)
                }
            }
        }
    }

    private fun updateItems(items: ArrayList<FileDirItem>) {
        if (!containsDirectory(items) && !mFirstUpdate && !pickFile && !showFAB) {
            verifyPath()
            return
        }

        val sortedItems = items.sortedWith(compareBy({ !it.isDirectory }, { it.name.toLowerCase() }))
        val adapter = FilepickerItemsAdapter(activity, sortedItems, mDialogView.filepicker_list) {
            if ((it as FileDirItem).isDirectory) {
                currPath = it.path
                tryUpdateItems()
            } else if (pickFile) {
                currPath = it.path
                verifyPath()
            }
        }

        val layoutManager = mDialogView.filepicker_list.layoutManager as LinearLayoutManager
        mScrollStates[mPrevPath.trimEnd('/')] = layoutManager.onSaveInstanceState()!!

        mDialogView.apply {
            filepicker_list.adapter = adapter
            filepicker_breadcrumbs.setBreadcrumb(currPath)
            filepicker_fastscroller.setViews(filepicker_list) {
                filepicker_fastscroller.updateBubbleText(sortedItems.getOrNull(it)?.getBubbleText(context, mDateFormat, mTimeFormat) ?: "")
            }

            layoutManager.onRestoreInstanceState(mScrollStates[currPath.trimEnd('/')])
            filepicker_list.onGlobalLayout {
                filepicker_fastscroller.setScrollToY(filepicker_list.computeVerticalScrollOffset())
            }
        }

        mFirstUpdate = false
        mPrevPath = currPath
    }

    private fun verifyPath() {
        if (activity.isPathOnOTG(currPath)) {
            val fileDocument = activity.getSomeDocumentFile(currPath) ?: return
            if ((pickFile && fileDocument.isFile) || (!pickFile && fileDocument.isDirectory)) {
                sendSuccess()
            }
        } else {
            val file = File(currPath)
            if ((pickFile && file.isFile) || (!pickFile && file.isDirectory)) {
                sendSuccess()
            }
        }
    }

    private fun sendSuccess() {
        currPath = if (currPath.length == 1) {
            currPath
        } else {
            currPath.trimEnd('/')
        }
        if (source == currPath) {
            activity.toast(R.string.source_and_destination_same)
        }else if (!activity.getDoesFilePathExist(currPath)) {
            activity.toast(R.string.invalid_destination)
        }else {
            callback(currPath)
            mDialog.dismiss()
        }
    }

    private fun getItems(path: String, callback: (List<FileDirItem>) -> Unit) {
        if (activity.isPathOnOTG(path)) {
            activity.getOTGItems(path, showHidden, false, callback)
        } else {
            val lastModifieds = activity.getFolderLastModifieds(path)
            getRegularItems(path, lastModifieds, callback)
        }
    }

    private fun getRegularItems(path: String, lastModifieds: HashMap<String, Long>, callback: (List<FileDirItem>) -> Unit) {
        val items = ArrayList<FileDirItem>()
        val base = File(path)
        val files = base.listFiles()
        if (files == null) {
            callback(items)
            return
        }

        for (file in files) {
//            if(file.length()<=0){
//                return
//            }

            if (!showHidden && file.name.startsWith('.')) {
                continue
            }

            val curPath = file.absolutePath
            val curName = curPath.getFilenameFromPath()
            val size = file.length()
            var lastModified = lastModifieds.remove(curPath)
            val isDirectory = if (lastModified != null) false else file.isDirectory
            if (lastModified == null) {
                lastModified = 0    // we don't actually need the real lastModified that badly, do not check file.lastModified()
            }

            val children = if (isDirectory) file.getDirectChildrenCount(showHidden) else 0
//            if(isDirectory)
                items.add(FileDirItem(curPath, curName, isDirectory, children, size, lastModified))
        }
        callback(items)
    }

    private fun containsDirectory(items: List<FileDirItem>) = items.any { it.isDirectory }

    override fun breadcrumbClicked(id: Int) {
        if (id == 0) {
            if (activity.hasExternalSDCard() || activity.hasOTGConnected()) {
                StoragePickerDialog(activity, currPath, forceShowRoot) {
//                currPath = it      //For SD Card And Otg
                    currPath = internalStoragePath
                    tryUpdateItems()
                }
            }else {
                currPath = internalStoragePath
                tryUpdateItems()
            }
        }else {
            val item = mDialogView.filepicker_breadcrumbs.getChildAt(id).tag as FileDirItem
            if (currPath != item.path.trimEnd('/')) {
                currPath = item.path
                tryUpdateItems()
            }
        }
    }
}
