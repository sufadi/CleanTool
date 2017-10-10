package cleantool.su.starcleanmaster.warnings;

import com.su.starcleanmaster.R;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.UserHandle;

import cleantool.su.starcleanmaster.service.CleanService;
import cleantool.su.starcleanmaster.util.CommonUtil;

public class CleanMasterNotificationWarnings implements CleanService.IAutoCleanStatueCallBack {

    private static final int SHOWING_NOTHING = 0;
    private static final int SHOWING_WARNING = 1;

    private static final String TAG_NOTIFICATION = "lava_cleanMaster";
    private static final String ACTION_LAVA_SETTINGS = "JunkCleanActivity";
    private static final String ACTION_DISMISSED_WARNING = "cleanMaster.dismissedWarning";
    private static final String ACTION_SHOW_CLEANMASTER_SETTINGS = "cleanMaster.batterySettings";

    private final Intent mOpenCleanMasterSettings = settings(ACTION_LAVA_SETTINGS);

    private int mShowing;
    private boolean mWarning;
    private String mJunckSize;

    private Context mContext;
    private MyReceiver mMyReceiver;
    private NotificationManager mNotificationManager;

    public CleanMasterNotificationWarnings(Context mContext) {
        this.mContext = mContext;

        mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

        mMyReceiver = new MyReceiver();
        mMyReceiver.init();
    }

    private class MyReceiver extends BroadcastReceiver {

        private void init() {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(ACTION_DISMISSED_WARNING);
            intentFilter.addAction(ACTION_SHOW_CLEANMASTER_SETTINGS);
            mContext.registerReceiverAsUser(this, UserHandle.ALL, intentFilter, android.Manifest.permission.STATUS_BAR_SERVICE, new Handler());
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (ACTION_DISMISSED_WARNING.equals(action)) {
                dissmissNotification();
            } else if (ACTION_SHOW_CLEANMASTER_SETTINGS.equals(action)) {
                dissmissNotification();
                mContext.startActivityAsUser(mOpenCleanMasterSettings, UserHandle.CURRENT);
            }
        }

    }

    public void showNotification() {
        mWarning = true;

        updateNotification();
    }

    public void dissmissNotification() {
        mWarning = false;

        updateNotification();
    }

    private void updateNotification() {
        if (mWarning) {
            showWarningNotification();
            mShowing = SHOWING_WARNING;
        } else {
            mNotificationManager.cancelAsUser(TAG_NOTIFICATION, R.id.notification_clean_master, UserHandle.ALL);
            mShowing = SHOWING_NOTHING;
        }

    }

    private void showWarningNotification() {
        Notification.Builder builder = new Notification.Builder(mContext);
        builder.setContentTitle(mContext.getString(R.string.app_name));
        builder.setSmallIcon(R.drawable.ic_launcher);
        builder.setContentText(mContext.getString(R.string.clean_auto_size_0, mJunckSize));
        builder.setOnlyAlertOnce(true);
        builder.setDeleteIntent(pendingBroadcast(ACTION_DISMISSED_WARNING));
        builder.setContentIntent(pendingBroadcast(ACTION_SHOW_CLEANMASTER_SETTINGS));
        builder.setPriority(Notification.PRIORITY_HIGH);
        builder.setVisibility(Notification.VISIBILITY_PUBLIC);

        mNotificationManager.notifyAsUser(TAG_NOTIFICATION, R.id.notification_clean_master, builder.build(), UserHandle.ALL);
    }

    private PendingIntent pendingBroadcast(String action) {
        return PendingIntent.getBroadcastAsUser(mContext, 0, new Intent(action), 0, UserHandle.CURRENT);
    }

    private Intent settings(String action) {
        return new Intent(action).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS | Intent.FLAG_ACTIVITY_NO_HISTORY
                | Intent.FLAG_ACTIVITY_CLEAR_TOP);
    }

    @Override
    public void AutoScanClean(long size) {
        mJunckSize = CommonUtil.getSizeStr(mContext, size);
        showWarningNotification();
    }
}
