package cleantool.su.piccompress.ui;

import java.util.ArrayList;

import com.su.starcleanmaster.R;
import cleantool.su.starcleanmaster.ui.BaseActivity;
import cleantool.su.piccompress.adapter.SelectedImagesAdapter;
import cleantool.su.piccompress.bean.PhotoUpImageItem;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.Toast;

public class SelectedImagesActivity extends BaseActivity implements OnClickListener {

    private GridView gridView;
    private ArrayList<PhotoUpImageItem> arrayList;
    private SelectedImagesAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pic_selected_images_grid);
        initViews();
        initValues();
        initListeners();
    }

    @Override
    protected void initViews() {
        setActionbar(true);

        gridView = (GridView) findViewById(R.id.selected_images_gridv);
    }

    @Override
    protected void initValues() {
        arrayList = (ArrayList<PhotoUpImageItem>) getIntent().getSerializableExtra("selectIma");
        adapter = new SelectedImagesAdapter(SelectedImagesActivity.this, arrayList);
        gridView.setAdapter(adapter);
    }

    @Override
    protected void initListeners() {
        gridView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_click:
                Toast.makeText(SelectedImagesActivity.this, "上传等操作", Toast.LENGTH_LONG).show();
                break;
        }
    }

}
