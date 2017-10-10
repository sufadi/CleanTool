package cleantool.su.starcleanmaster.ui;

import java.util.ArrayList;
import java.util.List;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageStats;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.IPackageDeleteObserver;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import cleantool.su.starcleanmaster.adapter.CustomAdapter;
import cleantool.su.starcleanmaster.model.AppInfo;

import cleantool.su.starcleanmaster.util.AppManagerUtil;
import cleantool.su.starcleanmaster.util.CommonUtil;
import cleantool.su.starcleanmaster.util.CustomToast;

import com.su.starcleanmaster.R;

public class UninstallAppActivity extends BaseActivity implements CustomAdapter.LayoutView, OnItemClickListener, OnClickListener {

    private final static String TAG = UninstallAppActivity.class.getSimpleName();

    private final static int UPDATE_UI = 0;
    private boolean isDialogShow = false;

    private Context mContext;
    private List<AppInfo> mAppInfos;
    private CustomAdapter<AppInfo> mCustomAdapter;
    private ProgressDialog mProgressDialog;

    private ListView lv_list;
    private ImageView iv_empty;
    private Button btn_click;

    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_UI:
                    mCustomAdapter.updateData((ArrayList<AppInfo>) mAppInfos);
                    break;

                default:
                    break;
            }

        }

        ;

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_uninstallapp);
        initViews();
        initValues();
        initListeners();
        loadAppTask();
    }

    @Override
    protected void initViews() {
        setActionbar(true);
        lv_list = (ListView) findViewById(R.id.lv_list);
        iv_empty = (ImageView) findViewById(R.id.iv_empty);
        btn_click = (Button) findViewById(R.id.btn_click);
    }

    @Override
    protected void initValues() {
        mContext = this;
        mAppInfos = new ArrayList<AppInfo>();
        mCustomAdapter = new CustomAdapter<AppInfo>(mAppInfos);

        mCustomAdapter.setLayoutView(this);
        lv_list.setAdapter(mCustomAdapter);

        mProgressDialog = new ProgressDialog(mContext);
        mProgressDialog.setMessage(getString(R.string.uninstall_ing));
    }

    @Override
    protected void initListeners() {
        btn_click.setOnClickListener(this);
        lv_list.setOnItemClickListener(this);
    }

    private void loadAppTask() {
        new AsyncTask<Void, AppInfo, Void>() {

            @Override
            protected void onPreExecute() {
                if (mAppInfos != null) {
                    mAppInfos.clear();
                }
            }

            ;

            @Override
            protected Void doInBackground(Void... params) {
                final PackageManager pm = mContext.getPackageManager();

                List<PackageInfo> packinfos = pm.getInstalledPackages(PackageManager.GET_PERMISSIONS);

                for (PackageInfo packinfo : packinfos) {
                    if ((packinfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                        // system app
                        continue;
                    }

                    final String packageName = packinfo.packageName;
                    final AppInfo appInfo = new AppInfo();
                    appInfo.packageName = packageName;
                    appInfo.appName = packinfo.applicationInfo.loadLabel(pm).toString();
                    appInfo.appIcon = packinfo.applicationInfo.loadIcon(pm);
                    publishProgress(appInfo);

                    pm.getPackageSizeInfo(packageName, new IPackageStatsObserver.Stub() {

                        @Override
                        public void onGetStatsCompleted(PackageStats pStats, boolean succeeded) throws RemoteException {
                            long total = pStats.codeSize + pStats.dataSize + pStats.externalCodeSize + pStats.externalDataSize + pStats.externalMediaSize + pStats.externalObbSize;

                            String mInfo = getString(R.string.uninstall_total_size, CommonUtil.getSizeStr(mContext, total));

                            for (AppInfo mAppInfo : mAppInfos) {
                                if (mAppInfo.packageName.equals(pStats.packageName)) {
                                    mAppInfo.info = mInfo;
                                    int position = mAppInfos.indexOf(mAppInfo);
                                    mAppInfos.set(position, mAppInfo);

                                    if (position == mAppInfos.size() - 1) {
                                        mHandler.sendEmptyMessage(UPDATE_UI);
                                    }
                                    break;
                                }
                            }
                        }
                    });
                }

                return null;
            }

            @Override
            protected void onProgressUpdate(AppInfo... values) {
                mAppInfos.add(values[0]);
                mHandler.sendEmptyMessage(UPDATE_UI);
                super.onProgressUpdate(values);
            }

            @Override
            protected void onPostExecute(Void result) {
                if (mAppInfos != null && mAppInfos.size() == 0) {
                    iv_empty.setVisibility(View.VISIBLE);
                    lv_list.setVisibility(View.GONE);
                } else {
                    btn_click.setVisibility(View.VISIBLE);
                }
            }

            ;
        }.execute();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();
        viewHolder.sw.toggle();

        AppInfo appInfo = mCustomAdapter.getAdapterData().get(position);
        appInfo.isCheck = !appInfo.isCheck;
        mAppInfos.set(position, appInfo);
        mCustomAdapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {
        unInstall();
    }

    class ViewHolder {
        ImageView iv_icon;
        TextView tv_info;
        TextView tv_appname;
        Switch sw;
    }

    @Override
    public <T> View setView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_uninstall_app_list, null);

            holder = new ViewHolder();
            holder.tv_info = (TextView) convertView.findViewById(R.id.tv_info);
            holder.tv_appname = (TextView) convertView.findViewById(R.id.tv_appname);
            holder.iv_icon = (ImageView) convertView.findViewById(R.id.iv_icon);
            holder.sw = (Switch) convertView.findViewById(R.id.sw);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        AppInfo appInfo = mAppInfos.get(position);

        holder.tv_info.setText(appInfo.info);
        holder.tv_appname.setText(appInfo.appName);
        holder.iv_icon.setImageDrawable(appInfo.appIcon);
        holder.sw.setChecked(appInfo.isCheck);

        return convertView;
    }

    private void showLoadingDialog() {
        if (mProgressDialog != null && !isDialogShow) {
            mProgressDialog.show();
            isDialogShow = true;
        }
    }

    private void hideLoadingDialog() {
        if (mProgressDialog != null && isDialogShow) {
            mProgressDialog.dismiss();
            isDialogShow = false;
        }
    }

    private void unInstall() {

        new AsyncTask<Void, Object, Void>() {

            private List<AppInfo> tempAppInfos = new ArrayList<AppInfo>();
            boolean isRunFinish;

            @Override
            protected void onPreExecute() {
                isRunFinish = false;
                tempAppInfos.addAll(mAppInfos);

                showLoadingDialog();
                super.onPreExecute();
            }

            @Override
            protected Void doInBackground(Void... params) {
                PackageManager pm = getPackageManager();

                for (AppInfo appInfo : tempAppInfos) {
                    if (appInfo.isCheck) {
                        final AppInfo mAppInfo = appInfo;

                        pm.deletePackage(appInfo.packageName, new IPackageDeleteObserver.Stub() {

                            @Override
                            public void packageDeleted(String packageName, int returnCode) {
                                publishProgress(returnCode, mAppInfo);
                                Log.d(TAG, "uninstall packageDeleted returnCode = " + returnCode);
                            }
                        }, 0);

                    }

                }
                return null;
            }

            @Override
            protected void onProgressUpdate(Object... values) {
                int result = (Integer) values[0];
                final AppInfo appInfo = (AppInfo) values[1];

                if (result == PackageManager.DELETE_SUCCEEDED) {
                    CustomToast.showToast(mContext, getString(R.string.uninstall_success, appInfo.appName), Toast.LENGTH_LONG);

                    if (isRunFinish) {
                        mAppInfos.remove(appInfo);
                        mHandler.sendEmptyMessage(UPDATE_UI);
                    }
                } else {
                    AppManagerUtil.setAppDisable(mContext, appInfo.packageName);

                    PackageManager pm = getPackageManager();
                    pm.deletePackage(appInfo.packageName, new IPackageDeleteObserver.Stub() {

                        @Override
                        public void packageDeleted(String packageName, int returnCode) {
                            if (returnCode == PackageManager.DELETE_SUCCEEDED) {
                                appInfo.isCheck = false;
                                mAppInfos.set(mAppInfos.indexOf(appInfo), appInfo);
                                CustomToast.showToast(mContext, getString(R.string.uninstall_fail, appInfo.appName), Toast.LENGTH_LONG);
                                mHandler.sendEmptyMessage(UPDATE_UI);
                            }
                        }
                    }, 0);

                }

                if (isRunFinish) {
                    hideLoadingDialog();
                    mHandler.sendEmptyMessage(UPDATE_UI);
                }
                super.onProgressUpdate(values);
            }

            @Override
            protected void onPostExecute(Void result) {
                isRunFinish = true;
                hideLoadingDialog();
                super.onPostExecute(result);
            }

        }.execute();

    }

}
