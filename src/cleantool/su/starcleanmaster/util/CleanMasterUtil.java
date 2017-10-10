package cleantool.su.starcleanmaster.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.session.MediaController;
import android.media.session.MediaSessionManager;
import android.media.session.PlaybackState;
import android.os.UserHandle;
import android.provider.MediaStore.Files.FileColumns;
import android.util.Log;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;

import com.su.starcleanmaster.R;

public class CleanMasterUtil {

    private static final String TAG = CleanMasterUtil.class.getSimpleName();

    public static void getScanCacheFromDb(Context mContext) {
        Log.d(TAG, "shz CleanMasterUtil getScanCacheFromDb start->");
        PackageManager mPm = mContext.getPackageManager();
        List<ApplicationInfo> mApplicationInfos = mPm.getInstalledApplications(PackageManager.GET_META_DATA);

        for (ApplicationInfo applicationInfo : mApplicationInfos) {
            String mPackageName = applicationInfo.packageName;

            Cursor mCursor = FileUtil.query(mContext, FileUtil.TYPE_CACHE, mPackageName, FileUtil.SORT_BY_SIZE);
            if (mCursor != null) {
                if (mCursor.moveToFirst()) {
                    do {
                        String filePath = mCursor.getString(mCursor.getColumnIndex(FileColumns.DATA));
                        Log.d(TAG, "shz CleanMasterUtil getScanCacheFromDb filePath = " + filePath);

                    } while (mCursor.moveToNext());
                }
                mCursor.close();
            }
        }
        Log.d(TAG, "shz CleanMasterUtil getScanCacheFromDb end->");
    }

    public static Map<String, String> getInputMethodAppList(Context context) {
        Map<String, String> list = new HashMap<String, String>();
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        List<InputMethodInfo> methodList = imm.getInputMethodList();
        if (methodList != null) {
            for (InputMethodInfo mi : methodList) {
                String inputMethodPackageName = mi.getPackageName();
                list.put(inputMethodPackageName, inputMethodPackageName);
            }
        }
        Log.d("TAG", "shz getInputMethodAppList = " + list.toString());
        return list;
    }

    /**
     * Get the current state of playback. One of the following:
     * <ul>
     * <li> {@link PlaybackState#STATE_NONE}</li>0
     * <li> {@link PlaybackState#STATE_STOPPED}</li>1
     * <li> {@link PlaybackState#STATE_PAUSED}</li>2
     * <li> {@link PlaybackState#STATE_PLAYING}</li>3
     * <li> {@link PlaybackState#STATE_FAST_FORWARDING}</li>4
     * <li> {@link PlaybackState#STATE_REWINDING}</li>5
     * <li> {@link PlaybackState#STATE_BUFFERING}</li>6
     * <li> {@link PlaybackState#STATE_ERROR}</li>7
     * </ul>
     * <p>
     * Need permission <uses-permission
     * android:name="android.permission.MEDIA_CONTENT_CONTROL" />
     * <uses-permission android:name="android.permission.INTERACT_ACROSS_USERS"
     * /> <uses-permission
     * android:name="android.permission.INTERACT_ACROSS_USERS_FULL" />
     */
    public static HashMap<String, Integer> findMediaProcess(Context mContext) {
        HashMap<String, Integer> result = new HashMap<String, Integer>();

        // TODO: use MediaSessionManager.SessionListener to hook us up to future
        // updates
        // in session state
        MediaSessionManager mMediaSessionManager = (MediaSessionManager) mContext.getSystemService(Context.MEDIA_SESSION_SERVICE);

        if (mMediaSessionManager != null) {

            final List<MediaController> sessions = mMediaSessionManager.getActiveSessionsForUser(null, UserHandle.USER_ALL);

            for (MediaController aController : sessions) {
                if (aController == null)
                    continue;
                // now to see if we have one like this
                final String pkg = aController.getPackageName();
                final PlaybackState state = aController.getPlaybackState();
                int stateInt = state == null ? -11 : state.getState();
                result.put(pkg, stateInt);
                Log.d(TAG, "findMusicProcess music app = " + pkg + ", status = " + stateInt);
                /*
                 * final PlaybackState state = aController.getPlaybackState();
                 * if (state == null) continue; switch (state.getState()) { case
                 * PlaybackState.STATE_STOPPED: case PlaybackState.STATE_ERROR:
                 * continue; default: // now to see if we have one like this
                 * final String pkg = aController.getPackageName();
                 * result.put(pkg, state.getState()); Log.d(TAG,
                 * "findMusicProcess music app = " + pkg + ", status = " +
                 * state.getState()); }
                 */
            }
        }

        return result;
    }

    public static ArrayList<String> getMemoryWhiteList(Context context) {
        ArrayList<String> result = new ArrayList<String>();

        String[] list = context.getResources().getStringArray(R.array.memory_white_list);

        if (list != null && list.length > 0) {
            for (String value : list) {
                result.add(value);
            }
        }

        return result;
    }

    public static ArrayList<String> getRecentUsageStatsList(Context mContext) {
        ArrayList<String> result = new ArrayList<String>();

        final long USAGE_STATS_PERIOD = 1000 * 60 * 60 * 24 * 1;// one day
        long now = System.currentTimeMillis();
        long beginTime = now - USAGE_STATS_PERIOD;

        UsageStatsManager mUsageStatsManager = (UsageStatsManager) mContext.getSystemService(Context.USAGE_STATS_SERVICE);
        Map<String, UsageStats> mapList = mUsageStatsManager.queryAndAggregateUsageStats(beginTime, now);

        if (mapList != null && mapList.size() > 0) {
            for (Map.Entry<String, UsageStats> entry : mapList.entrySet()) {
                result.add(entry.getValue().getPackageName());
            }
        }

        return result;
    }
}
