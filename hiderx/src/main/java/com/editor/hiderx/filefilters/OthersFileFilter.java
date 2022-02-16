package com.editor.hiderx.filefilters;


import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;

/**
 * Created by ashishsaini on 31/7/17.
 */

public class OthersFileFilter implements FileFilter {
    private final static String[] acceptedExtensions= {"docx","pdf","jpg","jpeg", "png","mp4","mp4v", "avi", "asf","avchd","dav", "arf", "ts", "mov", "qt","trc", "dv4", "dv4"
            , "mpg","mpeg", "mpeg4","webm","ogv","vp9","vob", "3gp", "riff", "m2ts", "m3u", "avc", "mkv", "wav", "flv", "wmv", "divx","swf","mp3", "mp2", "wav", "flac", "ogg", "au" , "snd", "mid", "midi", "kar"
            , "mga", "aif", "aiff", "aifc", "m3u", "oga", "spx","amr","webp"};


    @Override
    public boolean accept(File pathname) {
        boolean shouldInclude  = true;
        for (String acceptedExtension : acceptedExtensions) {
            if (pathname.getName().endsWith("." + acceptedExtension))
            {
                shouldInclude = false;
                break;
            }
        }
        return shouldInclude;
    }
}
