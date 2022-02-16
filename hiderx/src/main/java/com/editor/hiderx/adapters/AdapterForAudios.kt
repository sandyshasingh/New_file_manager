package com.editor.hiderx.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.editor.hiderx.listeners.OnAudioSelectedListener
import com.editor.hiderx.R
import com.editor.hiderx.database.HiddenFiles
import com.editor.hiderx.doGone
import com.editor.hiderx.doVisible
import com.editor.hiderx.listeners.ActionModeListener
import kotlinx.android.synthetic.main.audio_items.view.*

class AdapterForAudios(var audioList: List<HiddenFiles>, val recycler_view: RecyclerView, var context : Context, var onAudioSelectedListener : OnAudioSelectedListener, var actionModeListener: ActionModeListener?) : RecyclerView.Adapter<AdapterForAudios.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.audio_items, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItems()
    }

    override fun getItemCount(): Int {
        return audioList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindItems()
        {
            itemView.tv_name?.text = audioList[adapterPosition].name
            if(audioList[adapterPosition].isFile!!)
            {
                itemView.tv_size?.text = audioList[adapterPosition].size
                itemView.img_audio?.doVisible()
                itemView.img_folder?.doGone()
            }
            else
            {
                itemView.tv_size?.text = audioList[adapterPosition].count?.toString()
                itemView.img_audio?.doGone()
                itemView.img_folder?.doVisible()
            }
            if(audioList[adapterPosition].isSelected)
            {
                itemView.img_selected.visibility = View.VISIBLE
            }
            else
            {
                itemView.img_selected.visibility = View.GONE
            }
            itemView.setOnClickListener()
            {
                if((actionModeListener != null && actionModeListener?.getActionMode()!!) || actionModeListener == null)
                {
                    if(audioList[adapterPosition].isFile!!)
                    selectOrDeselectItems(adapterPosition)
                }
                else
                {
                    if(audioList[adapterPosition].isFile!!)
                        playFile(audioList,adapterPosition)
                    else
                        onAudioSelectedListener.onAudioFolderClicked(audioList[adapterPosition])
                }
            }
            itemView.setOnLongClickListener()
            {
                if(actionModeListener !=null && !actionModeListener?.getActionMode()!! && audioList[adapterPosition].isFile!!)
                {
                    selectOrDeselectItems(adapterPosition)
                    actionModeListener?.setActionModeValue(true)
                    true
                }
                false
            }
        }

        private fun selectOrDeselectItems(adapterPosition: Int) {
            if(audioList[adapterPosition].isSelected)
            {
                audioList[adapterPosition].isSelected = false
                onAudioSelectedListener.onAudioDeselected(audioList[adapterPosition])
            }
            else
            {
                audioList[adapterPosition].isSelected = true
                onAudioSelectedListener.onAudioSelected(audioList[adapterPosition])
            }
            notifyItemChanged(adapterPosition)
        }
    }

    private fun playFile(hiddenAudios: List<HiddenFiles>, adapterPosition: Int) {
        onAudioSelectedListener.onAudioClicked(hiddenAudios,adapterPosition)
    }

    fun deselectAll()
    {
            for(i in audioList)
            {
                i.isSelected = false
            }
        notifyDataSetChanged()
    }

}

