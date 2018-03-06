package com.filescanner.rmann52.androidfilescanner.model;

/**
 * Created by rmann52 on 3/2/18.
 */

public class RecyclerItemInfo {
    private String mFilename;

    private String mFileCount;

    public RecyclerItemInfo(String name, String count){
        mFilename = name;
        mFileCount = count;
    }

    public String getmFilename() {
        return mFilename;
    }

    public void setmFilename(String mFilename) {
        this.mFilename = mFilename;
    }

    public String getmFileCount() {
        return mFileCount;
    }

    public void setmFileCount(String mFileCount) {
        this.mFileCount = mFileCount;
    }


}
