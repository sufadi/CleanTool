package cleantool.su.starcleanmaster.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cleantool.su.starcleanmaster.providers.CleanMarsterData;
import cleantool.su.starcleanmaster.util.ConstantUtil;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class CleanMarsterDao {

    private static final String TAG = CleanMarsterDao.class.getSimpleName();

    public static final Uri URI_LAVA_UNINSTALL = Uri.parse("content://" + CleanMarsterData.AUTHORITY + "/" + CleanMarsterData.UserTableData.PATH_UNINSTALL);
    public static final Uri URI_LAVA_CACHE = Uri.parse("content://" + CleanMarsterData.AUTHORITY + "/" + CleanMarsterData.UserTableData.PATH_CACHE);

    public static void insertUninstallDb(Context context, String packageName) {
        ContentValues mContentValues = new ContentValues();
        mContentValues.put(CleanMarsterData.KEY_PACKNAME, packageName);
        context.getContentResolver().insert(URI_LAVA_UNINSTALL, mContentValues);
        if (ConstantUtil.DEBUG)
            Log.i(TAG, "shz insertUninstallDb packageName = " + packageName);
    }

    public static void deleteUninstallDb(Context context, String packageName) {
        context.getContentResolver().delete(URI_LAVA_UNINSTALL, CleanMarsterData.KEY_PACKNAME + "=?", new String[]{packageName});
        if (ConstantUtil.DEBUG)
            Log.i(TAG, "shz deleteUninstallDb packageName = " + packageName);
    }

    public static void deleteAllUninstallDb(Context context) {
        context.getContentResolver().delete(URI_LAVA_UNINSTALL, null, null);
        if (ConstantUtil.DEBUG)
            Log.i(TAG, "shz deleteAllUninstallDb");
    }

    public static boolean isExistUninstallDb(Context context, String packageName) {
        boolean result = false;
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(URI_LAVA_UNINSTALL, new String[]{CleanMarsterData.KEY_PACKNAME}, CleanMarsterData.KEY_PACKNAME + "=?", new String[]{packageName}, null);
            if (cursor != null) {
                result = cursor.moveToNext();
                if (ConstantUtil.DEBUG)
                    Log.i(TAG, "shz isExistUninstallDb packageName = " + packageName + ", result = " + result);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return result;
    }

    public static List<String> getUninstallApps(Context context) {
        List<String> apps = new ArrayList<String>();
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(URI_LAVA_UNINSTALL, new String[]{CleanMarsterData.KEY_PACKNAME}, null, null, null);

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    String packageName = cursor.getString(cursor.getColumnIndex(CleanMarsterData.KEY_PACKNAME));
                    apps.add(packageName);
                    if (ConstantUtil.DEBUG)
                        Log.i(TAG, "shz getUninstallApps packageName = " + packageName);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return apps;
    }

    public static void insertCacheSize(Context context, String packageName, Long size) {
        ContentValues mContentValues = new ContentValues();
        mContentValues.put(CleanMarsterData.KEY_PACKNAME, packageName);
        mContentValues.put(CleanMarsterData.KEY_CACHE_SIZE, size);
        mContentValues.put(CleanMarsterData.KEY_CACHE_LAST_TIME, System.currentTimeMillis());
        context.getContentResolver().insert(URI_LAVA_CACHE, mContentValues);
        if (ConstantUtil.DEBUG)
            Log.i(TAG, "shz insertCacheDb packageName = " + packageName);
    }

    public static void insertMinCacheSize(Context context, String packageName, Long size) {
        ContentValues mContentValues = new ContentValues();
        mContentValues.put(CleanMarsterData.KEY_PACKNAME, packageName);
        mContentValues.put(CleanMarsterData.KEY_MIN_CACHE_SIZE, size);
        mContentValues.put(CleanMarsterData.KEY_CACHE_LAST_TIME, System.currentTimeMillis());
        context.getContentResolver().insert(URI_LAVA_CACHE, mContentValues);
        if (ConstantUtil.DEBUG)
            Log.i(TAG, "shz insertCacheDb packageName = " + packageName);
    }

    public static void deleteCacheDb(Context context, String packageName) {
        context.getContentResolver().delete(URI_LAVA_CACHE, CleanMarsterData.KEY_PACKNAME + "=?", new String[]{packageName});
        if (ConstantUtil.DEBUG)
            Log.i(TAG, "shz deleteCacheDb packageName = " + packageName);
    }

    public static void deleteAllCacheDb(Context context) {
        context.getContentResolver().delete(URI_LAVA_CACHE, null, null);
        if (ConstantUtil.DEBUG)
            Log.i(TAG, "shz deleteAllCacheDb");
    }

    public static void updateCacheSize(Context context, String packageName, Long size) {
        if (isHaveCacheInfo(context, packageName)) {
            ContentValues mContentValues = new ContentValues();
            mContentValues.put(CleanMarsterData.KEY_PACKNAME, packageName);
            mContentValues.put(CleanMarsterData.KEY_CACHE_SIZE, size);
            mContentValues.put(CleanMarsterData.KEY_CACHE_LAST_TIME, System.currentTimeMillis());
            context.getContentResolver().update(URI_LAVA_CACHE, mContentValues, CleanMarsterData.KEY_PACKNAME + "=?", new String[]{packageName});
            if (ConstantUtil.DEBUG) {
                Log.i(TAG, "shz updateCacheSize packageName " + packageName + ", size = " + size);
            }
        } else {
            insertCacheSize(context, packageName, size);
        }
    }

    public static void updateMinCacheSize(Context context, String packageName, Long size) {
        if (isHaveCacheInfo(context, packageName)) {
            ContentValues mContentValues = new ContentValues();
            mContentValues.put(CleanMarsterData.KEY_PACKNAME, packageName);
            mContentValues.put(CleanMarsterData.KEY_CACHE_SIZE, size);
            mContentValues.put(CleanMarsterData.KEY_MIN_CACHE_SIZE, size);
            mContentValues.put(CleanMarsterData.KEY_CACHE_LAST_TIME, System.currentTimeMillis());
            context.getContentResolver().update(URI_LAVA_CACHE, mContentValues, CleanMarsterData.KEY_PACKNAME + "=?", new String[]{packageName});
            if (ConstantUtil.DEBUG) {
                Log.i(TAG, "shz updateMinCacheSize packageName " + packageName + ", size = " + size);
            }
        } else {
            insertMinCacheSize(context, packageName, size);
        }
    }

    public static boolean isHaveCacheInfo(Context context, String packageName) {
        boolean result = false;
        Cursor cursor = null;

        try {
            cursor = context.getContentResolver().query(URI_LAVA_CACHE, new String[]{CleanMarsterData.KEY_PACKNAME}, CleanMarsterData.KEY_PACKNAME + "=?", new String[]{packageName}, null);
            if (cursor != null && cursor.moveToNext()) {
                result = true;
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        if (ConstantUtil.DEBUG) {
            Log.d("TAG", "isHaveCacheInfo = " + result);
        }
        return result;
    }

    public static HashMap<String, Long> getMinCacheSize(Context context) {
        HashMap<String, Long> apps = new HashMap<String, Long>();
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(URI_LAVA_CACHE, new String[]{CleanMarsterData.KEY_PACKNAME, CleanMarsterData.KEY_MIN_CACHE_SIZE}, null, null, null);

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    String packageName = cursor.getString(cursor.getColumnIndex(CleanMarsterData.KEY_PACKNAME));
                    long cacheSize = cursor.getLong(cursor.getColumnIndex(CleanMarsterData.KEY_MIN_CACHE_SIZE));
                    apps.put(packageName, (long) cacheSize);
                    if (ConstantUtil.DEBUG)
                        Log.i(TAG, "shz getCacheApps packageName = " + packageName + ", mineCache = " + cacheSize);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return apps;
    }

    public static HashMap<String, Long> getCacheSize(Context context) {
        HashMap<String, Long> apps = new HashMap<String, Long>();
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(URI_LAVA_CACHE, new String[]{CleanMarsterData.KEY_PACKNAME, CleanMarsterData.KEY_CACHE_SIZE}, null, null, null);

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    String packageName = cursor.getString(cursor.getColumnIndex(CleanMarsterData.KEY_PACKNAME));
                    long cacheSize = cursor.getLong(cursor.getColumnIndex(CleanMarsterData.KEY_CACHE_SIZE));
                    apps.put(packageName, (long) cacheSize);
                    if (ConstantUtil.DEBUG)
                        Log.i(TAG, "shz getCacheApps packageName = " + packageName + ", mineCache = " + cacheSize);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return apps;
    }

    public static long getCacheLastTime(Context context, String packageName) {
        long result = 0l;
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(URI_LAVA_CACHE, new String[]{CleanMarsterData.KEY_PACKNAME, CleanMarsterData.KEY_CACHE_LAST_TIME}, CleanMarsterData.KEY_PACKNAME + "=?", new String[]{packageName}, null);

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    result = cursor.getLong(cursor.getColumnIndex(CleanMarsterData.KEY_CACHE_LAST_TIME));
                    if (ConstantUtil.DEBUG)
                        Log.i(TAG, "shz getCacheLastTime packageName = " + packageName + ", lastTime = " + result);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return result;
    }


    public static long getCacheSize(Context context, String packageName) {
        long result = 0l;
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(URI_LAVA_CACHE, new String[]{CleanMarsterData.KEY_PACKNAME, CleanMarsterData.KEY_CACHE_SIZE}, CleanMarsterData.KEY_PACKNAME + "=?", new String[]{packageName}, null);

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    result = cursor.getLong(cursor.getColumnIndex(CleanMarsterData.KEY_CACHE_SIZE));
                    if (ConstantUtil.DEBUG)
                        Log.i(TAG, "shz getCacheSize packageName = " + packageName + ", cacheSize = " + result);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return result;
    }
}
