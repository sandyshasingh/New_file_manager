/*
package com.simplemobiletools.filemanager.pro.extensions;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

*/
/*import com.malmstein.fenster.exoplayer.ExoVideoPlayerActivity;
import com.malmstein.fenster.helper.ExoPlayerDataHolder;
import com.malmstein.fenster.model.RootHelper;
import com.malmstein.fenster.model.VideoFileInfo;
import com.rocks.themelibrary.Constants;*//*

import com.simplemobiletools.commons.VideoFileInfo;
import com.simplemobiletools.filemanager.pro.R;

import java.util.LinkedList;
import java.util.List;

import static com.simplemobiletools.filemanager.pro.extensions.FileManagerUtilsKt.getVideoFilePosition;

public class FetchVideoFileService extends AsyncTask<Void,Void, List<VideoFileInfo>> {

    List<VideoFileInfo> videoFileInfoArrayList = new LinkedList();
    private String directoryPath;
    private String filePath;

    private boolean allVideos;
    private boolean filter_duplicates;
    private Context mContext;

    public FetchVideoFileService(Context context,String filePath , String path, boolean allVideos, boolean filterDuplicate) {
        directoryPath = path;
        this.allVideos = allVideos;
        this.filePath = filePath;
        this.filter_duplicates = filterDuplicate;
      //  this.mIDataLoaderListener = iDataLoaderListener;
        this.mContext  = context;
    }

    @Override
    protected List<VideoFileInfo> doInBackground(Void... voids) {
        try {
            videoFileInfoArrayList = RootHelper.getVideoFilesListFromFolder(mContext, directoryPath, 1, true, false, filter_duplicates);
            return videoFileInfoArrayList;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(List<VideoFileInfo> videoFileInfoArrayList) {
        super.onPostExecute(videoFileInfoArrayList);
        if (videoFileInfoArrayList !=null  && videoFileInfoArrayList.size()>0) {
            Intent videoIntent = new Intent(mContext, ExoVideoPlayerActivity.class);
           // videoIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            videoIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            int itemPosition = getVideoFilePosition(videoFileInfoArrayList, filePath);
            ExoPlayerDataHolder.setData(videoFileInfoArrayList);
            videoIntent.putExtra(Constants.EXOPLAYER_VIDEO_ITEM_INDEX, itemPosition);
            videoIntent.putExtra("DURATION", 0);
            mContext.startActivity(videoIntent);
          //  ((Activity)mContext).overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

        }
     //   mIDataLoaderListener.onDataFetched(videoFileInfoArrayList);
    }


}
*/
