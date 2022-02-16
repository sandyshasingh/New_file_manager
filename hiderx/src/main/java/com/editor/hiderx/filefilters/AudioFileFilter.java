package com.editor.hiderx.filefilters;


import java.io.File;
import java.io.FilenameFilter;

/**
 * Created by ashishsaini on 31/7/17.
 */

public class AudioFileFilter implements FilenameFilter {
    private final static String[] acceptedExtensions= {"mp3", "mp2", "wav", "flac", "ogg", "au" , "snd", "mid", "midi", "kar"
            , "mga", "aif", "aiff", "aifc", "m3u", "oga", "spx"};

    @Override
    public boolean accept(File dir, String filename) {
        for (String acceptedExtension : acceptedExtensions) {
            if (filename.endsWith("." + acceptedExtension)) return true;
        }

        return false;
    }

}
