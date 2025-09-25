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

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import qing.albatross.plugin.R;

public class AppDetailActivity extends Activity {
  private AppAdapter adapter;
  private final List<HideAppInfo> appList = new ArrayList<>();
  private final Set<String> selectedPackages = new HashSet<>();
  private HideAppSystemDbHelper dbHelper;
  private String targetPackage;
  private String targetName;
  private CheckBox checkBoxFilterSystemApps;
  private boolean isShowSystemApps = false;

  // 隐藏应用信息类
  private static class HideAppInfo {
    String packageName;
    String appName;
    String displayText;
    boolean isSystemApp;

    HideAppInfo(String packageName, String appName, boolean isSystemApp) {
      this.packageName = packageName;
      this.appName = appName;
      this.isSystemApp = isSystemApp;
      this.displayText = appName + "\n" + packageName;
    }
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_app_detail);

    targetPackage = getIntent().getStringExtra("target_package");
    targetName = getIntent().getStringExtra("target_name");

    if (targetPackage == null || targetName == null) {
      Toast.makeText(this, "参数错误", Toast.LENGTH_SHORT).show();
      finish();
      return;
    }

    setTitle("配置 " + targetName + " 要隐藏的应用");

    dbHelper = new HideAppSystemDbHelper(this);

    // 初始化控件
    ListView listView = findViewById(R.id.listViewApps);
    adapter = new AppAdapter();
    listView.setAdapter(adapter);

    Button btnSave = findViewById(R.id.buttonSave);
    btnSave.setOnClickListener(v -> saveAndFinish());

    checkBoxFilterSystemApps = findViewById(R.id.checkBoxFilterSystemApps);
    checkBoxFilterSystemApps.setOnCheckedChangeListener((buttonView, isChecked) -> {
      isShowSystemApps = isChecked;
      loadApps();
    });

    loadApps();
  }

  private void loadApps() {
    PackageManager pm = getPackageManager();
    Intent intent = new Intent(Intent.ACTION_MAIN);
    intent.addCategory(Intent.CATEGORY_LAUNCHER);
    List<ResolveInfo> resolveInfos;
    try {
      resolveInfos = pm.queryIntentActivities(intent, PackageManager.GET_META_DATA);
    } catch (SecurityException e) {
      Toast.makeText(this, "无法获取应用列表: 权限不足", Toast.LENGTH_SHORT).show();
      return;
    }
    if (resolveInfos == null || resolveInfos.isEmpty()) {
      Toast.makeText(this, "未找到可显示的应用", Toast.LENGTH_SHORT).show();
      return;
    }

    appList.clear();
    selectedPackages.clear();
    String selfPkg = getPackageName();

    // 加载已保存的隐藏规则
    for (HideAppSystemDbHelper.HideAppInfo saved : dbHelper.getHideAppsForTarget(targetPackage)) {
      selectedPackages.add(saved.packageName);
    }

    for (ResolveInfo info : resolveInfos) {
      ApplicationInfo appInfo = info.activityInfo.applicationInfo;
      String pkg = appInfo.packageName;
      if (selfPkg.equals(pkg) || targetPackage.equals(pkg)) continue;

      boolean isSystemApp = (appInfo.flags & (ApplicationInfo.FLAG_SYSTEM | ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)) != 0;
      if (!isShowSystemApps && isSystemApp) continue;

      CharSequence label = info.loadLabel(pm);
      String appName = label != null ? label.toString() : pkg;

      appList.add(new HideAppInfo(pkg, appName, isSystemApp));
    }
    adapter.notifyDataSetChanged();
  }

  // 自定义适配器
  private class AppAdapter extends BaseAdapter {
    private final PackageManager pm = getPackageManager();

    @Override
    public int getCount() {
      return appList.size();
    }

    @Override
    public Object getItem(int position) {
      return appList.get(position);
    }

    @Override
    public long getItemId(int position) {
      return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      ViewHolder holder;
      if (convertView == null) {
        convertView = View.inflate(AppDetailActivity.this, R.layout.app_list_item, null);
        holder = new ViewHolder();
        holder.textView = convertView.findViewById(R.id.textViewAppName);
        holder.imageView = convertView.findViewById(R.id.imageViewAppIcon);
        holder.checkBox = convertView.findViewById(R.id.checkBoxAppSelected);
        convertView.setTag(holder);
      } else {
        holder = (ViewHolder) convertView.getTag();
      }
      HideAppInfo appInfo = appList.get(position);
      holder.textView.setText(appInfo.displayText);
      try {
        holder.imageView.setImageDrawable(pm.getApplicationIcon(appInfo.packageName));
      } catch (PackageManager.NameNotFoundException e) {
        holder.imageView.setImageDrawable(null);
      }
      boolean isSelected = selectedPackages.contains(appInfo.packageName);
      holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
        if (isChecked) {
          selectedPackages.add(appInfo.packageName);
        } else {
          selectedPackages.remove(appInfo.packageName);
        }
      });
      holder.checkBox.setChecked(isSelected);
      return convertView;
    }

    private class ViewHolder {
      TextView textView;
      ImageView imageView;
      CheckBox checkBox;
    }
  }

  private void saveAndFinish() {
    // 保存隐藏规则到数据库
    List<HideAppSystemDbHelper.HideAppInfo> hideApps = new ArrayList<>();
    for (String packageName : selectedPackages) {
      // 从appList中找到对应的应用名称
      String appName = packageName;
      for (HideAppInfo appInfo : appList) {
        if (appInfo.packageName.equals(packageName)) {
          appName = appInfo.appName;
          break;
        }
      }
      hideApps.add(new HideAppSystemDbHelper.HideAppInfo(packageName, appName));
    }

    dbHelper.saveHideRules(targetPackage, hideApps);

    Toast.makeText(this, "已保存 " + selectedPackages.size() + " 个隐藏规则", Toast.LENGTH_SHORT).show();
    finish();
  }

  @Override
  public void onBackPressed() {
    saveAndFinish();
  }
}
