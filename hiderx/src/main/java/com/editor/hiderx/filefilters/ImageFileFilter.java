package com.editor.hiderx.filefilters;

import java.io.File;
import java.io.FilenameFilter;

/**
 * Created by ashishsaini on 31/7/17.
 */

public class ImageFileFilter implements FilenameFilter {
    private final  String[] acceptedExtensions = {"jpg","jpeg", "png"};;

    @Override
    public boolean accept(File dir, String filename) {

        for (String acceptedExtension : acceptedExtensions) {
            if (filename.endsWith("." + acceptedExtension)) {
                return true;
            }
        }
        return false;
    }

}
