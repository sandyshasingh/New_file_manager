package com.editor.hiderx.adapters

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.RecyclerView
import com.editor.hiderx.listeners.OnItemSelectedListener
import com.editor.hiderx.R
import com.editor.hiderx.dataclass.SimpleDataClass
import com.editor.hiderx.listeners.ActionModeListener
import com.editor.hiderx.loadUriFromPath
import kotlinx.android.synthetic.main.photos_items.view.*
import java.io.File

class AdapterForPhotosOrVideos(var itemsList: List<SimpleDataClass>, val recycler_view: RecyclerView, var context: Context, var onItemSelectedListener: OnItemSelectedListener, var actionModeListener: ActionModeListener?) : RecyclerView.Adapter<AdapterForPhotosOrVideos.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.photos_items, parent, false)
        val params = v.layoutParams
        params.width = recycler_view.measuredWidth / 2 - dpTopixel(context, 16f).toInt()
        v.layoutParams = params
        return ViewHolder(v)
    }

    fun dpTopixel(c: Context, dp: Float): Float {
        val density: Float = c.resources.displayMetrics.density
        return dp * density
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItems()
    }

    override fun getItemCount(): Int {
        return itemsList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindItems() {

            itemView.image.loadUriFromPath(itemsList[adapterPosition].path)
            if (itemsList[adapterPosition].isSelected) {
                itemView.img_selected.visibility = View.VISIBLE
                itemView.selection_layer.visibility = View.VISIBLE
            } else {
                itemView.img_selected.visibility = View.GONE
                itemView.selection_layer.visibility = View.GONE
            }
            itemView.setOnClickListener()
            {
                if ((actionModeListener!=null && actionModeListener?.getActionMode()!!) || actionModeListener == null && adapterPosition>=0) {
                    selectOrDeselectItems(adapterPosition)
                } else {
                        playFile(itemsList,adapterPosition)
                }
            }

            if (actionModeListener!=null) {
                itemView.setOnLongClickListener()
                {
                    if (!actionModeListener?.getActionMode()!! && adapterPosition>=0 ) {
                        selectOrDeselectItems(adapterPosition)
                        actionModeListener?.setActionModeValue(true)
                        true
                    }
                    false
                }
            }

        }

        private fun playFile(hiddenfiles: List<SimpleDataClass>,position: Int) {
                    onItemSelectedListener.onItemClicked(hiddenfiles,position)
        }

    }

    private fun selectOrDeselectItems(adapterPosition: Int) {
        if (itemsList[adapterPosition].isSelected) {
            itemsList[adapterPosition].isSelected = false
            onItemSelectedListener.onItemDeselected(itemsList[adapterPosition])
        } else {
            itemsList[adapterPosition].isSelected = true
            onItemSelectedListener.onItemSelected(itemsList[adapterPosition])
        }
        notifyItemChanged(adapterPosition)
    }

    fun deselectAll() {
        for (i in itemsList) {
            i.isSelected = false
        }

        notifyDataSetChanged()
    }

}

