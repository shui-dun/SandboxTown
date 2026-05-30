/**
 * 边界柔化工具类
 *
 * 核心思路：
 * 1. 道路/墙壁：从已渲染精灵读取实际像素边界构建细粒度掩码（15px/格）。
 *    对掩码追踪轮廓→移动平均平滑（钝角）→在原始轮廓和平滑轮廓之间
 *    绘制背景色"侵蚀带"，覆盖精灵矩形边缘。
 * 2. 神庙/墓碑周围：纯装饰层，从 gameMap.data（30px）上采样到 15px，平滑填充。
 *
 * 调试模式：this.debug=true 绘制轮廓线（红=原始，蓝=平滑）
 *
 * 重要：掩码使用 15px 网格（gameMap 原始是 30px），因为精灵边缘不总是
 * 落在 30px 格线上。15px 网格将最大量化误差从 ±15px 降到 ±7.5px。
 */
export class BoundarySoftener {

    /**
     * @param {Phaser.Scene} scene
     * @param {Object} gameMap - { width(像素), height(像素), data[gridX][gridY] }
     * @param {Array} buildingList - 建筑列表
     * @param {number} bgColor - 背景色（16进制）
     */
    constructor(scene, gameMap, buildingList, bgColor) {
        this.scene = scene;
        this.gameMap = gameMap;
        this.buildingList = buildingList;
        this.bgColor = bgColor;

        /** 掩码网格的单元格大小（像素），小于 gameMap 的 30px 以获得更高精度 */
        this.maskCellSize = 15;

        /** 掩码网格尺寸 */
        this.maskWidth = Math.ceil(gameMap.width / this.maskCellSize);
        this.maskHeight = Math.ceil(gameMap.height / this.maskCellSize);

        /** gameMap 的格子大小（不变） */
        this.gameCellSize = 30;

        /** 平滑迭代次数 */
        this.smoothPasses = 3;

        /** 噪声幅度（掩码格单位） */
        this.noiseAmplitude = 0.4;

        /** 调试模式 */
        this.debug = false;
    }

    // ============================================================
    //  主流程
    // ============================================================

    process() {
        // 第一遍：装饰层（底层）
        const templeMask = this.buildMaskFromGameMap(4);
        this.processDecorativeMask(templeMask, 0xFFFFFF, 0.075, -0.7);

        const tombstoneMask = this.buildMaskFromGameMap(8);
        this.processDecorativeMask(tombstoneMask, 0x000000, 0.075, -0.7);

        // 第二遍：侵蚀层（上层）
        const roadMask = this.buildMaskFromSprites('ROAD');
        this.processErosionMask(roadMask, 0.5, false);

        const wallMask = this.buildMaskFromSprites('WALL');
        this.processErosionMask(wallMask, null, true);
    }

    // ============================================================
    //  掩码构建
    // ============================================================

    /**
     * 从 gameMap.data（30px 格）上采样到细粒度掩码（15px 格）
     * @param {number} bitValue - 位标记值
     * @returns {boolean[][]} mask[x][y] 15px 精度的掩码
     */
    buildMaskFromGameMap(bitValue) {
        const mask = [];
        const scale = this.gameCellSize / this.maskCellSize; // 30/15 = 2
        for (let x = 0; x < this.maskWidth; x++) {
            mask[x] = [];
            const gameX = Math.floor(x / scale);
            for (let y = 0; y < this.maskHeight; y++) {
                const gameY = Math.floor(y / scale);
                if (gameX < this.gameMap.data.length && gameY < this.gameMap.data[0].length) {
                    mask[x][y] = (this.gameMap.data[gameX][gameY] & bitValue) !== 0;
                } else {
                    mask[x][y] = false;
                }
            }
        }
        return mask;
    }

    /**
     * 从已渲染精灵的实际像素边界构建细粒度掩码（15px 格）
     *
     * 使用 Phaser 的 getBounds() 读取精灵在屏幕上的实际渲染边界，
     * 完全避免基于 buildingList 坐标推算时 centerOffset 不一致的误差。
     *
     * @param {string} type - 精灵纹理键
     * @returns {boolean[][]} mask[x][y] 15px 精度的掩码
     */
    buildMaskFromSprites(type) {
        const mask = [];
        for (let x = 0; x < this.maskWidth; x++) {
            mask[x] = new Array(this.maskHeight).fill(false);
        }

        let spriteCount = 0;
        for (const child of this.scene.children.list) {
            if (!child.texture || child.texture.key !== type) continue;
            spriteCount++;

            // getBounds() 返回 Phaser.Geom.Rectangle：{ x, y, width, height, right, bottom }
            // 这是精灵在世界坐标系中的实际渲染边界
            const bounds = child.getBounds();

            const startX = Math.floor(bounds.x / this.maskCellSize);
            const startY = Math.floor(bounds.y / this.maskCellSize);
            const endX = Math.ceil(bounds.right / this.maskCellSize);
            const endY = Math.ceil(bounds.bottom / this.maskCellSize);

            for (let gx = startX; gx < endX && gx < this.maskWidth; gx++) {
                for (let gy = startY; gy < endY && gy < this.maskHeight; gy++) {
                    if (gx >= 0 && gy >= 0) mask[gx][gy] = true;
                }
            }
        }
        console.log(`[BoundarySoftener] buildMaskFromSprites('${type}'): ${spriteCount} sprites → ${this.maskWidth}x${this.maskHeight} mask`);
        return mask;
    }

    // ============================================================
    //  道路/墙壁 —— 侵蚀带
    // ============================================================

    processErosionMask(mask, depth, perComponent) {
        const visited = [];
        for (let x = 0; x < this.maskWidth; x++) {
            visited[x] = new Array(this.maskHeight).fill(false);
        }

        let sharedGraphics = null;
        if (!perComponent && depth !== null) {
            sharedGraphics = this.scene.add.graphics();
            sharedGraphics.setDepth(depth);
            sharedGraphics.fillStyle(this.bgColor, 1.0);
        }

        for (let x = 0; x < this.maskWidth; x++) {
            for (let y = 0; y < this.maskHeight; y++) {
                if (!mask[x][y] || visited[x][y]) continue;

                const component = this.floodFill(mask, x, y, visited);
                if (component.length < 4) continue; // 15px 格，最小分量提高到4格

                const contours = this.traceContours(component);
                if (contours.length === 0) continue;

                const outerContour = contours.reduce((a, b) => a.length >= b.length ? a : b);
                if (outerContour.length < 4) continue;

                const smoothedContour = this.buildSmoothedContour(outerContour);

                if (perComponent) {
                    // 墙壁深度：回退到 30px 格计算 maxGridY（与原精灵深度一致）
                    const maxGridY30 = component.reduce((max, c) => Math.max(max, Math.floor(c.y * this.maskCellSize / this.gameCellSize)), 0);
                    const compDepth = (maxGridY30 + 1) * this.gameCellSize + 1;
                    const g = this.scene.add.graphics();
                    g.setDepth(compDepth);
                    g.fillStyle(this.bgColor, 1.0);
                    this.renderErosionStrip(g, outerContour, smoothedContour);
                } else {
                    this.renderErosionStrip(sharedGraphics, outerContour, smoothedContour);
                }

                if (this.debug) {
                    this.drawDebugOutlines(outerContour, smoothedContour, perComponent ? null : depth);
                }
            }
        }
    }

    /**
     * 从掩码轮廓构建平滑曲线轮廓
     * 移动平均消除尖角 + 只向内的噪声（永远不会超出原始轮廓）
     */
    buildSmoothedContour(original) {
        let smoothed = this.movingAverageSmooth(original, this.smoothPasses);
        const n = smoothed.length;

        // 对每个点计算向内法线，噪声只沿向内方向叠加
        const noised = [];
        for (let i = 0; i < n; i++) {
            const o = original[i];
            const prevO = original[(i - 1 + n) % n];
            const nextO = original[(i + 1) % n];

            // 入边左法线（顺时针多边形，左=内）
            const dxB = o.x - prevO.x, dyB = o.y - prevO.y;
            const lenB = Math.sqrt(dxB * dxB + dyB * dyB) || 1;
            const inNxB = -dyB / lenB, inNyB = dxB / lenB;

            // 出边左法线
            const dxA = nextO.x - o.x, dyA = nextO.y - o.y;
            const lenA = Math.sqrt(dxA * dxA + dyA * dyA) || 1;
            const inNxA = -dyA / lenA, inNyA = dxA / lenA;

            // 平均向内法线
            let inNx = inNxB + inNxA, inNy = inNyB + inNyA;
            const inLen = Math.sqrt(inNx * inNx + inNy * inNy) || 1;
            inNx /= inLen; inNy /= inLen;

            // 平滑位移在向内法线上的投影
            const s = smoothed[i];
            const sd = (s.x - o.x) * inNx + (s.y - o.y) * inNy;

            // 噪声始终向内（正值沿向内法线）
            const noiseMag = this.pseudoRandom(o.x * 137.5 + o.y * 251.7) * this.noiseAmplitude;

            // 最终位移 = max(平滑向内, 0) + 噪声
            const totalIn = Math.max(sd, 0) + noiseMag;

            noised.push({
                x: o.x + inNx * totalIn,
                y: o.y + inNy * totalIn,
            });
        }
        return noised;
    }

    movingAverageSmooth(points, passes) {
        let result = points;
        for (let pass = 0; pass < passes; pass++) {
            const n = result.length;
            const smoothed = [];
            for (let i = 0; i < n; i++) {
                const prev = result[(i - 1 + n) % n];
                const curr = result[i];
                const next = result[(i + 1) % n];
                smoothed.push({
                    x: prev.x * 0.25 + curr.x * 0.5 + next.x * 0.25,
                    y: prev.y * 0.25 + curr.y * 0.5 + next.y * 0.25,
                });
            }
            result = smoothed;
        }
        return result;
    }

    /**
     * 绘制侵蚀带：outer[i]→outer[i+1]→inner[i+1]→inner[i] 的四边形条带
     * 用背景色填充，覆盖精灵的矩形边缘，露出平滑曲线
     */
    renderErosionStrip(graphics, outer, inner) {
        const n = outer.length;
        const cs = this.maskCellSize;

        graphics.beginPath();
        for (let i = 0; i < n; i++) {
            const o0 = outer[i], o1 = outer[(i + 1) % n];
            const i0 = inner[i], i1 = inner[(i + 1) % n];
            graphics.moveTo(o0.x * cs, o0.y * cs);
            graphics.lineTo(o1.x * cs, o1.y * cs);
            graphics.lineTo(i1.x * cs, i1.y * cs);
            graphics.lineTo(i0.x * cs, i0.y * cs);
            graphics.closePath();
        }
        graphics.fillPath();
    }

    // ============================================================
    //  神庙/墓碑周围 —— 装饰层
    // ============================================================

    processDecorativeMask(mask, color, alpha, depth) {
        const visited = [];
        for (let x = 0; x < this.maskWidth; x++) {
            visited[x] = new Array(this.maskHeight).fill(false);
        }

        const graphics = this.scene.add.graphics();
        graphics.setDepth(depth);
        graphics.fillStyle(color, alpha);

        for (let x = 0; x < this.maskWidth; x++) {
            for (let y = 0; y < this.maskHeight; y++) {
                if (!mask[x][y] || visited[x][y]) continue;

                const component = this.floodFill(mask, x, y, visited);
                if (component.length < 4) continue;

                const contours = this.traceContours(component);
                if (contours.length === 0) continue;

                const outerContour = contours.reduce((a, b) => a.length >= b.length ? a : b);
                if (outerContour.length < 4) continue;

                const smoothed = this.buildSmoothedContour(outerContour);
                this.fillPolygon(graphics, smoothed);
            }
        }
    }

    // ============================================================
    //  洪水填充（四连通）
    // ============================================================

    floodFill(mask, startX, startY, visited) {
        const component = [];
        const stack = [{ x: startX, y: startY }];
        while (stack.length > 0) {
            const { x, y } = stack.pop();
            if (x < 0 || x >= this.maskWidth || y < 0 || y >= this.maskHeight) continue;
            if (visited[x][y] || !mask[x][y]) continue;
            visited[x][y] = true;
            component.push({ x, y });
            stack.push({ x: x - 1, y }, { x: x + 1, y }, { x, y: y - 1 }, { x, y: y + 1 });
        }
        return component;
    }

    // ============================================================
    //  轮廓追踪
    // ============================================================

    traceContours(component) {
        const compSet = new Set(component.map(c => `${c.x},${c.y}`));
        const edgeMap = new Map();

        for (const { x: cx, y: cy } of component) {
            if (!compSet.has(`${cx},${cy - 1}`)) edgeMap.set(`${cx},${cy}`, { x: cx + 1, y: cy });
            if (!compSet.has(`${cx + 1},${cy}`)) edgeMap.set(`${cx + 1},${cy}`, { x: cx + 1, y: cy + 1 });
            if (!compSet.has(`${cx},${cy + 1}`)) edgeMap.set(`${cx + 1},${cy + 1}`, { x: cx, y: cy + 1 });
            if (!compSet.has(`${cx - 1},${cy}`)) edgeMap.set(`${cx},${cy + 1}`, { x: cx, y: cy });
        }

        const contours = [];
        const usedEdges = new Set();
        for (const startKey of edgeMap.keys()) {
            if (usedEdges.has(startKey)) continue;
            const polygon = [];
            let currentKey = startKey;
            do {
                usedEdges.add(currentKey);
                const [sx, sy] = currentKey.split(',').map(Number);
                polygon.push({ x: sx, y: sy });
                const next = edgeMap.get(currentKey);
                if (!next) break;
                currentKey = `${next.x},${next.y}`;
            } while (currentKey !== startKey && !usedEdges.has(currentKey));
            if (currentKey === startKey && polygon.length >= 3) {
                contours.push(polygon);
            }
        }
        return contours;
    }

    // ============================================================
    //  调试
    // ============================================================

    drawDebugOutlines(outer, inner, sharedDepth) {
        const cs = this.maskCellSize;
        const debugDepth = (sharedDepth !== null && sharedDepth !== undefined)
            ? sharedDepth + 0.01
            : (Math.max(...outer.map(p => p.y)) + 1) * cs + 2;

        // 红色 = 原始掩码轮廓（应与精灵矩形边缘重合）
        const red = this.scene.add.graphics();
        red.setDepth(debugDepth + 0.01);
        red.lineStyle(2, 0xff0000, 0.8);
        red.beginPath();
        red.moveTo(outer[0].x * cs, outer[0].y * cs);
        for (let i = 1; i < outer.length; i++) red.lineTo(outer[i].x * cs, outer[i].y * cs);
        red.closePath();
        red.strokePath();

        // 蓝色 = 平滑轮廓
        const blue = this.scene.add.graphics();
        blue.setDepth(debugDepth + 0.02);
        blue.lineStyle(2, 0x0000ff, 0.8);
        blue.beginPath();
        blue.moveTo(inner[0].x * cs, inner[0].y * cs);
        for (let i = 1; i < inner.length; i++) blue.lineTo(inner[i].x * cs, inner[i].y * cs);
        blue.closePath();
        blue.strokePath();
    }

    // ============================================================
    //  工具
    // ============================================================

    pseudoRandom(seed) {
        let x = Math.sin(seed * 127.1 + 311.7) * 43758.5453;
        return x - Math.floor(x);
    }

    fillPolygon(graphics, points) {
        const cs = this.maskCellSize;
        graphics.beginPath();
        graphics.moveTo(points[0].x * cs, points[0].y * cs);
        for (let i = 1; i < points.length; i++) graphics.lineTo(points[i].x * cs, points[i].y * cs);
        graphics.closePath();
        graphics.fillPath();
    }
}
