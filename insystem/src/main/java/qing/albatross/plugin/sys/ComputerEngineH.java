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

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.ResolveInfo;

import java.util.List;

import qing.albatross.annotation.MethodHookBackup;
import qing.albatross.annotation.TargetClass;

@TargetClass(className = "com.android.server.pm.ComputerEngine", required = false)
public class ComputerEngineH {


  @MethodHookBackup
  private List<ApplicationInfo> getInstalledApplications(long flags, int userId, int callingUid) {
    List<ApplicationInfo> result = getInstalledApplications(flags, userId, callingUid);
    List<String> hideApps = HideAppSystemPlugin.hideRules.get(callingUid);
    if (hideApps == null)
      return result;
    for (int i = result.size() - 1; i >= 0; i--) {
      final ApplicationInfo info = result.get(i);
      String packageName = info.packageName;
      if (hideApps.contains(packageName)) {
        result.remove(i);
      }
    }
    return result;
  }

  @MethodHookBackup
  private List<ResolveInfo> applyPostResolutionFilter(
      List<ResolveInfo> resolveInfos, String ephemeralPkgName, boolean allowDynamicSplits, int filterCallingUid,
      boolean resolveForStart, int userId, Intent intent) {
    List<ResolveInfo> result = applyPostResolutionFilter(resolveInfos, ephemeralPkgName, allowDynamicSplits, filterCallingUid, resolveForStart, userId, intent);
    List<String> hideApps = HideAppSystemPlugin.hideRules.get(filterCallingUid);
    if (hideApps == null)
      return result;
    for (int i = resolveInfos.size() - 1; i >= 0; i--) {
      final ResolveInfo info = resolveInfos.get(i);
      String packageName = PackageManagerServiceH.ResolveInfoH.getComponentInfo(info).packageName;
      if (hideApps.contains(packageName)) {
        resolveInfos.remove(i);
      }
    }
    return result;
  }

  @MethodHookBackup
  private PackageInfo getPackageInfoInternalBody(String packageName, long versionCode,
                                                 long flags, int filterCallingUid, int userId) {
    PackageInfo packageInfo = getPackageInfoInternalBody(packageName, versionCode, flags, filterCallingUid, userId);
    if (packageInfo != null) {
      List<String> hideApps = HideAppSystemPlugin.hideRules.get(filterCallingUid);
      if (hideApps != null && hideApps.contains(packageName))
        return null;
    }
    return packageInfo;

  }


}
