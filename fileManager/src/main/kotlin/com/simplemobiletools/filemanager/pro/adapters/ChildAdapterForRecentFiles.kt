package com.simplemobiletools.filemanager.pro.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.simplemobiletools.filemanager.pro.R
import com.simplemobiletools.filemanager.pro.models.ListItem
import kotlinx.android.synthetic.main.recent_file_item.view.*
import kotlinx.android.synthetic.main.recent_files.view.*

class ChildAdapterForRecentFiles (var mContext: Context, var mRecent:List<ListItem>?): RecyclerView.Adapter<ChildAdapterForRecentFiles.ViewHolder>() {

    class ViewHolder(itemView: View, mContext: Context) : RecyclerView.ViewHolder(itemView) {
        val recent_files_item = itemView.child_recent_item
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

    }

    override fun getItemCount(): Int {
        return mRecent?.size?:0
    }
}