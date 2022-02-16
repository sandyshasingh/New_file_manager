package com.editor.hiderx.adapters

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.editor.hiderx.*
import com.editor.hiderx.dataclass.FileDataClass
import com.editor.hiderx.fragments.FOLDER_TYPE_DEFAULT
import com.editor.hiderx.listeners.ActionModeListener
import com.editor.hiderx.listeners.OnFileClickedListener
import kotlinx.android.synthetic.main.folder_item.view.*
import kotlinx.android.synthetic.main.folder_item.view.img_selected
import kotlinx.android.synthetic.main.photos_items.view.*
import java.io.File

class HiddenFilesAdapter(
        var filesList: ArrayList<FileDataClass>,
        val recycler_view: RecyclerView,
        val context: Context?,
        var onFileSelectedListener: OnFileClickedListener,
        var actionModeListener: ActionModeListener?,
        var currentType: Int?
) : RecyclerView.Adapter<HiddenFilesAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.folder_item, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItems()
    }

    override fun getItemCount(): Int {
        return filesList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bindItems() {
            itemView.folder_name?.text = filesList[adapterPosition].name
            if (filesList[adapterPosition].isFile) {
                itemView.tv_details?.text = filesList[adapterPosition].size
            } else {
                itemView.tv_details?.text = filesList[adapterPosition].noOfItems?.toString() + " items"
            }
            if (filesList[adapterPosition].isSelected) {
                itemView.img_selected?.visibility = View.VISIBLE
            } else {
                itemView.img_selected?.visibility = View.GONE
            }
            val mimeType = filesList[adapterPosition].mimeType
            when {
                mimeType == null -> {
                    itemView.file_thumbnail?.doGone()
                    itemView.img_folder?.doVisible()
                    itemView.img_documents?.doGone()
                    itemView.img_audio?.doGone()
                }
                mimeType.startsWith("audio") -> {
                    itemView.file_thumbnail?.doGone()
                    itemView.img_folder?.doGone()
                    itemView.img_documents?.doGone()
                    itemView.img_audio?.doVisible()
                }
                mimeType.startsWith("video") || mimeType.startsWith("image") -> {
                    itemView.img_folder?.doGone()
                    itemView.img_audio?.doGone()
                    itemView.img_documents?.doGone()
                    itemView.file_thumbnail?.doVisible()
                    itemView.file_thumbnail.loadUri(Uri.fromFile(File(filesList[adapterPosition].path)))
                }
                filesList[adapterPosition].name?.endsWith("pdf")!! -> {
                    itemView.file_thumbnail?.doGone()
                    itemView.img_folder?.doGone()
                    itemView.img_audio?.doGone()
                    itemView.img_documents?.doVisible()
                    itemView.img_documents?.setImageResource(R.drawable.ic_pdf)
                }
                filesList[adapterPosition].name?.endsWith("docx")!! -> {
                    itemView.file_thumbnail?.doGone()
                    itemView.img_folder?.doGone()
                    itemView.img_audio?.doGone()
                    itemView.img_documents?.doVisible()
                    itemView.img_documents?.setImageResource(R.drawable.ic_docx)
                }
                filesList[adapterPosition].name?.endsWith("txt")!! -> {
                    itemView.file_thumbnail?.doGone()
                    itemView.img_folder?.doGone()
                    itemView.img_audio?.doGone()
                    itemView.img_documents?.doVisible()
                    itemView.img_documents?.setImageResource(R.drawable.ic_txt)
                }
                else -> {
                    itemView.file_thumbnail?.doGone()
                    itemView.img_folder?.doVisible()
                    itemView.img_audio?.doGone()
                    itemView.img_documents?.doGone()
                }
            }

            itemView.setOnClickListener()
            {
                if (adapterPosition >= 0) {
                    when {
                        (actionModeListener != null && actionModeListener?.getActionMode()!! && filesList[adapterPosition].isFile) -> selectOrDeselectFile(adapterPosition)
                        filesList[adapterPosition].isFile -> {
                            viewFile(filesList,adapterPosition)
                        }
                        (actionModeListener != null && !actionModeListener?.getActionMode()!!) -> onFileSelectedListener.onFolderClicked(filesList[adapterPosition])
                    }
                }
            }

            itemView.setOnLongClickListener()
            {

                if (currentType != FOLDER_TYPE_DEFAULT && !actionModeListener?.getActionMode()!! && filesList[adapterPosition].isFile) {
                    actionModeListener?.setActionModeValue(true)
                    selectOrDeselectFile(adapterPosition)
                    return@setOnLongClickListener true
                }
                false
            }
        }

        private fun viewFile(listOfFiles: ArrayList<FileDataClass>, position: Int) {
            onFileSelectedListener?.onFileClicked(listOfFiles,position)
        }

        private fun selectOrDeselectFile(adapterPosition: Int) {
            if (filesList[adapterPosition].isSelected) {
                filesList[adapterPosition].isSelected = false
                onFileSelectedListener.onFileDeselected(filesList[adapterPosition])
            } else {
                filesList[adapterPosition].isSelected = true
                onFileSelectedListener.onFileSelected(filesList[adapterPosition])
            }
            notifyItemChanged(adapterPosition)
        }

    }

}

