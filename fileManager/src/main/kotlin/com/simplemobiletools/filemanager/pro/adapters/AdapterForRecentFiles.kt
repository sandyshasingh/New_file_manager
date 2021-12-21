package com.simplemobiletools.filemanager.pro.adapters

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.simplemobiletools.filemanager.pro.R
import com.simplemobiletools.filemanager.pro.models.ListItem
import kotlinx.android.synthetic.main.recent_files.view.*
import java.text.SimpleDateFormat

//import kotlinx.android.synthetic.main.recent_files.view.*

class AdapterForRecentFiles(var mContext: Activity, var mRecent:Map<String,List<ListItem>>?): RecyclerView.Adapter<AdapterForRecentFiles.ViewHolder>() {

    class ViewHolder(itemView: View, mContext: Context) : RecyclerView.ViewHolder(itemView) {


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.recent_files, parent, false)
        return ViewHolder(v, mContext)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {


        holder.itemView.recent_file_text.text = mRecent?.keys?.elementAt(position).toString()
        holder.itemView.recent_file_item.adapter = ChildAdapterForRecentFiles(mContext,mRecent?.values?.elementAt(position))
    }

    override fun getItemCount(): Int {
       return mRecent?.keys?.size?:0
    }
}