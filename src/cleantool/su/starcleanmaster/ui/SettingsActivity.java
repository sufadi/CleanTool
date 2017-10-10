package cleantool.su.starcleanmaster.ui;

import com.su.starcleanmaster.R;
import cleantool.su.starcleanmaster.util.ConstantUtil;
import cleantool.su.starcleanmaster.util.ShareUtil;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

public class SettingsActivity extends BaseActivity implements OnClickListener {

    private static final int CLEAN_FREQUENCY_VALUE_0 = 0;

    private static final int CLEAN_FREQUENCY_VALUE_1 = 1;

    private static final int CLEAN_FREQUENCY_VALUE_2 = 2;

    private Switch sw_auto_settings;
    private TextView tv_clean_frequency_value;
    private LinearLayout ll_sw_auto_settings, ll_clean_frequency_value;

    private Context mContext;
    private ShareUtil mShareUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        initViews();
        initValues();
        initListeners();
    }

    @Override
    protected void onResume() {
        updateSwitch();
        updateAutoCleanFrequency();
        super.onResume();
    }

    @Override
    protected void initViews() {
        setActionbar(true);
        sw_auto_settings = (Switch) findViewById(R.id.sw_auto_settings);
        tv_clean_frequency_value = (TextView) findViewById(R.id.tv_clean_frequency_value);
        ll_sw_auto_settings = (LinearLayout) findViewById(R.id.ll_sw_auto_settings);
        ll_clean_frequency_value = (LinearLayout) findViewById(R.id.ll_clean_frequency_value);
    }

    @Override
    protected void initValues() {
        mContext = this;
        mShareUtil = new ShareUtil(mContext);
    }

    @Override
    protected void initListeners() {
        ll_sw_auto_settings.setOnClickListener(this);
        ll_clean_frequency_value.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_sw_auto_settings:
                setAutoCleanSettings(!sw_auto_settings.isChecked());

                updateSwitch();
                break;
            case R.id.ll_clean_frequency_value:
                showDialog();
                break;
            default:
                break;
        }
    }

    private void updateSwitch() {
        sw_auto_settings.setChecked(isAutoCleanSettings());
    }

    private void setAutoCleanSettings(boolean value) {
        mShareUtil.setShare(ConstantUtil.KEY_AUTO_CLEAN, value);
    }

    private boolean isAutoCleanSettings() {
        return mShareUtil.getBoolean(ConstantUtil.KEY_AUTO_CLEAN, ConstantUtil.DEFAULT_AUTO_CLEAN);
    }

    private void updateAutoCleanFrequency() {
        int mAutoCleanFrequency = getAutoCleanFrequency();
        switch (mAutoCleanFrequency) {
            case CLEAN_FREQUENCY_VALUE_0:
                tv_clean_frequency_value.setText(R.string.settings_clean_frequency_value_0);
                break;
            case CLEAN_FREQUENCY_VALUE_1:
                tv_clean_frequency_value.setText(R.string.settings_clean_frequency_value_1);
                break;
            case CLEAN_FREQUENCY_VALUE_2:
                tv_clean_frequency_value.setText(R.string.settings_clean_frequency_value_2);
                break;
            default:
                break;
        }
    }

    private void setAutoCleanFrequency(int value) {
        mShareUtil.setShare(ConstantUtil.KEY_CLEAN_FREQUENCY, value);
    }

    private int getAutoCleanFrequency() {
        return mShareUtil.getInt(ConstantUtil.KEY_CLEAN_FREQUENCY, ConstantUtil.DEFAULT_CLEAN_FREQUENCY);
    }

    private void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        final String[] content = {getString(R.string.settings_clean_frequency_value_0), getString(R.string.settings_clean_frequency_value_1), getString(R.string.settings_clean_frequency_value_2)};
        builder.setItems(content, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                setAutoCleanFrequency(which);
                updateAutoCleanFrequency();
            }
        });
        builder.show();
    }
}
