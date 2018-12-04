package com.lody.virtual.client.stub;


import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.lody.virtual.helper.compat.ContentProviderCompat;
import com.lody.virtual.helper.compat.ProxyFCPUriCompat;

import java.io.File;
import java.io.FileNotFoundException;

import static android.content.ContentResolver.SCHEME_FILE;

public class ProxyContentProvider extends ContentProvider {

    private static final boolean DEBUG = false;

    private static final String[] COLUMNS = {
        OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE
    };

    private static String[] copyOf(String[] original, int newLength) {
        final String[] result = new String[newLength];
        System.arraycopy(original, 0, result, 0, newLength);
        return result;
    }

    private static Object[] copyOf(Object[] original, int newLength) {
        final Object[] result = new Object[newLength];
        System.arraycopy(original, 0, result, 0, newLength);
        return result;
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    private Uri unWrapperUri(String form, Uri uri) {
        if (DEBUG) {
            Log.i("UriCompat", "unWrapperUri:" + form);
        }
        return ProxyFCPUriCompat.get().unWrapperUri(uri);
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Uri a = unWrapperUri("query", uri);
        if (a == null) {
            return null;
        }
        getContext().grantUriPermission(getCallingPackage(), uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        if (SCHEME_FILE.equals(uri.getQueryParameter("__va_scheme"))) {
            final File file = new File(a.getPath());
            if (projection == null) {
                projection = COLUMNS;
            }

            String[] cols = new String[projection.length];
            Object[] values = new Object[projection.length];
            int i = 0;
            for (String col : projection) {
                if (OpenableColumns.DISPLAY_NAME.equals(col)) {
                    cols[i] = OpenableColumns.DISPLAY_NAME;
                    values[i++] = file.getName();
                } else if (OpenableColumns.SIZE.equals(col)) {
                    cols[i] = OpenableColumns.SIZE;
                    values[i++] = file.length();
                } else if (MediaStore.MediaColumns.DATA.equals(col)) {
                    cols[i] = MediaStore.MediaColumns.DATA;
                    values[i++] = file.getAbsolutePath();
                }
            }
            cols = copyOf(cols, i);
            values = copyOf(values, i);
            final MatrixCursor cursor = new MatrixCursor(cols, 1);
            cursor.addRow(values);
            return cursor;
        }


        try {
            Cursor cursor = ContentProviderCompat.acquireContentProvider(getContext(), a)
                    .query(a, projection, selection, selectionArgs, sortOrder);
            return cursor;
        } catch (Exception e) {
            return new MatrixCursor(new String[]{});
        }
    }

    @Override
    public String getType(Uri uri) {
        Uri a = unWrapperUri("getType", uri);
        if (a == null) {
            return null;
        }
        if (SCHEME_FILE.equals(uri.getQueryParameter("__va_scheme"))) {
            final File file = new File(a.getPath());
            final int lastDot = file.getName().lastIndexOf('.');
            if (lastDot >= 0) {
                final String extension = file.getName().substring(lastDot + 1);
                final String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
                if (mime != null) {
                    return mime;
                }
            }
            return "application/octet-stream";
        }

        try {
            return ContentProviderCompat.acquireContentProvider(getContext(), a)
                    .getType(a);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        Uri a = unWrapperUri("insert", uri);
        if (a == null) {
            return null;
        }
        try {
            return ContentProviderCompat.acquireContentProvider(getContext(), a)
                    .insert(a, contentValues);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        Uri a = unWrapperUri("insert", uri);
        if (a == null) {
            return 0;
        }
        try {
            return ContentProviderCompat.acquireContentProvider(getContext(), a)
                    .bulkInsert(a, values);
        } catch (Throwable e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public int delete(Uri uri, String str, String[] strArr) {
        Uri a = unWrapperUri("delete", uri);
        if (a == null) {
            return 0;
        }
        try {
            return ContentProviderCompat.acquireContentProvider(getContext(), a)
                    .delete(a, str, strArr);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String str, String[] strArr) {
        Uri a = unWrapperUri("update", uri);
        if (a == null) {
            return 0;
        }
        try {
            return ContentProviderCompat.acquireContentProvider(getContext(), a)
                    .update(a, contentValues, str, strArr);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public AssetFileDescriptor openAssetFile(Uri uri, String str) throws FileNotFoundException {
        Uri a = unWrapperUri("openAssetFile", uri);
        if (a == null) {
            return null;
        }
        if ("file".equals(a.getScheme())) {
            ParcelFileDescriptor fd = openFile(uri, str);
            return fd != null ? new AssetFileDescriptor(fd, 0, -1) : null;
        }
        try {
            return ContentProviderCompat.acquireContentProvider(getContext(), a)
                    .openAssetFile(a, str);
        } catch (Throwable e) {
            if (DEBUG) {
                Log.w("UriCompat", "openAssetFile2", e);
            }
            return null;
        }
    }

    @Override
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public AssetFileDescriptor openAssetFile(Uri uri, String str, CancellationSignal cancellationSignal) {
        Uri a = unWrapperUri("openAssetFile2", uri);
        if (a == null) {
            return null;
        }
        try {
            return ContentProviderCompat.acquireContentProvider(getContext(), a)
                    .openAssetFile(a, str, cancellationSignal);
        } catch (Throwable e) {
            if (DEBUG) {
                Log.w("UriCompat", "openAssetFile2", e);
            }
            return null;
        }
    }

    private static int modeToMode(String mode) {
        int modeBits;
        if ("r".equals(mode)) {
            modeBits = ParcelFileDescriptor.MODE_READ_ONLY;
        } else if ("w".equals(mode) || "wt".equals(mode)) {
            modeBits = ParcelFileDescriptor.MODE_WRITE_ONLY
                    | ParcelFileDescriptor.MODE_CREATE
                    | ParcelFileDescriptor.MODE_TRUNCATE;
        } else if ("wa".equals(mode)) {
            modeBits = ParcelFileDescriptor.MODE_WRITE_ONLY
                    | ParcelFileDescriptor.MODE_CREATE
                    | ParcelFileDescriptor.MODE_APPEND;
        } else if ("rw".equals(mode)) {
            modeBits = ParcelFileDescriptor.MODE_READ_WRITE
                    | ParcelFileDescriptor.MODE_CREATE;
        } else if ("rwt".equals(mode)) {
            modeBits = ParcelFileDescriptor.MODE_READ_WRITE
                    | ParcelFileDescriptor.MODE_CREATE
                    | ParcelFileDescriptor.MODE_TRUNCATE;
        } else {
            throw new IllegalArgumentException("Invalid mode: " + mode);
        }
        return modeBits;
    }

    @Override
    public ParcelFileDescriptor openFile(Uri uri, String str) throws FileNotFoundException {
        Uri a = unWrapperUri("openFile", uri);
        if (a == null) {
            return null;
        }
        if ("file".equals(a.getScheme())) {
            return ParcelFileDescriptor.open(new java.io.File(a.getPath()), modeToMode(str));
        }
        try {
            return ContentProviderCompat.acquireContentProvider(getContext(), a)
                    .openFile(a, str);
        } catch (Exception e) {
            if (DEBUG) {
                Log.w("UriCompat", "openFile", e);
            }
            return null;
        }
    }

    @Override
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public ParcelFileDescriptor openFile(Uri uri, String str, CancellationSignal cancellationSignal) {
        Uri a = unWrapperUri("openFile2", uri);
        if (a == null) {
            return null;
        }
        try {
            return ContentProviderCompat.acquireContentProvider(getContext(), a)
                    .openFile(a, str, cancellationSignal);
        } catch (Exception e) {
            if (DEBUG) {
                Log.w("UriCompat", "openFile2", e);
            }
            return null;
        }
    }

    @Override
    public Bundle call(String str, String str2, Bundle bundle) {
        return super.call(str, str2, bundle);
    }

}
