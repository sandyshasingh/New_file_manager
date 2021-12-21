package com.simplemobiletools.filemanager.pro.adapters

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.simplemobiletools.filemanager.pro.R
import com.simplemobiletools.filemanager.pro.extensions.openWith
import com.simplemobiletools.filemanager.pro.models.ListItem
import kotlinx.android.synthetic.main.recent_file_item.view.*
import kotlinx.android.synthetic.main.recent_files.view.*

class ChildAdapterForRecentFiles (var mContext: Activity, var mRecent:List<ListItem>?): RecyclerView.Adapter<ChildAdapterForRecentFiles.ViewHolder>() {

    class ViewHolder(itemView: View, mContext: Context) : RecyclerView.ViewHolder(itemView) {
        val recent_files_item = itemView.child_recent_item
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
        Glide.with(mContext)
            .load(mRecent?.get(position)?.mPath)
            .into(holder.recent_files_item)

        holder.itemView.setOnClickListener {
            openWith(mRecent?.get(position))
        }

    }

    override fun getItemCount(): Int {
        return mRecent?.size?:0
    }
}