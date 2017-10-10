package cleantool.su.starcleanmaster.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteFullException;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio;
import android.provider.MediaStore.Files;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Images.Thumbnails;
import android.provider.MediaStore.Video;
import android.provider.MediaStore.Files.FileColumns;
import android.text.TextUtils;
import android.util.Log;

public class FileUtil {

    private static final String TAG = FileUtil.class.getSimpleName();

    // /data/data/com.android.providers.media/databases/external.db
    public static final String VOLUME_NAME = "external";

    public static final int MIN_BIGFILE_SIZE = 10 * 1024 * 1024;// 10 MB
    public static final int MIN_MUSIC_SIZE = 5 * 1024 * 1024;// 5 MB
    public static final int MIN_VIDEO_SIZE = 10 * 1024 * 1024;// 10 MB

    public static final int SORT_BY_TYPE = 0;
    public static final int SORT_BY_NAME = 1;
    public static final int SORT_BY_SIZE = 2;
    public static final int SORT_BY_TIME = 3;
    public static final int SORT_BY_NONE = 4;

    public static final int TYPE_SYSTEM = 0;
    public static final int TYPE_CACHE = 1;
    public static final int TYPE_APK = 2;
    public static final int TYPE_RESIDUAL = 3;
    public static final int TYPE_MEMORY = 4;
    public static final int TYPE_BIG_FILE = 5;
    public static final int TYPE_WEB = 6;
    public static final int TYPE_LOG = 7;
    public static final int TYPE_VIDEO = 8;
    public static final int TYPE_PICTURE = 9;
    public static final int TYPE_MUSIC = 10;

    private final static String[] ZIPS = new String[]{"zip", "rar"};

    private final static String[] DOCS = new String[]{"txt", "pdf", "rtf", "vsdx", "vsd", "mpp", "vcf", "doc", "docx", "dotx", "docm", "dotm", "dot", "xps", "xlsx", "xlsm", "xls", "xml", "xltx",
            "xltm", "xlt", "pptx", "pptm", "ppt", "potm", "pot", "ppsx", "ppsm", "pps", "potx"};

    private final static String[] OTHERS = new String[]{"cache", "chunk"};

    public static Cursor query(Context mContext, int type, int sort) {
        Uri uri = getContentUri(type);

        if (uri == null) {
            Log.e(TAG, "shz fail uri:" + uri);
            return null;
        }

        String selection = getSelection(type);
        String sortOrder = getSortOrder(sort);
        String[] columns = getColumns(type);

        return mContext.getContentResolver().query(uri, columns, selection, null, sortOrder);
    }

    public static Cursor query(Context mContext, int type, String key, int sort) {
        Uri uri = getContentUri(type);

        if (uri == null) {
            Log.e(TAG, "shz fail uri:" + uri);
            return null;
        }

        String selection = getSelection(type, key);
        String sortOrder = getSortOrder(sort);
        String[] columns = getColumns(type);

        return mContext.getContentResolver().query(uri, columns, selection, null, sortOrder);
    }

    public static String[] getColumns(int type) {
        switch (type) {
            case TYPE_SYSTEM:
                return new String[]{FileColumns.DATA};
            default:
                return new String[]{FileColumns._ID, FileColumns.DATA, FileColumns.SIZE, FileColumns.DATE_MODIFIED};
        }
    }

    public static Uri getContentUri(int type) {
        Uri uri;
        switch (type) {
            case TYPE_APK:
            case TYPE_WEB:
            case TYPE_CACHE:
            case TYPE_RESIDUAL:
            case TYPE_LOG:
            case TYPE_BIG_FILE:
                uri = Files.getContentUri(VOLUME_NAME);
                break;
            case TYPE_VIDEO:
                uri = Video.Media.getContentUri(VOLUME_NAME);
                break;
            case TYPE_SYSTEM:
                uri = Thumbnails.getContentUri(VOLUME_NAME);
                break;
            case TYPE_PICTURE:
                uri = Images.Media.getContentUri(VOLUME_NAME);
                break;
            case TYPE_MUSIC:
                uri = Audio.Media.getContentUri(VOLUME_NAME);
                break;
            default:
                uri = null;
                break;
        }
        return uri;
    }

    public static String getSelection(int type) {
        String selection = null;
        switch (type) {
            case TYPE_APK:
                selection = FileColumns.DATA + " LIKE '%.apk'";
                break;
            case TYPE_LOG:
                StringBuilder logSb = new StringBuilder();
                logSb.append(FileColumns.DATA + " like ");
                DatabaseUtils.appendEscapedSQLString(logSb, "%" + ".log" + "%");
                logSb.append(" or ").append(FileColumns.DATA + " like ");
                DatabaseUtils.appendEscapedSQLString(logSb, "%" + "log.txt" + "%");
                selection = logSb.toString();
                break;
            case TYPE_WEB:
                StringBuilder webSb = new StringBuilder();
                webSb.append(FileColumns.DATA + " like ");
                DatabaseUtils.appendEscapedSQLString(webSb, "%" + "/database/webview.db" + "%");
                webSb.append(" or ").append(FileColumns.DATA + " like ");
                DatabaseUtils.appendEscapedSQLString(webSb, "%" + "/database/webviewCache.db" + "%");
                selection = webSb.toString();
                break;
            case TYPE_BIG_FILE:
                ArrayList<String> bigFileList = new ArrayList<String>();
                for (String value : ZIPS) {
                    bigFileList.add(value);
                }

                for (String value : DOCS) {
                    bigFileList.add(value);
                }

                for (String value : OTHERS) {
                    bigFileList.add(value);
                }

                selection = getSelectionBuild(bigFileList, MIN_BIGFILE_SIZE);
                break;
            case TYPE_VIDEO:
                selection = FileColumns.SIZE + " >= " + String.valueOf(MIN_VIDEO_SIZE);
                break;
            case TYPE_MUSIC:
                selection = FileColumns.DATA + " not like '%legacy%'" + " and " + FileColumns.SIZE + " >= " + String.valueOf(MIN_MUSIC_SIZE);
                break;
            default:

                break;
        }
        Log.d(TAG, "shz getSelection = " + selection);
        return selection;
    }

    public static String getSelection(int type, String key) {
        String selection = null;
        switch (type) {
            case TYPE_CACHE:
                /**
                 * /data/data/package_name/cache
                 * /mnt/sdcard/Android/package_name/cache
                 */
                StringBuilder sb = new StringBuilder();
                sb.append(FileColumns.DATA + " like ");
                DatabaseUtils.appendEscapedSQLString(sb, "%" + key + "/cache" + "%");
                selection = sb.toString();
                break;
            case TYPE_WEB:
                /**
                 * /data/data/package_name/database/webview.db
                 * /data/data/package_name/database/webviewCache.db
                 */
                StringBuilder webSb = new StringBuilder();
                webSb.append(FileColumns.DATA + " like ");
                DatabaseUtils.appendEscapedSQLString(webSb, "%" + key + "/database/webview.db" + "%");
                webSb.append(" or ").append(FileColumns.DATA + " like ");
                DatabaseUtils.appendEscapedSQLString(webSb, "%" + key + "/database/webviewCache.db" + "%");
                selection = webSb.toString();
                break;
            case TYPE_RESIDUAL:
                StringBuilder uninstallSb = new StringBuilder();
                uninstallSb.append(FileColumns.DATA + " like ");
                DatabaseUtils.appendEscapedSQLString(uninstallSb, "%" + key + "%");
                selection = uninstallSb.toString();
                break;

            default:
                break;
        }
        Log.d(TAG, "shz getSelection = " + selection);
        return selection;
    }

    public static String getSortOrder(int sort) {
        String sortOrder = null;
        switch (sort) {
            case SORT_BY_TYPE:
                sortOrder = FileColumns.MIME_TYPE + " asc, " + FileColumns.TITLE + " asc";
                break;
            case SORT_BY_NAME:
                sortOrder = FileColumns.TITLE + " asc";
                break;
            case SORT_BY_SIZE:
                sortOrder = FileColumns.SIZE + " desc";
                break;
            case SORT_BY_TIME:
                sortOrder = FileColumns.DATE_MODIFIED + " desc";
                break;
            default:
                break;
        }
        return sortOrder;
    }

    public static long getFileSize(File f) {
        long size = 0;
        File list[] = f.listFiles();
        if (list == null) {
            return f.length();
        }
        for (int i = 0; i < list.length; i++) {
            if (list[i].isDirectory()) {
                size = size + getFileSize(list[i]);
            } else {
                size = size + list[i].length();
            }
        }
        return size;
    }

    public static boolean removeFile(String strFilePath) {
        boolean result = false;
        if (strFilePath == null || "".equals(strFilePath)) {
            return result;
        }
        File file = new File(strFilePath);
        if (file.isFile() && file.exists()) {
            result = file.delete();
            if (result == Boolean.TRUE) {
                Log.d(TAG, "[REMOE_FILE:" + strFilePath + "del success!]");
            } else {
                Log.d(TAG, "[REMOE_FILE:" + strFilePath + "del fail!]");
            }
        }
        return result;
    }

    public static boolean removeFolder(String strFolderPath) {
        boolean bFlag = false;
        try {
            if (strFolderPath == null || "".equals(strFolderPath)) {
                return bFlag;
            }
            File file = new File(strFolderPath.toString());
            bFlag = file.delete();
            if (bFlag == Boolean.TRUE) {
                Log.d(TAG, "[REMOE_FOLDER:" + file.getPath() + "del success!!]");
            } else {
                Log.d(TAG, "[REMOE_FOLDER:" + file.getPath() + "del fail!!]");
            }
        } catch (Exception e) {
            Log.d(TAG, "[FLOADER_PATH:" + strFolderPath + "del fail!!]");
            e.printStackTrace();
        }
        return bFlag;
    }

    public static void removeAllFileAndFolder(String strPath) {
        File file = new File(strPath);
        if (!file.exists()) {
            return;
        }
        if (!file.isDirectory()) {
            boolean result = file.delete();
            Log.d(TAG, "shz file del" + strPath + ", del result = " + result);
            return;
        }
        String[] fileList = file.list();
        File tempFile = null;
        for (int i = 0; i < fileList.length; i++) {
            if (strPath.endsWith(File.separator)) {
                tempFile = new File(strPath + fileList[i]);
            } else {
                tempFile = new File(strPath + File.separator + fileList[i]);
            }
            if (tempFile.isFile()) {
                tempFile.delete();
            }
            if (tempFile.isDirectory()) {
                removeAllFileAndFolder(strPath + "/" + fileList[i]);
                removeFolder(strPath + "/" + fileList[i]);
            }
        }
        if (file.exists()) {// still exists
            boolean result = file.delete();
            Log.d(TAG, "shz del again:" + strPath + ", del result = " + result);
        }
    }

    /**
     * delete the record in MediaStore
     *
     * @param paths the delete file or folder in MediaStore
     *              <p>
     *              copy for package
     *              com.mediatek.filemanager.service.MediaStoreHelper
     */
    public static void deleteFileInMediaStore(Context mContext, List<String> paths) {
        Log.d(TAG, "deleteFileInMediaStore.");
        Uri uri = MediaStore.Files.getContentUri(VOLUME_NAME);
        StringBuilder whereClause = new StringBuilder();
        whereClause.append("?");
        for (int i = 0; i < paths.size() - 1; i++) {
            whereClause.append(",?");
        }
        String where = MediaStore.Files.FileColumns.DATA + " IN(" + whereClause.toString() + ")";
        // notice that there is a blank before "IN(".
        if (mContext != null && !paths.isEmpty()) {
            ContentResolver cr = mContext.getContentResolver();
            String[] whereArgs = new String[paths.size()];
            paths.toArray(whereArgs);
            Log.d(TAG, "deleteFileInMediaStore,delete.");
            try {
                cr.delete(uri, where, whereArgs);
            } catch (SQLiteFullException e) {
                Log.e(TAG, "Error, database or disk is full!!!" + e);
            } catch (UnsupportedOperationException e) {
                Log.e(TAG, "Error, database is closed!!!");
            }
        }
    }

    /**
     * delete the record in MediaStore
     *
     * @param path the delete file or folder in MediaStore
     *             <p>
     *             copy for package
     *             com.mediatek.filemanager.service.MediaStoreHelper
     */
    public static void deleteFileInMediaStore(Context mContext, String path) {
        Log.d(TAG, "deleteFileInMediaStore,path =" + path);
        if (TextUtils.isEmpty(path)) {
            return;
        }
        Uri uri = MediaStore.Files.getContentUri("external");
        String where = MediaStore.Files.FileColumns.DATA + "=?";
        String[] whereArgs = new String[]{path};
        if (mContext != null) {
            ContentResolver cr = mContext.getContentResolver();
            Log.d(TAG, "deleteFileInMediaStore,delete.");
            try {
                try {
                    cr.delete(uri, where, whereArgs);
                } catch (UnsupportedOperationException e) {
                    Log.e(TAG, "Error, database is closed!!!");
                }
            } catch (SQLiteFullException e) {
                Log.e(TAG, "Error, database or disk is full!!!" + e);
            }
        }
    }

    public static void deleteFileInMediaStore(Context mContext, String path, int type) {
        Log.d(TAG, "deleteFileInMediaStore,path =" + path);
        if (TextUtils.isEmpty(path)) {
            return;
        }
        Uri uri = getContentUri(type);
        String where = MediaStore.Files.FileColumns.DATA + "=?";
        String[] whereArgs = new String[]{path};
        if (mContext != null) {
            ContentResolver cr = mContext.getContentResolver();
            try {
                try {
                    cr.delete(uri, where, whereArgs);
                    Log.d(TAG, "deleteFileInMediaStore, delete.");
                } catch (UnsupportedOperationException e) {
                    Log.e(TAG, "Error, database is closed!!!");
                }
            } catch (SQLiteFullException e) {
                Log.e(TAG, "Error, database or disk is full!!!" + e);
            }
        }
    }

    public static String getSelectionBuild(ArrayList<String> list) {
        StringBuilder selection = new StringBuilder();

        for (String value : list) {
            selection.append("(" + FileColumns.DATA + " like '" + "%." + value + "') or ");
        }
        return selection.substring(0, selection.lastIndexOf(")") + 1);
    }

    public static String getSelectionBuild(ArrayList<String> list, int limit) {
        StringBuilder selection = new StringBuilder();

        for (String value : list) {
            selection.append("((" + FileColumns.SIZE + " >= " + String.valueOf(MIN_BIGFILE_SIZE) + ") and (" + FileColumns.DATA + " like '" + "%." + value + "')) or ");
        }
        return selection.substring(0, selection.lastIndexOf(")") + 1);
    }
}
