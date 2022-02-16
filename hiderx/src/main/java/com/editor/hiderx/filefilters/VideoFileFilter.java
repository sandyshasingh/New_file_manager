package com.editor.hiderx.filefilters;


import java.io.File;
import java.io.FilenameFilter;

/**
 * Created by ashishsaini on 31/7/17.
 */


public class VideoFileFilter implements FilenameFilter {
    private final  String[] acceptedExtensions = {"mp4","mp4v", "avi", "asf","avchd","dav", "arf", "ts", "mov", "qt","trc", "dv4", "dv4"
            , "mpg","mpeg", "mpeg4","webm","ogv","vp9","vob", "3gp", "riff", "m2ts", "m3u", "avc", "mkv", "wav", "flv", "wmv", "divx","swf"};

    @Override
    public boolean accept(File dir, String filename) {

        for (int _i = 0; _i < acceptedExtensions.length; _i++) {
            if (filename.endsWith("." + acceptedExtensions[_i])) {
                return true;
            }
        }
        return false;
    }

}
