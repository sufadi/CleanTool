package cleantool.su.starcleanmaster.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

import com.su.starcleanmaster.R;

public class AppManagerUtil {

    public static Drawable getAppIcon(Context mContext, String path) {
        PackageManager pm = mContext.getPackageManager();
        PackageInfo pkInfo = pm.getPackageArchiveInfo(path, PackageManager.GET_ACTIVITIES);
        if (pkInfo != null) {
            ApplicationInfo appInfo = pkInfo.applicationInfo;
            appInfo.sourceDir = path;
            appInfo.publicSourceDir = path;
            try {
                return pm.getApplicationIcon(appInfo);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return mContext.getResources().getDrawable(R.drawable.defult_apk_icon);
    }

    public static void setAppDisable(Context mContext, String mPackageName) {
        PackageManager pm = mContext.getPackageManager();
        pm.setApplicationEnabledSetting(mPackageName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
    }
}
