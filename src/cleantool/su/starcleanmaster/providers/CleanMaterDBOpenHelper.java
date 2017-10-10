package cleantool.su.starcleanmaster.providers;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class CleanMaterDBOpenHelper extends SQLiteOpenHelper {

    public CleanMaterDBOpenHelper(Context context, String name, CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public CleanMaterDBOpenHelper(Context context, String name, int version) {
        super(context, name, null, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + CleanMarsterData.TABLE_NAME_UNINSTALL
                + "("
                + CleanMarsterData.KEY_PACKNAME + " text not null"
                + ");");

        db.execSQL("create table " + CleanMarsterData.TABLE_NAME_CACHE
                + "("
                + CleanMarsterData.KEY_PACKNAME + " text not null,"
                + CleanMarsterData.KEY_CACHE_SIZE + " integer DEFAULT 12288,"
                + CleanMarsterData.KEY_MIN_CACHE_SIZE + " integer DEFAULT 12288,"
                + CleanMarsterData.KEY_CACHE_LAST_TIME + " integer"
                + ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS lava_clean_master");
        onCreate(db);
    }

}
