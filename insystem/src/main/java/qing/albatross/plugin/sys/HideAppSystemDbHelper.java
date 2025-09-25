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
package qing.albatross.plugin.sys;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 数据库帮助类，用于存储隐藏应用系统插件的配置
 * 支持两级配置：目标应用 -> 要隐藏的应用列表
 */
public class HideAppSystemDbHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "hide_app_system.db";
    private static final int DB_VERSION = 1;

    // 目标应用表
    private static final String TABLE_TARGET_APPS = "target_apps";
    private static final String COL_TARGET_PACKAGE = "target_package";
    private static final String COL_TARGET_NAME = "target_name";
    private static final String COL_IS_ENABLED = "is_enabled";

    // 隐藏规则表
    private static final String TABLE_HIDE_RULES = "hide_rules";
    private static final String COL_HIDE_PACKAGE = "hide_package";
    private static final String COL_HIDE_NAME = "hide_name";

    public HideAppSystemDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 创建目标应用表
        String createTargetTable = "CREATE TABLE " + TABLE_TARGET_APPS + " (" +
                COL_TARGET_PACKAGE + " TEXT PRIMARY KEY, " +
                COL_TARGET_NAME + " TEXT NOT NULL, " +
                COL_IS_ENABLED + " INTEGER DEFAULT 0)";
        db.execSQL(createTargetTable);

        // 创建隐藏规则表
        String createRulesTable = "CREATE TABLE " + TABLE_HIDE_RULES + " (" +
                COL_TARGET_PACKAGE + " TEXT NOT NULL, " +
                COL_HIDE_PACKAGE + " TEXT NOT NULL, " +
                COL_HIDE_NAME + " TEXT NOT NULL, " +
                "PRIMARY KEY (" + COL_TARGET_PACKAGE + ", " + COL_HIDE_PACKAGE + "), " +
                "FOREIGN KEY (" + COL_TARGET_PACKAGE + ") REFERENCES " + TABLE_TARGET_APPS + "(" + COL_TARGET_PACKAGE + ") ON DELETE CASCADE)";
        db.execSQL(createRulesTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HIDE_RULES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TARGET_APPS);
        onCreate(db);
    }

    /**
     * 获取所有目标应用及其启用状态
     */
    public List<TargetAppInfo> getAllTargetApps() {
        List<TargetAppInfo> apps = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor cursor = db.query(TABLE_TARGET_APPS, null, null, null, null, null, null)) {
            while (cursor.moveToNext()) {
                String packageName = cursor.getString(cursor.getColumnIndexOrThrow(COL_TARGET_PACKAGE));
                String appName = cursor.getString(cursor.getColumnIndexOrThrow(COL_TARGET_NAME));
                boolean isEnabled = cursor.getInt(cursor.getColumnIndexOrThrow(COL_IS_ENABLED)) == 1;
                apps.add(new TargetAppInfo(packageName, appName, isEnabled));
            }
        }
        return apps;
    }

    /**
     * 添加或更新目标应用
     */
    public void saveTargetApp(String packageName, String appName, boolean isEnabled) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_TARGET_PACKAGE, packageName);
        values.put(COL_TARGET_NAME, appName);
        values.put(COL_IS_ENABLED, isEnabled ? 1 : 0);
        db.insertWithOnConflict(TABLE_TARGET_APPS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    /**
     * 删除目标应用及其所有规则
     */
    public void deleteTargetApp(String packageName) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete(TABLE_HIDE_RULES, COL_TARGET_PACKAGE + " = ?", new String[]{packageName});
            db.delete(TABLE_TARGET_APPS, COL_TARGET_PACKAGE + " = ?", new String[]{packageName});
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    /**
     * 获取指定目标应用要隐藏的应用列表
     */
    public List<HideAppInfo> getHideAppsForTarget(String targetPackage) {
        List<HideAppInfo> hideApps = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor cursor = db.query(TABLE_HIDE_RULES, 
                new String[]{COL_HIDE_PACKAGE, COL_HIDE_NAME}, 
                COL_TARGET_PACKAGE + " = ?", 
                new String[]{targetPackage}, 
                null, null, null)) {
            while (cursor.moveToNext()) {
                String packageName = cursor.getString(cursor.getColumnIndexOrThrow(COL_HIDE_PACKAGE));
                String appName = cursor.getString(cursor.getColumnIndexOrThrow(COL_HIDE_NAME));
                hideApps.add(new HideAppInfo(packageName, appName));
            }
        }
        return hideApps;
    }

    /**
     * 保存目标应用的隐藏规则
     */
    public void saveHideRules(String targetPackage, List<HideAppInfo> hideApps) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            // 先删除现有规则
            db.delete(TABLE_HIDE_RULES, COL_TARGET_PACKAGE + " = ?", new String[]{targetPackage});
            
            // 插入新规则
            for (HideAppInfo hideApp : hideApps) {
                ContentValues values = new ContentValues();
                values.put(COL_TARGET_PACKAGE, targetPackage);
                values.put(COL_HIDE_PACKAGE, hideApp.packageName);
                values.put(COL_HIDE_NAME, hideApp.appName);
                db.insert(TABLE_HIDE_RULES, null, values);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    /**
     * 获取所有配置的规则（用于返回给插件系统）
     */
    public Map<String, List<String>> getAllRules() {
        Map<String, List<String>> allRules = new HashMap<>();
        SQLiteDatabase db = getReadableDatabase();
        
        // 只获取启用状态的目标应用
        try (Cursor cursor = db.query(TABLE_TARGET_APPS, 
                new String[]{COL_TARGET_PACKAGE}, 
                COL_IS_ENABLED + " = 1", 
                null, null, null, null)) {
            while (cursor.moveToNext()) {
                String targetPackage = cursor.getString(cursor.getColumnIndexOrThrow(COL_TARGET_PACKAGE));
                List<String> hidePackages = new ArrayList<>();
                
                try (Cursor hideCursor = db.query(TABLE_HIDE_RULES, 
                        new String[]{COL_HIDE_PACKAGE}, 
                        COL_TARGET_PACKAGE + " = ?", 
                        new String[]{targetPackage}, 
                        null, null, null)) {
                    while (hideCursor.moveToNext()) {
                        hidePackages.add(hideCursor.getString(hideCursor.getColumnIndexOrThrow(COL_HIDE_PACKAGE)));
                    }
                }
                
                if (!hidePackages.isEmpty()) {
                    allRules.put(targetPackage, hidePackages);
                }
            }
        }
        return allRules;
    }

    /**
     * 目标应用信息
     */
    public static class TargetAppInfo {
        public String packageName;
        public String appName;
        public boolean isEnabled;

        public TargetAppInfo(String packageName, String appName, boolean isEnabled) {
            this.packageName = packageName;
            this.appName = appName;
            this.isEnabled = isEnabled;
        }
    }

    /**
     * 隐藏应用信息
     */
    public static class HideAppInfo {
        public String packageName;
        public String appName;

        public HideAppInfo(String packageName, String appName) {
            this.packageName = packageName;
            this.appName = appName;
        }
    }
}
