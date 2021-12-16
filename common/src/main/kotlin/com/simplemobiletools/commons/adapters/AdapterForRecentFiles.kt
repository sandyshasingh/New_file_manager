package com.simplemobiletools.commons.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.simplemobiletools.commons.R

class AdapterForRecentFiles(var mContext: Context): RecyclerView.Adapter<AdapterForRecentFiles.ViewHolder>() {

    class ViewHolder(itemView: View, mContext: Context) : RecyclerView.ViewHolder(itemView) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.recent_files, parent, false)
        return ViewHolder(v, mContext)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        TODO("Not yet implemented")
    }

    override fun getItemCount(): Int {
        TODO("Not yet implemented")
    }
}