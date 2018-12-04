package com.lody.virtual.remote;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Parcel;
import android.os.Parcelable;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.ipc.VPackageManager;
import com.lody.virtual.os.VEnvironment;

import java.io.File;

/**
 * @author Lody
 */
public final class InstalledAppInfo implements Parcelable {

    public String packageName;
    public boolean notCopyApk;
    public int appId;

    public InstalledAppInfo(String packageName, boolean notCopyApk, int appId) {
        this.packageName = packageName;
        this.notCopyApk = notCopyApk;
        this.appId = appId;
    }

    public String getApkPath()  {
        return getApkPath(VirtualCore.get().is64BitEngine());
    }

    public String getApkPath(boolean is64bit)  {
        if (notCopyApk) {
            try {
                ApplicationInfo info = VirtualCore.get().getUnHookPackageManager().getApplicationInfo(packageName, 0);
                return info.publicSourceDir;
            } catch (PackageManager.NameNotFoundException e) {
                throw new IllegalStateException(e);
            }
        }
        if (is64bit) {
            return VEnvironment.getPackageResourcePath64(packageName).getPath();
        } else {
            return VEnvironment.getPackageResourcePath(packageName).getPath();
        }
    }

    public File getOdexFile(){
        return getOdexFile(VirtualCore.get().is64BitEngine());
    }

    public File getOdexFile(boolean is64Bit) {
        if(is64Bit){
            return VEnvironment.getOdexFile64(packageName);
        }
        return VEnvironment.getOdexFile(packageName);
    }

    public ApplicationInfo getApplicationInfo(int userId) {
        return VPackageManager.get().getApplicationInfo(packageName, 0, userId);
    }

    public PackageInfo getPackageInfo(int userId) {
        return VPackageManager.get().getPackageInfo(packageName, 0, userId);
    }

    public int[] getInstalledUsers() {
        return VirtualCore.get().getPackageInstalledUsers(packageName);
    }

    public boolean isLaunched(int userId) {
        return VirtualCore.get().isPackageLaunched(userId, packageName);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.packageName);
        dest.writeByte(this.notCopyApk ? (byte) 1 : (byte) 0);
        dest.writeInt(this.appId);
    }

    protected InstalledAppInfo(Parcel in) {
        this.packageName = in.readString();
        this.notCopyApk = in.readByte() != 0;
        this.appId = in.readInt();
    }

    public static final Creator<InstalledAppInfo> CREATOR = new Creator<InstalledAppInfo>() {
        @Override
        public InstalledAppInfo createFromParcel(Parcel source) {
            return new InstalledAppInfo(source);
        }

        @Override
        public InstalledAppInfo[] newArray(int size) {
            return new InstalledAppInfo[size];
        }
    };
}
