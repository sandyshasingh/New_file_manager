package com.editor.hiderx.adapters

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.editor.hiderx.*
import com.editor.hiderx.database.HiddenFiles
import com.editor.hiderx.listeners.ActionModeListener
import com.editor.hiderx.listeners.OnImageSelectionListener
import kotlinx.android.synthetic.main.photos_items.view.*
import java.io.File

class HiddenPhotosAdapter(var photosList: List<HiddenFiles>, val recycler_view: RecyclerView, val context: Context?, var onImageSelectedListener: OnImageSelectionListener,var actionModeListener: ActionModeListener?) : RecyclerView.Adapter<HiddenPhotosAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.photos_items, parent, false)
        val params = v.layoutParams
        params.width = recycler_view.measuredWidth/2 - dpTopixel(context!!,16f).toInt()
        v.layoutParams = params
        return ViewHolder(v)
    }

    fun dpTopixel(c: Context?, dp: Float): Float {
        val density: Float? = c?.resources?.displayMetrics?.density
        return if(density != null)
            dp * density
        else
            dp
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItems()
    }

    override fun getItemCount(): Int {
        return photosList.size
    }

   inner class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
        fun bindItems()
        {
            if(photosList[adapterPosition].isFile!!)
            {
                itemView.rl_details?.doGone()
                itemView.image.loadUri(Uri.fromFile(File(photosList[adapterPosition].path)))
            }
            else
            {
                itemView.rl_details?.doVisible()
                itemView.image.loadUri(Uri.fromFile(File(photosList[adapterPosition].originalPath)))
                itemView.tv_folder_name?.text = photosList[adapterPosition].name
                itemView.tv_count?.text = photosList[adapterPosition].count?.toString()
            }
            if(photosList[adapterPosition].isSelected)
            {
                itemView.img_selected?.doVisible()
                itemView.selection_layer?.doVisible()
            }
            else
            {
                itemView.img_selected?.doGone()
                itemView.selection_layer?.doGone()
            }
            itemView.setOnClickListener()
            {
                    if((actionModeListener != null && actionModeListener?.getActionMode()!!) || actionModeListener == null)
                    {
                        if(photosList[adapterPosition].isFile!!)
                        selectOrDeselectItems(adapterPosition)
                    }
                    else
                    {
                        if(photosList[adapterPosition].isFile!!)
                        playFile(adapterPosition)
                        else
                            onImageSelectedListener.onImageFolderClicked(photosList[adapterPosition])
                    }
            }

            itemView.setOnLongClickListener()
            {
                if(actionModeListener !=null && !actionModeListener?.getActionMode()!! && photosList[adapterPosition].isFile!!)
                {
                    selectOrDeselectItems(adapterPosition)
                    actionModeListener?.setActionModeValue(true)
                    true
                }
                false
            }


        }

       private fun playFile(position : Int) {
           val tempList : ArrayList<HiddenFiles> = ArrayList()
           var minusCount = 0
           for(i in photosList)
           {
               if(i.isFile!!)
                tempList.add(i)
               else
                   minusCount++
           }
           var finalPosition = position-minusCount
           if(finalPosition < 0) finalPosition = 0
            onImageSelectedListener.onImageClicked(tempList,finalPosition)
       }

       private fun selectOrDeselectItems(adapterPosition: Int) {
           if(photosList[adapterPosition].isSelected)
           {
               photosList[adapterPosition].isSelected = false
               onImageSelectedListener.onImageDeselected(photosList[adapterPosition])
           }
           else
           {
               photosList[adapterPosition].isSelected = true
               onImageSelectedListener.onImageSelected(photosList[adapterPosition])
           }
           notifyItemChanged(adapterPosition)
       }
   }

}

