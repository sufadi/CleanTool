package cleantool.su.starcleanmaster.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import cleantool.su.piccompress.ui.AlbumsMainActivity;
import cleantool.su.starcleanmaster.model.ChildItem;
import cleantool.su.starcleanmaster.model.GroupItem;
import cleantool.su.starcleanmaster.model.ScanItem;
import cleantool.su.starcleanmaster.service.CleanService;
import cleantool.su.starcleanmaster.util.CommonUtil;
import cleantool.su.starcleanmaster.util.ConstantUtil;
import cleantool.su.starcleanmaster.util.CustomToast;
import cleantool.su.starcleanmaster.util.FileUtil;
import cleantool.su.starcleanmaster.util.PermissionUtils;
import cleantool.su.starcleanmaster.util.ShareUtil;
import cleantool.su.starcleanmaster.view.StickyLayout;
import cleantool.su.starcleanmaster.view.PinnedHeaderExpandableListView;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Build.VERSION;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.view.animation.Animation.AnimationListener;
import android.widget.BaseAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.CheckBox;
import android.widget.Toast;
import android.widget.AbsListView.LayoutParams;

import com.su.starcleanmaster.R;

public class JunkCleanActivity extends Activity implements View.OnClickListener, CleanService.OnScanListener, CleanService.OnCleanListenner, StickyLayout.OnGiveUpTouchEventListener,
        ExpandableListView.OnChildClickListener, ExpandableListView.OnGroupClickListener, PinnedHeaderExpandableListView.OnHeaderUpdateListener {

    private static final String TAG = JunkCleanActivity.class.getSimpleName();

    private static final int PERMISSION_CODE_0 = 123;
    private static final int REQUEST_CODE = 1;

    private static final int MSG_CONTENT_HEAD_VIEW = 0;
    private static final int MSG_DEFAULT = 1;
    private static final int MSG_SCAN_START = 2;
    private static final int MSG_SCAN_SCANNING = 3;
    private static final int MSG_SCAN_COMPLETE = 4;
    private static final int MSG_SCAN_CANCEL = 5;
    private static final int MSG_UPDATE_SCAN_SIZE = 6;
    private static final int MSG_CLEAN_COMPLETE = 7;
    private static final int MSG_CLEAN_CLEANNING = 8;
    private static final int MSG_CLEAN_START = 9;
    private static final int MSG_NOTHING_TO_CHEAN = 10;
    private static final int MSG_CONTENT_VIEW = 11;
    private static final int MSG_UPDATE_CLEAN_SIZE = 12;
    private static final int MSG_DEL_LISTVIEW_ANIM = 13;
    private static final int MSG_CLEAN_START_SERVICE = 14;

    public static final String KEY_JUNK_SIZE_INFO = "JUNK_SIZE_INFO";

    private int curStatus;
    private long totalJunkSize;
    private long cleanSize;

    private Context mContext;
    private ShareUtil mShareUtil;
    private CleanService mCleanService;
    private HashMap<Integer, Integer> mChildItemHelp;

    private Button btnClick;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_junkclean);
        initPermission();
        initView();
        initData();
        initListener();
        startCleanService();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.meun_more, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.more_settings:
                startActivity(new Intent(JunkCleanActivity.this, SettingsActivity.class));
                break;
            case R.id.more_picture:
                startActivity(new Intent(JunkCleanActivity.this, AlbumsMainActivity.class));
                break;
            case R.id.more_uninstall:
                startActivity(new Intent(JunkCleanActivity.this, UninstallAppActivity.class));
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initPermission() {
        if (VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            if (PermissionUtils.hasPermission(JunkCleanActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                // Good do nothing
                Log.d(TAG, "shz has Permission");
            } else {
                // Need to Apply
                Log.d(TAG, "shz need requestPermission");
                PermissionUtils.requestPermissions(JunkCleanActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_CODE_0);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSION_CODE_0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "shz PermissionsResult success");
            } else {
                // Permission Denied
                CustomToast.showToast(JunkCleanActivity.this, R.string.permission_tip, Toast.LENGTH_LONG);

                finish();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void initView() {
        initHeadView();
        initContentScanItemsView();
        initContentView();

        btnClick = (Button) findViewById(R.id.btn_click);
    }

    private void initData() {
        mContext = this;
        totalJunkSize = 0;
        curStatus = MSG_DEFAULT;
        mShareUtil = new ShareUtil(mContext);

        initContentHeadViewData();
        initContentViewData();
    }

    private void initListener() {
        btnClick.setOnClickListener(this);

        initHeadViewListener();
        initContentViewListener();
    }

    private void startCleanService() {
        bindService(new Intent(this, CleanService.class), mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    private boolean isAutoClean() {
        return mShareUtil.getBoolean(ConstantUtil.KEY_AUTO_CLEAN, ConstantUtil.DEFAULT_AUTO_CLEAN);
    }

    // add start UI reRresh
    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            int what = msg.what;
            switch (what) {
                case MSG_CONTENT_HEAD_VIEW:
                    Log.d(TAG, "shz ContentHead listview refresh");
                    if (mContentScanItemsAdapter != null) {
                        mContentScanItemsAdapter.notifyDataSetChanged();
                    }
                    break;
                case MSG_CONTENT_VIEW:
                    Log.d(TAG, "shz Content listview refresh");
                    if (mContentAdapter != null) {
                        mContentAdapter.notifyDataSetChanged();
                    }
                    break;
                case MSG_DEFAULT:
                    updataScanItemStatue(ConstantUtil.DEFAULT);
                    lv_content.setVisibility(View.GONE);
                    lv_content_header.setVisibility(View.VISIBLE);
                    break;
                case MSG_SCAN_START:
                case MSG_SCAN_SCANNING:
                    Log.d(TAG, "shz scan scanning");
                    btnClick.setText(R.string.junk_clean_btn_scan_stop);

                    lv_content.setVisibility(View.GONE);
                    lv_content_header.setVisibility(View.VISIBLE);
                    tvJunkSize.setText(R.string.junk_size);
                    tvJunkSizeInfo.setText("");
                    break;
                case MSG_SCAN_COMPLETE:
                    Log.d(TAG, "shz scan complete");
                    // Expand all group
                    for (int i = 0; i < mGroupItems.size(); i++) {
                        mGroupItems.get(i).setExpand(true);
                        lv_content.expandGroup(i);
                    }

                    btnClick.setText(R.string.junk_clean_btn_clean_start);

                    lv_content_header.setVisibility(View.GONE);
                    lv_content.setVisibility(View.VISIBLE);
                    tvJunkSizeInfo.setText(getString(R.string.junk_size_info, CommonUtil.getSizeStr(mContext, totalJunkSize)));

                    Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.push_right_in_anim);
                    LayoutAnimationController controller = new LayoutAnimationController(animation);
                    controller.setOrder(LayoutAnimationController.ORDER_NORMAL);
                    controller.setDelay(0.5f);
                    lv_content.setLayoutAnimation(controller);
                    lv_content.setHeadViewVisible(true);
                    mHandler.sendEmptyMessage(MSG_CONTENT_VIEW);
                    break;
                case MSG_CLEAN_START:
                    // collapse all group
                    for (int i = 0; i < mGroupItems.size(); i++) {
                        mGroupItems.get(i).setExpand(false);
                        lv_content.collapseGroup(i);
                    }
                    lv_content.setHeadViewVisible(false);
                    mHandler.sendEmptyMessage(MSG_CONTENT_VIEW);

                    mHandler.sendEmptyMessageDelayed(MSG_CLEAN_START_SERVICE, 100);
                    break;
                case MSG_SCAN_CANCEL:
                    Log.d(TAG, "shz scan cancle");
                    btnClick.setText(R.string.junk_clean_btn_scan_start);
                    tvJunkSize.setText(R.string.junk_clean_btn_scan_start);
                    break;
                case MSG_UPDATE_SCAN_SIZE:
                    long curSize = (long) msg.obj;
                    totalJunkSize = totalJunkSize + curSize;
                    tvJunkSize.setText(CommonUtil.getSizeStr(mContext, totalJunkSize));
                    break;
                case MSG_CLEAN_COMPLETE:
                    Log.d(TAG, "shz clean complete");
                    mChildItems.clear();
                    mGroupItems.clear();
                    if (mContentAdapter != null) {
                        mContentAdapter.notifyDataSetChanged();
                    }
                    btnClick.setText(R.string.junk_clean_btn_scan_start);
                    tvJunkSize.setText(R.string.junk_clean_btn_clean_finish);

                    String junkSizeInfo = getString(R.string.junk_clean_size_info, CommonUtil.getSizeStr(mContext, cleanSize));
                    tvJunkSizeInfo.setText(junkSizeInfo);

                    Intent intent = new Intent(mContext, CleanResultActivity.class);
                    intent.putExtra(KEY_JUNK_SIZE_INFO, junkSizeInfo);
                    startActivityForResult(intent, REQUEST_CODE);
                    break;
                case MSG_CLEAN_CLEANNING:
                    btnClick.setText(R.string.junk_clean_btn_clean_cleanning);
                    break;
                case MSG_NOTHING_TO_CHEAN:
                    btnClick.setText(R.string.junk_clean_btn_scan_start);
                    tvJunkSize.setText(R.string.junk_clean_nothing_to_clean);

                    junkSizeInfo = getString(R.string.junk_size_info, getString(R.string.junk_size));
                    tvJunkSizeInfo.setText(junkSizeInfo);

                    Intent cleanIntent = new Intent(mContext, CleanResultActivity.class);
                    cleanIntent.putExtra(KEY_JUNK_SIZE_INFO, junkSizeInfo);
                    startActivityForResult(cleanIntent, REQUEST_CODE);
                    break;
                case MSG_UPDATE_CLEAN_SIZE:
                    long size = (long) msg.obj;
                    cleanSize = cleanSize + size;
                    tvJunkSize.setText(CommonUtil.getSizeStr(mContext, (totalJunkSize - cleanSize)));
                    break;
                case MSG_DEL_LISTVIEW_ANIM:
                    delListViewAnim(0);
                    break;
                case MSG_CLEAN_START_SERVICE:
                    mCleanService.startClean(getCleanMasterdata());
                    break;
            }
            super.handleMessage(msg);
        }
    };

    @Override
    public void onClick(View v) {
        if (mCleanService == null) {
            Log.d(TAG, "shz mCleanService is not ready");
            return;
        }
        switch (curStatus) {
            case MSG_DEFAULT:
            case MSG_SCAN_CANCEL:
            case MSG_SCAN_START:
            case MSG_CLEAN_COMPLETE:
            case MSG_NOTHING_TO_CHEAN:
                // start scan
                mCleanService.startScan();
                Log.d(TAG, "shz onClick startScan");
                break;
            case MSG_SCAN_SCANNING:
                // stop scan
                mCleanService.stopScan();
                Log.d(TAG, "shz onClick stopScan");
                break;
            case MSG_SCAN_COMPLETE:
                // start clean
                Log.d(TAG, "shz onClick startClean");
                mHandler.sendEmptyMessage(MSG_CLEAN_START);
                break;
        }
    }

    // add end UI reRresh

    // add start CleanService
    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mCleanService = ((CleanService.CleanerServiceBinder) service).getService();
            if (mCleanService != null) {
                mCleanService.setOnScanListener(JunkCleanActivity.this);
                mCleanService.setOnCleanListenner(JunkCleanActivity.this);

                if (isAutoClean()) {
                    mCleanService.startScan();
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            if (mCleanService != null) {
                mCleanService.setOnScanListener(null);
            }
            mCleanService = null;
        }
    };
    // add end CleanService

    // add start headView
    private TextView tvJunkSize;
    private TextView tvJunkSizeInfo;
    private StickyLayout stickyLayout;

    private void initHeadView() {
        tvJunkSize = (TextView) findViewById(R.id.tv_junk_size);
        tvJunkSizeInfo = (TextView) findViewById(R.id.tv_junk_size_info);
        stickyLayout = (StickyLayout) findViewById(R.id.sticky_layout);
    }

    private void initHeadViewListener() {
        stickyLayout.setOnGiveUpTouchEventListener(this);
    }

    // add end headView

    // add start content_headView
    private List<ScanItem> mScanItems;
    private ContentScanItemsAdapter mContentScanItemsAdapter;

    private ListView lv_content_header;

    private void initContentScanItemsView() {
        lv_content_header = (ListView) findViewById(R.id.lv_content_header);
    }

    private void initContentHeadViewData() {
        mScanItems = new ArrayList<ScanItem>();

        for (ScanItem item : ConstantUtil.COMMON_ITEMS) {
            mScanItems.add(item);
        }

        mContentScanItemsAdapter = new ContentScanItemsAdapter(mContext);
        lv_content_header.setAdapter(mContentScanItemsAdapter);
    }

    class ContentHeadHolder {
        ImageView iv_left;
        ImageView iv_right;
        TextView tv_title;
        ProgressBar pb_right;
    }

    class ContentScanItemsAdapter extends BaseAdapter {

        private Context context;
        private LayoutInflater inflater;

        public ContentScanItemsAdapter(Context context) {
            this.context = context;
            inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return mScanItems.size();
        }

        @Override
        public Object getItem(int position) {
            return mScanItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public boolean areAllItemsEnabled() {
            return false;
        }

        @Override
        public boolean isEnabled(int position) {
            return false;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ContentHeadHolder mContentHeadHolder = null;
            if (convertView == null) {
                mContentHeadHolder = new ContentHeadHolder();
                convertView = inflater.inflate(R.layout.item_content_head_list, parent, false);
                mContentHeadHolder.iv_left = (ImageView) convertView.findViewById(R.id.iv_left);
                mContentHeadHolder.iv_right = (ImageView) convertView.findViewById(R.id.iv_right);
                mContentHeadHolder.pb_right = (ProgressBar) convertView.findViewById(R.id.pb_right);
                mContentHeadHolder.tv_title = (TextView) convertView.findViewById(R.id.tv_title);

                convertView.setTag(mContentHeadHolder);
            } else {
                mContentHeadHolder = (ContentHeadHolder) convertView.getTag();
            }

            final ScanItem scanItem = (ScanItem) getItem(position);
            mContentHeadHolder.tv_title.setText(scanItem.getTitleId());
            mContentHeadHolder.iv_left.setImageResource(scanItem.getLeftResId());

            switch (scanItem.getUiStatus()) {
                case ConstantUtil.DEFAULT:
                case ConstantUtil.SCAN_START:
                case ConstantUtil.SCAN_CANCEL:
                    mContentHeadHolder.iv_right.setVisibility(View.GONE);
                    mContentHeadHolder.pb_right.setVisibility(View.GONE);
                    // if (ConstantUtil.DEBUG) Log.d(TAG,
                    // "shz ListView getUiStatus = SCAN_CANCEL position = " +
                    // position);
                    break;
                case ConstantUtil.SCAN_SCANNING:
                    mContentHeadHolder.iv_right.setVisibility(View.GONE);
                    mContentHeadHolder.pb_right.setVisibility(View.VISIBLE);
                    // if (ConstantUtil.DEBUG) Log.d(TAG,
                    // "shz ListView getUiStatus = SCAN_SCANNING position = " +
                    // position);
                    break;
                case ConstantUtil.SCAN_COMPLETE:
                    mContentHeadHolder.iv_right.setVisibility(View.VISIBLE);
                    mContentHeadHolder.pb_right.setVisibility(View.GONE);
                    // if (ConstantUtil.DEBUG) Log.d(TAG,
                    // "shz ListView getUiStatus = SCAN_COMPLETE position = " +
                    // position);
                    break;
            }

            return convertView;
        }

    }

    // add end content_headView

    // add start contentView
    private ArrayList<ChildItem> mCacheItems;

    private ArrayList<List<ChildItem>> mChildItems;
    private ArrayList<GroupItem> mGroupItems;
    private JunkCleanexpandableListAdapter mContentAdapter;

    private PinnedHeaderExpandableListView lv_content;

    private void initContentView() {
        lv_content = (PinnedHeaderExpandableListView) findViewById(R.id.lv_content);
    }

    private void initContentViewData() {
        mChildItems = new ArrayList<List<ChildItem>>();
        mGroupItems = new ArrayList<GroupItem>();
        mCacheItems = new ArrayList<ChildItem>();
        mChildItemHelp = new HashMap<Integer, Integer>();

        mContentAdapter = new JunkCleanexpandableListAdapter(mContext);
        lv_content.setAdapter(mContentAdapter);
    }

    private void initContentViewListener() {
        lv_content.setOnGroupClickListener(this);
        lv_content.setOnChildClickListener(this);
        lv_content.setOnHeaderUpdateListener(this);
    }

    class GroupHolder {
        ImageView tv_group_arrow;
        TextView tv_group_title;
        TextView tv_group_size;
        CheckBox cb_group_check;
    }

    class ChildHolder {
        ImageView iv_child_icon;
        TextView tv_child_name;
        TextView tv_child_info;
        TextView tv_child_size;
        CheckBox cb_child_check;
    }

    class JunkCleanexpandableListAdapter extends BaseExpandableListAdapter {

        private Context context;
        private LayoutInflater inflater;

        public JunkCleanexpandableListAdapter(Context context) {
            this.context = context;
            inflater = LayoutInflater.from(context);
        }

        @Override
        public int getGroupCount() {
            return mGroupItems.size();
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return mChildItems.get(groupPosition).size();
        }

        @Override
        public Object getGroup(int groupPosition) {
            return mGroupItems.get(groupPosition);
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return mChildItems.get(groupPosition).get(childPosition);
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public View getGroupView(final int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            GroupHolder mGroupHolder = null;
            if (convertView == null) {
                mGroupHolder = new GroupHolder();
                convertView = inflater.inflate(R.layout.item_group_list, parent, false);
                mGroupHolder.tv_group_arrow = (ImageView) convertView.findViewById(R.id.tv_group_arrow);
                mGroupHolder.tv_group_title = (TextView) convertView.findViewById(R.id.tv_group_title);
                mGroupHolder.tv_group_size = (TextView) convertView.findViewById(R.id.tv_group_size);
                mGroupHolder.cb_group_check = (CheckBox) convertView.findViewById(R.id.cb_group_check);
                convertView.setTag(mGroupHolder);
            } else {
                mGroupHolder = (GroupHolder) convertView.getTag();
            }

            final GroupItem groupItem = (GroupItem) getGroup(groupPosition);

            mGroupHolder.tv_group_title.setText(groupItem.getTitle());
            mGroupHolder.tv_group_size.setText(CommonUtil.getSizeStr(mContext, groupItem.getSize()));
            mGroupHolder.cb_group_check.setChecked(groupItem.isCheck());
            if (groupItem.isExpand()) {
                mGroupHolder.tv_group_arrow.setImageResource(R.drawable.common_arrow_expand);
            } else {
                mGroupHolder.tv_group_arrow.setImageResource(R.drawable.common_arrow_collapse);
            }

            mGroupHolder.cb_group_check.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    updateChildCheckByGround(groupItem, groupPosition);
                }

            });

            return convertView;
        }

        @Override
        public View getChildView(final int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            ChildHolder mChildHolder = null;

            if (convertView == null) {
                mChildHolder = new ChildHolder();
                convertView = inflater.inflate(R.layout.item_child_list, null);
                mChildHolder.iv_child_icon = (ImageView) convertView.findViewById(R.id.iv_child_icon);
                mChildHolder.tv_child_name = (TextView) convertView.findViewById(R.id.tv_child_name);
                mChildHolder.tv_child_info = (TextView) convertView.findViewById(R.id.tv_child_info);
                mChildHolder.cb_child_check = (CheckBox) convertView.findViewById(R.id.cb_child_check);
                mChildHolder.tv_child_size = (TextView) convertView.findViewById(R.id.tv_child_size);
                convertView.setTag(mChildHolder);
            } else {
                mChildHolder = (ChildHolder) convertView.getTag();
            }

            final ChildItem childItem = (ChildItem) getChild(groupPosition, childPosition);

            Drawable mDrawable = childItem.getIcon();
            if (mDrawable == null) {
                mChildHolder.iv_child_icon.setImageResource(childItem.getIconId());
            } else {
                mChildHolder.iv_child_icon.setImageDrawable(childItem.getIcon());
            }
            mChildHolder.cb_child_check.setChecked(childItem.isCheck());
            mChildHolder.tv_child_name.setText(childItem.getTitle());
            mChildHolder.tv_child_info.setText(childItem.getInfo());
            mChildHolder.tv_child_size.setText(CommonUtil.getSizeStr(context, childItem.getSize()));

            mChildHolder.cb_child_check.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    boolean isChecked = !childItem.isCheck();
                    childItem.setCheck(isChecked);
                    mChildItems.get(groupPosition).set(childPosition, childItem);

                    mHandler.sendEmptyMessage(MSG_CONTENT_VIEW);
                }
            });

            return convertView;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }

    }

    @Override
    public boolean giveUpTouchEvent(MotionEvent event) {
        // fix content_view can't drop up
        if (lv_content_header.getVisibility() == View.VISIBLE) {
            if (lv_content_header.getFirstVisiblePosition() == 0) {
                View view = lv_content_header.getChildAt(0);
                if (view != null && view.getTop() >= 0) {
                    return true;
                }
            }
        }

        if (lv_content.getVisibility() == View.VISIBLE) {
            if (lv_content.getFirstVisiblePosition() == 0) {
                View view = lv_content.getChildAt(0);
                if (view != null && view.getTop() >= 0) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public View getPinnedHeader() {
        // PinnedHeaderExpandableListView.OnHeaderUpdateListener
        View headerView = (ViewGroup) getLayoutInflater().inflate(R.layout.item_group_list, null);
        headerView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        return headerView;
    }

    @Override
    public void updatePinnedHeader(View headerView, final int firstVisibleGroupPos) {
        // PinnedHeaderExpandableListView.OnHeaderUpdateListener
        if (headerView == null) {
            return;
        }

        if (lv_content.getVisibility() == View.VISIBLE) {
            GroupItem firstVisibleGroup = (GroupItem) mContentAdapter.getGroup(firstVisibleGroupPos);

            TextView textView = (TextView) headerView.findViewById(R.id.tv_group_title);
            textView.setText(firstVisibleGroup.getTitle());

            TextView tv_group_size = (TextView) headerView.findViewById(R.id.tv_group_size);
            tv_group_size.setText(CommonUtil.getSizeStr(mContext, firstVisibleGroup.getSize()));

            ImageView tv_group_arrow = (ImageView) headerView.findViewById(R.id.tv_group_arrow);
            if (firstVisibleGroup.isExpand()) {
                tv_group_arrow.setImageResource(R.drawable.common_arrow_expand);
            } else {
                tv_group_arrow.setImageResource(R.drawable.common_arrow_collapse);
            }
            Log.d(TAG, "shz updatePinnedHeader groupPosition = " + firstVisibleGroup + ", isGroupExpanded = " + firstVisibleGroup.isExpand());

            CheckBox cb_group_check = (CheckBox) headerView.findViewById(R.id.cb_group_check);
            cb_group_check.setChecked(firstVisibleGroup.isCheck());
            cb_group_check.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    updateChildCheckByGround(mGroupItems.get(firstVisibleGroupPos), firstVisibleGroupPos);
                }

            });

        }
    }

    @Override
    public void onPinnedHeaderClick(View headerView, int groupPosition, boolean isGroupExpanded) {
        if (headerView == null) {
            return;
        }
        isGroupExpanded = !isGroupExpanded;
        Log.d(TAG, "shz onPinnedHeaderClick groupPosition = " + groupPosition + ", isGroupExpanded = " + isGroupExpanded);
        ImageView tv_group_arrow = (ImageView) headerView.findViewById(R.id.tv_group_arrow);

        if (isGroupExpanded) {
            tv_group_arrow.setImageResource(R.drawable.common_arrow_expand);
        } else {
            tv_group_arrow.setImageResource(R.drawable.common_arrow_collapse);
        }

        GroupItem group = (GroupItem) mContentAdapter.getGroup(groupPosition);
        group.setExpand(isGroupExpanded);
        mGroupItems.set(groupPosition, group);

        // CustomToast.showToast(mContext, group.getTitle(),
        // Toast.LENGTH_SHORT);
    }

    @Override
    public boolean onGroupClick(ExpandableListView parent, View v, final int groupPosition, long id) {
        GroupHolder mGroupHolder = (GroupHolder) v.getTag();
        boolean isGroupExpanded = parent.isGroupExpanded(groupPosition);
        isGroupExpanded = !isGroupExpanded;
        Log.d(TAG, "shz onGroupClick groupPosition = " + groupPosition + ", isGroupExpanded = " + isGroupExpanded);
        if (isGroupExpanded) {
            mGroupHolder.tv_group_arrow.setImageResource(R.drawable.common_arrow_expand);
        } else {
            mGroupHolder.tv_group_arrow.setImageResource(R.drawable.common_arrow_collapse);
        }

        final GroupItem group = (GroupItem) mContentAdapter.getGroup(groupPosition);
        group.setExpand(isGroupExpanded);
        mGroupItems.set(groupPosition, group);
        // CustomToast.showToast(mContext, group.getTitle(),
        // Toast.LENGTH_SHORT);
        return false;
    }

    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
        Log.d(TAG, "shz onChildClick groupPosition = " + groupPosition + ", childPosition = " + childPosition);

        CustomToast.showToast(mContext, mChildItems.get(groupPosition).get(childPosition).getInfo(), Toast.LENGTH_LONG);
        return false;
    }

    private void updateChildCheckByGround(GroupItem groupItem, int groupPosition) {
        List<ChildItem> list = mChildItems.get(groupPosition);
        boolean isChecked = !groupItem.isCheck();

        Log.d(TAG, "shz updateChildCheckByGround groupPosition = " + groupPosition + ", isChecked " + isChecked);

        for (ChildItem childItem : list) {
            childItem.setCheck(isChecked);
        }
        groupItem.setCheck(isChecked);

        mGroupItems.set(groupPosition, groupItem);
        mChildItems.set(groupPosition, list);

        mHandler.sendEmptyMessage(MSG_CONTENT_VIEW);
    }

    // add end contentView

    // add start ScanTask.OnScanListener
    @Override
    public void onScanStart() {
        totalJunkSize = 0;
        curStatus = MSG_SCAN_START;

        if (mGroupItems != null)
            mGroupItems.clear();
        if (mChildItems != null)
            mChildItems.clear();

        updataScanItemStatue(ConstantUtil.SCAN_START);
    }

    @Override
    public void onScanCancel() {
        curStatus = MSG_SCAN_CANCEL;
        mHandler.sendEmptyMessage(MSG_SCAN_CANCEL);

        updataScanItemStatue(ConstantUtil.SCAN_CANCEL);
    }

    @Override
    public void onScanScanning() {
        curStatus = MSG_SCAN_SCANNING;
        mHandler.sendEmptyMessage(MSG_SCAN_SCANNING);

        updataScanItemStatue(ConstantUtil.SCAN_SCANNING);
    }

    @Override
    public void onScanSize(long cacheSize) {
        Message msg = new Message();
        msg.obj = cacheSize;
        msg.what = MSG_UPDATE_SCAN_SIZE;
        mHandler.sendMessage(msg);
    }

    @Override
    public void onScanComplete(HashMap<Integer, List<ChildItem>> result) {
        if (result == null || result.size() <= 0) {
            curStatus = MSG_NOTHING_TO_CHEAN;
            mHandler.sendEmptyMessage(MSG_NOTHING_TO_CHEAN);
            return;
        }

        mGroupItems.clear();
        mChildItems.clear();

        updateScanCompleteData(result);

        curStatus = MSG_SCAN_COMPLETE;
        mHandler.sendEmptyMessage(MSG_SCAN_COMPLETE);
    }

    @Override
    public void onScanCacheComplete(long size) {
        updataScanItemStatue(ConstantUtil.SCAN_COMPLETE, FileUtil.TYPE_CACHE, size);
    }

    @Override
    public void onScanFileApkComplete(long size) {
        updataScanItemStatue(ConstantUtil.SCAN_COMPLETE, FileUtil.TYPE_APK, size);
    }

    @Override
    public void onScanBigFileComplete(long size) {
        updataScanItemStatue(ConstantUtil.SCAN_COMPLETE, FileUtil.TYPE_BIG_FILE, size);
    }

    @Override
    public void onScanVideoComplete(long size) {
        updataScanItemStatue(ConstantUtil.SCAN_COMPLETE, FileUtil.TYPE_VIDEO, size);
    }

    @Override
    public void onScanPictureComplete(long size) {
        updataScanItemStatue(ConstantUtil.SCAN_COMPLETE, FileUtil.TYPE_PICTURE, size);
    }

    public void onScanMusicComplete(long size) {
        updataScanItemStatue(ConstantUtil.SCAN_COMPLETE, FileUtil.TYPE_MUSIC, size);
    }

    @Override
    public void onScanSystemComplete(long size) {
        updataScanItemStatue(ConstantUtil.SCAN_COMPLETE, FileUtil.TYPE_SYSTEM, size);
    }

    @Override
    public void onScanUninstallComplete(long size) {
        updataScanItemStatue(ConstantUtil.SCAN_COMPLETE, FileUtil.TYPE_RESIDUAL, size);
    }

    @Override
    public void onScanMemoryComplete(long size) {
        updataScanItemStatue(ConstantUtil.SCAN_COMPLETE, FileUtil.TYPE_MEMORY, size);
    }

    // --add start scan common fun
    private void updateScanCompleteData(HashMap<Integer, List<ChildItem>> result) {
        if (result == null)
            return;

        mChildItemHelp.clear();

        for (ScanItem mScanItem : mScanItems) {
            int type = mScanItem.getType();
            if (result.containsKey(type)) {
                if (type == FileUtil.TYPE_BIG_FILE || type == FileUtil.TYPE_VIDEO || type == FileUtil.TYPE_MUSIC || type == FileUtil.TYPE_PICTURE) {
                    mGroupItems.add(new GroupItem(mScanItem.getTitleId(), mScanItem.getSize(), false));
                } else {
                    mGroupItems.add(new GroupItem(mScanItem.getTitleId(), mScanItem.getSize()));
                }
                updateGroupItems(result.get(type), type);
            }
        }
    }

    private void updateGroupItems(List<ChildItem> mItem, int type) {
        mChildItems.add(mItem);
        mChildItemHelp.put(type, mChildItems.indexOf(mItem));
    }

    private HashMap<Integer, List<ChildItem>> getCleanMasterdata() {
        HashMap<Integer, List<ChildItem>> mCleanMasterdata = new HashMap<Integer, List<ChildItem>>();

        Iterator it = mChildItemHelp.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            int type = (Integer) entry.getKey();

            mCleanMasterdata.put(type, getChildItemListByType(type));
        }

        return mCleanMasterdata;
    }

    private List<ChildItem> getChildItemListByType(int type) {
        int position = mChildItemHelp.get(type);
        return mChildItems.get(position);
    }

    private void updataScanItemStatue(int status) {
        for (ScanItem mScanItem : mScanItems) {
            mScanItem.setUiStatus(status);
        }
        mHandler.sendEmptyMessage(MSG_CONTENT_HEAD_VIEW);
    }

    private void updataScanItemStatue(int status, int type, long size) {
        for (ScanItem mScanItem : mScanItems) {
            if (mScanItem.getType() == type) {
                mScanItem.setUiStatus(status);
                mScanItem.setSize(size);
                break;
            }
        }

        mHandler.sendEmptyMessage(MSG_CONTENT_HEAD_VIEW);
    }

    // --add end scan common fun

    // add end ScanTask.OnScanListener

    // add start CleanTask.OnCleanListenner
    @Override
    public void onCleanStart() {
        cleanSize = 0;
        curStatus = MSG_CLEAN_START;
    }

    @Override
    public void onCleanScanning() {
        curStatus = MSG_CLEAN_CLEANNING;
        mHandler.sendEmptyMessage(MSG_CLEAN_CLEANNING);
    }

    @Override
    public void onCleanSize(long size) {
        Message msg = new Message();
        msg.obj = size;
        msg.what = MSG_UPDATE_CLEAN_SIZE;
        mHandler.sendMessage(msg);
    }

    @Override
    public void onCleanComplete() {
        curStatus = MSG_CLEAN_COMPLETE;
        mHandler.sendEmptyMessageDelayed(MSG_CLEAN_COMPLETE, 500);
        Log.i(TAG, "shz onCleanComplete");
    }

    @Override
    public void onCleanCacheComplete() {
        mHandler.sendEmptyMessage(MSG_DEL_LISTVIEW_ANIM);
    }

    @Override
    public void onCleanFileApkComplete() {
        mHandler.sendEmptyMessage(MSG_DEL_LISTVIEW_ANIM);
    }

    @Override
    public void onCleanBigFileComplete() {
        mHandler.sendEmptyMessage(MSG_DEL_LISTVIEW_ANIM);
    }

    @Override
    public void onCleanVideoComplete() {
        mHandler.sendEmptyMessage(MSG_DEL_LISTVIEW_ANIM);
    }

    @Override
    public void onCleanPictureComplete() {
        mHandler.sendEmptyMessage(MSG_DEL_LISTVIEW_ANIM);
    }

    @Override
    public void onCleanMusicComplete() {
        mHandler.sendEmptyMessage(MSG_DEL_LISTVIEW_ANIM);
    }

    @Override
    public void onCleanSystemComplete() {
        mHandler.sendEmptyMessage(MSG_DEL_LISTVIEW_ANIM);
    }

    @Override
    public void onCleanUninstallComplete() {
        mHandler.sendEmptyMessage(MSG_DEL_LISTVIEW_ANIM);
    }

    @Override
    public void onCleanMemoryComplete() {
        mHandler.sendEmptyMessage(MSG_DEL_LISTVIEW_ANIM);
    }

    private void delListViewAnim(int position) {
        boolean isNeedAnim = true;
        if (isNeedAnim) {
            final Animation animation = (Animation) AnimationUtils.loadAnimation(mContext, R.anim.push_left_out_anim);

            // just test start
            position = mGroupItems.size() - 1;
            position = (position < 0) ? 0 : position;
            // just test end

            View view = lv_content.getChildAt(position);
            if (curStatus != MSG_CLEAN_COMPLETE && mGroupItems.size() > 0) {
                animation.setAnimationListener(new AnimationListener() {

                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {

                    }
                });

                mGroupItems.remove(position);
                mChildItems.remove(position);
                animation.cancel();
                mHandler.sendEmptyMessage(MSG_CONTENT_VIEW);
                if (animation != null) {
                    view.startAnimation(animation);
                }
            }
        } else {
            if (curStatus != MSG_CLEAN_COMPLETE && mGroupItems.size() > 0) {
                mGroupItems.remove(position);
                mChildItems.remove(position);
                mHandler.sendEmptyMessage(MSG_CONTENT_VIEW);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "shz onActivityResult");
        mHandler.sendEmptyMessageDelayed(MSG_DEFAULT, 100);
    }
    // add end ScanTask.onCleanListenner

}