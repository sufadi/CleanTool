package cleantool.su.starcleanmaster;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import cleantool.su.starcleanmaster.service.CleanService;
import cleantool.su.starcleanmaster.util.AlarmTaskUtil;
import cleantool.su.starcleanmaster.util.ConstantUtil;
import cleantool.su.starcleanmaster.util.ShareUtil;

public class MyApplication extends Application {

    private final static String TAG = MyApplication.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();
        if (isAppFistRun(this)) {
            // do anything
        }

        startAutoCleanAlarmTask();
    }

    private boolean isAppFistRun(Context context) {
        final String KEY_FIST_RUN = "fist run";
        boolean result = false;

        String curVersionName = getVersionName();

        ShareUtil mShareUtil = new ShareUtil(getApplicationContext());
        if (!curVersionName.equals(mShareUtil.getString(KEY_FIST_RUN, "v1.0"))) {
            mShareUtil.setShare(KEY_FIST_RUN, curVersionName);
            result = true;
        }
        Log.d(TAG, "VersionName = " + curVersionName + ", isFistRun = " + result);
        return result;
    }

    private String getVersionName() {
        PackageManager pm = getPackageManager();
        try {
            PackageInfo info = pm.getPackageInfo(getPackageName(), 0);
            return info.versionName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Can't find version";
    }

    private void startAutoCleanAlarmTask() {
        Intent intent = new Intent();
        intent.setClass(this, CleanService.class);
        intent.setAction(ConstantUtil.ACTION_AUTO_CLEAN_TRIGGER);
        AlarmTaskUtil.starRepeatAlarmTaskByService(this, ConstantUtil.AUTO_CLEAN_TRIG_TIME, intent);
        Log.d(TAG, "suhuazhi startAutoCleanAlarmTask");
    }
}
