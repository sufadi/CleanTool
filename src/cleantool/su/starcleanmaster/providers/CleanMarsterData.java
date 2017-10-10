package cleantool.su.starcleanmaster.providers;

import android.content.UriMatcher;
import android.provider.BaseColumns;

public class CleanMarsterData {

    public static final String AUTHORITY = "lavaCleanMaster";

    public static final String DATABASE_NAME = "lavacleanmarter.db";
    public static final int DATABASE_VERSION = 1;

    public static final String TABLE_NAME_UNINSTALL = "uninstall";
    public static final String TABLE_NAME_CACHE = "cache";

    public static final String KEY_PACKNAME = "packagename";
    public static final String KEY_CACHE_SIZE = "cache_size";
    public static final String KEY_MIN_CACHE_SIZE = "min_cache_size";
    public static final String KEY_CACHE_LAST_TIME = "cache_last_time";

    public static final String CONTENT_TYPE_UNINSTALL = "vnd.android.cursor.dir/com.lava.starcleanmaster.providers.uninstall";
    public static final String CONTENT_TYPE_CACHE = "vnd.android.cursor.dir/com.lava.starcleanmaster.providers.cache";

    public static final class UserTableData implements BaseColumns {

        public static final int CODE_CACHEL = 1;
        public static final int CODE_UNINSTALL = 2;
        public static final String PATH_CACHE = "path_cache";
        public static final String PATH_UNINSTALL = "path_uninstall";

        public static final UriMatcher URIMATCHER;

        static {
            URIMATCHER = new UriMatcher(UriMatcher.NO_MATCH);
            URIMATCHER.addURI(AUTHORITY, PATH_CACHE, CODE_CACHEL);
            URIMATCHER.addURI(AUTHORITY, PATH_UNINSTALL, CODE_UNINSTALL);
        }

    }

}
