package cleantool.su.piccompress.ui;

import java.util.ArrayList;
import java.util.List;

import android.Manifest;

import com.su.starcleanmaster.R;
import cleantool.su.starcleanmaster.ui.BaseActivity;
import cleantool.su.piccompress.adapter.AlbumsAdapter;
import cleantool.su.piccompress.bean.PhotoUpImageBucket;
import cleantool.su.piccompress.util.PhotoUpAlbumHelper;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

public class AlbumsMainActivity extends BaseActivity {

    private static final int REQUEST_CODE_SOME_FEATURES_PERMISSIONS = 1;

    private GridView gridView;
    private AlbumsAdapter adapter;
    private PhotoUpAlbumHelper photoUpAlbumHelper;
    private List<PhotoUpImageBucket> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pic_albums_gridview);
        initPermissions();
        initViews();
        initValues();
        initListeners();
    }

    private void initPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int hasCallPhonePermission = checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
            List<String> permissions = new ArrayList<String>();
            if (hasCallPhonePermission != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            } else {
                // AbSharedUtil.putString(this, "storage", "true");
            }

            if (!permissions.isEmpty()) {
                requestPermissions(permissions.toArray(new String[permissions.size()]), REQUEST_CODE_SOME_FEATURES_PERMISSIONS);
            }
        } else {// 小于6.0
            // AbSharedUtil.putString(this, "storage", "true");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_SOME_FEATURES_PERMISSIONS:
                for (int i = 0; i < permissions.length; i++) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        Log.e("suhuazhi", "Permissions --> " + "Permission Granted: " + permissions[i]);
                    } else if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        Log.e("suhuazhi", "Permissions --> " + "Permission Denied: " + permissions[i]);
                    }
                }
                break;

            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                break;
        }
    }

    @Override
    protected void initViews() {
        setActionbar(true);

        gridView = (GridView) findViewById(R.id.album_gridv);
        adapter = new AlbumsAdapter(AlbumsMainActivity.this);
        gridView.setAdapter(adapter);
    }

    @Override
    protected void initValues() {
        photoUpAlbumHelper = PhotoUpAlbumHelper.getHelper();
        photoUpAlbumHelper.init(AlbumsMainActivity.this);
        photoUpAlbumHelper.setGetAlbumList(new PhotoUpAlbumHelper.GetAlbumList() {
            @Override
            public void getAlbumList(List<PhotoUpImageBucket> list) {
                adapter.setArrayList(list);
                adapter.notifyDataSetChanged();
                AlbumsMainActivity.this.list = list;
            }
        });
        photoUpAlbumHelper.execute(false);

    }

    @Override
    protected void initListeners() {
        gridView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(AlbumsMainActivity.this, AlbumItemActivity.class);
                intent.putExtra("imagelist", list.get(position));
                startActivity(intent);
            }
        });

    }

}
