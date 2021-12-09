package com.simplemobiletools.commons;


import java.io.Serializable;

import static android.R.attr.type;

/**
 * A data model containing data for a single media item.
 */
public class MediaStoreData extends PhotoBaseFile implements Serializable {

  public final long rowId;
  public final String uri;
  public final String mimeType;
  public final long dateModified;
  public final int orientation;
  //public final Type type;
  public final long dateTaken;
  public final String dateForamt;
  public long fileSize;

  public boolean isSavedInStatus;
  public  String fileName;
  public  String newTag;



  public MediaStoreData(long rowId, String uri, long fileSize, String mimeType, long dateTaken, long dateModified,
                        int orientation, String dateForamt, String newTag) {
    this.rowId = rowId;
    this.uri = uri;
    this.dateModified = dateModified;
    this.mimeType = mimeType;
    this.orientation = orientation;
    //this.type = type;
    this.dateTaken = dateTaken;
    this.dateForamt = dateForamt;
    this.fileSize = fileSize;
    this.newTag = newTag;
  }

  @Override
  public int hashCode() {
    if (isFindDuplicate) {
      return getFileInfo().hashCode();
    }
    return uri.hashCode() ;
  }

  @Override
  public boolean equals(Object obj) {
    if (isFindDuplicate) {
      MediaStoreData commonFile = (MediaStoreData) obj;
      return commonFile.getFileInfo().equals(getFileInfo());
    }
    else{
      MediaStoreData commonFile = (MediaStoreData) obj;
      return uri.equalsIgnoreCase(commonFile.uri);
    }
  }


  @Override
  public String toString() {
    return "MediaStoreData{"
            + "rowId=" + rowId
            + ", uri=" + uri
            + ", mimeType='" + mimeType + '\''
            + ", dateModified=" + dateModified
            + ", orientation=" + orientation
            + ", type=" + type
            + ", dateTaken=" + dateTaken
            + ", newTag=" + newTag
            + '}';
  }

  /**
   * The type of data.
   */
  public enum Type {
    VIDEO,
    IMAGE
  }
}
