/**
 * tv_focus.js — 电视遥控器焦点优化脚本
 * 注入到 WebView 中，让央视网页在电视遥控器上流畅操作
 */

(function () {
    'use strict';

    // ====== 配置 ======
    const CONFIG = {
        focusBorderColor: '#FF6600',
        focusBorderWidth: '4px',
        focusBoxShadow: '0 0 20px rgba(255,102,0,0.6)',
        scrollPadding: 100,  // 焦点元素离边缘多远时滚动
        progressInterval: 3000, // 进度查询间隔 (ms)
    };

    // ====== 样式注入 ======
    const style = document.createElement('style');
    style.textContent = `
        .tv-focus-highlight {
            outline: ${CONFIG.focusBorderWidth} solid ${CONFIG.focusBorderColor} !important;
            box-shadow: ${CONFIG.focusBoxShadow} !important;
            transition: outline 0.15s ease, box-shadow 0.15s ease;
            z-index: 9999;
            position: relative;
        }
        .tv-focusable {
            cursor: pointer;
        }
        @media (pointer: coarse) {
            /* 电视上禁用 :hover 效果，避免闪烁 */
            *:hover { outline: none !important; }
        }
    `;
    document.head.appendChild(style);

    // ====== 焦点管理 ======
    let currentFocusIndex = -1;
    let focusableElements = [];

    /** 获取所有可聚焦的元素 */
    function getFocusables() {
        // 央视网卡片常见选择器
        const selectors = [
            'a[href]',
            'a[onclick]',
            '.u-card',
            '.card',
            '.video-card',
            '[class*="card"]',
            '[class*="item"]',
            '[class*="list"] a',
            'li a',
            'button',
            '[tabindex]',
            'img[onclick]',
            '.focusable',
        ];
        const elements = new Set();
        selectors.forEach(sel => {
            document.querySelectorAll(sel).forEach(el => {
                if (el.offsetParent !== null) { // 只取可见元素
                    elements.add(el);
                }
            });
        });
        // 如果没有找到任何可聚焦元素，退化为所有链接
        if (elements.size === 0) {
            document.querySelectorAll('a[href]').forEach(el => elements.add(el));
        }
        return Array.from(elements);
    }

    /** 刷新可聚焦列表 */
    function refreshFocusables() {
        focusableElements = getFocusables();
        // 给每个元素添加 class 和 tabindex
        focusableElements.forEach((el, i) => {
            el.classList.add('tv-focusable');
            el.setAttribute('tabindex', '-1');
            el.dataset.tvIndex = i;
        });
    }

    /** 清除所有高亮 */
    function clearHighlight() {
        document.querySelectorAll('.tv-focus-highlight').forEach(el => {
            el.classList.remove('tv-focus-highlight');
        });
    }

    /** 高亮指定索引的元素 */
    function highlightIndex(index) {
        clearHighlight();
        if (index < 0 || index >= focusableElements.length) return false;
        const el = focusableElements[index];
        if (!el || el.offsetParent === null) return false;
        el.classList.add('tv-focus-highlight');
        el.scrollIntoView({
            block: 'nearest',
            behavior: 'smooth',
        });
        currentFocusIndex = index;
        return true;
    }

    /** 计算网格中的下一个焦点（按网格布局移动） */
    function getNextFocusIndex(direction) {
        if (focusableElements.length === 0) return -1;

        // 如果还没有焦点，聚焦第一个
        if (currentFocusIndex < 0) {
            return direction === 'up' ? focusableElements.length - 1 : 0;
        }

        const currentEl = focusableElements[currentFocusIndex];
        if (!currentEl) return 0;

        const currentRect = currentEl.getBoundingClientRect();
        const cx = currentRect.left + currentRect.width / 2;
        const cy = currentRect.top + currentRect.height / 2;

        let bestIndex = -1;
        let bestDistance = Infinity;

        focusableElements.forEach((el, i) => {
            if (i === currentFocusIndex) return;
            const rect = el.getBoundingClientRect();
            const ex = rect.left + rect.width / 2;
            const ey = rect.top + rect.height / 2;

            const dx = ex - cx;
            const dy = ey - cy;
            let matches = false;

            switch (direction) {
                case 'right':
                    matches = dx > 0 && Math.abs(dy) < Math.max(rect.height, 50);
                    break;
                case 'left':
                    matches = dx < 0 && Math.abs(dy) < Math.max(rect.height, 50);
                    break;
                case 'down':
                    matches = dy > 0 && Math.abs(dx) < Math.max(rect.width, 100);
                    break;
                case 'up':
                    matches = dy < 0 && Math.abs(dx) < Math.max(rect.width, 100);
                    break;
            }

            if (matches) {
                const dist = dx * dx + dy * dy;
                if (dist < bestDistance) {
                    bestDistance = dist;
                    bestIndex = i;
                }
            }
        });

        // 没找到合适的邻居，按列表顺序上下移动
        if (bestIndex === -1) {
            if (direction === 'down' || direction === 'right') {
                bestIndex = Math.min(currentFocusIndex + 1, focusableElements.length - 1);
            } else {
                bestIndex = Math.max(currentFocusIndex - 1, 0);
            }
        }

        return bestIndex;
    }

    /** 点击当前聚焦的元素 */
    function clickCurrent() {
        if (currentFocusIndex >= 0 && currentFocusIndex < focusableElements.length) {
            const el = focusableElements[currentFocusIndex];
            if (el) {
                el.click();
                return true;
            }
        }
        return false;
    }

    // ====== 键盘/遥控器事件拦截 ======
    document.addEventListener('keydown', function (e) {
        const key = e.key;
        // 只处理方向键和确认键
        if (!['ArrowUp', 'ArrowDown', 'ArrowLeft', 'ArrowRight', 'Enter'].includes(key)) {
            return;
        }

        // 如果焦点在输入框内，不拦截
        if (document.activeElement && document.activeElement.tagName === 'INPUT') {
            if (key === 'Enter') return;
            // 方向键让输入框失去焦点，回到卡片导航
            document.activeElement.blur();
            refreshFocusables();
            highlightIndex(0);
            e.preventDefault();
            return;
        }

        e.preventDefault();
        e.stopPropagation();

        if (key === 'Enter') {
            clickCurrent();
            return;
        }

        const direction = key.replace('Arrow', '').toLowerCase();
        refreshFocusables();
        const nextIndex = getNextFocusIndex(direction);
        if (nextIndex >= 0) {
            highlightIndex(nextIndex);
        }
    }, true);

    // ====== 滚动支持 ======
    let scrollTimeout = null;
    document.addEventListener('scroll', function () {
        if (scrollTimeout) clearTimeout(scrollTimeout);
        scrollTimeout = setTimeout(() => {
            // 滚动后重新计算焦点，保持高亮元素可见
            if (currentFocusIndex >= 0) {
                const el = focusableElements[currentFocusIndex];
                if (el && el.offsetParent !== null) {
                    el.classList.add('tv-focus-highlight');
                } else {
                    // 当前焦点不可见了，重新聚焦
                    refreshFocusables();
                    highlightIndex(currentFocusIndex);
                }
            }
        }, 200);
    }, true);

    // ====== 初始化 ======
    function init() {
        refreshFocusables();
        if (focusableElements.length > 0) {
            highlightIndex(0);
        }
    }

    // 页面加载完成后初始化
    if (document.readyState === 'complete') {
        init();
    } else {
        window.addEventListener('load', init);
        // SPA 动态加载：监听 DOM 变化
        const observer = new MutationObserver(function () {
            refreshFocusables();
            if (currentFocusIndex < 0 && focusableElements.length > 0) {
                highlightIndex(0);
            }
        });
        observer.observe(document.body, {
            childList: true,
            subtree: true,
        });
    }

    // ====== 导出接口给原生调用 ======
    window.TvFocus = {
        refresh: refreshFocusables,
        focusNext: () => highlightIndex(getNextFocusIndex('down')),
        focusPrev: () => highlightIndex(getNextFocusIndex('up')),
        focusLeft: () => highlightIndex(getNextFocusIndex('left')),
        focusRight: () => highlightIndex(getNextFocusIndex('right')),
        click: clickCurrent,
        reset: function () {
            currentFocusIndex = -1;
            refreshFocusables();
            if (focusableElements.length > 0) highlightIndex(0);
        },
    };

})();
