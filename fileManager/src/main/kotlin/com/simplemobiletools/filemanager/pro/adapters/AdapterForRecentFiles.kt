package com.simplemobiletools.filemanager.pro.adapters

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.simplemobiletools.commons.DeleteShortcut
import com.simplemobiletools.commons.MemorySizeUtils
import com.simplemobiletools.commons.adapters.AdapterForFolders
import com.simplemobiletools.commons.adapters.AdapterForStorage
import com.simplemobiletools.commons.extensions.hasExternalSDCard
import com.simplemobiletools.commons.helpers.EXTERNAL_STORAGE
import com.simplemobiletools.commons.helpers.INTERNAL_STORAGE
import com.simplemobiletools.commons.models.FolderItem
import com.simplemobiletools.commons.models.StorageItem
import com.simplemobiletools.filemanager.pro.AppDataHolder
import com.simplemobiletools.filemanager.pro.MoreItemsList
import com.simplemobiletools.filemanager.pro.R
import com.simplemobiletools.filemanager.pro.RecentUpdatedFiles
import com.simplemobiletools.filemanager.pro.activities.FileManagerMainActivity
import com.simplemobiletools.filemanager.pro.models.ListItem
import kotlinx.android.synthetic.main.card_storage.view.*
import kotlinx.android.synthetic.main.folder_items.view.*
import kotlinx.android.synthetic.main.recent_files.view.*


//import kotlinx.android.synthetic.main.recent_files.view.*

class AdapterForRecentFiles(
    var mContext: Activity,
    var mRecent: RecentUpdatedFiles,
    var listener: MoreItemsList,
    var storageList:ArrayList<StorageItem>,
    var folderList: ArrayList<FolderItem>,
     val clickListener: (FolderItem) -> Unit,
   var deleteShortcut: DeleteShortcut?,
    val clickListenerStorage: (StorageItem) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    //var mRecentwva: List<ListItem>? = null
    private var storageItems = ArrayList<StorageItem>()
    class MainViewHolder(itemView: View, mContext: Context) : RecyclerView.ViewHolder(itemView) {


    }


    inner class FolderViewHolder(itemView: View, mContext: Context) : RecyclerView.ViewHolder(itemView) {
        fun bindItemsFolders() {
            val mainAdapter = AdapterForFolders(
                folderList,
                clickListener,
                mContext,
                deleteShortcut
            )
            //recyclerView?.layoutManager = LinearLayoutManager(activity,LinearLayoutManager.HORIZONTAL,false)
            itemView.recyclerView?.adapter = mainAdapter
        }

    }

    inner class StorageCardViewHolder(itemView: View, var mContext: Context) : RecyclerView.ViewHolder(itemView) {

        fun bindItems() {
           itemView.only_internal.setOnClickListener {
                (mContext as FileManagerMainActivity)?.onCategoryClick(
                    INTERNAL_STORAGE, "abc"
                )
            }
            itemView.rv_storage?.layoutManager =
                LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false)
            val storageAdapter = AdapterForStorage(
                storageList,
                clickListenerStorage,
                mContext
            )
            itemView.rv_storage?.adapter = storageAdapter
            val totalSizeInternal = MemorySizeUtils.getTotalInternalMemorySize()
            val availableSizeInternal = MemorySizeUtils.getAvailableInternalMemorySize()

            val totalSizeExternal = MemorySizeUtils.getTotalExternalMemorySize()
            val availableSizeExternal = MemorySizeUtils.getAvailableExternalMemorySize()

            fun String.getAmount(): String {
                return substring(indexOfFirst { it.isDigit() }, indexOfLast { it.isDigit() } + 1)
                    .filter { it.isDigit() || it == '.' }
            }

            var total = totalSizeInternal?.getAmount()?.toDouble()
            var available = availableSizeInternal?.getAmount()?.toDouble()
            var arcPercent = (available!! / total!!) * 100

            itemView.text_internal2.text = "Internal storage"
            itemView.icon_internalstorage2.setImageResource(R.drawable.ic_file_manager_storage)
            itemView.storage_size2.text = "$availableSizeInternal/$totalSizeInternal"
            itemView. arc_progress2.max = 100
            itemView.arc_progress2.progress = arcPercent.toInt()
            itemView.arc_progress2.suffixText = "%"

            if (mContext.hasExternalSDCard()) {
                total = totalSizeExternal?.getAmount()?.toDouble()
                available = availableSizeExternal?.getAmount()?.toDouble()
                arcPercent = (available!! / total!!) * 100

                itemView.rv_storage.visibility = View.VISIBLE
                itemView.only_internal.visibility = View.GONE

                storageItems.add(
                    StorageItem(
                        INTERNAL_STORAGE, "Internal storage", R.drawable.ic_file_manager_storage,
                        "$availableSizeInternal/$totalSizeInternal", arcPercent
                    )
                )


                storageItems.add(
                    StorageItem(
                        EXTERNAL_STORAGE,
                        "External storage",
                        R.drawable.ic_file_manager_external_storage,
                        "$availableSizeExternal/$totalSizeExternal", arcPercent
                    )
                )
            }
            //val usedMemoryProgress = itemView.usedMemoryProgress
            //  val folderLayout = itemView.folder_layout

        }


    }

    override fun getItemViewType(position: Int): Int {
        if (position==0)
            return 0
        else if (position==1)
            return 1
        else (position==2)
            return 2

    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType==2) {
            val v =
                LayoutInflater.from(parent.context).inflate(R.layout.recent_files, parent, false)
            return MainViewHolder(v, mContext)
        }
        else if(viewType==1){
            val v =
                LayoutInflater.from(parent.context).inflate(R.layout.folder_items, parent, false)
            return FolderViewHolder(v, mContext)
        }
        else{
            val v =
                LayoutInflater.from(parent.context).inflate(R.layout.card_storage, parent, false)
            return StorageCardViewHolder(v, mContext)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
//        var aa = mRecent?.keys
//        var bb = mRecent?.values
        //mRecentwva = mRecent?.entries?.elementAt(position)?.value
        if(holder is MainViewHolder)
        {
            var jj = AppDataHolder.mfinalValues.mKeys[position-2]
            holder.itemView.recent_file_text.text = jj
            holder.itemView.recent_file_item.adapter = ChildAdapterForRecentFiles(mContext,
                AppDataHolder.mfinalValues.mValues[position-2],listener,
                jj)
        }
        else if(holder is StorageCardViewHolder)
        {
            holder.bindItems()
        }
        else if(holder is FolderViewHolder)
        {
            holder.bindItemsFolders()
        }
    }

    override fun getItemCount(): Int {
        return AppDataHolder.mfinalValues.mKeys.size + 2
    }
}