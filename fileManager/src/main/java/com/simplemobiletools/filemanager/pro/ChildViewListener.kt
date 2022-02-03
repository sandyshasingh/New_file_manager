package com.simplemobiletools.filemanager.pro

import com.simplemobiletools.commons.ListItem

interface ChildViewListener {
    fun childItems(item: ListItem, position: Int, list:List<ListItem>)
}