package cleantool.su.starcleanmaster.providers;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

public class CleanMasterProvider extends ContentProvider {

    private CleanMaterDBOpenHelper mOpenHelper;

    @Override
    public boolean onCreate() {
        mOpenHelper = new CleanMaterDBOpenHelper(this.getContext(), CleanMarsterData.DATABASE_NAME, CleanMarsterData.DATABASE_VERSION);
        return true;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        long id = 0;
        switch (CleanMarsterData.UserTableData.URIMATCHER.match(uri)) {
            case CleanMarsterData.UserTableData.CODE_CACHEL:
                id = db.insert(CleanMarsterData.TABLE_NAME_CACHE, null, values);
                return ContentUris.withAppendedId(uri, id);
            case CleanMarsterData.UserTableData.CODE_UNINSTALL:
                id = db.insert(CleanMarsterData.TABLE_NAME_UNINSTALL, null, values);
                return ContentUris.withAppendedId(uri, id);
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count = 0;
        switch (CleanMarsterData.UserTableData.URIMATCHER.match(uri)) {
            case CleanMarsterData.UserTableData.CODE_CACHEL:
                count = db.delete(CleanMarsterData.TABLE_NAME_CACHE, selection, selectionArgs);
                break;
            case CleanMarsterData.UserTableData.CODE_UNINSTALL:
                count = db.delete(CleanMarsterData.TABLE_NAME_UNINSTALL, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        db.close();
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count = 0;
        switch (CleanMarsterData.UserTableData.URIMATCHER.match(uri)) {
            case CleanMarsterData.UserTableData.CODE_CACHEL:
                count = db.update(CleanMarsterData.TABLE_NAME_CACHE, values, selection, selectionArgs);
                break;
            case CleanMarsterData.UserTableData.CODE_UNINSTALL:
                count = db.update(CleanMarsterData.TABLE_NAME_UNINSTALL, values, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        db.close();
        return count;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        switch (CleanMarsterData.UserTableData.URIMATCHER.match(uri)) {
            case CleanMarsterData.UserTableData.CODE_CACHEL:
                return db.query(CleanMarsterData.TABLE_NAME_CACHE, projection, selection, selectionArgs, null, null, sortOrder);
            case CleanMarsterData.UserTableData.CODE_UNINSTALL:
                return db.query(CleanMarsterData.TABLE_NAME_UNINSTALL, projection, selection, selectionArgs, null, null, sortOrder);
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public String getType(Uri uri) {
        switch (CleanMarsterData.UserTableData.URIMATCHER.match(uri)) {
            case CleanMarsterData.UserTableData.CODE_CACHEL:
                return CleanMarsterData.CONTENT_TYPE_CACHE;
            case CleanMarsterData.UserTableData.CODE_UNINSTALL:
                return CleanMarsterData.CONTENT_TYPE_UNINSTALL;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }
}
