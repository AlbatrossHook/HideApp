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

import java.util.List;

import qing.albatross.annotation.DefOption;
import qing.albatross.annotation.FuzzyMatch;
import qing.albatross.annotation.MethodBackup;
import qing.albatross.annotation.MethodHookBackup;
import qing.albatross.annotation.TargetClass;
import qing.albatross.core.Albatross;
import qing.albatross.exception.AlbatrossErr;

@TargetClass(className = "com.android.server.pm.AppsFilterBase")
public class AppsFilterBaseH {

  static class PackageImpl {
    @MethodBackup(option = DefOption.VIRTUAL)
    private static native String getPackageName(Object self);

  }

  @TargetClass
  static class PackageStateInternal {
    @MethodBackup(option = DefOption.VIRTUAL)
    private static native Object getPkg(Object self);
  }

  static Boolean initHook;


  @MethodHookBackup
  private boolean shouldFilterApplication(@FuzzyMatch Object snapshot, int callingUid,
                                          Object callingSetting, @FuzzyMatch Object targetPkgSetting, int userId) {

    if (targetPkgSetting != null) {
      List<String> pkgs = HideAppSystemPlugin.hideRules.get(callingUid);
      if (pkgs != null) {
        if (!Albatross.isHooked(PackageStateInternal.class)) {
          try {
            Albatross.hookObject(PackageStateInternal.class, targetPkgSetting);
            Object pkg = PackageStateInternal.getPkg(targetPkgSetting);
            Albatross.hookObject(PackageImpl.class, pkg);
            String target = PackageImpl.getPackageName(pkg);
            initHook = true;
          } catch (AlbatrossErr e) {
            initHook = false;
          }
        }
        if (initHook) {
          Object pkg = PackageStateInternal.getPkg(targetPkgSetting);
          String target = PackageImpl.getPackageName(pkg);
          return pkgs.contains(target);
        }
      }
    }
    return shouldFilterApplication(snapshot, callingUid, callingSetting, targetPkgSetting, userId);
  }

}


