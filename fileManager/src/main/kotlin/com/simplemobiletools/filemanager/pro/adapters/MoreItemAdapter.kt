package com.simplemobiletools.filemanager.pro.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.simplemobiletools.commons.ListItem

import com.simplemobiletools.filemanager.pro.R
import kotlinx.android.synthetic.main.more_items.view.*


class MoreItemAdapter(var mContext: Context, var mRecent:List<ListItem>?, var listener: MoreItemsListener?): RecyclerView.Adapter<MoreItemAdapter.ViewHolder>() {

    private var currentItemsHash = mRecent.hashCode()
    private var textToHighlight = ""

    class ViewHolder(itemView: View, var listener: MoreItemsListener?) : RecyclerView.ViewHolder(itemView) {
        fun bindItems(item: ListItem, position: Int, mContext: Context ) {

            itemView.setOnClickListener{
                 listener?.moreItems(item,position)
             }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.more_items, parent, false)
        return ViewHolder(v,listener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        mRecent?.get(position)?.let { holder.bindItems(it,position,mContext) }
        holder.itemView.more_items_text.text = mRecent?.get(position)?.mName
        Glide.with(mContext)
            .load(mRecent?.get(position)?.mPath)
            .into(holder.itemView.more_items_image)
    }

    override fun getItemCount(): Int {
        return mRecent?.size?:0
    }

    fun updateItems(newItems: ArrayList<ListItem>, highlightText: String = "") {
        if (newItems.hashCode() != currentItemsHash) {
            currentItemsHash = newItems.hashCode()
            textToHighlight = highlightText
//            mRecent = newItems.clone() as ArrayList<ListItem>
            notifyDataSetChanged()
//            (bottomnavigation)
        } else if (textToHighlight != highlightText) {
            textToHighlight = highlightText
            notifyDataSetChanged()
        }

        mRecent = newItems
        notifyDataSetChanged()
        //fastScroller?.measureRecyclerView()
    }
    interface MoreItemsListener {
        fun moreItems(item: ListItem,position: Int)
    }

}