package cleantool.su.piccompress.ui;

import java.io.File;
import java.util.ArrayList;

import cleantool.su.piccompress.bean.PhotoUpImageBucket;
import cleantool.su.starcleanmaster.luban.OnCompressListener;
import cleantool.su.starcleanmaster.ui.BaseActivity;

import com.su.starcleanmaster.R;
import cleantool.su.starcleanmaster.luban.Luban;
import cleantool.su.starcleanmaster.util.CommonUtil;
import cleantool.su.piccompress.adapter.AlbumItemAdapter;
import cleantool.su.piccompress.bean.PhotoUpImageItem;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

public class AlbumItemActivity extends BaseActivity implements OnClickListener {

    private GridView gridView;
    private Button btn_compress;

    private Context mContext;
    private PhotoUpImageBucket photoUpImageBucket;
    private ArrayList<PhotoUpImageItem> selectImages;
    private AlbumItemAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pic_album_item_images);
        initViews();
        initValues();
        initListeners();
    }

    @Override
    protected void initViews() {
        setActionbar(true);

        gridView = (GridView) findViewById(R.id.album_item_gridv);
        btn_compress = (Button) findViewById(R.id.btn_compress);
    }

    @Override
    protected void initValues() {
        this.mContext = this;
        selectImages = new ArrayList<PhotoUpImageItem>();

        Intent intent = getIntent();
        photoUpImageBucket = (PhotoUpImageBucket) intent.getSerializableExtra("imagelist");
        adapter = new AlbumItemAdapter(photoUpImageBucket.getImageList(), AlbumItemActivity.this);
        gridView.setAdapter(adapter);

        freshBtnUI();
    }

    @Override
    protected void initListeners() {
        btn_compress.setOnClickListener(this);

        gridView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                CheckBox checkBox = (CheckBox) view.findViewById(R.id.check);
                photoUpImageBucket.getImageList().get(position).setSelected(!checkBox.isChecked());

                if (photoUpImageBucket.getImageList().get(position).isSelected()) {
                    if (selectImages.contains(photoUpImageBucket.getImageList().get(position))) {

                    } else {
                        selectImages.add(photoUpImageBucket.getImageList().get(position));
                    }
                } else {
                    if (selectImages.contains(photoUpImageBucket.getImageList().get(position))) {
                        selectImages.remove(photoUpImageBucket.getImageList().get(position));
                    } else {

                    }
                }

                freshBtnUI();

                adapter.notifyDataSetChanged();
            }
        });

    }

    private void freshBtnUI() {
        btn_compress.setText(getString(R.string.piccompress_pic_count, selectImages == null ? 0 : selectImages.size()));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_compress:
            /*
             * Intent intent = new Intent(AlbumItemActivity.this,
             * SelectedImagesActivity.class); intent.putExtra("selectIma",
             * selectImages); startActivity(intent);
             */
                for (PhotoUpImageItem images : selectImages) {
                    compressWithLs(new File(images.getImagePath()));
                }
                break;

            default:
                break;
        }
    }

    /**
     * 压缩单张图片 Listener 方式
     */
    private void compressWithLs(File file) {
        Log.d("suhuazhi", "11 compressWithLs path : " + file.getAbsolutePath() + ",Filse size = " + CommonUtil.getSizeStr(mContext, file.getAbsolutePath()));
        Luban.get(this).load(file).putGear(Luban.THIRD_GEAR).setFilename(file.getName()).setCoverPicture(true).setCompressListener(new OnCompressListener() {
            @Override
            public void onStart() {
                Toast.makeText(AlbumItemActivity.this, "Start", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSuccess(File file) {
                Toast.makeText(AlbumItemActivity.this, "Success ", Toast.LENGTH_SHORT).show();
                Log.i("path", file.getAbsolutePath());
                Log.d("suhuazhi", "22 compressWithLs path : " + file.getAbsolutePath() + ",Filse size = " + CommonUtil.getSizeStr(mContext, file.getAbsolutePath()));

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(Throwable e) {

            }
        }).launch();
    }
}
