package cleantool.su.starcleanmaster.recevier;

import cleantool.su.starcleanmaster.db.CleanMarsterDao;
import cleantool.su.starcleanmaster.util.ConstantUtil;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class CleanMasterRecevier extends BroadcastReceiver {

    private static final String TAG = CleanMasterRecevier.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (Intent.ACTION_PACKAGE_REMOVED.equals(action)) {
            String packageName = intent.getData().getSchemeSpecificPart();
            if (ConstantUtil.DEBUG)
                Log.d(TAG, "shz ACTION_PACKAGE_REMOVED packageName = " + packageName);
            if (CleanMarsterDao.isExistUninstallDb(context, packageName) == false) {
                CleanMarsterDao.insertUninstallDb(context, packageName);
            }
        } else if (Intent.ACTION_PACKAGE_ADDED.equals(action)) {
            String packageName = intent.getData().getSchemeSpecificPart();
            if (ConstantUtil.DEBUG)
                Log.d(TAG, "shz ACTION_PACKAGE_ADDED packageName = " + packageName);
            if (CleanMarsterDao.isExistUninstallDb(context, packageName) == true) {
                CleanMarsterDao.deleteUninstallDb(context, packageName);
            }
        }
    }

}
