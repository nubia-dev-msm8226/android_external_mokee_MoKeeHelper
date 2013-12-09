/*
 * Copyright (C) 2014 The MoKee OpenSource Project
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mokee.helper.misc;

import java.io.File;
import java.io.Serializable;

import com.mokee.helper.utils.Utils;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

public class ItemInfo implements Parcelable, Serializable {

    private static final long serialVersionUID = 1L;
    public String log;
    public String md5;
    public String name;
    public String rom;
    public String length;
    // extras
    public String description;
    public String checkflag;

    public ItemInfo(String log, String md5, String name, String rom, String length) {
        super();
        this.log = log;
        this.md5 = md5;
        this.name = name;
        this.rom = rom;
        this.rom = length;
    }

    public ItemInfo(String log, String md5, String name, String rom, String description,
            String checkflag, String length) {
        super();
        this.log = log;
        this.md5 = md5;
        this.name = name;
        this.rom = rom;
        this.length = length;
        this.description = description;
        this.checkflag = checkflag;
    }

    public ItemInfo(String name) {
        this(null, null, name, null,null);
        initializeName(name);
    }

    public String getLog() {
        return log;
    }

    public String getMd5() {
        return md5;
    }

    public String getName() {
        return name;
    }

    public String getRom() {
        return rom;
    }

    public String getLength() {
        return length;
    }

    public File getChangeLogFile(Context context) {
        return new File(context.getCacheDir(), name + ".html");
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<ItemInfo> CREATOR = new Parcelable.Creator<ItemInfo>() {
        public ItemInfo createFromParcel(Parcel in) {
            return new ItemInfo(in);
        }

        public ItemInfo[] newArray(int size) {
            return new ItemInfo[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(log);
        dest.writeString(name);
        dest.writeString(md5);
        dest.writeString(rom);
        dest.writeString(description);
        dest.writeString(checkflag);
        dest.writeString(length);
    }

    private void readFromParcel(Parcel in) {
        log = in.readString();
        name = in.readString();
        md5 = in.readString();
        rom = in.readString();
        description = in.readString();
        checkflag = in.readString();
        length = in.readString();
    }

    private void initializeName(String fileName) {
        name = fileName;
        if (!TextUtils.isEmpty(fileName)) {
            name = extractUiName(fileName);
        } else {
            name = null;
        }
    }

    public static String extractUiName(String fileName) {
        String deviceType = Utils.getDeviceType();
        String uiName = fileName.replaceAll("\\.zip$", "");
        return uiName.replaceAll("-" + deviceType + "-?", "");
    }

    private ItemInfo(Parcel in) {
        readFromParcel(in);
    }

    public String getDescription() {
        return description;
    }

    public String getCheckflag() {
        return checkflag;
    }

}
