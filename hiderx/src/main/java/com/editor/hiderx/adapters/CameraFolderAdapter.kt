package com.editor.hiderx.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.editor.hiderx.dataclass.FileDataClass
import com.editor.hiderx.listeners.OnFolderClickListener
import com.editor.hiderx.R
import kotlinx.android.synthetic.main.folder_item.view.*

class CameraFolderAdapter(var folderList : ArrayList<FileDataClass>?, var onItemclickListener : OnFolderClickListener?) : RecyclerView.Adapter<CameraFolderAdapter.ViewHolder>() {



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.folder_item, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItem(folderList?.get(holder.adapterPosition))
    }

    override fun getItemCount(): Int {
        return if(folderList!=null && folderList?.isNotEmpty()!!)
            folderList?.size!!
        else
            0
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    {
            fun bindItem(folder : FileDataClass?)
            {
                    itemView.setOnClickListener()
                    {
                        onItemclickListener?.onFolderClicked(folder!!)
                    }
                    itemView.folder_name?.text = folder?.name
                    itemView.tv_details?.text = folder?.noOfItems.toString()+" items  |  "+folder?.size
            }
    }

}