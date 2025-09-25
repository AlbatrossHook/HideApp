
# HideApp - Albatross Hook 框架插件

[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](LICENSE)
[![Author](https://img.shields.io/badge/author-QingWan-green.svg)](mailto:qingwanmail@foxmail.com)

Albatross应用隐藏插件，专为Albatross Hook 框架设计,适用于AlbatrossManager和AlbatrossServer。本项目提供了两种不同的方法来隐藏系统和其他应用中的应用。

## 📋 目录

- [概述](#概述)
- [功能特性](#功能特性)
- [架构设计](#架构设计)
- [安装指南](#安装指南)
- [使用方法](#使用方法)
- [配置说明](#配置说明)
- [技术细节](#技术细节)
- [学习资源](#学习资源)
- [贡献指南](#贡献指南)
- [许可证](#许可证)

## 🎯 概述

HideApp 专为与 Albatross Hook 框架配合使用而设计，在 Android 设备上提供应用隐藏功能。它提供两种不同的实现方法：

1. **系统级 Hook (`insystem`)** - 推荐用于生产环境
2. **应用级 Hook (`inapp`)** - 适合学习和研究用途

## ✨ 功能特性

### 系统级 Hook (insystem)
- **全局应用隐藏**: 为所有应用系统级隐藏应用
- **按应用规则**: 为特定应用配置隐藏规则
- **系统服务集成**: 钩入 PackageManagerService 实现全面覆盖
- **基于 UID 的过滤**: 使用 UID 规则进行精确控制
- **生产就绪**: 稳定可靠，适合日常使用

### 应用级 Hook (inapp)
- **定向隐藏**: 仅对使用插件的特定应用隐藏应用
- **教育价值**: 完美理解应用级钩子技术
- **包管理器钩子**: 拦截 PackageManager API 调用
- **研究工具**: 理想的学习应用级钩子工作原理

## 🏗️ 架构设计

```
HideApp/
├── inapp/                    # 应用级钩子实现
│   ├── HideAppPlugin.java    # 主插件类
│   ├── IPackageManagerH.java # PackageManager 钩子实现
│   └── PluginConfigActivity.java # 配置界面
├── insystem/                 # 系统级钩子实现
│   ├── HideAppSystemPlugin.java # 主插件类
│   ├── PackageManagerServiceH.java # 系统服务钩子
│   ├── ComputerEngineH.java  # 额外系统钩子
│   ├── AppsFilterBaseH.java  # 应用过滤钩子
│   └── PluginConfigActivity.java # 配置界面
└── lib/
    └── albatross.jar         # Albatross 框架库
```

## 🚀 安装指南

### 前置要求
- 具有 root 权限的 Android 设备
- 已安装 AlbatrossManager
- Android Studio 2025.1.2 Patch 2 或兼容的 IDE

### 构建说明

1. **克隆仓库**
   ```bash
   git clone https://github.com/AlbatrossHook/HideApp.git
   cd HideApp
   ```

2. **构建项目**
   ```bash
   ./gradlew build
   ```

3. **安装插件**
   ```bash
   # 安装系统级插件（推荐）
   adb install insystem/build/outputs/apk/release/insystem-release.apk
   
   # 或安装应用级插件（用于研究）
   adb install inapp/build/outputs/apk/release/inapp-release.apk
   ```

## 📱 使用方法

### 系统级插件（推荐）

1. **安装app到设备上，在AlbatrossManager上添加插件**
2. AlbatrossManager进入插件详情页面，点击打开插件进行配置,**配置隐藏规则** 使用格式：`package1:rule1,rule2|package2:rule3,rule4`
3. **设置目标应用** 应该应用这些隐藏规则的应用
4. **通过 AlbatrossManager激活插件**

### 应用级插件（研究）

1. **安装app到设备上，在AlbatrossManager上添加插件**
2. AlbatrossManager进入插件详情页面，点击打开插件进行配置
3. **应用到特定应用** 需要隐藏功能的应用
4. **通过 AlbatrossManager激活插件**

## ⚙️ 配置说明

在AlbatrossManager中可手动配置，但容易输错，不推荐，最好在AlbatrossManager中跳转到插件中配置，返回的配置信息AlbatrossManager会保存

### 系统级配置

**规则格式**: `packageName:hideRule1,hideRule2|packageName2:hideRule3`

**示例**:
```
com.example.app1:com.target.app1,com.target.app2|com.example.app2:com.target.app3
```

这意味着：
- `com.example.app1` 将隐藏 `com.target.app1` 和 `com.target.app2`
- `com.example.app2` 将隐藏 `com.target.app3`

### 应用级配置

**包列表**: 要隐藏的逗号分隔包名

**示例**:
```
com.target.app1,com.target.app2,com.target.app3
```

## 🔧 技术细节

### 系统级钩子

系统级实现钩入：

- **PackageManagerService**: 核心包管理服务
- **ComputerEngine**: 包解析引擎
- **AppsFilterBase**: 应用过滤系统

**钩子的关键方法**:
- `applyPostResolutionFilter()` - 过滤已解析的意图
- `filterAppAccessLPr()` - 控制应用访问权限
- `getInstalledApplicationsListInternal()` - 过滤已安装应用列表
- `getInstalledPackages()` - 过滤包信息

### 应用级钩子

应用级实现钩入：

- **IPackageManager**: 包管理器接口
- **ResolveInfo**: 意图解析信息

**钩子的关键方法**:
- `queryIntentActivities()` - 过滤活动查询
- `queryIntentServices()` - 过滤服务查询
- `queryIntentReceivers()` - 过滤接收器查询
- `getPackageInfo()` - 过滤包信息
- `getInstalledPackages()` - 过滤已安装包

### 钩子框架集成

两个插件都扩展了 `AlbatrossPlugin` 并实现：

- `load()` - 插件初始化
- `parseParams()` - 参数解析
- `beforeApplicationCreate()` - 应用前设置
- `onConfigChange()` - 配置更新

## 🎓 学习资源

### 理解应用级钩子

`inapp` 模块是学习以下内容的绝佳资源：

1. **PackageManager API 钩子**: 学习如何拦截包查询
2. **意图解析钩子**: 理解如何过滤意图解析
3. **应用级安全**: 研究应用级隐藏技术
4. **钩子实现**: 查看钩子实现的实际示例

### 关键学习要点

- 如何钩子 PackageManager 方法
- 意图解析过滤技术
- ParceledListSlice 操作
- ComponentInfo 提取和过滤
- 钩子实现中的错误处理

## 📄 许可证

本项目采用 Apache License 2.0 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。

