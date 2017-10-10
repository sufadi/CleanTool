package cleantool.su.starcleanmaster.util;

import java.io.File;

import android.content.Context;
import android.text.format.Formatter;

import com.su.starcleanmaster.R;

public class CommonUtil {

    // invalid size value used initially and also when size retrieval through
    // PackageManager
    // fails for whatever reason
    private static final int SIZE_INVALID = -1;

    public static String getSizeStr(Context mContext, long size) {
        if (size == SIZE_INVALID) {
            return mContext.getResources().getString(R.string.junk_clean_invalid_size_value);
        }
        return Formatter.formatFileSize(mContext, size);
    }

    public static long getSize(String path) {
        return path == null ? SIZE_INVALID : getSize(new File(path));
    }

    public static long getSize(File file) {
        if (file != null && file.exists()) {
            if (file.isDirectory()) {
                File[] children = file.listFiles();
                long size = 0;
                if (children != null && children.length > 0) {
                    for (File f : children) {
                        size += getSize(f);
                    }
                    return size;
                } else {
                    return file.length();
                }
            } else {
                return file.length();
            }
        } else {
            return SIZE_INVALID;
        }
    }

    public static String getSizeStr(Context mContext, String path) {
        return getSizeStr(mContext, getSize(path));
    }
}
