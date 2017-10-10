package cleantool.su.starcleanmaster.warnings;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class CleanMasterWarningUI {

    private Context mContext;
    private static CleanMasterWarningUI mInstance;
    private CleanMasterWarningUIRecevier mCleanMasterWarningUIRecevier;

    private IScreenStatueCallBack mIScreenStatueCallBack;

    public interface IScreenStatueCallBack {
        void screenOn();

        void screenOff();
    }

    public void setIScreenStatueCallBack(IScreenStatueCallBack mIScreenStatueCallBack) {
        this.mIScreenStatueCallBack = mIScreenStatueCallBack;
    }

    public static CleanMasterWarningUI getInstance(Context mContext) {
        if (null == mInstance) {
            synchronized (CleanMasterWarningUI.class) {
                if (null == mInstance) {
                    mInstance = new CleanMasterWarningUI(mContext);
                }
            }
        }
        return mInstance;
    }

    private CleanMasterWarningUI(Context mContext) {
        this.mContext = mContext;

        mCleanMasterWarningUIRecevier = new CleanMasterWarningUIRecevier();
        mCleanMasterWarningUIRecevier.init();
    }

    private class CleanMasterWarningUIRecevier extends BroadcastReceiver {

        private void init() {
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_SCREEN_OFF);
            filter.addAction(Intent.ACTION_SCREEN_ON);
            mContext.registerReceiver(this, filter);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                if (mIScreenStatueCallBack != null) {
                    mIScreenStatueCallBack.screenOff();
                }
            } else if (Intent.ACTION_SCREEN_ON.equals(action)) {
                if (mIScreenStatueCallBack != null) {
                    mIScreenStatueCallBack.screenOn();
                }
            }
        }

    }
}
