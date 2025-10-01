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

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.util.ArraySet;
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
import java.util.List;
import java.util.Set;

import qing.albatross.plugin.R;

public class PluginConfigActivity extends Activity {
  private ListView listView;
  private AppAdapter adapter;
  private final List<AppInfo> appList = new ArrayList<>();
  private HideAppDbHelper dbHelper;
  private CheckBox checkBoxFilterSystemApps;
  private Button buttonSelectAll;
  private Button buttonDeselectAll;
  private boolean isShowSystemApps = false;

  // 应用信息类
  private static class AppInfo {
    String packageName;
    String appName;
    String displayText;
    boolean isSystemApp;
    boolean isSelected;

    AppInfo(String packageName, String appName, boolean isSystemApp) {
      this.packageName = packageName;
      this.appName = appName;
      this.isSystemApp = isSystemApp;
      this.displayText = appName + "\n" + packageName;
      isSelected = false;
    }
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_plugin_config); // 通过布局文件加载UI

    dbHelper = new HideAppDbHelper(this);

    // 初始化控件
    listView = findViewById(R.id.listViewApps);
    listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
    adapter = new AppAdapter();
    listView.setAdapter(adapter);

    Button btnSave = findViewById(R.id.buttonSave);
    btnSave.setOnClickListener(v -> {
      saveAndFinish();
    });

    checkBoxFilterSystemApps = findViewById(R.id.checkBoxFilterSystemApps);
    checkBoxFilterSystemApps.setOnCheckedChangeListener((buttonView, isChecked) -> {
      isShowSystemApps = isChecked;
      loadApps(); // 重新加载应用列表
    });

    // 初始化全选和取消全选按钮
    buttonSelectAll = findViewById(R.id.buttonSelectAll);
    buttonDeselectAll = findViewById(R.id.buttonDeselectAll);

    buttonSelectAll.setOnClickListener(v -> {
      selectAllApps(true);
    });

    buttonDeselectAll.setOnClickListener(v -> {
      selectAllApps(false);
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
      // 处理权限异常
      Toast.makeText(this, "无法获取应用列表: 权限不足", Toast.LENGTH_SHORT).show();
      return;
    }
    if (resolveInfos.isEmpty()) {
      Toast.makeText(this, "未找到可显示的应用", Toast.LENGTH_SHORT).show();
      return;
    }
    appList.clear();
    String selfPkg = getPackageName();

    for (ResolveInfo info : resolveInfos) {
      ApplicationInfo appInfo = info.activityInfo.applicationInfo;
      String pkg = appInfo.packageName;
      if (selfPkg.equals(pkg)) continue;

      // 更全面的系统应用判断
      boolean isSystemApp = (appInfo.flags & (ApplicationInfo.FLAG_SYSTEM | ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)) != 0;

      if (!isShowSystemApps && isSystemApp) continue;

      CharSequence label = info.loadLabel(pm);
      String appName = label != null ? label.toString() : pkg;
      appList.add(new AppInfo(pkg, appName, isSystemApp));
    }

    adapter.notifyDataSetChanged();
    Set<String> preselected = dbHelper.loadSelectedPackages();
    for (int i = 0; i < appList.size(); i++) {
      AppInfo appInfo = appList.get(i);
      if (preselected.contains(appInfo.packageName)) {
//        listView.setItemChecked(i, true);
        appInfo.isSelected = true;
      }
    }
    updateSelectAllButtonState();
  }

  /**
   * 全选或取消全选所有应用
   * @param selectAll true为全选，false为取消全选
   */
  private void selectAllApps(boolean selectAll) {
    for (AppInfo appInfo : appList) {
      appInfo.isSelected = selectAll;
    }
    adapter.notifyDataSetChanged();
    updateSelectAllButtonState();
  }

  /**
   * 更新全选按钮的状态
   */
  private void updateSelectAllButtonState() {
    if (appList.isEmpty()) {
      buttonSelectAll.setEnabled(false);
      buttonDeselectAll.setEnabled(false);
      return;
    }

    buttonSelectAll.setEnabled(true);
    buttonDeselectAll.setEnabled(true);

    // 检查是否所有应用都已选中
    boolean allSelected = true;
    boolean anySelected = false;
    for (AppInfo appInfo : appList) {
      if (appInfo.isSelected) {
        anySelected = true;
      } else {
        allSelected = false;
      }
    }

    // 根据选择状态更新按钮文本
    if (allSelected) {
      buttonSelectAll.setText("已全选");
      buttonSelectAll.setEnabled(false);
    } else {
      buttonSelectAll.setText("全选");
      buttonSelectAll.setEnabled(true);
    }

    if (!anySelected) {
      buttonDeselectAll.setText("已清空");
      buttonDeselectAll.setEnabled(false);
    } else {
      buttonDeselectAll.setText("取消全选");
      buttonDeselectAll.setEnabled(true);
    }
  }

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
        convertView = View.inflate(PluginConfigActivity.this,
            R.layout.app_list_item, null);
        holder = new ViewHolder();
        holder.textView = convertView.findViewById(R.id.textViewAppName);
        holder.imageView = convertView.findViewById(R.id.imageViewAppIcon);
        holder.checkBox = convertView.findViewById(R.id.checkBoxAppSelected);
        convertView.setTag(holder);
      } else {
        holder = (ViewHolder) convertView.getTag();
      }
      AppInfo appInfo = appList.get(position);
      holder.textView.setText(appInfo.displayText);
      try {
        holder.imageView.setImageDrawable(pm.getApplicationIcon(appInfo.packageName));
      } catch (PackageManager.NameNotFoundException e) {
        holder.imageView.setImageDrawable(null);
      }
      holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
        appInfo.isSelected = isChecked;
        updateSelectAllButtonState(); // 更新按钮状态
      });
      holder.checkBox.setChecked(appInfo.isSelected);
      return convertView;
    }

    private class ViewHolder {
      TextView textView;
      ImageView imageView;
      CheckBox checkBox;
    }
  }

  private void saveAndFinish() {
    Set<String> preselected = new ArraySet<>();
    for (AppInfo appInfo : appList) {
      if (appInfo.isSelected)
        preselected.add(appInfo.packageName);
    }
    dbHelper.saveSelectedPackages(preselected);
    Intent result = new Intent();
    if (!preselected.isEmpty()) {
      StringBuilder builder = new StringBuilder();
      for (String pkg : preselected) {
        builder.append(pkg).append(",");
      }
      result.putExtra("plugin_params", builder.substring(0, builder.length() - 1));
    }
    result.putExtra("plugin_class", this.getClass().getName().replace("PluginConfigActivity", "HideAppPlugin"));
    setResult(RESULT_OK, result);
    finish();
  }

}
