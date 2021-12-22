package com.simplemobiletools.filemanager.pro.adapters

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.simplemobiletools.filemanager.pro.R
import com.simplemobiletools.filemanager.pro.models.ListItem
import kotlinx.android.synthetic.main.more_items.view.*
import kotlinx.android.synthetic.main.recent_file_item.view.*

class MoreItemAdapter(var mContext: Context, var mRecent:List<ListItem>?): RecyclerView.Adapter<MoreItemAdapter.ViewHolder>() {

    class ViewHolder(itemView: View, mContext: Context) : RecyclerView.ViewHolder(itemView) {


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.more_items, parent, false)
        return ViewHolder(v, mContext)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Glide.with(mContext)
            .load(mRecent?.get(position)?.mPath)
            .into(holder.itemView.more_items_image)
    }

    override fun getItemCount(): Int {
        return mRecent?.size?:0
    }

}