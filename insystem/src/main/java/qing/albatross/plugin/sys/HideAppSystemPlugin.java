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

import android.app.Application;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import qing.albatross.agent.AlbatrossPlugin;
import qing.albatross.core.Albatross;

public class HideAppSystemPlugin extends AlbatrossPlugin {


  public HideAppSystemPlugin(String libName, String argString, int flags) {
    super(libName, argString, flags);
    hideRules = new HashMap<>();
  }

  @Override
  public void beforeMakeApplication() {
    Albatross.log("HideAppSystemPlugin beforeMakeApplication");
  }

  @Override
  public boolean load() {
    Albatross.log("HideAppSystemPlugin load");
    return super.load();
  }

  @Override
  public boolean parseParams(String hideRulesStr, int flags) {
    hideRules.clear();
    if (hideRulesStr != null) {
      Application application = Albatross.currentApplication();
      PackageManager packageManager = application.getPackageManager();
      Albatross.log("begin parse rule:" + hideRulesStr);
      String[] ruleEntries = hideRulesStr.split("\\|");
      for (String entry : ruleEntries) {
        String[] parts = entry.split(":");
        if (parts.length != 2) {
          Albatross.log("Invalid rule entry: " + entry);
          continue;
        }
        String pkg = parts[0];
        try {
          PackageInfo packageInfo = packageManager.getPackageInfo(pkg, 0);
          String[] rules = parts[1].split(",");
          List<String> ruleList = new ArrayList<>();
          for (String rule : rules) {
            if (!rule.isEmpty()) {
              ruleList.add(rule);
            }
          }
          int uid = packageInfo.applicationInfo.uid;
          hideRules.put(uid, ruleList);
          Albatross.log(pkg + " add rule:" + ruleList);
        } catch (PackageManager.NameNotFoundException e) {
          Albatross.log("Package not found: " + pkg);
        }
      }
    }
    return true;
  }

  @Override
  public void beforeApplicationCreate(Application application) {
    Albatross.log("HideAppSystemPlugin beforeApplicationCreate");
  }

  @Override
  public void afterApplicationCreate(Application application) {
    Albatross.log("HideAppSystemPlugin afterApplicationCreate");
  }

  public static Map<Integer, List<String>> hideRules;


  @Override
  public void onAttachSystem(Application application) {
    if (!PackageManagerServiceH.initHook()) {
      Albatross.log("init hook err");
    }
  }
}
