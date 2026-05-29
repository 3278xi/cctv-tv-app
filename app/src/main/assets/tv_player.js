/**
 * tv_player.js — 视频播放进度监听脚本
 * 注入到央视播放页面，监听 video 进度并传给原生层实现断点续播
 */

(function () {
    'use strict';

    const CONFIG = {
        saveInterval: 10000,  // 每 10 秒保存一次进度
        checkInterval: 2000,  // 每 2 秒检查视频元素是否出现
        maxRetries: 30,       // 最多等 60 秒
    };

    // ====== 进度管理 ======
    let videoElement = null;
    let saveTimer = null;
    let checkTimer = null;
    let retryCount = 0;
    let lastSavedTime = 0;
    let videoUrl = window.location.href;
    let videoTitle = document.title;

    /** 从页面提取视频标题 */
    function extractTitle() {
        const selectors = [
            'h1',
            '.video-title',
            '[class*="title"]',
            '.program-name',
            'meta[property="og:title"]',
        ];
        for (const sel of selectors) {
            const el = document.querySelector(sel);
            if (el) {
                if (el.tagName === 'META') return el.getAttribute('content') || '';
                return el.textContent.trim();
            }
        }
        return document.title;
    }

    /** 从页面提取视频封面 */
    function extractCover() {
        const selectors = [
            'meta[property="og:image"]',
            'video[poster]',
            '.video-poster img',
            '[class*="poster"] img',
            '[class*="cover"] img',
        ];
        for (const sel of selectors) {
            const el = document.querySelector(sel);
            if (el) {
                if (el.tagName === 'META') return el.getAttribute('content') || '';
                if (el.tagName === 'VIDEO') return el.getAttribute('poster') || '';
                return el.getAttribute('src') || '';
            }
        }
        return '';
    }

    /** 查找页面中的 video 元素 */
    function findVideo() {
        // 央视网常用播放器
        const selectors = [
            'video',
            '#videoId',
            '#player video',
            '.ckplayer-video',
            '.video-player video',
            '[class*="player"] video',
            'video[src]',
            'video[controls]',
        ];
        for (const sel of selectors) {
            const el = document.querySelector(sel);
            if (el && el.tagName === 'VIDEO' && el.readyState > 0) {
                return el;
            }
        }
        // 放宽条件：任何 video 标签
        const allVideos = document.querySelectorAll('video');
        for (const v of allVideos) {
            if (v.readyState > 0) return v;
        }
        return null;
    }

    /** 保存进度到原生层 */
    function saveProgress() {
        if (!videoElement || !videoElement.duration) return;

        const currentTime = videoElement.currentTime;
        const duration = videoElement.duration;

        // 进度变化超过 5 秒才保存，避免频繁写入
        if (Math.abs(currentTime - lastSavedTime) < 5) return;
        lastSavedTime = currentTime;

        // 通过 JsBridge 传给原生
        if (window.AndroidBridge && window.AndroidBridge.saveProgress) {
            window.AndroidBridge.saveProgress(
                videoUrl,
                videoTitle,
                extractCover(),
                Math.floor(currentTime),
                Math.floor(duration)
            );
        }
    }

    /** 尝试恢复上次播放进度 */
    function restoreProgress() {
        if (!videoElement) return;

        if (window.AndroidBridge && window.AndroidBridge.getProgress) {
            const progress = window.AndroidBridge.getProgress(videoUrl);
            if (progress && progress.position > 10) {  // 超过 10 秒才恢复
                videoElement.currentTime = progress.position;
                // 通知原生层显示提示
                if (window.AndroidBridge && window.AndroidBridge.showResumeTip) {
                    window.AndroidBridge.showResumeTip(
                        progress.title,
                        Math.floor(progress.position)
                    );
                }
            }
        }
    }

    /** 绑定视频事件 */
    function bindVideo(video) {
        if (videoElement === video) return;
        videoElement = video;

        videoTitle = extractTitle();

        // 视频元数据加载完成后恢复进度
        video.addEventListener('loadedmetadata', function onMeta() {
            restoreProgress();
            video.removeEventListener('loadedmetadata', onMeta);
        });

        // 视频播放时定期保存进度
        video.addEventListener('play', function () {
            if (saveTimer) clearInterval(saveTimer);
            saveTimer = setInterval(saveProgress, CONFIG.saveInterval);
        });

        video.addEventListener('pause', function () {
            // 暂停时立即保存一次
            saveProgress();
            if (saveTimer) {
                clearInterval(saveTimer);
                saveTimer = null;
            }
        });

        video.addEventListener('seeked', function () {
            // 跳转后保存
            saveProgress();
        });

        // 页面关闭/离开前保存
        window.addEventListener('beforeunload', function () {
            saveProgress();
        });

        // 如果视频已经开始播放，立即恢复进度
        if (video.readyState >= 1) {
            setTimeout(restoreProgress, 500);
        }

        console.log('[TVPlayer] 已绑定视频播放器');
    }

    /** 轮询查找视频元素 */
    function startWatching() {
        if (checkTimer) return;

        checkTimer = setInterval(function () {
            const video = findVideo();
            if (video) {
                bindVideo(video);
                clearInterval(checkTimer);
                checkTimer = null;
                return;
            }

            retryCount++;
            if (retryCount >= CONFIG.maxRetries) {
                clearInterval(checkTimer);
                checkTimer = null;
                console.log('[TVPlayer] 未找到视频元素，停止重试');
            }
        }, CONFIG.checkInterval);
    }

    // ====== 初始化 ======
    function init() {
        // 立即尝试一次
        const video = findVideo();
        if (video) {
            bindVideo(video);
            return;
        }
        // 没找到则轮询等待
        startWatching();
    }

    if (document.readyState === 'complete') {
        init();
    } else {
        window.addEventListener('load', init);
    }

    // ====== 导出接口 ======
    window.TvPlayer = {
        getProgress: function () {
            if (videoElement && videoElement.duration) {
                return {
                    currentTime: videoElement.currentTime,
                    duration: videoElement.duration,
                };
            }
            return null;
        },
        seekTo: function (seconds) {
            if (videoElement) {
                videoElement.currentTime = seconds;
            }
        },
        play: function () {
            if (videoElement) videoElement.play();
        },
        pause: function () {
            if (videoElement) videoElement.pause();
        },
    };

})();
