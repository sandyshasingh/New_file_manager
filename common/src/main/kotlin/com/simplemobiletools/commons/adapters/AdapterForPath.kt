package com.simplemobiletools.commons.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.helpers.INTERNAL_STORAGE_NAME
import com.simplemobiletools.commons.helpers.SD_CARD_NAME
import com.simplemobiletools.commons.models.FileDirItem
import kotlinx.android.synthetic.main.item_path.view.*
import java.io.File


class AdapterForPath(var pathList: ArrayList<String>, var listener: BreadcrumbsListenerNew?, var mContext: Context,var mpathtext:String?) : RecyclerView.Adapter<AdapterForPath.ViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_path, parent, false)
        mContext=parent.context
        return ViewHolder(v,listener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (position==0 && !(mpathtext== INTERNAL_STORAGE_NAME||mpathtext== SD_CARD_NAME)){
//          //  pathList.clear()
//            pathList.add(mpathtext!!)
//            holder.bindItems(pathList[position],mContext,pathList)
            holder.itemView.folder_path.text = mpathtext
            holder.itemView.arrow.visibility = View.GONE
        }
       else{
            holder.bindItems(pathList[position],mContext,pathList)

        }


    }

    override fun getItemCount(): Int {
        return pathList.size
    }

    class ViewHolder( itemView: View,var listener: BreadcrumbsListenerNew?) : RecyclerView.ViewHolder(itemView) {

        var context : Context? = null
        fun bindItems(path: String, mContext: Context, pathList: ArrayList<String>
        ) {
            context = mContext

            val folderNameTextView = itemView.folder_path
            val arrowIcon = itemView.arrow
            if(adapterPosition==0){
                arrowIcon?.beGone()
            }

            val item = setBreadcrumb(path)
            if(item!=null) {
                folderNameTextView?.text = item.name
            }
            itemView.setOnClickListener{
               if (File(pathList[adapterPosition]).listFiles().isNotEmpty())
                 listener?.breadcrumbClickedNew(pathList[adapterPosition],adapterPosition)

            }
        }

        private fun setBreadcrumb(fullPath: String): FileDirItem? {
            val basePath = fullPath.getBasePath(context!!)
            var currPath = basePath
            val tempPath = context!!.humanizePath(fullPath)
            var item : FileDirItem? = null

            val dirs = tempPath.split("/").dropLastWhile(String::isEmpty)
            for (i in dirs.indices) {
                val dir = dirs[i]
                if (i > 0) {
                    currPath += "$dir/"
                }

                if (dir.isEmpty()) {
                    continue
                }
                currPath = "${currPath.trimEnd('/')}/"
                item = FileDirItem(currPath, dir, true, 0, 0, 0)
            }
            return item
        }

    }

    fun updateDataAndNotify(updatedPathList: List<String>) {
        this.pathList= updatedPathList as ArrayList<String>
        notifyDataSetChanged()
    }

    interface BreadcrumbsListenerNew {
        fun breadcrumbClickedNew(path: String,position: Int)
    }


}

