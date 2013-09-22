
package com.mokee.helper.misc;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import java.io.File;
import java.io.Serializable;

public class ExtraInfo implements Parcelable, Serializable {

    private static final long serialVersionUID = -930325533649941575L;

    private String mUiName;
    private String mFileName;
    private String mDownloadUrl;
    private String mMD5Sum;
    private int mVersion;

    public ExtraInfo(String fileName, String url, String md5, int version) {
        initializeName(fileName);
        mDownloadUrl = url;
        mMD5Sum = md5;
        mVersion = version;
    }

    public ExtraInfo(String fileName) {
        this(fileName, null, null, 0);
    }

    private ExtraInfo(Parcel in) {
        readFromParcel(in);
    }

    private void initializeName(String fileName) {
        mFileName = fileName;
        if (!TextUtils.isEmpty(fileName)) {
            mUiName = extractUiName(fileName);
        } else {
            mUiName = null;
        }
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(mUiName);
        out.writeString(mFileName);
        out.writeString(mDownloadUrl);
        out.writeString(mMD5Sum);
        out.writeInt(mVersion);
    }

    private void readFromParcel(Parcel in) {
        mUiName = in.readString();
        mFileName = in.readString();
        mDownloadUrl = in.readString();
        mMD5Sum = in.readString();
        mVersion = in.readInt();
    }

    public static String extractUiName(String fileName) {
        String uiName = fileName.replaceAll("\\.zip$", "");
        return uiName;
    }

    public static final Parcelable.Creator<ExtraInfo> CREATOR = new Parcelable.Creator<ExtraInfo>() {
        public ExtraInfo createFromParcel(Parcel in) {
            return new ExtraInfo(in);
        }

        public ExtraInfo[] newArray(int size) {
            return new ExtraInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    public String getmUiName() {
        return mUiName;
    }

    public String getmFileName() {
        return mFileName;
    }

    public String getmDownloadUrl() {
        return mDownloadUrl;
    }

    public String getmMD5Sum() {
        return mMD5Sum;
    }

    public int getmVersion() {
        return mVersion;
    }
    
    public File getChangeLogFile(Context context) {
        return new File(context.getCacheDir(), mFileName + ".changelog");
    }
}
