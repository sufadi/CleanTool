package cleantool.su.starcleanmaster.ui;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import cleantool.su.starcleanmaster.luban.Luban;
import cleantool.su.starcleanmaster.util.CommonUtil;
import cleantool.su.starcleanmaster.util.CustomToast;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

public class PictureCompressionActivity extends BaseActivity {

    private static final int REQUEST_CODE_CAPTURE_CAMEIA = 0;

    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViews();
        initValues();
        initListeners();
    }

    @Override
    protected void initViews() {
        setActionbar(true);
    }

    @Override
    protected void initValues() {
        mContext = this;
        getImageFromCamera();
    }

    @Override
    protected void initListeners() {

    }

    protected void getImageFromCamera() {
        String state = Environment.getExternalStorageState();
        if (state.equals(Environment.MEDIA_MOUNTED)) {
            Intent getImageByCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(getImageByCamera, REQUEST_CODE_CAPTURE_CAMEIA);
        } else {
            CustomToast.showToast(mContext, "sd card no found", Toast.LENGTH_LONG);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_CAPTURE_CAMEIA) {
            Uri uri = data.getData();
            if (uri == null) {
                // use bundle to get data
                Bundle bundle = data.getExtras();
                if (bundle != null) {
                    Log.d("suhuazhi", "bundle " + bundle);
                } else {
                    CustomToast.showToast(mContext, "error...", Toast.LENGTH_LONG);
                    return;
                }
            } else {
                Log.d("suhuazhi", "REQUEST_CODE_CAPTURE_CAMEIA " + uri);

                try {
                    compressPic(new File(new URI(uri.toString())));
                } catch (URISyntaxException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }

    private void compressPic(File file) {
        Log.d("suhuazhi", "11 compressPic path : " + file.getAbsolutePath() + "Filse size" + CommonUtil.getSizeStr(mContext, file.getAbsolutePath()));
        ((Luban) Luban.get(this).load(file).putGear(Luban.THIRD_GEAR).asObservable().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).doOnError(new Action1<Throwable>() {

            @Override
            public void call(Throwable throwable) {
                throwable.printStackTrace();
            }
        }).onErrorResumeNext(new Func1<Throwable, Observable<? extends File>>() {

            @Override
            public Observable<? extends File> call(Throwable throwable) {
                return Observable.empty();
            }
        }).subscribe(new Action1<File>() {

            @Override
            public void call(File file) {
                // success
                Log.d("suhuazhi", "22 compressPic path : " + file.getAbsolutePath() + "Filse size" + CommonUtil.getSizeStr(mContext, file.getAbsolutePath()));
            }
        })).launch();
    }

}
