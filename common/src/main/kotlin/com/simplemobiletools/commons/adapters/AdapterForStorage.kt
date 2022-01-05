package com.simplemobiletools.commons.adapters

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.github.lzyzsd.circleprogress.ArcProgress
import com.simplemobiletools.commons.MemorySizeUtils
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.extensions.isDarkTheme
import com.simplemobiletools.commons.models.FolderItem
import com.simplemobiletools.commons.models.StorageItem
import com.simplemobiletools.commons.setTypeFaceOpenSensSmBold
import kotlinx.android.synthetic.main.folder_item_view.view.*
import kotlinx.android.synthetic.main.storage_card.view.*
import kotlinx.android.synthetic.main.storage_card.view.folder_layout as folder_layout1

class AdapterForStorage(var storageList:ArrayList<StorageItem>, private val clickListener: (StorageItem) -> Unit, var mContext: Context):RecyclerView.Adapter<AdapterForStorage.ViewHolder>() {

    class ViewHolder(itemView: View, mContext: Context) : RecyclerView.ViewHolder(itemView) {
        val totalSize = MemorySizeUtils.getTotalInternalMemorySizeInLong()
        val arcProgress = itemView.arc_progress
        var storageCard = itemView.storage_card
        val storageTextView = itemView.text_internal
        val storageSize = itemView.storage_size

       // var darkThemeBackground : Drawable? = null
       // var isDarkTheme = false

//        init {
//            isDarkTheme = mContext.isDarkTheme()
//            if(isDarkTheme) {
//                //  darkThemeBackground = mContext.resources.getDrawable(R.drawable.rectangle_semitranparent_dark_theme)
//            }
//        }



        fun bindItems(storageItem: StorageItem, c1: (StorageItem) -> Unit) {



            val storageIcon = itemView.icon_internalstorage

            //val usedMemoryProgress = itemView.usedMemoryProgress
          //  val folderLayout = itemView.folder_layout

            storageTextView.text = storageItem.storageTextView
            storageSize.text = storageItem.storageSize
            arcProgress.max = 100
            arcProgress.progress = storageItem.arcPercent.toInt()
            val ad= arcProgress

            storageIcon.setImageResource(storageItem.storageIcon)

            itemView.setOnClickListener{ c1(storageItem) }
//            usedMemoryProgress.max = (totalSize / 1024).toInt()
//            usedMemoryProgress.progress= folder.size.toInt()
//            if(isDarkTheme) {
//                folderLayout.setBackgroundDrawable(darkThemeBackground)
//            }else{
////                folderLayout.setBackgroundDrawable(folder.backgroundColor)
//            }
//            storageTextView.text = folder.folderName
           // storageIcon = R.drawable.

//            folderNameTextView?.setTypeFaceOpenSensSmBold()
//            folderSize?.setTypeFaceOpenSensSmBold()
//
//            folderNameTextView.setTextColor(folder.textColor)
//            folderSize.setTextColor(folder.textColor)
//
//            folderIcon.setImageResource(folder.folderIcon)
//            itemView.setOnClickListener{ c1(folder) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.storage_card, parent, false)
        return ViewHolder(v, mContext)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItems(storageList[position], clickListener)
        if(position == 1){
           holder.storageCard.setCardBackgroundColor(Color.WHITE)
            holder.storageTextView.setTextColor(Color.parseColor("#282361"))
            holder.storageSize.setTextColor(Color.parseColor("#313131"))
            holder.arcProgress.textColor = mContext.resources.getColor(R.color.btm_background)
            holder.arcProgress.finishedStrokeColor =  mContext.resources.getColor(R.color.btm_background)
        }


    }

    override fun getItemCount(): Int {
        return storageList.size    }

}