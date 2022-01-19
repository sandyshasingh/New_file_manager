package com.simplemobiletools.filemanager.pro;


import androidx.recyclerview.widget.DiffUtil;

import com.simplemobiletools.commons.ListItem;

import java.util.List;

public class ListItemDiffCallback extends DiffUtil.Callback {
    private final List<ListItem> mOldEmployeeList;
    private final List<ListItem> mNewEmployeeList;

    public ListItemDiffCallback(List<ListItem> oldEmployeeList, List<ListItem> newEmployeeList) {
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
        final ListItem oldEmployee = mOldEmployeeList.get(oldItemPosition);
        final ListItem newEmployee = mNewEmployeeList.get(newItemPosition);
        return oldEmployee.getMPath().equals(newEmployee.getMPath());
    }


}
