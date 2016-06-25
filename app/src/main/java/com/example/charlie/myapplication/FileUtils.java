package com.example.charlie.myapplication;

/**
 * Created by Charlie on 2016/6/7.
 */

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
//import cmc.toolkit.FileKit;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;

public class FileUtils {

    /**
     * 由外部 Activity 回傳的資料取得檔案路徑
     * @param context   Activity
     * @param uri       Uri
     * @return
     *  檔案路徑
     * @throws URISyntaxException

    public static String getPath(Context context, Uri uri) throws URISyntaxException {
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = { "_data" };
            Cursor cursor = null;

            try {
                cursor = context.getContentResolver().query(uri, projection, null, null, null);
                int column_index = cursor.getColumnIndexOrThrow("_data");
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index);
                }
            } catch (Exception e) {
                // Do nothing
            }
            finally {
                if( cursor != null ) cursor.close();
            }
        }
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

     */

    /**
     * 從 uri 取得檔案路徑 這將從 Storage Access Framework Documents 取得路徑
     * 也會使用從 _data 欄位取得 MediaStore 以及其他 file-based ContentProviders 的對應路徑
     *
     * @param context The context.
     * @param uri The Uri to query.
     * @author paulburke
     */
    public static String getPath(final Context context, final Uri uri) {

        // DocumentProvider
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if ( DocumentsContract.isDocumentUri(context, uri) ) {
                // ExternalStorageProvider
                if (isExternalStorageDocument(uri)) {
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];

                    if ("primary".equalsIgnoreCase(type)) {
                        String path = Environment.getExternalStorageDirectory().getPath();

                        if (split.length > 1) {
                            path += "/" + split[1];
                        }

                        return path;
                    }
                    else {
                        String path;
                        if (Environment.isExternalStorageRemovable()) {
                            path = System.getenv("EXTERNAL_STORAGE");
                        } else {
                            path = System.getenv("SECONDARY_STORAGE");
                            if (path == null || path.length() == 0) {
                                path = System.getenv("EXTERNAL_SDCARD_STORAGE");
                            }
                        }

                        if (split.length > 1) {
                            path += "/" + split[1];
                        }

                        return path;
                    }
                }
                // DownloadsProvider
                else if (isDownloadsDocument(uri)) {

                    final String id = DocumentsContract.getDocumentId(uri);
                    final Uri contentUri = ContentUris.withAppendedId(
                            Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                    return getDataColumn(context, contentUri, null, null);
                }
                // MediaProvider
                else if (isMediaDocument(uri)) {
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];

                    Uri contentUri = null;
                    if ("image".equals(type)) {
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                    } else if ("video".equals(type)) {
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                    } else if ("audio".equals(type)) {
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                    }

                    final String selection = "_id=?";
                    final String[] selectionArgs = new String[] {
                            split[1]
                    };

                    return getDataColumn(context, contentUri, selection, selectionArgs);
                }
            }
            // MediaStore (and general)
            else if ("content".equalsIgnoreCase(uri.getScheme())) {
                // Return the remote address
                if (isGooglePhotosUri(uri)) return uri.getLastPathSegment();

                return getDataColumn(context, uri, null, null);
            }
            // File
            else if ("file".equalsIgnoreCase(uri.getScheme())) {
                return uri.getPath();
            }
        }

        return null;
    }

    /**
     * 從 data 欄位取得 uri 對應的實體路徑
     * 主要用以取得 MediaStore Uris 以及其他 file-based ContentProviders 的對應路徑
     *
     * @param context The context.
     * @param uri The Uri to query.
     * @param selection (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     * @author paulburke
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    /**
     * @param uri The Uri to check.
     * @return 是否為 ExternalStorageProvider 類型的 uri
     * @author paulburke
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return 是否為 DownloadsProvider 類型的 uri
     * @author paulburke
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return MediaProvider 類型的 uri
     * @author paulburke
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return 是否為 Google Photos 類型的 uri
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    // 檢查檔案類型的小範例

    /**
     * 檢查取回的檔案副檔名
     * @param path  檔案路徑
     * @return
     *  若檔案類型正確則回傳檔案名稱, 反之則回傳空字串
     * @throws //EmptyFilePathException 路徑是空字串或是 null 物件
     * @throws FileNotFoundException 檔案不存在
     * @throws //IllegalTrueTypeFontException 檔案類型不正確
     */
    public static String typefaceChecker(String path) throws /*EmptyFilePathException,*/ FileNotFoundException/*, IllegalTrueTypeFontException*/ {
        //if( path == null || path.isEmpty() ){
          //  throw new EmptyFilePathException( "File path is empty" );
        //}

        File file = new File(path);

        if( !file.exists() || !file.isFile() ) {
            throw new FileNotFoundException( "File path : " + path );
        }

        String filename = file.getName();
        //String ext      = FileKit.getFileExt(filename);

        //if( !ext.equalsIgnoreCase(".ttf") ){
        //    throw new IllegalTrueTypeFontException( "File path : " + path + ", File name : " + filename + ", ext : " + ext );
        //}

        return filename;
    }

    // 一併附上取得副檔名的程式碼, FileKit 是我剛學 Java 時自己寫的小工具

    public static String getFileExt(String fileName) {
        int dotIndex = fileName.lastIndexOf(".");
        return dotIndex == -1 ? "" : fileName.substring(dotIndex);
    }
}


