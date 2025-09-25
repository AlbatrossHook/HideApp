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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import qing.albatross.plugin.R;

public class PluginConfigActivity extends Activity {
  private AppAdapter adapter;
  private final List<TargetAppInfo> appList = new ArrayList<>();
  private HideAppSystemDbHelper dbHelper;
  private boolean isShowSystemApps = false;

  private static class TargetAppInfo {
    String packageName;
    String appName;
    String displayText;
    boolean isSystemApp;
    boolean isEnabled;

    TargetAppInfo(String packageName, String appName, boolean isSystemApp, boolean isEnabled) {
      this.packageName = packageName;
      this.appName = appName;
      this.isSystemApp = isSystemApp;
      this.isEnabled = isEnabled;
      this.displayText = appName + "\n" + packageName;
    }
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_plugin_config);

    dbHelper = new HideAppSystemDbHelper(this);

    ListView listView = findViewById(R.id.listViewApps);
    adapter = new AppAdapter();
    listView.setAdapter(adapter);

    Button btnSave = findViewById(R.id.buttonSave);
    btnSave.setOnClickListener(v -> saveAndFinish());

    CheckBox checkBoxFilterSystemApps = findViewById(R.id.checkBoxFilterSystemApps);
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
    String selfPkg = getPackageName();

    // 加载已保存的目标应用配置
    Map<String, Boolean> savedConfigs = new HashMap<>();
    for (HideAppSystemDbHelper.TargetAppInfo saved : dbHelper.getAllTargetApps()) {
      savedConfigs.put(saved.packageName, saved.isEnabled);
    }

    for (ResolveInfo info : resolveInfos) {
      ApplicationInfo appInfo = info.activityInfo.applicationInfo;
      String pkg = appInfo.packageName;
      if (selfPkg.equals(pkg)) continue;

      boolean isSystemApp = (appInfo.flags & (ApplicationInfo.FLAG_SYSTEM | ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)) != 0;
      if (!isShowSystemApps && isSystemApp) continue;

      CharSequence label = info.loadLabel(pm);
      String appName = label != null ? label.toString() : pkg;
      boolean isEnabled = savedConfigs.getOrDefault(pkg, false);

      appList.add(new TargetAppInfo(pkg, appName, isSystemApp, isEnabled));
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
        convertView = View.inflate(PluginConfigActivity.this, R.layout.app_list_item, null);
        holder = new ViewHolder();
        holder.textView = convertView.findViewById(R.id.textViewAppName);
        holder.imageView = convertView.findViewById(R.id.imageViewAppIcon);
        holder.checkBox = convertView.findViewById(R.id.checkBoxAppSelected);
        convertView.setTag(holder);
      } else {
        holder = (ViewHolder) convertView.getTag();
      }

      TargetAppInfo appInfo = appList.get(position);
      holder.textView.setText(appInfo.displayText);

      try {
        holder.imageView.setImageDrawable(pm.getApplicationIcon(appInfo.packageName));
      } catch (PackageManager.NameNotFoundException e) {
        holder.imageView.setImageDrawable(null);
      }

      holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
        appInfo.isEnabled = isChecked;
        // 实时保存到数据库
        dbHelper.saveTargetApp(appInfo.packageName, appInfo.appName, isChecked);
      });
      holder.checkBox.setChecked(appInfo.isEnabled);
      // 点击列表项进入详细配置
      convertView.setOnClickListener(v -> {
        Intent intent = new Intent(PluginConfigActivity.this, AppDetailActivity.class);
        intent.putExtra("target_package", appInfo.packageName);
        intent.putExtra("target_name", appInfo.appName);
        startActivity(intent);
      });
      return convertView;
    }

    private class ViewHolder {
      TextView textView;
      ImageView imageView;
      CheckBox checkBox;
    }
  }

  private void saveAndFinish() {
    // 获取所有启用的目标应用及其隐藏规则
    Map<String, List<String>> allRules = dbHelper.getAllRules();

    Intent result = new Intent();
    result.putExtra("plugin_class", this.getClass().getName().replace("PluginConfigActivity", "HideAppSystemPlugin"));

    // 将规则转换为字符串格式存储
    StringBuilder rulesBuilder = new StringBuilder();
    for (Map.Entry<String, List<String>> entry : allRules.entrySet()) {
      if (!rulesBuilder.toString().isEmpty()) {
        rulesBuilder.append("|");
      }
      rulesBuilder.append(entry.getKey()).append(":");
      for (int i = 0; i < entry.getValue().size(); i++) {
        if (i > 0) rulesBuilder.append(",");
        rulesBuilder.append(entry.getValue().get(i));
      }
    }
    result.putExtra("plugin_params", rulesBuilder.toString());
    setResult(RESULT_OK, result);
    finish();
  }

  @Override
  public void onBackPressed() {
    saveAndFinish();
  }

  @Override
  protected void onResume() {
    super.onResume();
    loadApps();
  }
}
