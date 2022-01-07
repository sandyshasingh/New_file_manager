package com.simplemobiletools.commons.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.simplemobiletools.commons.*
import com.simplemobiletools.commons.extensions.isDarkTheme
import com.simplemobiletools.commons.helpers.SHORTCUT_FOLDER_ID
import com.simplemobiletools.commons.models.FolderItem
import kotlinx.android.synthetic.main.folder_item_view.view.*

class AdapterForFolders(var folderList: ArrayList<FolderItem>, private val clickListener: (FolderItem) -> Unit, var mContext: Context, var deleteShortcut: DeleteShortcut?) : RecyclerView.Adapter<AdapterForFolders.HeaderViewHolder>()
{



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HeaderViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.folder_item_view, parent, false)
        return HeaderViewHolder(v,mContext)
    }

    override fun onBindViewHolder(holder: HeaderViewHolder, position: Int) {
        holder.bindItems(folderList[position], clickListener)

        holder.deleteShortcut.setOnClickListener {
                //folderList.removeAt(position)
            deleteShortcut?.deleteFolder(folderList[position].sizeString)
        }

    }

    override fun getItemCount(): Int {
        return folderList.size
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    class HeaderViewHolder(itemView: View, mContext: Context) : RecyclerView.ViewHolder(itemView) {
        val totalSize = MemorySizeUtils.getTotalInternalMemorySizeInLong()
        var darkThemeBackground : Drawable? = null
        var isDarkTheme = false
        var deleteShortcut = itemView.delete_shortcut

        init {
            isDarkTheme = mContext.isDarkTheme()
            if(isDarkTheme) {
              //  darkThemeBackground = mContext.resources.getDrawable(R.drawable.rectangle_semitranparent_dark_theme)
            }
        }

        fun bindItems(folder: FolderItem, c1: (FolderItem) -> Unit) {
            val folderNameTextView = itemView.folder_name
            val folderIcon = itemView.folder_icon
//            val folderSize = itemView.folder_size
//            val usedMemoryProgress = itemView.usedMemoryProgress
            val folderLayout = itemView.folder_layout

//            usedMemoryProgress.max = (totalSize / 1024).toInt()
//            usedMemoryProgress.progress= folder.size.toInt()
            if(isDarkTheme) {
                folderLayout.setBackgroundDrawable(darkThemeBackground)
            }else{
                folderLayout.setBackgroundDrawable(folder.backgroundColor)
            }
            folderNameTextView.text = folder.folderName
//            folderSize.text = folder.sizeString

            folderNameTextView?.setTypeFaceOpenSensSmBold()
        //    folderSize?.setTypeFaceOpenSensSmBold()

           // folderNameTextView.setTextColor(folder.textColor)
            //folderSize.setTextColor(folder.textColor)

            folderIcon.setImageResource(folder.folderIcon)
            itemView.setOnClickListener{ c1(folder) }

            if(folder.id == SHORTCUT_FOLDER_ID){
               itemView.setOnLongClickListener {
                    deleteShortcut.visibility = View.VISIBLE
                   true
                }

            }






        }
    }


    fun updateDataAndNotify(folderDataClassList: List<FolderItem>) {
        try {
            val diffCallback = FolderDiffCallBack(folderList, folderDataClassList)
            val diffResult = DiffUtil.calculateDiff(diffCallback)
            this.folderList = folderDataClassList as ArrayList<FolderItem>
            diffResult.dispatchUpdatesTo(this)
        } catch (e: Exception) {
            this.folderList = folderDataClassList as ArrayList<FolderItem>
            notifyDataSetChanged()
        }
    }

    fun updateFolderItems(folderItems: ArrayList<FolderItem>) {
        this.folderList = folderItems
        notifyDataSetChanged()
    }

}

