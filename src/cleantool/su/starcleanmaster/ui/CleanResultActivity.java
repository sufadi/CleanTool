package cleantool.su.starcleanmaster.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.su.starcleanmaster.R;

public class CleanResultActivity extends Activity {

    private final int MSG_FINISH_SELF = 0;

    private TextView tv_result;
    private ImageView iv_circle;

    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case MSG_FINISH_SELF:
                    Intent intent = new Intent(CleanResultActivity.this, JunkCleanActivity.class);
                    setResult(RESULT_OK, intent);
                    finish();
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
        setContentView(R.layout.activity_clean_result);
        initView();
        initData();
    }

    private void initView() {
        tv_result = (TextView) findViewById(R.id.tv_result);
        iv_circle = (ImageView) findViewById(R.id.iv_circle);
    }

    private void initData() {
        final Animation animation = AnimationUtils.loadAnimation(this, R.anim.clean_result_rotate_circle_anim);

        animation.setAnimationListener(new AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
                tv_result.setText("");
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                String junkSizeInf = getIntent().getStringExtra(JunkCleanActivity.KEY_JUNK_SIZE_INFO);
                tv_result.setText(junkSizeInf);
                animation.cancel();
                mHandler.sendEmptyMessageDelayed(MSG_FINISH_SELF, 1000);
            }
        });

        iv_circle.startAnimation(animation);
    }

}
