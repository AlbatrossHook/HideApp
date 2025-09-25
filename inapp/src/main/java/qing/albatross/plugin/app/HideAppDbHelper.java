/*
 * Copyright 2025 QingWan (qingwanmail@foxmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package qing.albatross.plugin.app;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.HashSet;
import java.util.Set;

class HideAppDbHelper extends SQLiteOpenHelper {
  private static final String DB_NAME = "hide_app.db";
  private static final int DB_VERSION = 1;

  static final String TABLE_SELECTED = "selected_apps";
  static final String COL_PACKAGE = "package_name";

  HideAppDbHelper(Context context) {
    super(context, DB_NAME, null, DB_VERSION);
  }

  @Override
  public void onCreate(SQLiteDatabase db) {
    db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_SELECTED + " (" +
        COL_PACKAGE + " TEXT PRIMARY KEY)");
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    // no-op for now
  }

  Set<String> loadSelectedPackages() {
    Set<String> set = new HashSet<>();
    SQLiteDatabase db = getReadableDatabase();
    try (Cursor c = db.query(TABLE_SELECTED, new String[]{COL_PACKAGE}, null, null, null, null, null)) {
      while (c.moveToNext()) {
        set.add(c.getString(0));
      }
    }
    return set;
  }

  void saveSelectedPackages(Set<String> packages) {
    SQLiteDatabase db = getWritableDatabase();
    db.beginTransaction();
    try {
      db.delete(TABLE_SELECTED, null, null);
      ContentValues values = new ContentValues();
      for (String pkg : packages) {
        values.clear();
        values.put(COL_PACKAGE, pkg);
        db.insertWithOnConflict(TABLE_SELECTED, null, values, SQLiteDatabase.CONFLICT_REPLACE);
      }
      db.setTransactionSuccessful();
    } finally {
      db.endTransaction();
    }
  }
}


