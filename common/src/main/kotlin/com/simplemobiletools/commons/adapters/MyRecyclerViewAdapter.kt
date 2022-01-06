package com.simplemobiletools.commons.adapters

import android.annotation.SuppressLint
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.view.ActionMode
import androidx.recyclerview.widget.RecyclerView
import com.simplemobiletools.commons.BottomNavigationVisible
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.activities.BaseSimpleActivity
import com.simplemobiletools.commons.extensions.baseConfig
import com.simplemobiletools.commons.helpers.isLongPressClick
import com.simplemobiletools.commons.interfaces.ItemOperationsListener
import com.simplemobiletools.commons.interfaces.MyActionModeCallback
import com.simplemobiletools.commons.models.FolderItem
import com.simplemobiletools.commons.views.FastScroller
import com.simplemobiletools.commons.views.MyRecyclerView
//import es.dmoral.toasty.Toasty
import java.util.*
import kotlin.collections.ArrayList

abstract class MyRecyclerViewAdapter(
    val activity: BaseSimpleActivity,val recyclerView: MyRecyclerView, val fastScroller: FastScroller? = null,
    val itemClick: (Any, Int) -> Unit,
    val isAddEnabled: ((Boolean) -> Unit)?,
    var btmListener: BottomNavigationVisible?
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    protected val baseConfig = activity.baseConfig
    protected val resources = activity.resources!!
    protected var shortcut:Boolean=false
    protected val layoutInflater = activity.layoutInflater
    protected var actModeCallback: MyActionModeCallback
    protected var selectedKeys = LinkedHashSet<Int>()
    private var bottomNavigation: View? = null

    private var actMode: ActionMode? = null
    private var actBarTextView: TextView? = null
    private var lastLongPressedItem = -1
    private var listener: ItemOperationsListener? = null

    abstract fun getActionMenuId(): Int

    abstract fun prepareActionMode(menu: Menu)

    abstract fun actionItemPressed(id: Int)

    abstract fun getSelectableItemCount(): Int

    abstract fun getIsItemSelectable(position: Int): Boolean

    abstract fun getItemSelectionKey(position: Int): Int?

    abstract fun getItemKeyPosition(key: Int): Int

    abstract fun onActionModeCreated()

    abstract fun onActionModeDestroyed()

    abstract fun mLongClick()

    protected fun isOneItemSelected() = selectedKeys.size == 1

    init {
        fastScroller?.resetScrollPositions()

        actModeCallback = object : MyActionModeCallback() {
            override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
                actionItemPressed(item.itemId)
                return true
            }

            override fun onCreateActionMode(actionMode: ActionMode, menu: Menu?): Boolean {
                if (getActionMenuId() == 0) {
                    return true
                }

                isSelectable = true
                actMode = actionMode
                actBarTextView = layoutInflater.inflate(R.layout.actionbar_title, null) as TextView
                actBarTextView!!.layoutParams = ActionBar.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                actMode!!.customView = actBarTextView
                /* actBarTextView!!.setOnClickListener {
                     if (getSelectableItemCount() == selectedKeys.size) {
                         finishActMode()
                     } else {
                         selectAll()
                     }
                 }*/
                activity.menuInflater.inflate(getActionMenuId(), menu)
                onActionModeCreated()
                return true
            }

            override fun onPrepareActionMode(actionMode: ActionMode, menu: Menu): Boolean {
                prepareActionMode(menu)
                return true
            }

            override fun onDestroyActionMode(actionMode: ActionMode) {
                isSelectable = false
                if(selectedKeys.size == 0){
                    if (!shortcut){
                        finishActMode()

                    }
                }else {
                    (selectedKeys.clone() as HashSet<Int>).forEach {
                        val position = getItemKeyPosition(it)
                        if (position != -1) {
                            toggleItemSelection(false, position, false, null)
                        }
                    }
                }
                updateTitle()
                selectedKeys.clear()
                actBarTextView?.text = ""
                actMode = null
                lastLongPressedItem = -1
                isLongPressClick = false
//                isActionModeEnabled = false
                if(!shortcut){
                    listener?.refreshItems(false)
                    notifyDataSetChanged()

                }

                onActionModeDestroyed()
            }
        }
    }

    protected fun toggleItemSelection(select: Boolean, pos: Int, updateTitle: Boolean = true, i: Int?) {
//        if (select && !getIsItemSelectable(pos-1)) {
//            return
//        }

        val itemKey = getItemSelectionKey(pos) ?: return
        if ((select && selectedKeys.contains(itemKey)) || (!select && !selectedKeys.contains(itemKey))) {
            return
        }

        if (select) {
            selectedKeys.add(itemKey)
        } else {
            selectedKeys.remove(itemKey)
        }

        notifyItemChanged(pos)

        if (updateTitle) {
            updateTitle()
        }
        if(i == null) {
            if (selectedKeys.isEmpty()) {
                if (!shortcut){
                    finishActMode()
                }

            }
        }
    }

    private fun updateTitle() {
        val selectableItemCount = getSelectableItemCount()
        val selectedCount = selectedKeys.size.coerceAtMost(selectableItemCount)
        val oldTitle = actBarTextView?.text
        val newTitle = "$selectedCount / $selectableItemCount"
        if (oldTitle != newTitle) {
            actBarTextView?.text = newTitle
            actMode?.invalidate()
        }
    }

    fun itemLongClicked(position: Int) {
        recyclerView.setDragSelectActive(position)
        lastLongPressedItem = if (lastLongPressedItem == -1) {
            position
        } else {
            val min = lastLongPressedItem.coerceAtMost(position)
            val max = lastLongPressedItem.coerceAtLeast(position)
            for (i in min..max) {
                toggleItemSelection(true, i, false, null)
            }
            updateTitle()
            position
        }
        mLongClick()
    }

    /* protected fun getSelectedItemPositions(sortDescending: Boolean = true): ArrayList<Int> {
         val positions = ArrayList<Int>()
         val keys = selectedKeys.toList()
         keys.forEach {
             val position = getItemKeyPosition(it)
             if (position != -1) {
                 positions.add(position)
             }
         }

         if (sortDescending) {
             positions.sortDescending()
         }
         return positions
     }*/

    protected fun selectAll() {
        if(bottomNavigation!=null && bottomNavigation?.visibility==View.GONE) {
            bottomNavigation?.visibility=View.VISIBLE
        }

        for (i in 0 until itemCount) {
            toggleItemSelection(true, i, false, 5)
        }
        lastLongPressedItem = -1
        updateTitle()
    }

    protected fun deSelectAll() {
//        if(bottomNavigation!=null && bottomNavigation?.visibility==View.VISIBLE) {
//            bottomNavigation?.visibility=View.GONE
//
//        }
        btmListener?.btmVisible(false)
        for (i in 0 until itemCount) {
            toggleItemSelection(false, i, false,5)
        }
        lastLongPressedItem = -1
        updateTitle()
    }

    /* protected fun setupDragListener(enable: Boolean) {
         if (enable) {
             recyclerView.setupDragListener(object : MyRecyclerView.MyDragListener {
                 override fun selectItem(position: Int) {
                     toggleItemSelection(true, position, true)
                 }

                 override fun selectRange(initialSelection: Int, lastDraggedIndex: Int, minReached: Int, maxReached: Int) {
                     selectItemRange(initialSelection,0.coerceAtLeast(lastDraggedIndex), 0.coerceAtLeast(minReached), maxReached)
                     if (minReached != maxReached) {
                         lastLongPressedItem = -1
                     }
                 }
             })
         } else {
             recyclerView.setupDragListener(null)
         }
     }*/

    /* protected fun selectItemRange(from: Int, to: Int, min: Int, max: Int) {
         if (from == to) {
             (min..max).filter { it != from }.forEach { toggleItemSelection(false, it, true) }
             return
         }

         if (to < from) {
             for (i in to..from) {
                 toggleItemSelection(true, i, true)
             }

             if (min > -1 && min < to) {
                 (min until to).filter { it != from }.forEach { toggleItemSelection(false, it, true) }
             }

             if (max > -1) {
                 for (i in from + 1..max) {
                     toggleItemSelection(false, i, true)
                 }
             }
         } else {
             for (i in from..to) {
                 toggleItemSelection(true, i, true)
             }

             if (max > -1 && max > to) {
                 (to + 1..max).filter { it != from }.forEach { toggleItemSelection(false, it, true) }
             }

             if (min > -1) {
                 for (i in min until from) {
                     toggleItemSelection(false, i, true)
                 }
             }
         }
     }*/

    fun setupZoomListener(zoomListener: MyRecyclerView.MyZoomListener?) {
        recyclerView.setupZoomListener(zoomListener)
    }


    @SuppressLint("RestrictedApi")
    fun finishActMode() {
        actMode?.finish()
        isLongPressClick = false
//        isActionModeEnabled = false
        //isHeaderShow = true



        if(!shortcut){
            listener?.refreshItems(false)
            notifyDataSetChanged()

        }
      /*  if(bottomNavigation!=null && bottomNavigation?.visibility==View.VISIBLE) {
            bottomNavigation?.visibility=View.GONE
        }*/
        btmListener?.btmVisible(false)
    }

    protected fun createViewHolder(layoutType: Int, parent: ViewGroup?): ViewHolder {
        val view = layoutInflater.inflate(layoutType, parent, false)
        return ViewHolder(view)
    }

    protected fun bindViewHolder(holder: RecyclerView.ViewHolder) {
        holder.itemView.tag = holder
    }

    protected fun removeSelectedItems(positions: ArrayList<Int>) {
        positions.forEach {
            notifyItemRemoved(it)
        }
        finishActMode()
        fastScroller?.measureRecyclerView()
    }



    private fun headFolderClick(folder: FolderItem) {
        listener?.headerFolderClick(folder)
     //   Toasty.success(activity, folder.folderName, Toast.LENGTH_LONG).show()
        Toast.makeText(activity,folder.folderName,Toast.LENGTH_SHORT).show()


    }
    open inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        fun bindView(any: Any,position: Int, lis: ItemOperationsListener?,
                     allowSingleClick: Boolean, allowLongClick: Boolean, callback: (itemView: View, adapterPosition: Int) -> Unit): View {
//            bottomNavigation=bottomnavigation
            listener=lis

            return itemView.apply {
                callback(this, adapterPosition)

                if (allowSingleClick) {
                    setOnClickListener {
                        viewClicked(any, position, true)
                    }
                    setOnLongClickListener {
                        if (allowLongClick) {
//                            isHeaderShow=false
                            viewLongClicked(position)
                            notifyDataSetChanged()
                        }
                        else {
                            viewClicked(any, position, false)
                        }
                        true
                    }
                } else {
                    setOnClickListener(null)
                    setOnLongClickListener(null)
                }
            }
        }



        fun viewClicked(any: Any, position: Int, fromItemClicked: Boolean) {
//            if(fromItemClicked && actModeCallback.isSelectable){
//                itemClick.invoke(any,position)
//               // bottomNavigation?.visibility=View.GONE
//            }
           // var fromShortcut = fromItemClicked && actModeCallback.isSelectable
            if(actModeCallback.isSelectable && (!shortcut || !fromItemClicked ))
            {
                val isSelected = selectedKeys.contains(getItemSelectionKey(position))
                toggleItemSelection(!isSelected, position, true, null)
                if(selectedKeys.size>0){

                    isAddEnabled?.invoke(true)

//                    if(bottomNavigation!=null && bottomNavigation?.visibility==View.GONE && !shortcut) {
//                        bottomNavigation?.visibility=View.VISIBLE
//                    }
                }
                else
                {
                    isAddEnabled?.invoke(false)
                }
               // activity.startSupportActionMode(actModeCallback)
            }
            else {
                itemClick.invoke(any,position)
            }

            /*if (actModeCallback.isSelectable) {
                val isSelected = selectedKeys.contains(getItemSelectionKey(position))
                toggleItemSelection(!isSelected, position, true, null)
            } else {
                itemClick.invoke(any,position)
            }*/
            lastLongPressedItem = -1
        }

        private fun viewLongClicked(position: Int) {
            if (!actModeCallback.isSelectable) {
                activity.startSupportActionMode(actModeCallback)
            }

            toggleItemSelection(true, position, true, null)
            itemLongClicked(position)
        }
    }
}
