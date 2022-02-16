package com.editor.hiderx.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.editor.hiderx.listeners.FragmentInteractionListener
import com.editor.hiderx.R
import com.editor.hiderx.doGone
import com.editor.hiderx.doVisible
import kotlinx.android.synthetic.main.bottom_top_view.view.*
import kotlinx.android.synthetic.main.layout_bottom_folders.view.*
import java.io.File

const val VIEW_TYPE_FOLDERS = 1
const val VIEW_TYPE_TOP = 0

class BottomViewFoldersAdapter(var defaultFolder: String, var folders: ArrayList<String>?, val fragmentInteractionListener: FragmentInteractionListener?) : RecyclerView.Adapter<RecyclerView.ViewHolder>()
{

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return if(viewType == VIEW_TYPE_TOP) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.bottom_top_view, parent, false)
            TopViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_bottom_folders, parent, false)
            ViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if(holder is ViewHolder)
            holder.bindItems()
            else if(holder is TopViewHolder)
               holder.bindItems()
    }

    override fun getItemViewType(position: Int): Int {
        return if(position == 0) {
            VIEW_TYPE_TOP
        } else {
            VIEW_TYPE_FOLDERS
        }
    }

    override fun getItemCount(): Int {
        if(folders!=null && folders?.size!!>0)
        {
            return folders?.size!!+1
        }
        else
        {
            return 1
        }
    }

    inner class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView)
    {
        fun bindItems() {
            val folderPath = folders?.get(adapterPosition-1)
            itemView.folder_name?.text = File(folderPath!!).name
            itemView.setOnClickListener()
            {
                fragmentInteractionListener?.dismissDialog()
                fragmentInteractionListener?.setDirectoryToFolder(File(folderPath).path,adapterPosition-1)
            }
            if(fragmentInteractionListener?.getSelectedFolder() != null)
            {
                if(File(fragmentInteractionListener?.getSelectedFolder()!!).name == File(folderPath).name)
                {
                    itemView.btn_select?.visibility = View.VISIBLE
                }
                else
                {
                    itemView.btn_select?.visibility = View.GONE
                }
            }
        }
    }

    inner class TopViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView)
    {
        fun bindItems()
        {
            itemView.tv_default?.text = File(defaultFolder).name
            if(folders!=null && folders?.size!!>0)
            {
                itemView.view_my_folders.doVisible()
            }
            else
            {
                itemView.view_my_folders?.doGone()
            }
            itemView.layout_default?.setOnClickListener()
            {
                    fragmentInteractionListener?.setDirectoryToDefault()
            }
            itemView.layout_new_folder?.setOnClickListener()
            {
                    fragmentInteractionListener?.showNewFolderDialog()
            }
            if(fragmentInteractionListener?.getSelectedFolder()!=null)
            {
                if(File(fragmentInteractionListener.getSelectedFolder()!!).name == File(defaultFolder).name)
                {
                    itemView.btn_select_default?.visibility = View.VISIBLE
                }
                else
                {
                    itemView.btn_select_default?.visibility = View.GONE
                }
            }
        }
    }
}