package com.simplemobiletools.filemanager.pro.adapters

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.simplemobiletools.filemanager.pro.MoreItemsList
import com.simplemobiletools.filemanager.pro.R
import com.simplemobiletools.filemanager.pro.models.ListItem
import kotlinx.android.synthetic.main.recent_files.view.*

//import kotlinx.android.synthetic.main.recent_files.view.*

class AdapterForRecentFiles(
    var mContext: Activity,
    var mRecent: Map<String, List<ListItem>>?,
    var listener: MoreItemsList
) : RecyclerView.Adapter<AdapterForRecentFiles.ViewHolder>() {
    //var mRecentwva: List<ListItem>? = null

    class ViewHolder(itemView: View, mContext: Context) : RecyclerView.ViewHolder(itemView) {


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.recent_files, parent, false)
        return ViewHolder(v, mContext)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        var aa = mRecent?.keys
//        var bb = mRecent?.values
        //mRecentwva = mRecent?.entries?.elementAt(position)?.value
        var jj = mRecent?.keys?.elementAt(position).toString()
        holder.itemView.recent_file_text.text = jj
        holder.itemView.recent_file_item.adapter = ChildAdapterForRecentFiles(mContext,mRecent?.values?.elementAt(position),listener,
            jj)
    }

    override fun getItemCount(): Int {
        return mRecent?.entries?.size ?: 0
    }
}