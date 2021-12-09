package com.simplemobiletools.commons;

import static com.simplemobiletools.commons.ExtensionKt.logException;

import android.net.Uri;
import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

import java.io.File;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;


/**
 * Created by ashishsaini on 28/7/17.
 */

public class VideoFileInfo extends BaseFile implements  Serializable  {

    private static final long serialVersionUID = 1L;

    @SerializedName("row_id")
    public long row_ID = 0L;

    @SerializedName("file_path")
    public String file_path;

    @SerializedName("file_name")
    public String file_name;

    @SerializedName("createdTime")
    public long createdTime;

    @SerializedName("isDirectory")
    public boolean isDirectory;

    @SerializedName("last_duration")
    public Long lastPlayedDuration = 0L;

    @SerializedName("newTag")
    public String newTag ="";

    @SerializedName("resolution")
    public String resolution ="";

    @SerializedName("recentTag")
    public String recentTag = "";

    @SerializedName("uri")
    public Uri uri = null;

    public String fileLocation;

    public String getRecentTag() {
        return recentTag;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }


    public long getFileDuration(){
        if (getFileInfo() == null ||getFileInfo().getDuration()==null){
            return 0L;
        }

        long duration = getFileInfo().getDuration();
        if (duration<1) {
            return 0L;
        }
        return duration/1000;

    }

    public   String getFile_duration() {
        if (getFileInfo() == null ||getFileInfo().getDuration()==null){
            return "";
        }

        long duration = getFileInfo().getDuration();
        if (duration<1) {
            return "";
        }

        DecimalFormat df = new DecimalFormat("0.00");

        float seconds = 1000.0f;
        float minute = seconds * 60;
        float hours = minute * 60;



        if(duration < minute)
            return df.format(duration / seconds)+ " seconds";
        else if(duration < hours)
            return df.format(duration / minute) + " minutes";
        else if(duration > hours) {
            return df.format(duration / hours) + " hours";
        }


        return "";
    }

    public   String getFile_duration_inDetail() {

        if (getFileInfo() == null ||getFileInfo().getDuration()==null){
            return "";
        }

        long duration = getFileInfo().getDuration();
        if (duration<1) {
            return "";
        }

        duration = (long) (duration/1000.0f);//seconds
        long sec = duration % 60;
        long minutes = duration % 3600 / 60;
        long hours = duration % 86400 / 3600;


        if(hours > 0) {

           // return hours +" hrs "+minutes+ " min "+sec +" sec";

            if (sec<10){
                if (minutes < 10) {
                    return hours + ":0" + minutes + ":0" + sec;
                }
                else {
                    return hours + ":" + minutes + ":0" + sec;
                }

            } else if(sec >9){
                if (minutes < 10) {
                    return hours + ":0" + minutes + ":" + sec;
                }
                else {
                    return hours + ":" + minutes + ":" + sec;
                }
            }

            return hours +":"+minutes+ ":"+sec;

        }
        else if(minutes >0) {
           // return minutes+ " min "+sec +" sec";
            if (sec<10){
                return minutes+ ":0"+sec;
            }
            return minutes+ ":"+sec;

        }
        else if(sec > 0) {
           // return sec +" sec";
            if (sec<10){
                return "0:0"+sec;
            }
            return "0:"+sec;
        }


        return "";
    }


    public Long getCreatedTime() {
        try {
            File file = new File(this.file_path);
            createdTime = file.lastModified();
            return createdTime;
        }catch (Exception e){
            return Long.valueOf(0);
        }
    }

    public String getCreatedDateFormat() {
        try {
            if (!TextUtils.isEmpty(recentTag)){
                return recentTag;
            }
            File file = new File(file_path);
            SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
            if (file != null) {
                return format1.format(file.lastModified());
            }
        }catch (Exception e){
           logException(new Throwable("Creation Date Format issue",e));
        }
        return "N/A";
    }
    public  String getStringSizeLengthFile() {
        if (getFileInfo() == null){
            return "";
        }
        long size = getFileInfo().size;
        if (size<1) {
            return "";
        }

        DecimalFormat df = new DecimalFormat("0.00");
        float sizeKb = 1024.0f;
        float sizeMo = sizeKb * sizeKb;
        float sizeGo = sizeMo * sizeKb;
        float sizeTerra = sizeGo * sizeKb;


        if(size < sizeMo)
            return df.format(size / sizeKb)+ " Kb";
        else if(size < sizeGo)
            return df.format(size / sizeMo) + " Mb";
        else if(size < sizeTerra)
            return df.format(size / sizeGo) + " Gb";

        return "";
    }

    @Override
    public int hashCode() {
        if (isFindDuplicate) {
            return getFileInfo().hashCode();
        }
        return file_path.hashCode() ;
    }

    @Override
    public boolean equals(Object obj) {
        if (isFindDuplicate) {
            VideoFileInfo commonFile = (VideoFileInfo) obj;
            return commonFile.getFileInfo().equals(getFileInfo());
        }
        else{
            VideoFileInfo commonFile = (VideoFileInfo) obj;
            return file_path.equalsIgnoreCase(commonFile.file_path);
        }
    }

    public long getRow_ID() {
        return row_ID;
    }

    @Override
    public String toString() {
        return "VideoFileInfo{" +
                "row_ID=" + row_ID +
                ", file_path='" + file_path + '\'' +
                ", file_name='" + file_name + '\'' +
                ", createdTime=" + createdTime +
                ", isDirectory=" + isDirectory +
                ", lastPlayedDuration=" + lastPlayedDuration +
                ", newTag='" + newTag + '\'' +
                ", resolution='" + resolution + '\'' +
                ", recentTag='" + recentTag + '\'' +
              //  ", newVideoCount='" + newVideoCount + '\'' +
                '}';
    }
}
