package baidumapsdk.demo.util;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * DBHelper
 *
 * @author Mislead
 *         DATE: 2015/7/22
 *         DESC:
 **/
public class DBHelper extends SQLiteOpenHelper {

  private static String TAG = "DBHelper";

  public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
    super(context, name, factory, version);
  }

  @Override public void onCreate(SQLiteDatabase db) {

  }

  @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

  }
}
