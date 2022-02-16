package com.editor.hiderx.listeners;

import android.net.Uri;

public interface ScannerListener {

    void oneComplete(String path, Uri uri);

    void allComplete(String[] filePaths);
}
