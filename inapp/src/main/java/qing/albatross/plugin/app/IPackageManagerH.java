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

import android.content.Intent;
import android.content.pm.ComponentInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Parcelable;

import java.util.List;

import qing.albatross.annotation.DefOption;
import qing.albatross.annotation.FieldRef;
import qing.albatross.annotation.MethodBackup;
import qing.albatross.annotation.MethodHookBackup;
import qing.albatross.annotation.TargetClass;
import qing.albatross.core.Albatross;
import qing.albatross.exception.AlbatrossErr;

@TargetClass
public class IPackageManagerH {

  @TargetClass
  static class ParceledListSlice<T extends Parcelable> {

    @FieldRef(option = DefOption.VIRTUAL)
    private List<T> mList;

  }

  static class PackageManagerH {

    @FieldRef(option = DefOption.INSTANCE)
    IPackageManagerH mPM;
  }

  static boolean init() {
    Albatross.transactionBegin();
    try {
      Albatross.hookObject(PackageManagerH.class, Albatross.currentApplication().getPackageManager());
      Albatross.hookClass(ResolveInfoH.class);
      Albatross.transactionEnd(true);
      return true;
    } catch (AlbatrossErr e) {
      Albatross.transactionEnd(false);
      return false;
    }

  }

  @TargetClass(ResolveInfo.class)
  static class ResolveInfoH {
    @MethodBackup
    public static native ComponentInfo getComponentInfo(ResolveInfo resolveInfo);
  }

  @MethodHookBackup(maxSdk = 32)
  private ParceledListSlice<ResolveInfo> queryIntentActivities(Intent intent, String resolvedType, int flags, int userId) {
    return filterParceledListSlice(queryIntentActivities(intent, resolvedType, flags, userId), o -> ResolveInfoH.getComponentInfo(o).packageName);
  }

  @MethodHookBackup(minSdk = 32)
  private ParceledListSlice<ResolveInfo> queryIntentActivities(Intent intent, String resolvedType, long flags, int userId) {
    return filterParceledListSlice(queryIntentActivities(intent, resolvedType, flags, userId), o -> ResolveInfoH.getComponentInfo(o).packageName);
  }

  @MethodHookBackup(maxSdk = 32)
  private ParceledListSlice<ResolveInfo> queryIntentServices(Intent intent, String resolvedType, int flags, int userId) {
    return filterParceledListSlice(queryIntentServices(intent, resolvedType, flags, userId), o -> ResolveInfoH.getComponentInfo(o).packageName);
  }

  @MethodHookBackup(minSdk = 32)
  private ParceledListSlice<ResolveInfo> queryIntentServices(Intent intent, String resolvedType, long flags, int userId) {
    return filterParceledListSlice(queryIntentServices(intent, resolvedType, flags, userId), o -> ResolveInfoH.getComponentInfo(o).packageName);
  }

  @MethodHookBackup(maxSdk = 32)
  private ParceledListSlice<ResolveInfo> queryIntentReceivers(Intent intent, String resolvedType, int flags, int userId) {
    return filterParceledListSlice(queryIntentReceivers(intent, resolvedType, flags, userId), o -> ResolveInfoH.getComponentInfo(o).packageName);
  }

  @MethodHookBackup(minSdk = 32)
  private ParceledListSlice<ResolveInfo> queryIntentReceivers(Intent intent, String resolvedType, long flags, int userId) {
    return filterParceledListSlice(queryIntentReceivers(intent, resolvedType, flags, userId), o -> ResolveInfoH.getComponentInfo(o).packageName);
  }


  @MethodHookBackup(maxSdk = 32)
  private ParceledListSlice<ResolveInfo> queryIntentContentProviders(Intent intent, String resolvedType, int flags, int userId) {
    return filterParceledListSlice(queryIntentContentProviders(intent, resolvedType, flags, userId), o -> ResolveInfoH.getComponentInfo(o).packageName);
  }

  @MethodHookBackup(minSdk = 32)
  private ParceledListSlice<ResolveInfo> queryIntentContentProviders(Intent intent, String resolvedType, long flags, int userId) {
    return filterParceledListSlice(queryIntentContentProviders(intent, resolvedType, flags, userId), o -> ResolveInfoH.getComponentInfo(o).packageName);
  }

  @MethodHookBackup(maxSdk = 32)
  private ResolveInfo resolveService(Intent intent, String resolvedType, int flags, int userId) {
    return checkResolveInfoInfo(resolveService(intent, resolvedType, flags, userId));
  }

  @MethodHookBackup(minSdk = 32)
  private ResolveInfo resolveService(Intent intent, String resolvedType, long flags, int userId) {
    return checkResolveInfoInfo(resolveService(intent, resolvedType, flags, userId));
  }


  private static ResolveInfo checkResolveInfoInfo(ResolveInfo resolveInfo) {
    ComponentInfo componentInfo = ResolveInfoH.getComponentInfo(resolveInfo);
    if (HideAppPlugin.hideApps.contains(componentInfo.packageName))
      return null;
    return resolveInfo;
  }

  @MethodHookBackup(maxSdk = 32)
  private ResolveInfo resolveIntent(Intent intent, String resolvedType, int flags, int userId) {
    ResolveInfo resolveInfo = resolveIntent(intent, resolvedType, flags, userId);
    return checkResolveInfoInfo(resolveInfo);
  }

  @MethodHookBackup(minSdk = 32)
  private ResolveInfo resolveIntent(Intent intent, String resolvedType, long flags, int userId) {
    ResolveInfo resolveInfo = resolveIntent(intent, resolvedType, flags, userId);
    return checkResolveInfoInfo(resolveInfo);
  }


  interface ParceledListGet<T> {
    String getPackageName(T object);
  }

  @MethodHookBackup(maxSdk = 32)
  private PackageInfo getPackageInfo(String packageName, int flags, int userId) throws PackageManager.NameNotFoundException {
    if (packageName != null && HideAppPlugin.hideApps.contains(packageName)) {
      throw new PackageManager.NameNotFoundException(packageName);
    }
    return getPackageInfo(packageName, flags, userId);
  }

  @MethodHookBackup(minSdk = 32)
  private PackageInfo getPackageInfo(String packageName, long flags, int userId) throws PackageManager.NameNotFoundException {
    if (packageName != null && HideAppPlugin.hideApps.contains(packageName)) {
      throw new PackageManager.NameNotFoundException(packageName);
    }
    return getPackageInfo(packageName, flags, userId);
  }

  static <T extends Parcelable> ParceledListSlice<T> filterParceledListSlice(ParceledListSlice<T> parceledList, ParceledListGet<T> get) {
    List<T> applicationInfos = parceledList.mList;
    if (applicationInfos != null) {
      int size = applicationInfos.size();
      if (size > 0) {
        for (int i = size - 1; i >= 0; i--) {
          T o = applicationInfos.get(i);
          if (o == null)
            continue;
          String app_pkg = get.getPackageName(o);
          if (app_pkg != null && HideAppPlugin.hideApps.contains(app_pkg)) {
            applicationInfos.remove(i);
          }
        }
      }
    }
    return parceledList;
  }


  @MethodHookBackup(maxSdk = 32)
  private ParceledListSlice<PackageInfo> getInstalledPackages(int flags, int userId) {
    ParceledListSlice<PackageInfo> parceledList = getInstalledPackages(flags, userId);
    if (parceledList == null)
      return null;
    return filterParceledListSlice(parceledList, o -> o.packageName);
  }

  @MethodHookBackup(minSdk = 32)
  private ParceledListSlice<PackageInfo> getInstalledPackages(long flags, int userId) {
    ParceledListSlice<PackageInfo> parceledList = getInstalledPackages(flags, userId);
    if (parceledList == null)
      return null;
    return filterParceledListSlice(parceledList, o -> o.packageName);
  }


  @MethodHookBackup
  private String getInstallerPackageName(String packageName) throws PackageManager.NameNotFoundException {
    if (packageName != null && HideAppPlugin.hideApps.contains(packageName)) {
      throw new PackageManager.NameNotFoundException(packageName);
    }
    return getInstallerPackageName(packageName);
  }
}
