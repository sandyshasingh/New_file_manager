package com.editor.hiderx.filefilters;


import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;

/**
 * Created by ashishsaini on 31/7/17.
 */

public class DocumentsFileFilter implements FileFilter {
    private final static String[] acceptedExtensions= {"docx","pdf","txt"};

    @Override
    public boolean accept(File pathname) {
        if(pathname.isFile())
        {
            for (String acceptedExtension : acceptedExtensions) {
                if (pathname.getName().endsWith("." + acceptedExtension)) return true;
            }
        }
        else
            return true;
        return false;
    }
}
