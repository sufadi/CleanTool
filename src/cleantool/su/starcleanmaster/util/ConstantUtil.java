package cleantool.su.starcleanmaster.util;

import cleantool.su.starcleanmaster.model.ScanItem;
import com.su.starcleanmaster.R;

public class ConstantUtil {

    public final static boolean DEBUG = true;

    public static final ScanItem COMMON_ITEMS[] = {new ScanItem(R.string.junk_clean_system_title, R.drawable.junk_clean_system, FileUtil.TYPE_SYSTEM),

            new ScanItem(R.string.junk_clean_cache_title, R.drawable.junk_clean_cache, FileUtil.TYPE_CACHE),

            new ScanItem(R.string.junk_clean_invalid_apk_title, R.drawable.junk_clean_invalid_apk, FileUtil.TYPE_APK),

            new ScanItem(R.string.junk_clean_uninstall_title, R.drawable.junk_clean_uninstall, FileUtil.TYPE_RESIDUAL),

            new ScanItem(R.string.junk_clean_memory_title, R.drawable.junk_clean_memory, FileUtil.TYPE_MEMORY),

            new ScanItem(R.string.junk_clean_video_title, R.drawable.junk_clean_cache, FileUtil.TYPE_VIDEO),

            new ScanItem(R.string.junk_clean_picture_title, R.drawable.junk_clean_invalid_apk, FileUtil.TYPE_PICTURE),

            new ScanItem(R.string.junk_clean_music_title, R.drawable.junk_clean_memory, FileUtil.TYPE_MUSIC),

            new ScanItem(R.string.junk_clean_bigfile_title, R.drawable.junk_clean_bigfile, FileUtil.TYPE_BIG_FILE)};

    public final static long MINE_CACHE_SIZE = 12 * 1024;// 12 KB

    public final static long MINE_CACHE_FILE_SIZE = 0;// 0 KB

    // add start cleanService status
    public final static int DEFAULT = 0;

    public final static int SCAN_START = 1;

    public final static int SCAN_CANCEL = 2;

    public final static int SCAN_SCANNING = 3;

    public final static int SCAN_COMPLETE = 4;

    public final static int CLEAN_START = 5;

    public final static int CLEAN_SCANNING = 6;

    public final static int CLEAN_COMPLETE = 7;
    // add end cleanService status

    // Settings
    public static final String KEY_AUTO_CLEAN = "key_auto_clean";

    public static final boolean DEFAULT_AUTO_CLEAN = true;

    public static final String KEY_CLEAN_FREQUENCY = "key_clean_frequency";

    public static final int DEFAULT_CLEAN_FREQUENCY = 0;

    public static final long AUTO_CLEAN_TRIG_TIME = 1/* 6 * 60 */;// 6hours

    public static final String ACTION_AUTO_CLEAN_TRIGGER = "action_auto_clean_trigger";

    public static final long AUTO_CLEAN_LIMIT_VALUE_0 = 100 * 1024 * 1024;// 100MB

    public static final long AUTO_CLEAN_LIMIT_VALUE_1 = 300 * 1024 * 1024;// 300MB

    public static final double AUTO_CLEAN_SCALE_VALUE = 0.1;// 10%
}
