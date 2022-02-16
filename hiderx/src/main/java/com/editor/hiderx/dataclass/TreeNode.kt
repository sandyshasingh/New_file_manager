package com.editor.hiderx.dataclass

class TreeNode<T>(val value: T) {
        var children: MutableList<TreeNode<T>> = mutableListOf()
        fun add(child : TreeNode<T>)
        {
            children.add(child)
        }
}
    