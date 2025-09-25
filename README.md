# HideApp - Albatross Hook Framework Plugin

[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](LICENSE)
[![Author](https://img.shields.io/badge/author-QingWan-green.svg)](mailto:qingwanmail@foxmail.com)

A  Android app hiding plugin for the Albatross Hook Framework. This project provides two different approaches to hide applications from the system and other apps.

[中文版本](README_CN.md)

## 📋 Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Architecture](#architecture)
- [Installation](#installation)
- [Usage](#usage)
- [Configuration](#configuration)
- [Technical Details](#technical-details)
- [Contributing](#contributing)
- [License](#license)

## 🎯 Overview

HideApp is designed to work with the Albatross Hook Framework to provide application hiding capabilities on Android devices. It offers two distinct implementation approaches:

1. **System-Level Hook (`insystem`)** - Recommended for production use
2. **App-Level Hook (`inapp`)** - Ideal for learning and research purposes

## ✨ Features

### System-Level Hook (insystem)
- **Global App Hiding**: Hides apps system-wide for all applications
- **Per-App Rules**: Configure hiding rules for specific applications
- **System Server Integration**: Hooks into PackageManagerService for comprehensive coverage
- **UID-Based Filtering**: Uses UID-based rules for precise control
- **Production Ready**: Stable and reliable for daily use

### App-Level Hook (inapp)
- **Targeted Hiding**: Hides apps only for the specific application using the plugin
- **Educational Value**: Perfect for understanding app-level hooking techniques
- **Package Manager Hooks**: Intercepts PackageManager API calls
- **Research Tool**: Ideal for studying how app-level hooks work

## 🏗️ Architecture

```
HideApp/
├── inapp/                    # App-level hook implementation
│   ├── HideAppPlugin.java    # Main plugin class
│   ├── IPackageManagerH.java # PackageManager hook implementation
│   └── PluginConfigActivity.java # Configuration UI
├── insystem/                 # System-level hook implementation
│   ├── HideAppSystemPlugin.java # Main plugin class
│   ├── PackageManagerServiceH.java # System service hooks
│   ├── ComputerEngineH.java  # Additional system hooks
│   ├── AppsFilterBaseH.java  # App filtering hooks
│   └── PluginConfigActivity.java # Configuration UI
└── lib/
    └── albatross.jar         # Albatross framework library
```

## 🚀 Installation

### Prerequisites
- Android device with root access
- AlbatrossManager installed
- Android Studio 2025.1.2 Patch 2 or compatible IDE

### Build Instructions

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd HideApp
   ```

2. **Build the project**
   ```bash
   ./gradlew build
   ```

3. **Install the plugins**
   ```bash
   # Install system-level plugin (recommended)
   adb install insystem/build/outputs/apk/release/insystem-release.apk
   
   # Or install app-level plugin (for research)
   adb install inapp/build/outputs/apk/release/inapp-release.apk
   ```

## 📱 Usage

### System-Level Plugin (Recommended)

1. **Install the system plugin to device** and add plugin in AlbatrossManager
2. **Configure hiding rules** using the format: `package1:rule1,rule2|package2:rule3,rule4`
3. **Set target applications** that should have these hiding rules applied
4. **Activate the plugin** through AlbatrossManager

### App-Level Plugin (Research)

1. **Install the app plugin to device** and add plugin in AlbatrossManager
2. **Configure package list** using comma-separated package names
3. **Apply to specific app** that needs hiding functionality
4. **Test and observe** how app-level hooks work

## ⚙️ Configuration

### System-Level Configuration

**Rule Format**: `packageName:hideRule1,hideRule2|packageName2:hideRule3`

**Example**:
```
com.example.app1:com.target.app1,com.target.app2|com.example.app2:com.target.app3
```

This means:
- `com.example.app1` will hide `com.target.app1` and `com.target.app2`
- `com.example.app2` will hide `com.target.app3`

### App-Level Configuration

**Package List**: Comma-separated package names to hide

**Example**:
```
com.target.app1,com.target.app2,com.target.app3
```

## 🔧 Technical Details

### System-Level Hooks

The system-level implementation hooks into:

- **PackageManagerService**: Core package management service
- **ComputerEngine**: Package resolution engine
- **AppsFilterBase**: Application filtering system

**Key Methods Hooked**:
- `applyPostResolutionFilter()` - Filters resolved intents
- `filterAppAccessLPr()` - Controls app access permissions
- `getInstalledApplicationsListInternal()` - Filters installed apps list
- `getInstalledPackages()` - Filters package information

### App-Level Hooks

The app-level implementation hooks into:

- **IPackageManager**: Package manager interface
- **ResolveInfo**: Intent resolution information

**Key Methods Hooked**:
- `queryIntentActivities()` - Filters activity queries
- `queryIntentServices()` - Filters service queries
- `queryIntentReceivers()` - Filters receiver queries
- `getPackageInfo()` - Filters package information
- `getInstalledPackages()` - Filters installed packages

### Hook Framework Integration

Both plugins extend `AlbatrossPlugin` and implement:

- `load()` - Plugin initialization
- `parseParams()` - Parameter parsing
- `beforeApplicationCreate()` - Pre-application setup
- `onConfigChange()` - Configuration updates

## 🎓 Learning Resources

### Understanding App-Level Hooks

The `inapp` module serves as an excellent learning resource for:

1. **PackageManager API Hooking**: Learn how to intercept package queries
2. **Intent Resolution Hooking**: Understand how to filter intent resolution
3. **App-Level Security**: Study app-level hiding techniques
4. **Hook Implementation**: See practical examples of hook implementation

### Key Learning Points

- How to hook PackageManager methods
- Intent resolution filtering techniques
- ParceledListSlice manipulation
- ComponentInfo extraction and filtering
- Error handling in hook implementations


### Development Guidelines

1. Follow the existing code style
2. Add appropriate comments for complex logic
3. Test both system and app-level implementations
4. Update documentation for new features

## 📄 License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

---
