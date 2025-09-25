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

import android.app.Application;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import qing.albatross.agent.AlbatrossPlugin;
import qing.albatross.core.Albatross;

public class HideAppPlugin extends AlbatrossPlugin {

  public static List<String> hideApps;

  public HideAppPlugin(String libName, String argString, int flags) {
    super(libName, argString, flags);
    hideApps = new ArrayList<>();
  }

  @Override
  public void beforeMakeApplication() {
    Albatross.log("HideAppPlugin beforeMakeApplication");
  }

  @Override
  public boolean load() {
    Albatross.log("HideAppPlugin load");
    return super.load();
  }

  @Override
  public boolean parseParams(String argString, int flags) {
    hideApps.clear();
    if (argString != null) {
      String[] packages = argString.split(",");
      Collections.addAll(hideApps, packages);
    }
    return true;
  }

  @Override
  public void onConfigChange(String config, int flags) {
    super.onConfigChange(config, flags);
    Application application = Albatross.currentApplication();
    if (application != null) {
      String targetPackage = application.getPackageName();
      hideApps.remove(targetPackage);
    }
  }

  @Override
  public void beforeApplicationCreate(Application application) {
    Albatross.log("HideAppPlugin beforeApplicationCreate");
    String targetPackage = application.getPackageName();
    hideApps.remove(targetPackage);
    IPackageManagerH.init();
  }

  @Override
  public void afterApplicationCreate(Application application) {
    Albatross.log("HideAppPlugin afterApplicationCreate");
  }
}
