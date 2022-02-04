package com.simplemobiletools.commons.adapters

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.bumptech.glide.request.RequestOptions
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.activities.BaseSimpleActivity
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.helpers.getFilePlaceholderDrawables
import com.simplemobiletools.commons.models.FileDirItem
import com.simplemobiletools.commons.views.MyRecyclerView
import kotlinx.android.synthetic.main.filepicker_list_item.view.*
import java.util.*

class FilepickerItemsAdapter(activity: BaseSimpleActivity, val fileDirItems: List<FileDirItem>, recyclerView: MyRecyclerView,
                             itemClick: (Any,Int) -> Unit) : MyRecyclerViewAdapter(
    activity,
    recyclerView,
    null,
    itemClick,
    null,
    null,
    false,

) {

    private lateinit var fileDrawable: Drawable
    private var folderDrawable: Drawable? = null
    private var fileDrawables = HashMap<String, Drawable>()
    private val hasOTGConnected = activity.hasOTGConnected()
    private val cornerRadius = resources.getDimension(R.dimen.rounded_corner_radius_small).toInt()
    private var isDarkTheme = activity.isDarkTheme()

    init {
        isDarkTheme = activity.isDarkTheme()
        initDrawables()
    }

    override fun getActionMenuId() = 0

    override fun prepareActionMode(menu: Menu) {
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = createViewHolder(R.layout.filepicker_list_item, parent)
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val fileDirItem = fileDirItems[position]
        val viewHolder  = holder as ViewHolder

        viewHolder.bindView(fileDirItem,position,null,true, false,) { itemView, adapterPosition ->
            setupView(itemView, fileDirItem)
        }
        bindViewHolder(viewHolder)
    }

    override fun getItemCount() = fileDirItems.size

    override fun actionItemPressed(id: Int) {}

    override fun getSelectableItemCount() = fileDirItems.size

    override fun getIsItemSelectable(position: Int) = false

    override fun getItemKeyPosition(key: Int) = fileDirItems.indexOfFirst { it.path.hashCode() == key }

    override fun getItemSelectionKey(position: Int) = fileDirItems[position].path.hashCode()
    override fun checkIsZipFile(position: Int): Boolean? {
        return false
    }

    override fun onActionModeCreated() {}

    override fun onActionModeDestroyed() {}
    override fun mLongClick() {
    }
    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        super.onViewRecycled(holder)
        if (!activity.isDestroyed && !activity.isFinishing) {
            Glide.with(activity).clear(holder.itemView.list_item_icon!!)
        }
    }

    private fun setupView(view: View, fileDirItem: FileDirItem) {
        view.apply {
            list_item_name.text = fileDirItem.name

            if (fileDirItem.isDirectory) {
                if(folderDrawable!=null)
                    list_item_icon.setImageDrawable(folderDrawable)
                else
                    list_item_icon.setImageDrawable(resources.getDrawable(R.drawable.ic_icon_folder__light2))
                list_item_details.text = getChildrenCnt(fileDirItem)
            } else {
                list_item_details.text = fileDirItem.size.formatSize()
                val path = fileDirItem.path
                val placeholder = fileDrawables.getOrElse(fileDirItem.name.substringAfterLast(".").toLowerCase(Locale.getDefault()), { fileDrawable })
                val options = RequestOptions()
                        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                        .centerCrop()
                        .error(placeholder)

                var itemToLoad = if (fileDirItem.name.endsWith(".apk", true)) {
                    val packageInfo = context.packageManager.getPackageArchiveInfo(path, PackageManager.GET_ACTIVITIES)
                    if (packageInfo != null) {
                        val appInfo = packageInfo.applicationInfo
                        appInfo.sourceDir = path
                        appInfo.publicSourceDir = path
                        appInfo.loadIcon(context.packageManager)
                    } else {
                        path
                    }
                } else {
                    path
                }

                if (!activity.isDestroyed && !activity.isFinishing) {
                    if (itemToLoad.toString().isGif()) {
                        Glide.with(activity).asBitmap().load(itemToLoad).apply(options).into(list_item_icon)
                    } else {
                        if (hasOTGConnected && itemToLoad is String && activity.isPathOnOTG(itemToLoad)) {
                            itemToLoad = itemToLoad.getOTGPublicPath(activity)
                        }

                        Glide.with(activity)
                                .load(itemToLoad)
                                .transition(withCrossFade())
                                .apply(options)
                                .transform(CenterCrop(), RoundedCorners(cornerRadius))
                                .into(list_item_icon)
                    }
                }
            }
        }
    }

    private fun getChildrenCnt(item: FileDirItem): String {
        val children = item.children
        return activity.resources.getQuantityString(R.plurals.items, children, children)
    }
    @SuppressLint("UseCompatLoadingForDrawables")
    private fun getFolderDrawable() : Drawable?{
        return if (isDarkTheme) {
            resources.getDrawable(R.drawable.ic_file_manager_fldr)
        } else {
            resources.getDrawable(R.drawable.ic_icon_folder__light2)
        }
    }
    private fun initDrawables() {
        folderDrawable = getFolderDrawable()
        folderDrawable?.alpha = 180
        fileDrawable = resources.getDrawable(R.drawable.ic_file_generic)
        fileDrawables = getFilePlaceholderDrawables(activity)
    }
}
