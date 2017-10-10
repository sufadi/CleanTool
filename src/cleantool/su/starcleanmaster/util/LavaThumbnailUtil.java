package cleantool.su.starcleanmaster.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;

import com.su.starcleanmaster.R;

public class LavaThumbnailUtil {

    private static final int LAVA_WIDTH = 80;

    private static final int LAVA_HEIGHT = 80;

    public static Drawable getLavaImageThumbnail(Context mContext, String path) {
        Bitmap bitmap = getImageThumbnail(path, LAVA_WIDTH, LAVA_HEIGHT);
        if (bitmap == null) {
            return mContext.getResources().getDrawable(R.drawable.default_file_icon);
        } else {
            return new BitmapDrawable(mContext.getResources(), bitmap);
        }

    }

    public static Drawable getVideoThumbnail(Context mContext, String path) {
        Bitmap bitmap = getVideoThumbnail(path, LAVA_WIDTH, LAVA_HEIGHT, MediaStore.Video.Thumbnails.MICRO_KIND);
        if (bitmap == null) {
            return mContext.getResources().getDrawable(R.drawable.default_file_icon);
        } else {
            return new BitmapDrawable(mContext.getResources(), bitmap);
        }
    }

    public static Bitmap getImageThumbnail(String path, int width, int height) {
        Bitmap bitmap = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        options.inJustDecodeBounds = false;
        int h = options.outHeight;
        int w = options.outWidth;
        int beWidth = w / width;
        int beHeight = h / height;
        int be = 1;
        if (beWidth < beHeight) {
            be = beWidth;
        } else {
            be = beHeight;
        }
        if (be <= 0) {
            be = 1;
        }
        options.inSampleSize = be;
        bitmap = BitmapFactory.decodeFile(path, options);
        bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
        return bitmap;
    }

    public static Bitmap getVideoThumbnail(String path, int width, int height, int kind) {
        Bitmap bitmap = null;
        bitmap = ThumbnailUtils.createVideoThumbnail(path, kind);
        bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
        return bitmap;
    }

}
