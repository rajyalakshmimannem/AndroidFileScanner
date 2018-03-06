package com.filescanner.rmann52.androidfilescanner.model;

import com.filescanner.rmann52.androidfilescanner.util.ScanUtils;

import java.io.Serializable;

/**
 * Created by rmann52 on 3/2/18.
 */

public class ScanFileInfo implements Comparable<ScanFileInfo>, Serializable {
    private String mName;
    private long mSize; // size in Kb

    private String sizeString;

    public ScanFileInfo(String name, long count) {
        mName = name;
        mSize = count;
    }

    @Override
    public int compareTo(ScanFileInfo fileInfo) {
        if (mSize > fileInfo.mSize) {
            return 1;
        } else if (mSize < fileInfo.mSize) {
            return -1;
        }
        return 0;
    }

    public String getName() {
        return mName;
    }

    public long getSizeCount() {
        return mSize;
    }

    public String getSize() {
        return ScanUtils.getConvertedSize(mSize);
    }

}
