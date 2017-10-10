package cleantool.su.starcleanmaster.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;

public class DispathlessLayout extends LinearLayout {

    public DispathlessLayout(Context context) {
        super(context);
    }

    public DispathlessLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DispathlessLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return false;
    }
}
