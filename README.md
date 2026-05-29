# 央视动画 TV

Android TV 应用 — 观看央视动画片片库，支持断点续播和语音搜索。

## 功能

| 功能 | 说明 |
|---|---|
| 🎬 动画片 | 浏览央视动画片库 |
| 📺 电视剧 / 纪录片 | 同样支持 |
| ▶️ 继续观看 | 显示有播放进度的视频 |
| 🔍 语音搜索 | 按住遥控器麦克风键说话搜片 |
| ⏸ 断点续播 | 自动记录进度，下次恢复播放 |
| 🎮 遥控器优化 | 蓝色高亮框，方向键网格移动 |

## 技术架构

```
原生 Leanback 导航栏（分类切换）
      │
      ▼
WebView 加载央视网页内容
      │
      ▼
注入 JS 优化（焦点 + 播放监听）
      │
      ▼
Room 数据库（观看历史 + 搜索历史）
```

## 如何获取 APK（不需要安装任何工具）

### 第 1 步：注册 GitHub

打开 https://github.com 注册账号（免费，5 分钟）

### 第 2 步：新建仓库

登录后点右上角 `+` → **New repository**
- 仓库名随意，比如 `cctv-tv-app`
- 权限选 **Public**（免费）
- 点 **Create repository**

### 第 3 步：上传代码

在新仓库页面点 **uploading an existing file**
- 把 `android-tv-app/` 文件夹里的**所有文件和文件夹**拖进去
- 注意要包含 `.github/workflows/build.yml` 这个文件
- 点 **Commit changes**

### 第 4 步：自动编译 APK

上传完成后
- 点顶部 **Actions** 标签
- 左侧点 **Build Android TV APK**
- 右边点 **Run workflow** → **Run workflow**
- 等 3-5 分钟，出现绿色 ✅ 表示编译完成

### 第 5 步：下载 APK

- 在绿色 ✅ 的 workflows 列表里点进去
- 往下翻到 **Artifacts** 区域
- 点 **CCTV-TV-App-Debug** 下载 ZIP
- 解压得到 `app-debug.apk`

### 第 6 步：安装到索尼电视

方式一（最简单）：U 盘
1. APK 文件复制到 U 盘
2. U 盘插到电视 USB 口
3. 电视上打开"文件管理器"或"当贝市场"
4. 找到 APK 安装

方式二（需要电脑）：ADB
```
adb connect 电视IP地址
adb install app-debug.apk
```

> 索尼电视开启开发者模式：设置 → 设备偏好设置 → 关于 → 连续点"版本号"7次

## 项目结构

```
android-tv-app/
├── .github/workflows/build.yml   # GitHub Actions 编译脚本
├── build.gradle.kts               # 项目级构建
├── settings.gradle.kts
├── gradle.properties
├── app/
│   ├── build.gradle.kts           # 模块构建（依赖配置）
│   └── src/main/
│       ├── AndroidManifest.xml    # TV 声明
│       ├── assets/
│       │   ├── tv_focus.js        # 遥控器焦点优化
│       │   └── tv_player.js       # 播放进度监听
│       ├── java/com/example/tvapp/
│       │   ├── MainActivity.kt    # 入口
│       │   ├── MainFragment.kt    # 分类导航
│       │   ├── WebViewFragment.kt # WebView 内容
│       │   ├── ContinueWatchingFragment.kt  # 继续观看
│       │   ├── SearchFragment.kt  # 搜索页面
│       │   ├── SearchActivity.kt  # 搜索入口
│       │   ├── tv/
│       │   │   └── TvWebViewClient.kt
│       │   ├── js/
│       │   │   └── JsBridge.kt    # JS ↔ 原生通信
│       │   └── data/
│       │       ├── AppDatabase.kt
│       │       ├── HistoryDao.kt
│       │       ├── WatchHistory.kt
│       │       └── SearchHistory.kt
│       └── res/                   # 资源文件
```

## 调试提示

- 电视上安装后如果白屏，检查电视网络是否能访问 `tv.cctv.com`
- 部分索尼电视需要开启"允许未知来源应用"
- 首次打开加载较慢，央视页面需要时间渲染
