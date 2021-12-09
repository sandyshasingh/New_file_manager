package com.simplemobiletools.commons;

import androidx.recyclerview.widget.DiffUtil;

import com.simplemobiletools.commons.models.FolderItem;

import java.util.List;

public class FolderDiffCallBack extends DiffUtil.Callback {
    private final List<FolderItem> mOldEmployeeList;
    private final List<FolderItem> mNewEmployeeList;

    public FolderDiffCallBack(List<FolderItem> oldEmployeeList, List<FolderItem> newEmployeeList) {
        this.mOldEmployeeList = oldEmployeeList;
        this.mNewEmployeeList = newEmployeeList;
    }

    @Override
    public int getOldListSize() {
        return mOldEmployeeList.size();
    }

    @Override
    public int getNewListSize() {
        return mNewEmployeeList.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return mOldEmployeeList.get(oldItemPosition).hashCode() == mNewEmployeeList.get(
                newItemPosition).hashCode();
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        final FolderItem oldEmployee = mOldEmployeeList.get(oldItemPosition);
        final FolderItem newEmployee = mNewEmployeeList.get(newItemPosition);
        return oldEmployee.getClickCount() == newEmployee.getClickCount();
    }
}
