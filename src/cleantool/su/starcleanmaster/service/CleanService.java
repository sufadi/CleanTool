package cleantool.su.starcleanmaster.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.session.PlaybackState;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import cleantool.su.starcleanmaster.db.CleanMarsterDao;
import cleantool.su.starcleanmaster.model.ChildItem;
import cleantool.su.starcleanmaster.util.AlarmTaskUtil;
import cleantool.su.starcleanmaster.util.AppManagerUtil;
import cleantool.su.starcleanmaster.util.CleanMasterUtil;
import cleantool.su.starcleanmaster.util.CommonUtil;
import cleantool.su.starcleanmaster.util.ConstantUtil;
import cleantool.su.starcleanmaster.util.GHDao;
import cleantool.su.starcleanmaster.util.FileUtil;
import cleantool.su.starcleanmaster.util.LavaThumbnailUtil;
import cleantool.su.starcleanmaster.util.MemoryStatusUtil;
import cleantool.su.starcleanmaster.util.SystemStatusUtil;
import cleantool.su.starcleanmaster.warnings.CleanMasterWarningUI;
import cleantool.su.starcleanmaster.warnings.CleanMasterNotificationWarnings;

// add start ScanCache
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageStats;
import android.graphics.drawable.Drawable;
import android.os.RemoteException;
import android.provider.MediaStore.Files.FileColumns;

import java.io.File;
import java.util.concurrent.CountDownLatch;
// add end ScanCache

//add start CleanCache
import android.content.pm.IPackageDataObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
//add stop CleanCache

// memory
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
// memory

import com.su.starcleanmaster.R;

public class CleanService extends Service implements CleanMasterWarningUI.IScreenStatueCallBack {

    private static final String TAG = CleanService.class.getSimpleName();

    private long cleanSize;
    private boolean isScanning;
    private boolean isAutoScanning;

    private Context mContext;
    private ScanTask mScanTask;
    private CleanTask mCleanTask;
    private CleanMasterWarningUI mCleanMasterWarningUI;
    private CleanMasterNotificationWarnings mWarnings;

    private IAutoCleanStatueCallBack mAutoCleanStatueCallBack;

    public interface IAutoCleanStatueCallBack {
        void AutoScanClean(long size);
    }

    private void setIAutoCleanStatueCallBack(IAutoCleanStatueCallBack mAutoCleanStatueCallBack) {
        this.mAutoCleanStatueCallBack = mAutoCleanStatueCallBack;
    }

    // add start Binder
    private CleanerServiceBinder mBinder = new CleanerServiceBinder();

    public class CleanerServiceBinder extends Binder {

        public CleanService getService() {
            return CleanService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    // add end Binder

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;

        mWarnings = new CleanMasterNotificationWarnings(mContext);
        mCleanMasterWarningUI = CleanMasterWarningUI.getInstance(mContext);
        mCleanMasterWarningUI.setIScreenStatueCallBack(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            return super.onStartCommand(intent, flags, startId);
        }

        String action = intent.getAction();
        if (ConstantUtil.ACTION_AUTO_CLEAN_TRIGGER.equals(action)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                startAutoCleanAlarmTask();
            }

            boolean isScreenOn = SystemStatusUtil.isScreenOn(mContext);
            if (isScreenOn == false) {
                List<ChildItem> cacheList = getCacheList();
                long cachesize = getCacheSize(cacheList);
                if (isMemoryAvailableLow()) {
                    if (cachesize >= ConstantUtil.AUTO_CLEAN_LIMIT_VALUE_0) {
                        Log.d(TAG, "shz auto clean");
                        AutoClean(cacheList);
                    }
                } else {
                    if (cachesize >= ConstantUtil.AUTO_CLEAN_LIMIT_VALUE_1) {
                        Log.d(TAG, "shz auto clean");
                        AutoClean(cacheList);
                    }
                }
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mScanTask = null;
    }

    // add start command
    public void startScan() {
        Log.d(TAG, "shz startScan");
        isScanning = true;

        mScanTask = null;
        mScanTask = new ScanTask();
        mScanTask.execute();
    }

    public void stopScan() {
        Log.d(TAG, "shz stopScan");
        isScanning = false;

        if (mScanTask != null && mScanTask.getStatus() == AsyncTask.Status.RUNNING) {
            mScanTask.cancel(true);
        }
    }

    public void startClean(HashMap<Integer, List<ChildItem>> mCleanMasterdata) {
        Log.d(TAG, "shz startClean");
        isScanning = false;

        mCleanTask = null;
        mCleanTask = new CleanTask();
        mCleanTask.execute(mCleanMasterdata);
    }

    public boolean isCacheCleanning() {
        return false;
    }

    // add end command

    // start add ScanTask
    public static interface OnScanListener {

        public void onScanStart();

        public void onScanCancel();

        public void onScanScanning();

        public void onScanSize(long cacheSize);

        public void onScanComplete(HashMap<Integer, List<ChildItem>> mChildItems);

        public void onScanCacheComplete(long size);

        public void onScanFileApkComplete(long size);

        public void onScanBigFileComplete(long size);

        public void onScanVideoComplete(long size);

        public void onScanPictureComplete(long size);

        public void onScanMusicComplete(long size);

        public void onScanSystemComplete(long size);

        public void onScanUninstallComplete(long size);

        public void onScanMemoryComplete(long size);
    }

    private OnScanListener onScanListener;

    public void setOnScanListener(OnScanListener onScanListener) {
        this.onScanListener = onScanListener;
    }

    public class ScanTask extends AsyncTask<Void, Long, HashMap<Integer, List<ChildItem>>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (ConstantUtil.DEBUG) {
                Log.d(TAG, "shz ScanTask_00 onPreExecute");
            }

            if (onScanListener != null) {
                onScanListener.onScanStart();
            }
        }

        @Override
        protected HashMap<Integer, List<ChildItem>> doInBackground(Void... params) {
            if (ConstantUtil.DEBUG)
                Log.d(TAG, "shz ScanTask doInBackground start->");
            if (onScanListener != null) {
                onScanListener.onScanScanning();
            }

            List<ChildItem> cacheItems = getScanCache();

            List<ChildItem> systemList = getScanSystemJunk();

            List<ChildItem> apkList = getScanByType(FileUtil.TYPE_APK);

            List<ChildItem> residualList = getScanResidual();

            List<ChildItem> memoryItems = isAutoScanning == true ? null : getScanMemory();

            List<ChildItem> bigFiles = isAutoScanning == true ? null : getScanByType(FileUtil.TYPE_BIG_FILE);

            List<ChildItem> videoFiles = isAutoScanning == true ? null : getScanByType(FileUtil.TYPE_VIDEO);

            List<ChildItem> musicFiles = isAutoScanning == true ? null : getScanByType(FileUtil.TYPE_MUSIC);

            List<ChildItem> pictureFiles = isAutoScanning == true ? null : getScanByType(FileUtil.TYPE_PICTURE);

            HashMap<Integer, List<ChildItem>> result = new HashMap<Integer, List<ChildItem>>();
            result.clear();

            if (cacheItems != null && cacheItems.size() > 0) {
                result.put(FileUtil.TYPE_CACHE, cacheItems);
            }
            if (apkList != null && apkList.size() > 0) {
                result.put(FileUtil.TYPE_APK, apkList);
            }
            if (bigFiles != null && bigFiles.size() > 0) {
                result.put(FileUtil.TYPE_BIG_FILE, bigFiles);
            }
            if (systemList != null && systemList.size() > 0) {
                result.put(FileUtil.TYPE_SYSTEM, systemList);
            }
            if (residualList != null && residualList.size() > 0) {
                result.put(FileUtil.TYPE_RESIDUAL, residualList);
            }
            if (memoryItems != null && memoryItems.size() > 0) {
                result.put(FileUtil.TYPE_MEMORY, memoryItems);
            }
            if (videoFiles != null && videoFiles.size() > 0) {
                result.put(FileUtil.TYPE_VIDEO, videoFiles);
            }
            if (musicFiles != null && musicFiles.size() > 0) {
                result.put(FileUtil.TYPE_MUSIC, musicFiles);
            }
            if (pictureFiles != null && pictureFiles.size() > 0) {
                result.put(FileUtil.TYPE_PICTURE, pictureFiles);
            }

            if (ConstantUtil.DEBUG) {
                Log.d(TAG, "shz ScanTask doInBackground stop->");
            }
            return result;
        }

        @Override
        protected void onCancelled(HashMap<Integer, List<ChildItem>> result) {
            super.onCancelled(result);
            if (ConstantUtil.DEBUG)
                Log.d(TAG, "shz ScanTask onCancelled");
            if (onScanListener != null) {
                onScanListener.onScanCancel();
            }
        }

        @Override
        protected void onProgressUpdate(Long... values) {
            super.onProgressUpdate(values);
            if (onScanListener != null) {
                onScanListener.onScanSize((long) values[0]);
            }
        }

        @Override
        protected void onPostExecute(HashMap<Integer, List<ChildItem>> result) {
            super.onPostExecute(result);
            if (ConstantUtil.DEBUG)
                Log.d(TAG, "shz ScanTask_00 complete");
            onScanListener.onScanComplete(result);

            if (isAutoScanning) {
                startClean(result);
            }
        }

        // -- scanDb()
        private List<ChildItem> getScanByType(int type) {
            if (ConstantUtil.DEBUG)
                Log.d(TAG, "shz getScanByType start type->" + type);
            long totalFileSize = 0;

            Cursor mCursor = FileUtil.query(mContext, type, getSortByType(type));

            List<ChildItem> apkList = new ArrayList<ChildItem>();
            if (mCursor != null) {
                if (mCursor.moveToFirst()) {
                    do {
                        if (isScanning == false) {
                            if (ConstantUtil.DEBUG)
                                Log.d(TAG, "shz getScanByType onCancelled type-> " + type);
                            return null;
                        }

                        String filePath = mCursor.getString(mCursor.getColumnIndex(FileColumns.DATA));
                        if (ConstantUtil.DEBUG)
                            Log.d(TAG, "shz getScanByType filePath-> " + filePath);
                        ChildItem mChildItem = getChildItemByPath(filePath, type);
                        if (mChildItem != null) {
                            apkList.add(mChildItem);

                            long size = mChildItem.getSize();
                            totalFileSize = totalFileSize + size;
                            publishProgress(size);
                        }
                        // Log.d(TAG, "shz db filePath = " + filePath);
                    } while (mCursor.moveToNext());
                }
                mCursor.close();

            }

            switch (type) {
                case FileUtil.TYPE_APK:
                    if (onScanListener != null) {
                        onScanListener.onScanFileApkComplete(totalFileSize);
                    }
                    break;
                case FileUtil.TYPE_BIG_FILE:
                    if (onScanListener != null) {
                        onScanListener.onScanBigFileComplete(totalFileSize);
                    }
                    break;
                case FileUtil.TYPE_VIDEO:
                    if (onScanListener != null) {
                        onScanListener.onScanVideoComplete(totalFileSize);
                    }
                    break;
                case FileUtil.TYPE_PICTURE:
                    if (onScanListener != null) {
                        onScanListener.onScanPictureComplete(totalFileSize);
                    }
                    break;
                case FileUtil.TYPE_MUSIC:
                    if (onScanListener != null) {
                        onScanListener.onScanMusicComplete(totalFileSize);
                    }
                default:
                    break;
            }

            if (ConstantUtil.DEBUG)
                Log.d(TAG, "shz getScanByType end type->" + type);
            return apkList;
        }

        private List<ChildItem> getScanResidual() {
            if (ConstantUtil.DEBUG)
                Log.d(TAG, "shz getScanResidual start->");
            long totalFileSize = 0;
            String externalAbsPath = Environment.getExternalStorageDirectory().getAbsolutePath();
            Log.i(TAG, "getScanResidual -> externalAbsPath = " + externalAbsPath);

            GHDao dbManagerUtil = new GHDao(mContext);
            SQLiteDatabase db = dbManagerUtil.openDataBase();

            List<ChildItem> residualList = new ArrayList<ChildItem>();
            List<String> residuals = CleanMarsterDao.getUninstallApps(mContext);
            Cursor cursor = null;
            for (String packageName : residuals) {
                Log.i(TAG, "getScanResidual -> packageName = " + packageName);

                cursor = db.query(GHDao.TABLE_NAME_RESIDUAL, new String[]{GHDao.ROW_FOLDER, GHDao.ROW_PACKAGE_NAME}, GHDao.ROW_PACKAGE_NAME + "=?",
                        new String[]{packageName}, null, null, null);
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        if (isScanning == false) {
                            if (ConstantUtil.DEBUG)
                                Log.d(TAG, "shz getScanResidual onCancelled");
                            return null;
                        }

                        String path = externalAbsPath + cursor.getString(cursor.getColumnIndex(GHDao.ROW_FOLDER));
                        Log.i(TAG, "getScanResidual -> packageName = " + packageName + ", path = " + path);
                        ChildItem mChildItem = getChildItemByPath(path, FileUtil.TYPE_RESIDUAL);
                        if (mChildItem != null) {
                            mChildItem.setTitle(packageName);
                            mChildItem.setPackageName(packageName);
                            residualList.add(mChildItem);

                            long size = mChildItem.getSize();
                            totalFileSize = totalFileSize + size;
                            publishProgress(size);
                        }
                    }
                }
            }
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
            dbManagerUtil.closeDatabase();

            if (onScanListener != null) {
                onScanListener.onScanUninstallComplete(totalFileSize);
            }
            if (ConstantUtil.DEBUG)
                Log.d(TAG, "shz getScanResidual end->");
            return residualList;
        }

        private List<ChildItem> getScanSystemJunk() {
            List<ChildItem> systemList = getScanByType(FileUtil.TYPE_SYSTEM);
            List<ChildItem> logList = getScanByType(FileUtil.TYPE_LOG);
            if (logList != null && logList.size() > 0) {
                systemList.addAll(logList);
            }

            long TotalsystemSize = 0;
            for (ChildItem childItem : systemList) {
                TotalsystemSize = TotalsystemSize + childItem.getSize();
            }

            onScanListener.onScanSystemComplete(TotalsystemSize);
            return systemList;
        }

        private int getSortByType(int type) {
            switch (type) {
                case FileUtil.TYPE_SYSTEM:
                    return FileUtil.SORT_BY_NONE;
                default:
                    return FileUtil.SORT_BY_SIZE;
            }
        }

        private ChildItem getChildItemByPath(String filePath, int type) {
            File mFile = new File(filePath);
            if (mFile.exists() == false)
                return null;

            String fileName = mFile.getName();
            long size = mFile.length();
            ChildItem childItem = new ChildItem();

            childItem.setTitle(fileName);
            childItem.setInfo(filePath);
            childItem.setSize(size);
            childItem.setPath(filePath);
            childItem.setFileName(fileName);
            childItem.setType(type);

            switch (type) {
                case FileUtil.TYPE_APK:
                    childItem.setIcon(AppManagerUtil.getAppIcon(mContext, filePath));
                    break;
                case FileUtil.TYPE_VIDEO:
                    childItem.setCheck(false);
                    childItem.setIcon(LavaThumbnailUtil.getVideoThumbnail(mContext, filePath));
                    break;
                case FileUtil.TYPE_PICTURE:
                    childItem.setCheck(false);
                    childItem.setIcon(LavaThumbnailUtil.getLavaImageThumbnail(mContext, filePath));
                    break;
                case FileUtil.TYPE_BIG_FILE:
                case FileUtil.TYPE_MUSIC:
                    childItem.setCheck(false);
                    childItem.setIconId(R.drawable.default_file_icon);
                    break;
                default:
                    childItem.setIconId(R.drawable.default_file_icon);
                    break;
            }

            return childItem;
        }

        // -- scanDb()

        // -- scanCache()
        private List<ChildItem> getScanCache() {
            Log.d(TAG, "shz getScanCache start");
            final PackageManager mPm = mContext.getPackageManager();
            Log.d(TAG, "shz getScanCache 01 getInstalledApplications ");
            final List<ApplicationInfo> mApplicationInfos = mPm.getInstalledApplications(PackageManager.GET_META_DATA);
            Log.d(TAG, "shz getScanCache 02 getInstalledApplications ");
            final List<ChildItem> cacheItems = new ArrayList<ChildItem>();
            int totalSize = mApplicationInfos.size();
            final CountDownLatch countDownLatch = new CountDownLatch(totalSize);

            Log.d(TAG, "shz getScanCache 01 getCacheApps ");
            final HashMap<String, Long> mineCachelist = CleanMarsterDao.getMinCacheSize(mContext);
            Log.d(TAG, "shz getScanCache 02 getCacheApps ");
            Log.d(TAG, "shz getScanCache 01 getRecentUsageStatsList ");
            final List<String> mRecentUsageList = CleanMasterUtil.getRecentUsageStatsList(mContext);
            Log.d(TAG, "shz getScanCache 02 getRecentUsageStatsList ");

            try {
                for (ApplicationInfo applicationInfo : mApplicationInfos) {
                    if (isScanning == false) {
                        if (ConstantUtil.DEBUG)
                            Log.d(TAG, "shz getScanCache onCancelled");
                        return null;
                    }

                    final String mPackageName = applicationInfo.packageName;

                    if (getPackageName().equals(mPackageName)) {
                        synchronized (countDownLatch) {
                            countDownLatch.countDown();
                        }
                        continue;
                    }

                    Log.d(TAG, "shz getScanCache 01 appname ");
                    final String mApplicationName = mPm.getApplicationLabel(applicationInfo).toString();
                    Log.d(TAG, "shz getScanCache 02 appname ");
                    Log.d(TAG, "shz getScanCache 01 mPackageName " + mPackageName);
                    if (mPackageName.startsWith("com.android.providers")) {
                        synchronized (countDownLatch) {
                            countDownLatch.countDown();
                        }
                        continue;
                    }

                    if (mRecentUsageList != null && !mRecentUsageList.isEmpty() && !mRecentUsageList.contains(mPackageName)) {
                        long cacheSizedb = CleanMarsterDao.getCacheSize(mContext, mPackageName);
                        if (cacheSizedb != 0) {

                            long mineCache = ConstantUtil.MINE_CACHE_SIZE;
                            if (mineCachelist != null && mineCachelist.size() > 0) {
                                if (mineCachelist.containsKey(mPackageName)) {
                                    mineCache = (long) mineCachelist.get(mPackageName);
                                }
                            }

                            if (cacheSizedb > mineCache) {
                                publishProgress(cacheSizedb);
                                cacheItems.add(getChildItem(mContext, mApplicationName, mPackageName, cacheSizedb));
                            }
                        }

                        synchronized (countDownLatch) {
                            countDownLatch.countDown();
                        }
                        continue;
                    }

                    long cacheSize = getCacheSizeFromDb(mContext, mPackageName);
                    if (cacheSize != DEFAULT_CACHE_SIZE) {
                        long mineCache = ConstantUtil.MINE_CACHE_SIZE;
                        if (mineCachelist != null && mineCachelist.size() > 0) {
                            if (mineCachelist.containsKey(mPackageName)) {
                                mineCache = (long) mineCachelist.get(mPackageName);
                            }
                        }
                        Log.d(TAG, "suhuzhi cacheSize = " + cacheSize + ", mineCache = " + mineCache);
                        if (cacheSize > mineCache) {
                            publishProgress(cacheSize);
                            cacheItems.add(getChildItem(mContext, mApplicationName, mPackageName, cacheSize));
                        }

                        synchronized (countDownLatch) {
                            countDownLatch.countDown();
                        }
                        continue;
                    }

                    Log.d(TAG, "shz getScanCache 01 getPackageSizeInfo " + mPackageName);
                    mPm.getPackageSizeInfo(mPackageName, new IPackageStatsObserver.Stub() {

                        @Override
                        public void onGetStatsCompleted(PackageStats pStats, boolean succeeded) throws RemoteException {
                            long cacheTotalSize = pStats.cacheSize + pStats.externalCacheSize;

                            /**
                             * Size of cache used by the application. (e.g.,
                             * /data/data/<app>/cache)
                             */
                            synchronized (cacheItems) {

                                long mineCache = ConstantUtil.MINE_CACHE_SIZE;
                                if (mineCachelist != null && mineCachelist.size() > 0) {
                                    if (mineCachelist.containsKey(mPackageName)) {
                                        mineCache = (long) mineCachelist.get(mPackageName);
                                    }
                                }
                                Log.d(TAG, "shz getScanCache 02 getPackageSizeInfo " + mPackageName);
                                if (ConstantUtil.DEBUG)
                                    Log.d(TAG, "shz --->" + mPackageName + ", cacheSize = " + pStats.cacheSize + ", externalCacheSize = " + pStats.externalCacheSize + ", cacheTotalSize = "
                                            + cacheTotalSize + ", mineCache = " + mineCache);

                                if (succeeded && cacheTotalSize > mineCache) {
                                    publishProgress(cacheTotalSize);

                                    cacheItems.add(getChildItem(mContext, mApplicationName, mPackageName, cacheTotalSize));
                                }
                                CleanMarsterDao.updateCacheSize(mContext, mPackageName, cacheTotalSize);
                            }

                            synchronized (countDownLatch) {
                                countDownLatch.countDown();
                            }
                        }
                    });
                }

                if (false) {
                    countDownLatch.await();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            long totalCacheSize = 0;
            for (ChildItem childItem : cacheItems) {
                totalCacheSize = totalCacheSize + childItem.getSize();
            }

            if (onScanListener != null) {
                onScanListener.onScanCacheComplete(totalCacheSize);
            }
            Log.d(TAG, "shz getScanCache end");
            return cacheItems;
        }

        // -- scanCache()

        // -- scanMemory
        private List<ChildItem> getScanMemory() {
            if (ConstantUtil.DEBUG)
                Log.i(TAG, "start getScanMemory");
            List<ChildItem> childItems = new ArrayList<ChildItem>();
            HashMap<String, ChildItem> map = new HashMap<String, ChildItem>();
            Map<String, String> inputList = CleanMasterUtil.getInputMethodAppList(mContext);
            HashMap<String, Integer> mediaAppList = CleanMasterUtil.findMediaProcess(mContext);
            ArrayList<String> memoryList = CleanMasterUtil.getMemoryWhiteList(mContext);

            ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
            PackageManager pm = mContext.getPackageManager();
            List<RunningAppProcessInfo> runlist = am.getRunningAppProcesses();
            String myPackageName = getApplicationContext().getPackageName();

            for (RunningAppProcessInfo runInfo : runlist) {
                if (isScanning == false) {
                    if (ConstantUtil.DEBUG)
                        Log.d(TAG, "shz getScanMemory onCancelled");
                    return null;
                }

                String processName = runInfo.processName;
                String packageName = processName;
                if (processName.indexOf(":") != -1) {
                    packageName = processName.split(":")[0];
                }

                if (myPackageName.equals(packageName)) {
                    if (ConstantUtil.DEBUG) {
                        Log.i(TAG, "don't kill myself = " + packageName);
                        continue;
                    }
                }

                if (inputList != null && inputList.size() > 0) {
                    if (inputList.containsKey(packageName)) {
                        if (ConstantUtil.DEBUG) {
                            Log.i(TAG, "getScanMemory input app = " + packageName);
                            continue;
                        }
                    }
                }

                if (memoryList != null && memoryList.size() > 0) {
                    if (memoryList.contains(packageName)) {
                        Log.i(TAG, "memory white app = " + packageName);
                        continue;
                    }
                }

                if (mediaAppList != null && mediaAppList.containsKey(packageName)) {
                    int playState = mediaAppList.get(packageName);
                    Log.i(TAG, "Music/Fm app:" + packageName + ", playState = " + playState);
                    if (playState == PlaybackState.STATE_STOPPED || playState == PlaybackState.STATE_PAUSED) {
                        // kill this stop or paused music/FM
                    } else {
                        Log.i(TAG, "don't scan media app = " + packageName);
                        continue;
                    }
                }

                Drawable mIcon = null;
                String appName = packageName;

                try {
                    ApplicationInfo appInfo = pm.getApplicationInfo(packageName, 0);
                    if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                        if (ConstantUtil.DEBUG)
                            Log.i(TAG, "getScanMemory system app packageName = " + packageName);
                        continue;
                    }

                    mIcon = appInfo.loadIcon(pm);
                    appName = appInfo.loadLabel(pm).toString();
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                    if (ConstantUtil.DEBUG)
                        Log.e(TAG, "NameNotFoundException processName = " + processName + ", packageName = " + packageName);
                }

                if (mIcon == null) {
                    if (ConstantUtil.DEBUG)
                        Log.i(TAG, "getScanMemory mIcon == null packageName = " + packageName);
                    continue;
                }

                long memorySize = am.getProcessMemoryInfo(new int[]{runInfo.pid})[0].getTotalPrivateDirty() * 1024;
                String sizeStr = CommonUtil.getSizeStr(mContext, memorySize);

                ChildItem childItem = new ChildItem(appName, packageName, memorySize, mIcon);
                childItem.setAppName(appName);
                childItem.setPackageName(packageName);

                if (map.containsKey(packageName)) {
                    ChildItem temp = map.get(packageName);
                    long tempSize = temp.getSize();

                    tempSize = tempSize + memorySize;
                    temp.setSize(tempSize);

                    map.remove(packageName);
                    map.put(packageName, childItem);

                } else {
                    map.put(packageName, childItem);
                }

                if (ConstantUtil.DEBUG)
                    Log.i(TAG, "getScanMemory packageName = " + packageName + ", processName = " + processName + ", sizeStr = " + sizeStr);
            }

            long totalMemorySize = 0;

            if (map != null && map.size() > 0) {
                Iterator it = map.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry entry = (Map.Entry) it.next();
                    ChildItem item = (ChildItem) entry.getValue();
                    long size = item.getSize();
                    publishProgress(size);
                    totalMemorySize = totalMemorySize + size;
                    childItems.add(item);
                }
            }

            if (onScanListener != null) {
                onScanListener.onScanMemoryComplete(totalMemorySize);
            }
            if (ConstantUtil.DEBUG)
                Log.i(TAG, "end getScanMemory");
            return childItems;
        }

        // -- scanMemory
    }

    // start end ScanTask

    // start add CleanTask
    public static interface OnCleanListenner {

        public void onCleanStart();

        public void onCleanSize(long size);

        public void onCleanScanning();

        public void onCleanComplete();

        public void onCleanCacheComplete();

        public void onCleanFileApkComplete();

        public void onCleanBigFileComplete();

        public void onCleanVideoComplete();

        public void onCleanPictureComplete();

        public void onCleanMusicComplete();

        public void onCleanSystemComplete();

        public void onCleanUninstallComplete();

        public void onCleanMemoryComplete();

    }

    private OnCleanListenner onCleanListenner;

    public void setOnCleanListenner(OnCleanListenner onCleanListenner) {
        this.onCleanListenner = onCleanListenner;
    }

    class CleanTask extends AsyncTask<HashMap<Integer, List<ChildItem>>, Long, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            cleanSize = 0;
            onCleanListenner.onCleanStart();
        }

        @Override
        protected Void doInBackground(HashMap<Integer, List<ChildItem>>... params) {
            if (ConstantUtil.DEBUG)
                Log.d(TAG, "shz CleanTask doInBackground start->");
            onCleanListenner.onCleanScanning();
            HashMap<Integer, List<ChildItem>> cleanlist = params[0];
            if (cleanlist.containsKey(FileUtil.TYPE_MEMORY)) { // must_fist_clean
                cleanMemory(cleanlist.get(FileUtil.TYPE_MEMORY));
            }

            Iterator it = cleanlist.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry entry = (Entry) it.next();
                int type = (Integer) entry.getKey();
                List<ChildItem> value = (List<ChildItem>) entry.getValue();
                switch (type) {
                    case FileUtil.TYPE_CACHE:
                        cleanCache(value);
                        updataMinCacheDb(value);
                        break;
                    case FileUtil.TYPE_APK:
                    case FileUtil.TYPE_VIDEO:
                    case FileUtil.TYPE_MUSIC:
                    case FileUtil.TYPE_BIG_FILE:
                    case FileUtil.TYPE_SYSTEM:
                    case FileUtil.TYPE_RESIDUAL:
                    case FileUtil.TYPE_PICTURE:
                        cleanFiles(value, type);
                        break;
                    default:
                        break;
                }
            }

            if (ConstantUtil.DEBUG)
                Log.d(TAG, "shz CleanTask doInBackground stop->");
            return null;
        }

        @Override
        protected void onProgressUpdate(Long... values) {
            super.onProgressUpdate(values);
            cleanSize = cleanSize + (long) values[0];
            onCleanListenner.onCleanSize((long) values[0]);
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            onCleanListenner.onCleanComplete();
            if (isAutoScanning) {
                if (mWarnings != null) {
                    mWarnings.AutoScanClean(cleanSize);
                }
                isAutoScanning = false;
            }

        }

        // --add start clean
        private void cleanCache(List<ChildItem> mChildItem) {
            if (ConstantUtil.DEBUG)
                Log.d(TAG, "shz cleanCache start->");
            final CountDownLatch countDownLatch = new CountDownLatch(mChildItem.size());
            final PackageManager mPm = mContext.getPackageManager();

            try {
                for (ChildItem cacheItem : mChildItem) {
                    if (cacheItem.isCheck() == false) {
                        synchronized (countDownLatch) {
                            countDownLatch.countDown();
                        }
                        continue;
                    }

                    publishProgress(cacheItem.getSize());

                    // need android.permission.DELETE_CACHE_FILES
                    mPm.deleteApplicationCacheFiles(cacheItem.getPackageName(), new IPackageDataObserver.Stub() {

                        int count = 0;

                        @Override
                        public void onRemoveCompleted(final String packageName, final boolean succeeded) throws RemoteException {
                            if (ConstantUtil.DEBUG)
                                Log.d(TAG, "shz --->" + packageName + " clean cache statue = " + succeeded);

                            synchronized (countDownLatch) {
                                countDownLatch.countDown();
                            }
                        }
                    });
                }

                countDownLatch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (ConstantUtil.DEBUG)
                Log.d(TAG, "shz cleanCache end->");

            if (onCleanListenner != null) {
                onCleanListenner.onCleanCacheComplete();
            }
        }

        private void cleanFiles(List<ChildItem> mChildItem, int type) {
            if (ConstantUtil.DEBUG)
                Log.d(TAG, "shz cleanFiles start->");
            for (ChildItem childItem : mChildItem) {
                if (childItem.isCheck()) {
                    String path = childItem.getPath();
                    publishProgress(childItem.getSize());
                    FileUtil.removeAllFileAndFolder(path);
                    FileUtil.deleteFileInMediaStore(mContext, path, type);
                }
            }
            switch (type) {
                case FileUtil.TYPE_APK:
                    if (ConstantUtil.DEBUG)
                        Log.d(TAG, "shz clean apk complete!");
                    if (onCleanListenner != null) {
                        onCleanListenner.onCleanFileApkComplete();
                    }
                    break;
                case FileUtil.TYPE_BIG_FILE:
                    if (ConstantUtil.DEBUG)
                        Log.d(TAG, "shz clean big file complete!");
                    if (onCleanListenner != null) {
                        onCleanListenner.onCleanBigFileComplete();
                    }
                    break;
                case FileUtil.TYPE_VIDEO:
                    if (ConstantUtil.DEBUG)
                        Log.d(TAG, "shz clean video complete!");
                    if (onCleanListenner != null) {
                        onCleanListenner.onCleanVideoComplete();
                    }
                    break;
                case FileUtil.TYPE_PICTURE:
                    if (ConstantUtil.DEBUG) {
                        Log.d(TAG, "shz clean picture complete!");
                    }
                    if (onCleanListenner != null) {
                        onCleanListenner.onCleanPictureComplete();
                    }
                    break;
                case FileUtil.TYPE_MUSIC:
                    if (ConstantUtil.DEBUG) {
                        Log.d(TAG, "shz clean music complete!");
                    }
                    if (onCleanListenner != null) {
                        onCleanListenner.onCleanMusicComplete();
                    }
                    break;
                case FileUtil.TYPE_SYSTEM:
                    if (ConstantUtil.DEBUG)
                        Log.d(TAG, "shz clean system complete!");
                    if (onCleanListenner != null) {
                        onCleanListenner.onCleanSystemComplete();
                    }
                    break;
                case FileUtil.TYPE_RESIDUAL:
                    if (ConstantUtil.DEBUG)
                        Log.d(TAG, "shz clean uninstall complete!");
                    if (onCleanListenner != null) {
                        onCleanListenner.onCleanUninstallComplete();
                    }
                    break;
                default:
                    break;
            }
            if (ConstantUtil.DEBUG)
                Log.d(TAG, "shz cleanFiles end->");
        }

        private void cleanMemory(List<ChildItem> value) {
            if (ConstantUtil.DEBUG)
                Log.d(TAG, "shz cleanMemory start->");
            ActivityManager mAm = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
            for (ChildItem childItem : value) {
                if (childItem.isCheck()) {
                    mAm.forceStopPackage(childItem.getPackageName());
                    publishProgress(childItem.getSize());
                    if (ConstantUtil.DEBUG)
                        Log.d(TAG, "shz forceStopPackage packageName->" + childItem.getPackageName());
                }
            }
            if (ConstantUtil.DEBUG)
                Log.d(TAG, "shz cleanMemory end->");
            if (onCleanListenner != null) {
                onCleanListenner.onCleanMemoryComplete();
            }
        }

        private void updataMinCacheDb(List<ChildItem> value) {
            PackageManager mPm = mContext.getPackageManager();
            //CleanMarsterDao.deleteAllCacheDb(mContext);

            for (ChildItem childItem : value) {
                final String packageName = childItem.getPackageName();

                mPm.getPackageSizeInfo(packageName, new IPackageStatsObserver.Stub() {

                    @Override
                    public void onGetStatsCompleted(PackageStats pStats, boolean succeeded) throws RemoteException {
                        long cacheTotalSize = pStats.cacheSize + pStats.externalCacheSize;
                        CleanMarsterDao.updateMinCacheSize(mContext, pStats.packageName, cacheTotalSize);
                        if (ConstantUtil.DEBUG)
                            Log.d(TAG, "shz updataMinCacheDb insertCacheDb->" + pStats.packageName + ", cacheSize =" + cacheTotalSize);
                    }
                });
            }
        }
        // --add end clean File

    }

    // start end CleanTask

    private void startAutoCleanAlarmTask() {
        Intent intent = new Intent();
        intent.setClass(this, CleanService.class);
        intent.setAction(ConstantUtil.ACTION_AUTO_CLEAN_TRIGGER);
        AlarmTaskUtil.starRepeatAlarmTaskByService(this, ConstantUtil.AUTO_CLEAN_TRIG_TIME, intent);
        // Log.d(TAG, "shz startAutoCleanAlarmTask");
    }

    private boolean isMemoryAvailableLow() {
        long totalMemoryAvailableSize = MemoryStatusUtil.getAvailableInternalMemorySize() + MemoryStatusUtil.getAvailableExternalMemorySize();
        long totalMemorySize = MemoryStatusUtil.getTotalInternalMemorySize() + MemoryStatusUtil.getTotalExternalMemorySize();
        Log.d(TAG, "shz isMemoryAvailableLow curMemoryAvailableSize = " + totalMemoryAvailableSize + ", totalMemorySize = " + totalMemorySize);

        if (ConstantUtil.AUTO_CLEAN_SCALE_VALUE > (totalMemoryAvailableSize / totalMemorySize)) {
            Log.d(TAG, "shz isMemoryAvailableLow");
            return true;
        } else {
            return false;
        }
    }

    private List<ChildItem> getCacheList() {
        final PackageManager mPm = mContext.getPackageManager();
        final List<ApplicationInfo> mApplicationInfos = mPm.getInstalledApplications(PackageManager.GET_META_DATA);
        final List<ChildItem> cacheItems = new ArrayList<ChildItem>();
        int totalSize = mApplicationInfos.size();
        final CountDownLatch countDownLatch = new CountDownLatch(totalSize);

        Log.d(TAG, "shz getCacheSize start");
        try {
            for (ApplicationInfo applicationInfo : mApplicationInfos) {
                final String mPackageName = applicationInfo.packageName;

                mPm.getPackageSizeInfo(mPackageName, new IPackageStatsObserver.Stub() {

                    @Override
                    public void onGetStatsCompleted(PackageStats pStats, boolean succeeded) throws RemoteException {
                        long cacheTotalSize = pStats.cacheSize + pStats.externalCacheSize;

                        synchronized (cacheItems) {

                            long mineCache = ConstantUtil.MINE_CACHE_SIZE;

                            if (succeeded && cacheTotalSize > mineCache) {
                                ChildItem cachItem = new ChildItem();
                                cachItem.setPackageName(mPackageName);
                                cachItem.setSize(cacheTotalSize);
                                cacheItems.add(cachItem);
                            }
                        }

                        synchronized (countDownLatch) {
                            countDownLatch.countDown();
                        }
                    }
                });
            }

            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return cacheItems;
    }

    private long getCacheSize(List<ChildItem> cacheItems) {
        long totalCacheSize = 0;
        if (cacheItems != null && !cacheItems.isEmpty()) {
            for (ChildItem childItem : cacheItems) {
                totalCacheSize = totalCacheSize + childItem.getSize();
            }
        }
        return totalCacheSize;
    }

    private void AutoClean(List<ChildItem> cacheItems) {
        isAutoScanning = true;
        startScan();
    }

    @Override
    public void screenOn() {
        if (isAutoScanning) {
            stopScan();
        }
    }

    @Override
    public void screenOff() {

    }

    private final int DEFAULT_CACHE_SIZE = -1;

    private long getCacheSizeFromDb(Context mContext, String packageName) {
        long result = DEFAULT_CACHE_SIZE;
        final long CACHE_INTERVAL = 1000 * 60 * 5;//5 minute
        long curreTime = System.currentTimeMillis();
        long diff = curreTime - CleanMarsterDao.getCacheLastTime(mContext, packageName);

        if (diff < CACHE_INTERVAL) {
            result = CleanMarsterDao.getCacheSize(mContext, packageName);
            if (ConstantUtil.DEBUG)
                Log.d(TAG, "shz getCacheSizeFromDb packageName = " + packageName + ", cacheSize =" + result + ", diif = " + diff);
        }
        return result;
    }

    private ChildItem getChildItem(Context mContext, String mApplicationName, String mPackageName, long size) {
        ChildItem cachItem = new ChildItem();
        try {
            Drawable mIcon = mContext.getPackageManager().getApplicationIcon(mPackageName);
            cachItem.setPackageName(mPackageName);
            cachItem.setTitle(mApplicationName);
            cachItem.setInfo(mPackageName);
            cachItem.setAppName(mApplicationName);
            cachItem.setSize(size);
            cachItem.setIcon(mIcon);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        return cachItem;
    }
}
