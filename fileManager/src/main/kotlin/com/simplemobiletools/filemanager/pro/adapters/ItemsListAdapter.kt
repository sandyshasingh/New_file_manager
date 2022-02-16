package com.simplemobiletools.filemanager.pro.adapters

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.util.TypedValue
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.simplemobiletools.commons.activities.BaseSimpleActivity
import com.simplemobiletools.commons.adapters.MyRecyclerViewAdapter
import com.simplemobiletools.commons.dialogs.*
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.helpers.*
import com.simplemobiletools.commons.interfaces.ItemOperationsListener
import com.simplemobiletools.commons.models.FileDirItem
import com.simplemobiletools.commons.models.FolderItem
import com.simplemobiletools.commons.setTypeFaceOpenSensSmBold
import com.simplemobiletools.commons.views.FastScroller
import com.simplemobiletools.commons.views.MyRecyclerView
import com.simplemobiletools.commons.BottomNavigationVisible
import com.simplemobiletools.commons.ListItem
import com.simplemobiletools.commons.UpdateServiceIntent
import com.simplemobiletools.filemanager.pro.ActionMenuClick
import com.simplemobiletools.filemanager.pro.ListItemDiffCallback
import com.simplemobiletools.filemanager.pro.R
import com.simplemobiletools.filemanager.pro.activities.FileManagerMainActivity
import com.simplemobiletools.filemanager.pro.dialogs.CompressAsDialog
import com.simplemobiletools.filemanager.pro.extensions.*
import com.simplemobiletools.filemanager.pro.helpers.RootHelpers
import com.stericson.RootTools.RootTools
import kotlinx.android.synthetic.main.file_manager_activity.*
import kotlinx.android.synthetic.main.item_file_dir_list.view.*
import kotlinx.android.synthetic.main.item_grid_dir.view.*
import kotlinx.android.synthetic.main.item_grid_dir.view.item_icon
import kotlinx.android.synthetic.main.item_grid_dir.view.item_name
import kotlinx.android.synthetic.main.item_section.view.*
import java.io.Closeable
import java.io.File
import java.io.FileInputStream
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream
import kotlin.collections.ArrayList

class ItemsListAdapter (activity: BaseSimpleActivity, var isClickable:ActionMenuClick?,var folderItems: ArrayList<FolderItem>,
                        var bottomListener: BottomNavigationVisible?, var listItems: MutableList<ListItem>, var listener: ItemOperationsListener?,
                        fastScroller: FastScroller?,
                        recyclerView: MyRecyclerView, var shortCutClicked:Boolean,
                        itemClick: (Any,Int) -> Unit, isAddEnabled: (Boolean) -> Unit) :
    MyRecyclerViewAdapter(activity, recyclerView, fastScroller, itemClick,isAddEnabled,bottomListener,shortCutClicked) {

    private var fontSize = 0f
    private var textToHighlight = ""
    var dialog: BottomSheetDialog? = null
    private var folderDrawable: Drawable? = null
    private var fileDrawables = HashMap<String, Drawable>()
    private lateinit var fileDrawable: Drawable
    private var isDarkTheme = false
    private var currentItemsHash = listItems.hashCode()
     var listenerUpdate: UpdateServiceIntent? = null
    private val sharedPrefFile = "com.example.new_file_manager"
    var last_login_time :Long ?=0L
    private var currentposition = 0

    init {

//       setupDragListener(true)
        isDarkTheme = activity.isDarkTheme()
        initDrawables()

        isLongPressClick = shortcutClicked
        actModeCallback.isSelectable = shortcutClicked
        shortcut = shortcutClicked
        if (shortcutClicked){
//            bottomnavigation?.visibility = View.GONE
            bottomListener?.btmVisible(false)
        }
        val sharedPreferences: SharedPreferences? = activity.getSharedPreferences(sharedPrefFile,
            Context.MODE_PRIVATE)
        last_login_time=sharedPreferences?.getLong("LAST_LOGIN",0L)

//        updateFontSizes()
    }



    private fun initDrawables() {
        folderDrawable = getFolderDrawable()
        folderDrawable?.alpha = 180
        fileDrawable = resources.getDrawable(R.drawable.ic_file_generic)
        fileDrawables = getFilePlaceholderDrawables(activity)
    }
    private fun getFolderDrawable() : Drawable?{
        return if (isDarkTheme) {
            resources.getDrawable(R.drawable.ic_file_manager_fldr)
        } else {
            resources.getDrawable(R.drawable.ic_icon_folder__light2)
        }
    }






    override fun mLongClick() {
        isLongPressClick = true

//        if(bottomnavigation == null){
//            return
//        }

        if(selectedKeys.isNotEmpty()) {
           // bottomnavigation?.visibility = View.VISIBLE
            bottomListener?.btmVisible(true)




        }

//        val mSend: LinearLayout = bottomnavigation?.findViewById(R.id.bottom_send)!!
//        val mDelete: LinearLayout = bottomnavigation?.findViewById(R.id.bottom_delete)!!
//        val mMove: LinearLayout = bottomnavigation?.findViewById(R.id.bottom_move)!!
//        val mRename: LinearLayout = bottomnavigation?.findViewById(R.id.bottom_rename)!!
//        val mProperties: LinearLayout = bottomnavigation?.findViewById(R.id.bottom_details)!!
//        val mCopyTo: LinearLayout = bottomnavigation?.findViewById(R.id.bottom_copyto)!!
//        val mCopyPath: LinearLayout = bottomnavigation?.findViewById(R.id.bottom_copy_path)!!
//        val mHide: LinearLayout = bottomnavigation?.findViewById(R.id.bottom_hide)!!
//        val mUnHide: LinearLayout = bottomnavigation?.findViewById(R.id.bottom_unhide)!!
//        val mCompress: LinearLayout = bottomnavigation?.findViewById(R.id.bottom_compress)!!
//        val mDecompress: LinearLayout = bottomnavigation?.findViewById(R.id.bottom_decompress)!!
//        val mOpenWith: LinearLayout = bottomnavigation?.findViewById(R.id.bottom_openwith)!!

//        mOpenWith.beVisibleIf(isOneFileSelected())
//        mDecompress.beVisibleIf(getSelectedFileDirItems().map { it.path }.any { it.isZipFile() })
//        mCompress.beVisibleIf(!mDecompress.isVisible())
//        mCopyPath.beVisibleIf(isOneItemSelected())
//        checkHideBtnVisibility(mHide, mUnHide, null)
//
//        mSend.setOnClickListener {
//            shareFiles(null)
//        }
//        mDelete.setOnClickListener {
//            askConfirmDelete()
//        }
//        mMove.setOnClickListener {
//            copyMoveTo(false, null)
//        }
//        mRename.setOnClickListener {
//            displayRenameDialog(null)
//        }
//        mProperties.setOnClickListener {
//            showProperties(null)
//        }
//
//        mCopyTo.setOnClickListener {
//            copyMoveTo(true, null)
//        }
//        mCopyPath.setOnClickListener {
//            copyPath(null)
//        }
//        mOpenWith.setOnClickListener {
//            openWith(null)
//        }
//        mHide.setOnClickListener {
//            toggleFileVisibility(true, null)
//        }
//        mUnHide.setOnClickListener {
//            toggleFileVisibility(false, null)
//        }
//        mCompress.setOnClickListener {
//            compressSelection(null)
//        }
//        mDecompress.setOnClickListener {
//            decompressSelection(null)
//        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = layoutInflater.inflate(R.layout.item_file_dir_list, parent, false)
        return ViewHolder(view)

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {



        val fileDirItem = listItems[position]
        if(listItems[position].modified > last_login_time!!) {
            holder.itemView.new_text.visibility = View.VISIBLE
        }
        if(holder is ViewHolder)
        {
            holder.bindView(
                fileDirItem,
                position,
                listener,
                !fileDirItem.isSectionTitle,
                !fileDirItem.isSectionTitle,

//                bottomnavigation
            ) { itemView, layoutPosition ->
                setupView(itemView, fileDirItem, holder, position)
            }
        }
    }
    override fun getItemCount(): Int {
        return listItems.size
    }

    override fun getActionMenuId() = R.menu.menu_favorites

    override fun prepareActionMode(menu: Menu) {
    }

    fun updateListItems(listItems: MutableList<ListItem>) {
        try {
            val diffCallback = ListItemDiffCallback(this.listItems, listItems)
            val diffResult = DiffUtil.calculateDiff(diffCallback)
            this.listItems = listItems
            diffResult.dispatchUpdatesTo(this)
        } catch (e: Exception) {
            this.listItems = listItems
            notifyDataSetChanged()
        }
    }
    fun updateItems(newItems: ArrayList<ListItem>, highlightText: String = "") {
        if (newItems.hashCode() != currentItemsHash) {
            currentItemsHash = newItems.hashCode()
            textToHighlight = highlightText
//            listItems = newItems.clone() as ArrayList<ListItem>
            notifyDataSetChanged()
//            (bottomnavigation)
        } else if (textToHighlight != highlightText) {
            textToHighlight = highlightText
            notifyDataSetChanged()
        }

        listItems = newItems
        notifyDataSetChanged()
        fastScroller?.measureRecyclerView()
    }

    override fun actionItemPressed(id: Int) {
        if(id==R.id.select_all) {
            if (getSelectableItemCount() == selectedKeys.size) {
                deSelectAll()
            } else {
                selectAll()
            }
        }
    }

    fun selectAllFolders(){
        if (getSelectableItemCount() == selectedKeys.size) {
            deSelectAll()
        } else {
            selectAll()
        }

    }

    override fun getSelectableItemCount() = listItems.filter { !it.isSectionTitle }.size

    override fun getIsItemSelectable(position: Int) = !listItems[position].isSectionTitle

    override fun getItemSelectionKey(position: Int) =
        listItems.getOrNull(position)?.path?.hashCode()

    override fun checkIsZipFile(position: Int) : Boolean = listItems.getOrNull(position)?.path?.endsWith(".zip") == true

    override fun getItemKeyPosition(key: Int) = listItems.indexOfFirst { it.path.hashCode() == key }

    override fun onActionModeCreated() {
        isClickable?.isClickable(false)
    }

    override fun onActionModeDestroyed() {
        isClickable?.isClickable(true)
    }


     fun copyPath(listItem: ListItem?) {

        val clip = if(listItem!=null){
            ClipData.newPlainText(activity.getString(R.string.app_name), listItem.mPath)
        }else{
            ClipData.newPlainText(activity.getString(R.string.app_name), getFirstSelectedItemPath())

        }
        (activity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(clip)
        finishActMode()
        activity.toast(R.string.path_copied)
    }

    private fun setupView(view: View, listItem: ListItem, holder: ViewHolder, position: Int) {

        val isSelected = selectedKeys.contains(listItem.path.hashCode())

        view.apply {


            if (listItem.isSectionTitle) {
                item_section.text = listItem.mName
                item_section.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSize)
            } else {
                item_frame?.isSelected = isSelected
                item_frame1?.isSelected = isSelected

                val fileName = listItem.name
                item_name?.setTypeFaceOpenSensSmBold()
                item_name?.text = if (textToHighlight.isEmpty())
                    fileName
                else
                    fileName.highlightTextPart(textToHighlight, context.resources.getColor(R.color.color_primary))

                if(isLongPressClick){
                    item_check_view?.beVisible()
                    item_check_view_grid?.beVisible()
                    //threedot?.beGone()
                    threedot_grid?.beGone()
                }
//                if(!isLongPressClick){
//                    (activity as FileManagerMainActivity).add_the_folder.visibility=View.GONE
//                }
                else{
                    item_check_view?.beGone()
                    item_check_view_grid?.beGone()
                   // threedot?.beVisible()
                    threedot_grid?.beVisible()
                }

                if(isSelected) {
                    item_check_view?.setChecked(true)
                    item_check_view_grid?.setChecked(true)
                }else{
                    item_check_view?.setChecked(false)
                    item_check_view_grid?.setChecked(false)
                }
                item_check_view?.setOnClickListener {
                    holder.viewClicked(listItem, position, false)
                }
                item_check_view_grid?.setOnClickListener{
                    holder.viewClicked(listItem, position, false)
                }

//                threedot?.setOnClickListener{
//                    showBottomSheet(listItem, position)
//                }
                threedot_grid?.setOnClickListener{
                    showBottomSheet( listItem, position)
                }

                if (listItem.isDirectory) {
                    if(folderDrawable!=null)
                        item_icon.setImageDrawable(folderDrawable)
                    else
                        item_icon.setImageDrawable(resources.getDrawable(R.drawable.ic_icon_folder__light2))

                    item_details?.text = getChildrenCnt(listItem)
                    item_count?.text = getChildrenCnt(listItem)
                } else {
                    item_details?.text = listItem.size.formatSize()
                    item_count?.text = listItem.size.formatSize()
                    val drawable = fileDrawables.getOrElse(fileName.substringAfterLast(".").toLowerCase(Locale.ROOT), { fileDrawable })
//                    Log.d("#qwe",dpToPx(16).toString())
//                    Log.d("#qwe",pxToDp(16).toString())

                    val options = RequestOptions()
                        .placeholder(drawable)
                        .transform(CenterCrop(), RoundedCorners(dpToPx(16)))

                    val itemToLoad = listItem.audioImageUri ?: getImagePathToLoad(listItem.path)

                    if (!activity.isDestroyed) {
                        Glide.with(activity)
                            .load(itemToLoad)
                            .apply(options)
                            .into(item_icon)
                    }
                }
            }
        }
    }
    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    private fun getImagePathToLoad(path: String): Any {
        var itemToLoad = if (path.endsWith(".apk", true)) {
            val packageInfo = activity.packageManager.getPackageArchiveInfo(path, PackageManager.GET_ACTIVITIES)
            if (packageInfo != null) {
                val appInfo = packageInfo.applicationInfo
                appInfo.sourceDir = path
                appInfo.publicSourceDir = path
                appInfo.loadIcon(activity.packageManager)
            } else {
                path
            }
        } else {
            path
        }

//        if (hasOTGConnected && itemToLoad is String && activity.isPathOnOTG(itemToLoad) && baseConfig.OTGTreeUri.isNotEmpty() && baseConfig.OTGPartition.isNotEmpty()) {
//            itemToLoad = getOTGPublicPath(itemToLoad)
//        }

        return itemToLoad
    }


    private fun getChildrenCnt(item: FileDirItem): String {
        val children = item.children
        return activity.resources.getQuantityString(R.plurals.items, children, children)
    }

    private fun showBottomSheet( listItem: ListItem?, position: Int) {
        dialog = BottomSheetDialog(activity)
        val dialogView = activity.layoutInflater.inflate(R.layout.file_manager_bottom_shee, null)

        dialog?.setContentView(dialogView)
        dialog?.show()

        val mCopyTo: LinearLayout = dialog?.findViewById(R.id.cab_copy_to)!!
        val mCopyPath: LinearLayout = dialog?.findViewById(R.id.cab_copy_path)!!
        val mHide: LinearLayout = dialog?.findViewById(R.id.cab_hide)!!
        val mUnHide: LinearLayout = dialog?.findViewById(R.id.cab_unhide)!!
        val mCompress: LinearLayout = dialog?.findViewById(R.id.cab_compress)!!
        val mDecompress: LinearLayout = dialog?.findViewById(R.id.cab_decompress)!!
        val mOpenWith: LinearLayout = dialog?.findViewById(R.id.cab_open_with)!!

        // For Three Dot
        val mDetails: LinearLayout = dialog?.findViewById(R.id.cab_properties)!!
        val mShare: LinearLayout = dialog?.findViewById(R.id.cab_share)!!
        val mMoveTo: LinearLayout = dialog?.findViewById(R.id.cab_move_to)!!
        val mRename: LinearLayout = dialog?.findViewById(R.id.cab_rename)!!
        val mDelete: LinearLayout = dialog?.findViewById(R.id.cab_delete)!!

        if (listItem != null) {
            mOpenWith.beVisibleIf(!listItem.isDirectory)
        }else
            mOpenWith.beVisibleIf(isOneFileSelected())

        if(listItem!=null){
            mDecompress.beVisibleIf(listItem.path.isZipFile())
        }else {
            mDecompress.beVisibleIf(getSelectedFileDirItems().map { it.path }.any { it.isZipFile() })
        }

        mCompress.beVisibleIf(!mDecompress.isVisible())
        checkHideBtnVisibility(mHide, mUnHide, listItem)

        mDelete.setOnClickListener {
            val items = resources.getQuantityString(R.plurals.delete_items, 1, 1)
            val question = String.format(resources.getString(R.string.deletion_confirmation), items)
            ConfirmationDialog(activity, question) {
                listenerUpdate?.updateDatabase(true)
                deleteFiles(listItem, position)
            }
            dialogDismiss()

        }

        mRename.setOnClickListener {
            displayRenameDialog(listItem)
            dialogDismiss()
        }
        mMoveTo.setOnClickListener {
            copyMoveTo(false, listItem)
            dialogDismiss()
        }
        mShare.setOnClickListener {
            shareFiles(listItem)
            dialogDismiss()
        }
        mDetails.setOnClickListener {
            showProperties(listItem)
            dialogDismiss()
        }

        mCopyTo.setOnClickListener {
            copyMoveTo(true, listItem)
            dialogDismiss()
        }
        mCopyPath.setOnClickListener {
            copyPath(listItem)
            dialogDismiss()
        }
        mOpenWith.setOnClickListener {
            openWith(listItem)
            dialogDismiss()
        }
        mHide.setOnClickListener {
            toggleFileVisibility(true, listItem)
            dialogDismiss()
        }
        mUnHide.setOnClickListener {
            toggleFileVisibility(false, listItem)
            dialogDismiss()
        }
        mCompress.setOnClickListener {
            compressSelection(listItem)
            dialogDismiss()
        }
        mDecompress.setOnClickListener {
            decompressSelection(listItem)
            dialogDismiss()
        }
    }

    private fun dialogDismiss() {
        if (dialog != null && dialog!!.isShowing) {
            dialog!!.dismiss()
        }
    }

     fun toggleFileVisibility(hide: Boolean, listItem: ListItem?) {
        ensureBackgroundThread {
            if(listItem!=null){
                activity.toggleItemVisibility(listItem.mPath, hide)
//                {
//                    Toast.makeText(this,"Already unhide ", Toast.LENGTH_SHORT).show()
//                }
            }else {
                getSelectedFileDirItems().forEach {
                    activity.toggleItemVisibility(it.path, hide)
                    { _, message ->
                        if (message!=null)
                        activity.toast("$message")
                    }
                }
            }
            activity.runOnUiThread {
                listener?.refreshItems(false)
                finishActMode()
            }
        }
    }

     fun compressSelection(listItem: ListItem?) {
        val firstPath = listItem?.mPath ?: getFirstSelectedItemPath()
        if (activity.isPathOnOTG(firstPath)) {
            activity.toast(R.string.unknown_error_occurred)
            return
        }

        CompressAsDialog(activity, firstPath) {
            val destination = it
            activity.handleSAFDialog(firstPath) {
                if (!it) {
                    return@handleSAFDialog
                }

                activity.toast(R.string.compressing)
                val paths = if(listItem!=null){
                    arrayListOf<String>(listItem.path)
                }else{
                    getSelectedFileDirItems().map { it.path }
                }

                ensureBackgroundThread {

                    if (compressPaths(paths, destination)) {
                        activity.runOnUiThread {
                            activity.toast(R.string.compression_successful)
                            listener?.refreshItems(false)
                            finishActMode()
                        }
                    } else {
                        activity.toast(R.string.compressing_failed)
                    }
                }
            }
        }
    }

    private fun compressPaths(sourcePaths: List<String>, targetPath: String): Boolean {
        val queue = LinkedList<File>()
        val fos = activity.getFileOutputStreamSync(targetPath, "application/zip") ?: return false

        val zout = ZipOutputStream(fos)
        var res: Closeable = fos

        try {
            sourcePaths.forEach {
                var name: String
                var mainFile = File(it)
                val base = mainFile.parentFile.toURI()
                res = zout
                queue.push(mainFile)
                if (activity.getIsPathDirectory(mainFile.absolutePath)) {
                    name = "${mainFile.name.trimEnd('/')}/"
                    zout.putNextEntry(ZipEntry(name))
                }

                while (!queue.isEmpty()) {
                    mainFile = queue.pop()
                    if (activity.getIsPathDirectory(mainFile.absolutePath)) {
                        for (file in mainFile.listFiles()) {
                            name = base.relativize(file.toURI()).path
                            if (activity.getIsPathDirectory(file.absolutePath)) {
                                queue.push(file)
                                name = "${name.trimEnd('/')}/"
                                zout.putNextEntry(ZipEntry(name))
                            } else {
                                zout.putNextEntry(ZipEntry(name))
                                FileInputStream(file).copyTo(zout)
                                zout.closeEntry()
                            }
                        }
                    } else {
                        name = if (base.path == it) it.getFilenameFromPath() else base.relativize(mainFile.toURI()).path
                        zout.putNextEntry(ZipEntry(name))
                        FileInputStream(mainFile).copyTo(zout)
                        zout.closeEntry()
                    }
                }
            }
        } catch (exception: Exception) {
            activity.showErrorToast(exception)
            return false
        } finally {
            res.close()
        }
        return true
    }

     fun decompressSelection(listItem: ListItem?) {
        val firstPath: String = listItem?.mPath ?: getFirstSelectedItemPath()

        if (activity.isPathOnOTG(firstPath)) {
            activity.toast(R.string.unknown_error_occurred)
            return
        }

        activity.handleSAFDialog(firstPath) {
            if (!it) {
                return@handleSAFDialog
            }
            val paths = if(listItem!=null){
                arrayListOf<String>(listItem.path)
            }else {
                getSelectedFileDirItems().asSequence().map { it.path }.filter { it.isZipFile() }.toList()
            }
            tryDecompressingPaths(paths) {
                if (it) {
                    activity.toast(R.string.decompression_successful)
                    activity.runOnUiThread {
                        listener?.refreshItems(false)
                        finishActMode()
                    }
                } else {
                    activity.toast(R.string.decompressing_failed)
                }
            }
        }
    }
    private fun tryDecompressingPaths(sourcePaths: List<String>, callback: (success: Boolean) -> Unit) {
        sourcePaths.forEach {
            try {
                val zipFile = ZipFile(it)
                val entries = zipFile.entries()
                val fileDirItems = ArrayList<FileDirItem>()
                while (entries.hasMoreElements()) {
                    val entry = entries.nextElement()
                    val currPath = if (entry.isDirectory) it else "${it.getParentPath().trimEnd('/')}/${entry.name}"
                    val fileDirItem = FileDirItem(currPath, entry.name, entry.isDirectory, 0, entry.size)
                    fileDirItems.add(fileDirItem)
                }

                val destinationPath = fileDirItems.first().getParentPath().trimEnd('/')
                activity.checkConflicts(fileDirItems, destinationPath, 0, LinkedHashMap()) {
                    ensureBackgroundThread {
                        decompressPaths(sourcePaths, it, callback)
                    }
                }
            } catch (exception: Exception) {
                activity.showErrorToast(exception)
            }
        }
    }

    private fun decompressPaths(paths: List<String>, conflictResolutions: LinkedHashMap<String, Int>, callback: (success: Boolean) -> Unit) {
        paths.forEach {
            try {
                val zipFile = ZipFile(it)
                val entries = zipFile.entries()
                val zipFileName = it.getFilenameFromPath()
                val newFolderName = zipFileName.subSequence(0, zipFileName.length - 4)
                while (entries.hasMoreElements()) {
                    val entry = entries.nextElement()
                    val parentPath = it.getParentPath()
                    val newPath = "$parentPath/$newFolderName/${entry.name.trimEnd('/')}"

                    val resolution = getConflictResolution(conflictResolutions, newPath)
                    val doesPathExist = activity.getDoesFilePathExist(newPath)
                    if (doesPathExist && resolution == CONFLICT_OVERWRITE) {
                        val fileDirItem = FileDirItem(newPath, newPath.getFilenameFromPath(), entry.isDirectory)
                        if (activity.getIsPathDirectory(it)) {
                            activity.deleteFolderBg(fileDirItem, false) {
                                if (it) {
                                    extractEntry(newPath, entry, zipFile)
                                } else {
                                    callback(false)
                                }
                            }
                        } else {
                            activity.deleteFileBg(fileDirItem, false) {
                                if (it) {
                                    extractEntry(newPath, entry, zipFile)
                                } else {
                                    callback(false)
                                }
                            }
                        }
                    } else if (!doesPathExist) {
                        extractEntry(newPath, entry, zipFile)
                    }
                }
                callback(true)
            } catch (e: Exception) {
                activity.showErrorToast(e)
                callback(false)
            }
        }
    }
    private fun extractEntry(newPath: String, entry: ZipEntry, zipFile: ZipFile) {
        if (entry.isDirectory) {
            if (!activity.createDirectorySync(newPath) && !activity.getDoesFilePathExist(newPath)) {
                val error = String.format(activity.getString(R.string.could_not_create_file), newPath)
                activity.showErrorToast(error)
            }
        } else {
            val ins = zipFile.getInputStream(entry)
            ins.use {
                val fos = activity.getFileOutputStreamSync(newPath, newPath.getMimeType())
                if (fos != null) {
                    ins.copyTo(fos)
                }
            }
        }
    }

    private fun getConflictResolution(conflictResolutions: LinkedHashMap<String, Int>, path: String): Int {
        return if (conflictResolutions.size == 1 && conflictResolutions.containsKey("")) {
            conflictResolutions[""]!!
        } else if (conflictResolutions.containsKey(path)) {
            conflictResolutions[path]!!
        } else {
            CONFLICT_SKIP
        }
    }
    private fun isOneFileSelected() = isOneItemSelected() && getItemWithKey(selectedKeys.first())?.isDirectory == false
    private fun getSelectedFileDirItems() = listItems.filter {
        selectedKeys.contains(it.path.hashCode()) } as ArrayList<FileDirItem>
    private fun getItemWithKey(key: Int): FileDirItem? = listItems.firstOrNull { it.path.hashCode() == key }
    private fun getItemPosition(item:FileDirItem)  =
         (listItems.indices)
            .firstOrNull { i: Int -> item == listItems[i] }


    private fun checkHideBtnVisibility(mHide: LinearLayout, mUnHide: LinearLayout, listItem: ListItem?) {

        var hiddenCnt = 0
        var unhiddenCnt = 0
        if(listItem!=null){
            if (listItem.name.startsWith(".")) {
                hiddenCnt++
            } else {
                unhiddenCnt++
            }
        }else {
            getSelectedFileDirItems().map { it.name }.forEach {
                if (it.startsWith(".")) {
                    hiddenCnt++
                } else {
                    unhiddenCnt++
                }
            }
        }

        mHide.beVisibleIf(unhiddenCnt > 0)
        mUnHide.beVisibleIf(hiddenCnt > 0)
    }

     fun addFiles(listItem: ListItem?){
        var selectedItems = getSelectedFileDirItems()
        if(listItem!=null) {
            selectedItems = arrayListOf(listItem)
        }
        var pathss : ArrayList<String>?= ArrayList()
         for (values in selectedItems){
             pathss?.add(values.path)
           //  paths.add(values.toString())
         }
//        selectedItems.forEach {
//            addFileUris(it.path, paths)
//        }
        (activity as FileManagerMainActivity).onAddShortcutClicked(pathss!!,null)
         pathss.clear()
    }

     fun shareFiles(listItem: ListItem?) {
        var selectedItems = getSelectedFileDirItems()
        if(listItem!=null) {
            selectedItems = arrayListOf(listItem)
        }
        val paths = ArrayList<String>(selectedItems.size)
        selectedItems.forEach {
            addFileUris(it.path, paths)
        }
        activity.sharePaths(paths)
    }

     fun askConfirmDelete() {
        val selectionSize = selectedKeys.size
        val items = resources.getQuantityString(R.plurals.delete_items, selectionSize, selectionSize)
        val question = String.format(resources.getString(R.string.deletion_confirmation), items)
        ConfirmationDialog(activity, question) {
            deleteFiles(null, -1)
        }

    }

    private fun addFileUris(path: String, paths: ArrayList<String>) {
        if (activity.getIsPathDirectory(path)) {
            val shouldShowHidden = activity.config.shouldShowHidden
            if (activity.isPathOnOTG(path)) {
                activity.getDocumentFile(path)?.listFiles()?.filter { if (shouldShowHidden) true else !it.name!!.startsWith(".") }?.forEach {
                    addFileUris(it.uri.toString(), paths)
                }
            } else {
                File(path).listFiles()?.filter { if (shouldShowHidden) true else !it.name.startsWith('.') }?.forEach {
                    addFileUris(it.absolutePath, paths)
                }
            }
        } else {
            paths.add(path)
        }
    }
    private fun getFirstSelectedItemPath() = getSelectedFileDirItems().first().path

    private fun deleteFiles(listItem: ListItem?, pos: Int) {
        /* if (selectedKeys.isEmpty()) {
             return
         }
 */
        val  SAFPath = listItem?.mPath ?: getFirstSelectedItemPath()

        if (activity.isPathOnRoot(SAFPath) && !RootTools.isRootAvailable()) {
            activity.toast(R.string.rooted_device_only)
            return
        }

        activity.handleSAFDialog(SAFPath) { it ->
            if (!it) {
                return@handleSAFDialog
            }

            val files = ArrayList<FileDirItem>(selectedKeys.size)
            val positions = ArrayList<Int>()

            if(listItem!=null){
                files.add(listItem)
                positions.add(pos)
            }else {
                selectedKeys.forEach { i ->
//                    activity.config.removeFavorite(getItemWithKey(i)?.path ?: "")
                    val key = i
                    val position = listItems.indexOfFirst { it.path.hashCode() == key }
                    if (position != -1) {
                        positions.add(position)
                        files.add(listItems[position])
                    }
                }
            }
            positions.sortDescending()
            removeSelectedItems(positions)

            listener?.deleteFiles(files)
            positions.forEach { i ->
                listItems.removeAt(i)

            }
//   (activity as FileManagerMainActivity).updateDatabase

       //     listener?.refreshItems(false)
            listenerUpdate?.updateDatabase(true)
        }
    }

     fun copyMoveTo(isCopyOperation: Boolean, listItem: ListItem?) {
        var files = getSelectedFileDirItems()
        if(listItem!=null) {
            files = arrayListOf(listItem)
        }
        val firstFile = files[0]
        val source = firstFile.getParentPath()
        val positiveButtonText = if(isCopyOperation) "Copy" else "Move"
        FilePickerDialog(activity, source, positiveButtonText,"Cancel",false, activity.config.shouldShowHidden, true, true) {
            if (activity.isPathOnRoot(it) || activity.isPathOnRoot(firstFile.path)) {
                copyMoveRootItems(files, it, isCopyOperation)
            } else {
                activity.copyMoveFilesTo(files, source, it, isCopyOperation, false, activity.config.shouldShowHidden) {
                  //  updateDatabase
                    listenerUpdate?.updateDatabase(true)
                    listener?.refreshItems(false)
                    finishActMode()
                }
            }
        }
    }

    private fun copyMoveRootItems(files: ArrayList<FileDirItem>, destinationPath: String, isCopyOperation: Boolean) {
        activity.toast(R.string.copying)
        ensureBackgroundThread {
            val fileCnt = files.size
            RootHelpers(activity).copyMoveFiles(files, destinationPath, isCopyOperation) {
                when (it) {
                    fileCnt -> activity.toast(R.string.copying_success)
                    0 -> activity.toast(R.string.copy_failed)
                    else -> activity.toast(R.string.copying_success_partial)
                }

                activity.runOnUiThread {
                    listener?.refreshItems(false)
                    finishActMode()
                }
            }
        }
    }

     fun displayRenameDialog(listItem: ListItem?) {
        var fileDirItems = getSelectedFileDirItems()
         var position = getItemPosition(fileDirItems[0])

             if(listItem!=null) {

            fileDirItems = arrayListOf(listItem)
        }
        val paths = fileDirItems.asSequence().map { it.path }.toMutableList() as ArrayList<String>
        when {
            paths.size == 1 -> {
                val oldPath = paths.first()
                RenameItemDialog(activity, oldPath) {
//                    activity.config.moveFavorite(oldPath, it)
                    activity.runOnUiThread {
                       // updateDatabase

                        fileDirItems[0].name = it.getFilenameFromPath()
                        if (position != null) {
                            notifyItemChanged(position)
                        }
//                        notifyItemRangeChanged(0,15)

                        listener?.refreshItems(false)
                        listenerUpdate?.updateDatabase(true)

                        finishActMode()
                    }
                }
            }
            fileDirItems.any { it.isDirectory } -> RenameItemsDialog(activity, paths) {
                activity.runOnUiThread {
                    // updateDatabase
                    listenerUpdate?.updateDatabase(true)
                    listener?.refreshItems(false)
                    finishActMode()
                }
            }
            else -> RenameDialog(activity, paths, false) {
                activity.runOnUiThread {
                    // updateDatabase
                    listenerUpdate?.updateDatabase(true)
                    listener?.refreshItems(false)
                    finishActMode()
                }
            }
        }
    }


     fun openWith(listItem: ListItem?) {
        if(listItem!=null){
            activity.openWith(listItem.mPath)
        }else {
            activity.openWith(getFirstSelectedItemPath())
        }
    }

     fun showProperties(listItem: ListItem?) {

        when {
            listItem!=null -> {
                PropertiesDialog(activity, listItem.mPath, activity.config.shouldShowHidden)
            }
            selectedKeys.size <= 1 -> {
                PropertiesDialog(activity, getFirstSelectedItemPath(), activity.config.shouldShowHidden)
            }
            else -> {
                val paths = getSelectedFileDirItems().map { it.path }
                PropertiesDialog(activity, paths, activity.config.shouldShowHidden)
            }
        }
    }
}