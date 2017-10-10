package cleantool.su.starcleanmaster.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;

import com.su.starcleanmaster.R;

public class GHDao {
    private static final String TAG = GHDao.class.getSimpleName();

    public static final String DB_NAME = "appfolder.db";
    public static final String TABLE_NAME_RESIDUAL = "app_residual";
    public static final String ROW_FOLDER = "folder";
    public static final String ROW_PACKAGE_NAME = "package_name";

    private final int BUFFER_SIZE = 400000;

    private String dbPath;

    private Context mContext;
    private SQLiteDatabase database;

    public GHDao(Context mContext) {
        this.mContext = mContext;
        // /data/data/packageName/databases/xxx.db
        this.dbPath = Environment.getDataDirectory().getAbsolutePath() + "/data/" + mContext.getApplicationContext().getPackageName() + "/databases/" + DB_NAME;
    }

    public String getdbPath() {
        return dbPath;
    }

    public SQLiteDatabase openDataBase() {
        return this.openDataBase(dbPath);
    }

    private SQLiteDatabase openDataBase(String dbPath) {
        try {
            if (new File(dbPath).exists() == false) {
                InputStream is = mContext.getResources().openRawResource(R.raw.appfolder);

                FileOutputStream fos = new FileOutputStream(dbPath);
                byte[] buffer = new byte[BUFFER_SIZE];

                int count = 0;
                while ((count = is.read(buffer)) > 0) {
                    fos.write(buffer, 0, count);
                }
                fos.close();
                is.close();
                Log.i(TAG, "coyp db success!");
            }

            database = SQLiteDatabase.openOrCreateDatabase(dbPath, null);
            return database;
        } catch (FileNotFoundException e) {
            Log.e(TAG, "File not found");
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void closeDatabase() {
        if (database != null) {
            database.close();
        }
    }
}
