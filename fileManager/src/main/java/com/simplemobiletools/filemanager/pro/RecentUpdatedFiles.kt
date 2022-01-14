package com.simplemobiletools.filemanager.pro

import com.simplemobiletools.filemanager.pro.models.ListItem

data class RecentUpdatedFiles(
    val mKeys: List<String>,
    val mValues: List<List<ListItem>>,
)
