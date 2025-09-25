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

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.ComponentInfo;
import android.content.pm.PackageInfo;
import android.content.pm.ResolveInfo;
import android.os.Binder;
import android.os.Parcelable;

import java.util.List;

import qing.albatross.annotation.DefOption;
import qing.albatross.annotation.FieldRef;
import qing.albatross.annotation.MethodBackup;
import qing.albatross.annotation.MethodHookBackup;
import qing.albatross.annotation.TargetClass;
import qing.albatross.core.Albatross;
import qing.albatross.exception.AlbatrossErr;

@TargetClass(className = "com.android.server.pm.PackageManagerService", required = false)
public class PackageManagerServiceH {


  static Boolean isInit;

  static boolean initHook() {
    if (isInit != null)
      return isInit;
    Albatross.transactionBegin();
    try {
      int count = Albatross.hookClass(PackageManagerServiceH.class);
      count += Albatross.hookClass(ResolveInfoH.class);
      count += Albatross.hookClass(ComputerEngineH.class);
      count += Albatross.hookClass(AppsFilterBaseH.class);
      if (count > 1) {
        Albatross.transactionEnd(true);
        isInit = true;
        return true;
      }
    } catch (AlbatrossErr e) {
      Albatross.log("hook PackageManagerService err", e);
    }
    Albatross.transactionEnd(false);
    isInit = false;
    return false;
  }


  @TargetClass
  static class PackageSetting {

    @FieldRef(option = DefOption.VIRTUAL)
    public String name;

  }

  @TargetClass(ResolveInfo.class)
  static class ResolveInfoH {

    @MethodBackup
    public static native ComponentInfo getComponentInfo(ResolveInfo resolveInfo);
  }


  @MethodHookBackup
  private List<ResolveInfo> applyPostResolutionFilter(List<ResolveInfo> resolveInfos,
                                                      String ephemeralPkgName, boolean allowDynamicSplits, int filterCallingUid,
                                                      boolean resolveForStart, int userId, Intent intent) {
    List<ResolveInfo> result = applyPostResolutionFilter(resolveInfos, ephemeralPkgName, allowDynamicSplits, filterCallingUid, resolveForStart, userId, intent);
    if (result == null || result.isEmpty())
      return result;
    List<String> hideApps = HideAppSystemPlugin.hideRules.get(filterCallingUid);
    if (hideApps == null)
      return result;
    for (int i = result.size() - 1; i >= 0; i--) {
      final ResolveInfo info = result.get(i);
      String packageName = ResolveInfoH.getComponentInfo(info).packageName;
      if (hideApps.contains(packageName)) {
        result.remove(i);
      }
    }
    return result;
  }

  @MethodHookBackup
  private boolean filterAppAccessLPr(PackageSetting ps, int callingUid,
                                     ComponentName component, int componentType, int userId) {
    List<String> hideApps = HideAppSystemPlugin.hideRules.get(callingUid);
    if (hideApps == null) {
      return filterAppAccessLPr(ps, callingUid, component, componentType, userId);
    }
    if (component != null) {
      String packages = component.getPackageName();
      return hideApps.contains(packages);
    }
    if (ps != null) {
      String packages = ps.name;
      return hideApps.contains(packages);
    }
    return filterAppAccessLPr(ps, callingUid, component, componentType, userId);
  }

  @TargetClass
  static class ParceledListSlice<T extends Parcelable> {
    @FieldRef(option = DefOption.VIRTUAL)
    private List<T> mList;

  }

  @MethodHookBackup
  private List<ApplicationInfo> getInstalledApplicationsListInternal(int flags, int userId,
                                                                     int callingUid) {
    List<ApplicationInfo> result = getInstalledApplicationsListInternal(flags, userId, callingUid);
    List<String> hideApps = HideAppSystemPlugin.hideRules.get(callingUid);
    if (hideApps == null) {
      return result;
    }
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
  private ParceledListSlice<PackageInfo> getInstalledPackages(int flags, int userId) {
    ParceledListSlice<PackageInfo> result = getInstalledPackages(flags, userId);
    int callingUid = Binder.getCallingUid();
    List<String> hideApps = HideAppSystemPlugin.hideRules.get(callingUid);
    if (hideApps == null) {
      return result;
    }
    List<PackageInfo> mList = result.mList;
    for (int i = mList.size() - 1; i >= 0; i--) {
      final PackageInfo info = mList.get(i);
      String packageName = info.packageName;
      if (hideApps.contains(packageName)) {
        mList.remove(i);
      }
    }
    return result;
  }
}
