package com.simplemobiletools.filemanager.pro.adapters

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.simplemobiletools.commons.ListItem
import com.simplemobiletools.filemanager.pro.MoreItemsList
import com.simplemobiletools.filemanager.pro.R
import com.simplemobiletools.filemanager.pro.extensions.openWith
import kotlinx.android.synthetic.main.recent_file_item.view.*

class ChildAdapterForRecentFiles(
    var mContext: Activity,
    var mRecent: List<ListItem>?,
    var listener: MoreItemsList,
    var key: String?
): RecyclerView.Adapter<ChildAdapterForRecentFiles.ViewHolder>() {

    inner class ViewHolder(itemView: View, mContext: Context) : RecyclerView.ViewHolder(itemView) {
        val recent_files_item = itemView.child_recent_item
        init {
            itemView.setOnClickListener {
                if(mRecent?.size!! > 5 && adapterPosition == 4){
                    listener.moreItemsList(mRecent!!)
                }
                else
                    openWith(mRecent?.get(adapterPosition))
            }

        }
    }

    private fun openWith(listItem: ListItem?) {
        if(listItem!=null){
            mContext.openWith(listItem.mPath)
        }else {
//            activity.openWith(getFirstSelectedItemPath())
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.recent_file_item, parent, false)
        return ViewHolder(v, mContext)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
     //   holder.recent_files.text = mRecent?.keys!!.elementAt(position).toString()
        val filetype = mRecent?.get(position)?.mimeType
        if (filetype != null) {
            if (filetype.startsWith("video")){
                holder.itemView.play_pause.visibility = View.VISIBLE
            }
            else
            {
                holder.itemView.play_pause.visibility = View.GONE
            }
        }

        if(mRecent?.size!! > 5 && holder.adapterPosition == 4){
            holder.itemView.more_items.visibility = View.VISIBLE
            holder.itemView.play_pause.visibility = View.GONE
            holder.itemView.more_items.text = "+${mRecent?.size}"
        }
        else{
            holder.itemView.more_items.visibility = View.GONE
        }

        Glide.with(mContext)
            .load(mRecent?.get(position)?.mPath)
            .into(holder.recent_files_item)

    }

    override fun getItemCount(): Int {
        if(mRecent?.size!! <= 5){
            return mRecent?.size?:0
        }
        else
            return 5
    }
}