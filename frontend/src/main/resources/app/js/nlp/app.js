var __extends = (this && this.__extends) || function (d, b) {
    for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p];
    function __() { this.constructor = d; }
    d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
};
define("annotations/util", ["require", "exports"], function (require, exports) {
    "use strict";
    var Util;
    (function (Util) {
        var SpatialItem = (function () {
            function SpatialItem(id, minX, maxX, value) {
                this.id = id;
                this.value = value;
                this.minX = minX;
                this.maxX = maxX;
            }
            return SpatialItem;
        }());
        Util.SpatialItem = SpatialItem;
        var SpatialContainer = (function () {
            function SpatialContainer() {
                this.minX = Number.POSITIVE_INFINITY;
                this.maxX = Number.NEGATIVE_INFINITY;
                this.items = [];
            }
            SpatialContainer.prototype.add = function (item) {
                this.minX = Math.min(item.minX, this.minX);
                this.maxX = Math.max(item.maxX, this.maxX);
                this.items.push(item);
            };
            return SpatialContainer;
        }());
        var SparseGridSpatialIndex = (function () {
            function SparseGridSpatialIndex(minX, maxX, numBuckets) {
                this.buckets = {};
                this.idCnt = 0;
                this.minX = minX;
                this.maxX = maxX;
                this.range = maxX - minX;
                this.numBuckets = numBuckets;
            }
            SparseGridSpatialIndex.prototype.add = function (minX, maxX, elem) {
                var u = Math.floor((minX - this.minX) / this.range * this.numBuckets);
                var v = Math.floor((maxX - this.minX) / this.range * this.numBuckets);
                var startBucket = Math.min(this.numBuckets - 1, Math.max(0, u));
                var endBucket = Math.min(this.numBuckets - 1, Math.max(0, v)) + 1;
                for (var k = startBucket; k < endBucket; k++) {
                    var container = this.buckets[k];
                    if (container === undefined) {
                        container = new SpatialContainer();
                        this.buckets[k] = container;
                    }
                    container.add(new SpatialItem(this.idCnt, minX, maxX, elem));
                    this.idCnt += 1;
                }
            };
            SparseGridSpatialIndex.prototype.anyIntersection = function (minX, maxX) {
                var u = Math.floor((minX - this.minX) / this.range * this.numBuckets);
                var v = Math.floor((maxX - this.minX) / this.range * this.numBuckets);
                var startBucket = Math.min(this.numBuckets - 1, Math.max(0, u));
                var endBucket = Math.min(this.numBuckets - 1, Math.max(0, v)) + 1;
                var added = {};
                var result = [];
                for (var k = startBucket; k < endBucket; k++) {
                    var container = this.buckets[k];
                    if (container !== undefined) {
                        if (minX < container.maxX && maxX > container.minX) {
                            for (var _i = 0, _a = container.items; _i < _a.length; _i++) {
                                var item = _a[_i];
                                if (minX < item.maxX && maxX > item.minX) {
                                    return true;
                                }
                            }
                        }
                    }
                }
                return false;
            };
            SparseGridSpatialIndex.prototype.intersections = function (minX, maxX) {
                var u = Math.floor((minX - this.minX) / this.range * this.numBuckets);
                var v = Math.floor((maxX - this.minX) / this.range * this.numBuckets);
                var startBucket = Math.min(this.numBuckets - 1, Math.max(0, u));
                var endBucket = Math.min(this.numBuckets - 1, Math.max(0, u)) + 1;
                var added = {};
                var result = [];
                for (var k = startBucket; k < endBucket; k++) {
                    var container = this.buckets[k];
                    if (container !== undefined) {
                        if (minX < container.maxX && maxX > container.minX) {
                            for (var _i = 0, _a = container.items; _i < _a.length; _i++) {
                                var item = _a[_i];
                                if (added[item.id] !== undefined) {
                                    if (minX < item.maxX && maxX > item.minX) {
                                        result.push(item);
                                        added[item.id] = true;
                                    }
                                }
                            }
                        }
                    }
                }
                return result;
            };
            return SparseGridSpatialIndex;
        }());
        Util.SparseGridSpatialIndex = SparseGridSpatialIndex;
        function rgb(r, g, b) {
            return { r: r, g: g, b: b };
        }
        Util.rgb = rgb;
        function hsl(h, s, l) {
            return { h: h, s: s, l: l };
        }
        Util.hsl = hsl;
        function mixRgb(x, y) {
            return { r: (x.r + y.r) / 2.0,
                g: (x.g + y.g) / 2.0,
                b: (x.b + y.b) / 2.0 };
        }
        Util.mixRgb = mixRgb;
        function rightTriangle(w, h) {
            return [0, 0, 0, h, w, h / 2];
        }
        Util.rightTriangle = rightTriangle;
        function leftTriangle(w, h) {
            return [w, 0, w, h, 0, h / 2];
        }
        Util.leftTriangle = leftTriangle;
        function shuffle(array) {
            var cnt = array.length;
            while (cnt > 0) {
                var idx = Math.floor(Math.random() * cnt);
                cnt--;
                var temp = array[cnt];
                array[cnt] = array[idx];
                array[idx] = temp;
            }
            return array;
        }
        Util.shuffle = shuffle;
        function randomRgb() {
            return {
                r: Math.random(),
                g: Math.random(),
                b: Math.random()
            };
        }
        Util.randomRgb = randomRgb;
        function darkerHsl(hsl, amount) {
            var fact = amount || 0.5;
            fact = 1.0 - fact;
            return { h: hsl.h, s: hsl.s, l: hsl.l * fact };
        }
        Util.darkerHsl = darkerHsl;
        function mulHsl(x, y) {
            return { h: x.h * y.h, s: x.s * y.s, l: x.l * y.l };
        }
        Util.mulHsl = mulHsl;
        function generatePastelColors(n) {
            if (n < 180) {
                var result = [];
                var offset = Math.random() * 360.0;
                var deltaOffset = 360.0 / n;
                for (var k = 0; k < n; k++) {
                    var startOffset = offset + (deltaOffset * k);
                    var h = startOffset + (deltaOffset / 2.0) + (2.0 * Math.random() - 1.0) * (deltaOffset / 8.0);
                    if (h >= 360.0) {
                        h -= 360.0;
                    }
                    var s = Math.random() * 0.5 + 0.5;
                    var l = 0.85;
                    result.push(hsl(h, s, l));
                }
                return shuffle(result);
            }
            else {
                var result = [];
                var white = rgb(1.0, 1.0, 1.0);
                for (var i = 0; i < n; i++) {
                    result.push({
                        h: Math.random() * 360.0,
                        s: Math.random() * 0.5 + 0.5,
                        l: Math.random() * 0.125 + 0.75
                    });
                }
                return result;
            }
        }
        Util.generatePastelColors = generatePastelColors;
        function hsl2rgb(hc) {
            var h = hc.h / 360.0, s = hc.s, l = hc.l;
            var r, g, b;
            if (s === 0) {
                r = g = b = l;
            }
            else {
                var hue2rgb = function hue2rgb(p, q, t) {
                    if (t < 0)
                        t += 1;
                    if (t > 1)
                        t -= 1;
                    if (t < 1 / 6)
                        return p + (q - p) * 6 * t;
                    if (t < 1 / 2)
                        return q;
                    if (t < 2 / 3)
                        return p + (q - p) * (2 / 3 - t) * 6;
                    return p;
                };
                var q = l < 0.5 ? l * (1 + s) : l + s - l * s;
                var p = 2 * l - q;
                r = hue2rgb(p, q, h + 1 / 3);
                g = hue2rgb(p, q, h);
                b = hue2rgb(p, q, h - 1 / 3);
            }
            return rgb(r, g, b);
        }
        Util.hsl2rgb = hsl2rgb;
        function rgb2hsl(c) {
            var r = c.r;
            var g = c.g;
            var b = c.b;
            var cmax = Math.max(r, Math.max(g, b));
            var cmin = Math.min(r, Math.min(g, b));
            var delta = cmax - cmin;
            var eps = 1e-7;
            var h = 0.0;
            var l = (cmax + cmin) / 2.0;
            var s = 0.0;
            if (delta >= -eps && delta < eps) {
                h = 0.0;
            }
            else if (cmax === r) {
                h = (((g - b) / delta) % 6.0) * 60.0;
                s = delta / (1.0 - Math.floor(2.0 * l - 1.0));
            }
            else if (cmax === g) {
                h = (((b - r) / delta) + 2) * 60.0;
                s = delta / (1.0 - Math.floor(2.0 * l - 1.0));
            }
            else if (cmax === b) {
                h = (((r - g) / delta) + 4) * 60.0;
                s = delta / (1.0 - Math.floor(2.0 * l - 1.0));
            }
            return {
                h: h,
                s: s,
                l: l
            };
        }
        Util.rgb2hsl = rgb2hsl;
        function hex2rgb(hex) {
            var r, g, b;
            if (hex.length === 3) {
                r = hex[0] + hex[0];
                g = hex[1] + hex[1];
                g = hex[2] + hex[2];
            }
            else if (hex.length !== 6) {
                throw new Error("Invalid hex passed to hex2rgb!");
            }
            var rv = parseInt(r, 16) / 255.0;
            var gv = parseInt(g, 16) / 255.0;
            var bv = parseInt(b, 16) / 255.0;
            return { r: rv, g: gv, b: bv };
        }
        Util.hex2rgb = hex2rgb;
        function rgb2hex(c) {
            var r = Math.round(c.r * 255).toString(16);
            var g = Math.round(c.g * 255).toString(16);
            var b = Math.round(c.b * 255).toString(16);
            return "#" +
                (r.length > 1 ? r : "0" + r) +
                (g.length > 1 ? g : "0" + g) +
                (b.length > 1 ? b : "0" + b);
        }
        Util.rgb2hex = rgb2hex;
        function curlyPath(x_start, x_middle, x_end, y_top, height) {
            return "M" + x_start + " " + (y_top + height) +
                "C" + x_start + " " + y_top + "," +
                x_middle + " " + (y_top + height) + "," +
                x_middle + " " + y_top +
                "C" + x_middle + " " + (y_top + height) + "," +
                x_end + " " + y_top + "," +
                x_end + " " + (y_top + height);
        }
        Util.curlyPath = curlyPath;
        function closestIndex(num, arr) {
            var mid;
            var lo = 0;
            var hi = arr.length - 1;
            while (hi - lo > 1) {
                mid = Math.floor((lo + hi) / 2);
                if (arr[mid] < num) {
                    lo = mid;
                }
                else {
                    hi = mid;
                }
            }
            if (num - arr[lo] <= arr[hi] - num) {
                return lo;
            }
            return hi;
        }
        Util.closestIndex = closestIndex;
    })(Util || (Util = {}));
    exports.Util = Util;
});
define("annotations/componentmodel", ["require", "exports", "annotations/util"], function (require, exports, util_ts_1) {
    "use strict";
    (function (AnchorPoint) {
        AnchorPoint[AnchorPoint["LEFT_TOP"] = 0] = "LEFT_TOP";
        AnchorPoint[AnchorPoint["LEFT_CENTER"] = 1] = "LEFT_CENTER";
        AnchorPoint[AnchorPoint["LEFT_BOTTOM"] = 2] = "LEFT_BOTTOM";
        AnchorPoint[AnchorPoint["RIGHT_TOP"] = 3] = "RIGHT_TOP";
        AnchorPoint[AnchorPoint["RIGHT_CENTER"] = 4] = "RIGHT_CENTER";
        AnchorPoint[AnchorPoint["RIGHT_BOTTOM"] = 5] = "RIGHT_BOTTOM";
        AnchorPoint[AnchorPoint["TOP_LEFT"] = 6] = "TOP_LEFT";
        AnchorPoint[AnchorPoint["TOP_CENTER"] = 7] = "TOP_CENTER";
        AnchorPoint[AnchorPoint["TOP_RIGHT"] = 8] = "TOP_RIGHT";
        AnchorPoint[AnchorPoint["BOTTOM_LEFT"] = 9] = "BOTTOM_LEFT";
        AnchorPoint[AnchorPoint["BOTTOM_CENTER"] = 10] = "BOTTOM_CENTER";
        AnchorPoint[AnchorPoint["BOTTOM_RIGHT"] = 11] = "BOTTOM_RIGHT";
        AnchorPoint[AnchorPoint["CENTER"] = 12] = "CENTER";
    })(exports.AnchorPoint || (exports.AnchorPoint = {}));
    var AnchorPoint = exports.AnchorPoint;
    ;
    var BBox = (function () {
        function BBox(x, y, w, h) {
            this.x = x || 0.0;
            this.y = y || 0.0;
            this.w = w || 0.0;
            this.h = h || 0.0;
        }
        BBox.prototype.minX = function () {
            return this.x;
        };
        BBox.prototype.maxX = function () {
            return this.x + this.w;
        };
        BBox.prototype.minY = function () {
            return this.y;
        };
        BBox.prototype.maxY = function () {
            return this.y + this.h;
        };
        BBox.prototype.anchor = function (point) {
            switch (point) {
                case AnchorPoint.TOP_LEFT:
                case AnchorPoint.LEFT_TOP:
                    return { x: this.x, y: this.y };
                case AnchorPoint.LEFT_CENTER:
                    return { x: this.x, y: this.y + (this.h / 2.0) };
                case AnchorPoint.BOTTOM_LEFT:
                case AnchorPoint.LEFT_BOTTOM:
                    return { x: this.x, y: this.y + this.h };
                case AnchorPoint.TOP_CENTER:
                    return { x: this.x + (this.w / 2.0), y: this.y };
                case AnchorPoint.TOP_RIGHT:
                case AnchorPoint.RIGHT_TOP:
                    return { x: this.x + this.w, y: this.y };
                case AnchorPoint.RIGHT_CENTER:
                    return { x: this.x + this.w, y: this.y + (this.h / 2.0) };
                case AnchorPoint.RIGHT_BOTTOM:
                case AnchorPoint.BOTTOM_RIGHT:
                    return { x: this.x + this.w, y: this.y + this.h };
                case AnchorPoint.CENTER:
                    return { x: this.x + (this.w / 2.0), y: this.y + (this.h / 2.0) };
                case AnchorPoint.BOTTOM_CENTER:
                    return { x: this.x + (this.w / 2.0), y: this.y + this.h };
            }
        };
        BBox.prototype.translate = function (dx, dy) {
            var box = new BBox();
            box.x = this.x + dx;
            box.y = this.y + dy;
            box.w = this.w;
            box.h = this.h;
            return box;
        };
        return BBox;
    }());
    exports.BBox = BBox;
    (function (RenderLayer) {
        RenderLayer[RenderLayer["EDGE_BACKGROUND"] = 0] = "EDGE_BACKGROUND";
        RenderLayer[RenderLayer["BACKGROUND"] = 1] = "BACKGROUND";
        RenderLayer[RenderLayer["HIGHLIGHT_BACKGROUND"] = 2] = "HIGHLIGHT_BACKGROUND";
        RenderLayer[RenderLayer["TEXT"] = 3] = "TEXT";
        RenderLayer[RenderLayer["LABEL"] = 4] = "LABEL";
    })(exports.RenderLayer || (exports.RenderLayer = {}));
    var RenderLayer = exports.RenderLayer;
    ;
    var Svg;
    (function (Svg) {
        Svg.NS = "http://www.w3.org/2000/svg";
        function measureTexts(renderElem, texts, classes) {
            var measurements = {};
            var elems = {};
            for (var i = 0; i < texts.length; i++) {
                var text = texts[i];
                var clazz = classes[i];
                if (measurements[clazz] === undefined) {
                    measurements[clazz] = {};
                    elems[clazz] = {};
                }
                if (measurements[clazz][text] === undefined) {
                    measurements[clazz][text] = new BBox();
                }
            }
            var g = document.createElementNS(Svg.NS, "g");
            for (var _i = 0, _a = Object.keys(measurements); _i < _a.length; _i++) {
                var clazzkey = _a[_i];
                for (var _b = 0, _c = Object.keys(measurements[clazzkey]); _b < _c.length; _b++) {
                    var textkey = _c[_b];
                    var el = document.createElementNS(Svg.NS, "text");
                    el.setAttribute("x", "0.0");
                    el.setAttribute("y", "0.0");
                    el.setAttribute("class", clazzkey);
                    el.appendChild(document.createTextNode(textkey));
                    g.appendChild(el);
                    elems[clazzkey][textkey] = el;
                }
            }
            renderElem.appendChild(g);
            for (var _d = 0, _e = Object.keys(measurements); _d < _e.length; _d++) {
                var clazzkey = _e[_d];
                for (var _f = 0, _g = Object.keys(measurements[clazzkey]); _f < _g.length; _f++) {
                    var key = _g[_f];
                    var bbox = elems[clazzkey][key].getBBox();
                    var store = measurements[clazzkey][key];
                    store.x = bbox.x;
                    store.y = bbox.y;
                    store.w = bbox.width;
                    store.h = bbox.height;
                }
            }
            renderElem.removeChild(g);
            return measurements;
        }
        Svg.measureTexts = measureTexts;
    })(Svg = exports.Svg || (exports.Svg = {}));
    var RenderLayers = [
        RenderLayer.EDGE_BACKGROUND,
        RenderLayer.BACKGROUND,
        RenderLayer.HIGHLIGHT_BACKGROUND,
        RenderLayer.TEXT,
        RenderLayer.LABEL
    ];
    var RenderLayerClasses = [
        "annotation-layer-edge-background",
        "annotation-layer-background",
        "annotation-layer-highlight",
        "annotation-layer-text",
        "annotation-layer-label"
    ];
    var AnnoGraphics = (function () {
        function AnnoGraphics(renderElem) {
            this.layers = {};
            this.stack = [];
            this.fill = "";
            this.stroke = "";
            this.styleClass = "";
            this.data = {};
            this.layerClasses = {};
            this.translateX = 0;
            this.translateY = 0;
            this.translationStack = [];
            this.renderElem = renderElem;
            for (var k = 0; k < RenderLayers.length; k++) {
                var layer = RenderLayers[k];
                var layerClass = RenderLayerClasses[k];
                this.layers[layer] = document.createElementNS(Svg.NS, "g");
                this.layers[layer].setAttribute("class", layerClass);
            }
        }
        AnnoGraphics.prototype.write = function () {
            for (var _i = 0, RenderLayers_1 = RenderLayers; _i < RenderLayers_1.length; _i++) {
                var layer = RenderLayers_1[_i];
                this.renderElem.appendChild(this.layers[layer]);
            }
        };
        AnnoGraphics.prototype.clearStyle = function () {
            this.stroke = "";
            this.fill = "";
            this.styleClass = "";
        };
        AnnoGraphics.prototype.pushTranslation = function (translateX, translateY) {
            this.translationStack.push(this.translateX);
            this.translationStack.push(this.translateY);
            this.translateX += translateX;
            this.translateY += translateY;
        };
        AnnoGraphics.prototype.popTranslation = function () {
            this.translateY = this.translationStack.pop();
            this.translateX = this.translationStack.pop();
        };
        AnnoGraphics.prototype.push = function (translateX, translateY, id) {
            translateX = translateX || 0.0;
            translateY = translateY || 0.0;
            translateX += this.translateX;
            translateY += this.translateY;
            this.translationStack.push(this.translateX);
            this.translationStack.push(this.translateY);
            this.translateX = 0.0;
            this.translateY = 0.0;
            this.stack.push(this.layers);
            this.layers = {};
            for (var k = 0; k < RenderLayers.length; k++) {
                var layer = RenderLayers[k];
                var layerClass = RenderLayerClasses[k];
                var el = document.createElementNS(Svg.NS, "g");
                if (this.layerClasses[layer] !== undefined) {
                    el.setAttribute("class", layerClass + " " + this.layerClasses[layer]);
                }
                else {
                    el.setAttribute("class", layerClass);
                }
                if (id !== undefined) {
                    el.setAttribute("id", id);
                }
                if (translateX !== 0.0 || translateY !== 0.0) {
                    el.setAttribute("transform", "translate(" + translateX.toString(10) + ", " + translateY.toString(10) + ")");
                }
                this.layers[layer] = el;
            }
            this.layerClasses = {};
        };
        AnnoGraphics.prototype.addText = function (layer, text, x, y) {
            var el = document.createElementNS(Svg.NS, "text");
            if (this.styleClass !== "") {
                el.setAttribute("class", this.styleClass);
            }
            el.setAttribute("x", (x + this.translateX).toString(10));
            el.setAttribute("y", (y + this.translateY).toString(10));
            for (var _i = 0, _a = Object.keys(this.data); _i < _a.length; _i++) {
                var key = _a[_i];
                el.setAttribute("data-" + key, this.data[key]);
            }
            this.data = {};
            el.appendChild(document.createTextNode(text));
            this.layers[layer].appendChild(el);
        };
        AnnoGraphics.prototype.addTextSpanLine = function (layer, text, x, y) {
            var el = document.createElementNS(Svg.NS, "text");
            if (this.styleClass !== "") {
                el.setAttribute("class", this.styleClass);
            }
            el.setAttribute("x", "0");
            el.setAttribute("y", "0");
            for (var _i = 0, _a = Object.keys(this.data); _i < _a.length; _i++) {
                var key = _a[_i];
                el.setAttribute("data-" + key, this.data[key]);
            }
            this.data = {};
            for (var k = 0; k < text.length; k++) {
                var tspan = document.createElementNS(Svg.NS, "tspan");
                tspan.setAttribute("x", (x[k] + this.translateX).toString(10));
                tspan.setAttribute("y", (y[k] + this.translateY).toString(10));
                tspan.appendChild(document.createTextNode(text[k]));
                el.appendChild(tspan);
            }
            this.layers[layer].appendChild(el);
        };
        AnnoGraphics.prototype.addRect = function (layer, x, y, w, h, rx, ry) {
            var rect = document.createElementNS(Svg.NS, "rect");
            if (this.styleClass !== "") {
                rect.setAttribute("class", this.styleClass);
            }
            rect.setAttribute("x", (x + this.translateX).toString(10));
            rect.setAttribute("y", (y + this.translateY).toString(10));
            rect.setAttribute("width", w.toString(10));
            rect.setAttribute("height", h.toString(10));
            for (var _i = 0, _a = Object.keys(this.data); _i < _a.length; _i++) {
                var key = _a[_i];
                rect.setAttribute("data-" + key, this.data[key]);
            }
            this.data = {};
            if (rx !== 0.0 || ry !== 0.0) {
                rect.setAttribute("rx", rx.toString(10));
                rect.setAttribute("ry", ry.toString(10));
            }
            if (this.stroke !== "") {
                rect.setAttribute("stroke", this.stroke);
            }
            if (this.fill !== "") {
                rect.setAttribute("fill", this.fill);
            }
            this.layers[layer].appendChild(rect);
        };
        AnnoGraphics.prototype.addEdgeLine = function (layer, x_start, y_start, x_middle, y_middle, x_end, y_end, arrow) {
            x_start += this.translateX;
            x_middle += this.translateX;
            x_end += this.translateX;
            y_start += this.translateY;
            y_middle += this.translateY;
            y_end += this.translateY;
            var path = "M " + x_start + "," + y_start;
            if (Math.abs(x_middle - x_end) < 1e-5 && Math.abs(y_middle - y_end) < 1e-5) {
                path += " L " + x_end + "," + y_end;
            }
            else {
                path += " Q " + x_middle + "," + y_middle + " " + x_end + "," + y_end;
            }
            var pathel = document.createElementNS(Svg.NS, "path");
            pathel.setAttribute("d", path);
            if (this.styleClass !== "") {
                pathel.setAttribute("class", this.styleClass);
            }
            if ((arrow || "") === "end") {
                pathel.setAttribute("marker-end", "url(#arrow)");
            }
            if (this.stroke !== "") {
                pathel.setAttribute("stroke", this.stroke);
            }
            if (this.fill !== "") {
                pathel.setAttribute("fill", this.fill);
            }
            this.layers[layer].appendChild(pathel);
        };
        AnnoGraphics.prototype.addCurly = function (layer, x_left, x_middle, x_right, y_top, height) {
            x_left += this.translateX;
            x_middle += this.translateX;
            x_right += this.translateX;
            y_top += this.translateY;
            var path = util_ts_1.Util.curlyPath(x_left, x_middle, x_right, y_top, height);
            var pathel = document.createElementNS(Svg.NS, "path");
            pathel.setAttribute("d", path);
            if (this.styleClass !== "") {
                pathel.setAttribute("class", this.styleClass);
            }
            if (this.stroke !== "") {
                pathel.setAttribute("stroke", this.stroke);
            }
            if (this.fill !== "") {
                pathel.setAttribute("fill", this.fill);
            }
            this.layers[layer].appendChild(pathel);
        };
        AnnoGraphics.prototype.addLine = function (layer, x1, y1, x2, y2, strokeWidth) {
            var lineel = document.createElementNS(Svg.NS, "line");
            lineel.setAttribute("x1", (x1 + this.translateX).toString(10));
            lineel.setAttribute("y1", (y1 + this.translateY).toString(10));
            lineel.setAttribute("x2", (x2 + this.translateX).toString(10));
            lineel.setAttribute("y2", (y2 + this.translateY).toString(10));
            if (strokeWidth !== undefined) {
                lineel.setAttribute("stroke-width", strokeWidth.toString(10));
            }
            if (this.stroke !== "") {
                lineel.setAttribute("stroke", this.stroke);
            }
            if (this.styleClass !== "") {
                lineel.setAttribute("class", this.styleClass);
            }
            this.layers[layer].appendChild(lineel);
        };
        AnnoGraphics.prototype.addPolygon = function (layer, x, y, pts) {
            x += this.translateX;
            y += this.translateY;
            var poly = document.createElementNS(Svg.NS, "polygon");
            var items = [];
            for (var k = 0; k < pts.length; k += 2) {
                items.push((pts[k] + x).toString() + "," + (pts[k + 1] + y).toString());
            }
            poly.setAttribute("points", items.join(" "));
            if (this.styleClass !== "") {
                poly.setAttribute("class", this.styleClass);
            }
            if (this.stroke !== "") {
                poly.setAttribute("stroke", this.stroke);
            }
            if (this.fill !== "") {
                poly.setAttribute("fill", this.fill);
            }
            this.layers[layer].appendChild(poly);
        };
        AnnoGraphics.prototype.pop = function () {
            var parent = this.stack.pop();
            for (var _i = 0, RenderLayers_2 = RenderLayers; _i < RenderLayers_2.length; _i++) {
                var layer = RenderLayers_2[_i];
                if (this.layers[layer].hasChildNodes) {
                    parent[layer].appendChild(this.layers[layer]);
                }
            }
            this.layers = parent;
            this.popTranslation();
        };
        return AnnoGraphics;
    }());
    exports.AnnoGraphics = AnnoGraphics;
    var AnnoElement = (function () {
        function AnnoElement() {
            this.renderLayer = RenderLayer.TEXT;
            this.x = 0.0;
            this.y = 0.0;
            this.data = {};
        }
        AnnoElement.prototype.translate = function (dx, dy) {
            this.x = this.x + dx;
            this.y = this.y + dy;
            return this;
        };
        AnnoElement.prototype.attach = function (anchor, point) {
            switch (point) {
                case AnchorPoint.TOP_LEFT:
                case AnchorPoint.LEFT_TOP:
                    {
                        this.x = anchor.x;
                        this.y = anchor.y;
                        break;
                    }
                case AnchorPoint.LEFT_CENTER:
                    {
                        this.x = anchor.x;
                        this.y = anchor.y - this.height() / 2.0;
                        break;
                    }
                case AnchorPoint.BOTTOM_LEFT:
                case AnchorPoint.LEFT_BOTTOM:
                    {
                        this.x = anchor.x;
                        this.y = anchor.y - this.height();
                        break;
                    }
                case AnchorPoint.TOP_CENTER:
                    {
                        this.x = anchor.x - this.width() / 2.0;
                        this.y = anchor.y;
                        break;
                    }
                case AnchorPoint.TOP_RIGHT:
                case AnchorPoint.RIGHT_TOP:
                    {
                        this.x = anchor.x - this.width();
                        this.y = anchor.y;
                        break;
                    }
                case AnchorPoint.RIGHT_CENTER:
                    {
                        this.x = anchor.x - this.width();
                        this.y = anchor.y - this.height() / 2;
                        break;
                    }
                case AnchorPoint.RIGHT_BOTTOM:
                case AnchorPoint.BOTTOM_RIGHT:
                    {
                        this.x = anchor.x - this.width();
                        this.y = anchor.y - this.height();
                        break;
                    }
                case AnchorPoint.CENTER:
                    {
                        this.x = anchor.x - this.width() / 2.0;
                        this.y = anchor.y - this.height() / 2.0;
                        break;
                    }
                case AnchorPoint.BOTTOM_CENTER:
                    {
                        this.x = anchor.x - this.width() / 2.0;
                        this.y = anchor.y - this.height();
                        break;
                    }
            }
            return this;
        };
        AnnoElement.prototype.anchor = function (point) {
            switch (point) {
                case AnchorPoint.TOP_LEFT:
                case AnchorPoint.LEFT_TOP:
                    return { x: this.x, y: this.y };
                case AnchorPoint.LEFT_CENTER:
                    return { x: this.x, y: this.y + (this.height() / 2.0) };
                case AnchorPoint.BOTTOM_LEFT:
                case AnchorPoint.LEFT_BOTTOM:
                    return { x: this.x, y: this.y + this.height() };
                case AnchorPoint.TOP_CENTER:
                    return { x: this.x + (this.width() / 2.0), y: this.y };
                case AnchorPoint.TOP_RIGHT:
                case AnchorPoint.RIGHT_TOP:
                    return { x: this.x + this.width(), y: this.y };
                case AnchorPoint.RIGHT_CENTER:
                    return { x: this.x + this.width(), y: this.y + (this.height() / 2.0) };
                case AnchorPoint.RIGHT_BOTTOM:
                case AnchorPoint.BOTTOM_RIGHT:
                    return { x: this.x + this.width(), y: this.y + this.height() };
                case AnchorPoint.CENTER:
                    return { x: this.x + (this.width() / 2.0), y: this.y + (this.height() / 2.0) };
                case AnchorPoint.BOTTOM_CENTER:
                    return { x: this.x + (this.width() / 2.0), y: this.y + this.height() };
            }
        };
        return AnnoElement;
    }());
    exports.AnnoElement = AnnoElement;
    var AnnoTextElement = (function (_super) {
        __extends(AnnoTextElement, _super);
        function AnnoTextElement(x, y, text, textMeas, textClass) {
            _super.call(this);
            this.x = x || 0.0;
            this.y = y || 0.0;
            this.text = text || "";
            this.textMeas = textMeas || new BBox();
            this.textClass = textClass || "annotation-text";
        }
        AnnoTextElement.prototype.width = function () {
            return this.textMeas.w;
        };
        AnnoTextElement.prototype.height = function () {
            return this.textMeas.h;
        };
        AnnoTextElement.prototype.render = function (g) {
            g.data = this.data;
            g.styleClass = this.textClass;
            g.addText(this.renderLayer, this.text, this.x, this.y - this.textMeas.y);
            g.clearStyle();
        };
        return AnnoTextElement;
    }(AnnoElement));
    exports.AnnoTextElement = AnnoTextElement;
    var AnnoLineElement = (function (_super) {
        __extends(AnnoLineElement, _super);
        function AnnoLineElement(x1, y1, x2, y2) {
            _super.call(this);
            this.x2 = 0.0;
            this.y2 = 0.0;
            this.strokeWidth = 1.0;
            this.stroke = "";
            this.styleClass = "";
            this.x = x1 || 0.0;
            this.y = y1 || 0.0;
            this.x2 = x2 || 0.0;
            this.y2 = y2 || 0.0;
        }
        AnnoLineElement.prototype.height = function () {
            return Math.abs(this.y2);
        };
        AnnoLineElement.prototype.width = function () {
            return Math.abs(this.x2);
        };
        AnnoLineElement.prototype.render = function (g) {
            g.data = this.data;
            g.styleClass = this.styleClass;
            g.stroke = this.stroke;
            g.addLine(this.renderLayer, this.x, this.y, this.x2 + this.x, this.y2 + this.y, this.strokeWidth);
            g.clearStyle();
        };
        return AnnoLineElement;
    }(AnnoElement));
    exports.AnnoLineElement = AnnoLineElement;
    var AnnoEdgeLineElement = (function (_super) {
        __extends(AnnoEdgeLineElement, _super);
        function AnnoEdgeLineElement(x, y, x_middle, y_middle, x_end, y_end) {
            _super.call(this);
            this.dxMiddle = 0.0;
            this.dyMiddle = 0.0;
            this.dxEnd = 0.0;
            this.dyEnd = 0.0;
            this.strokeWidth = 1.0;
            this.stroke = "";
            this.styleClass = "";
            this.arrow = "";
            this.x = x || 0.0;
            this.y = y || 0.0;
            this.dxMiddle = (x_middle || 0.0) - this.x;
            this.dyMiddle = (y_middle || 0.0) - this.y;
            this.dxEnd = (x_end || 0.0) - this.x;
            this.dyEnd = (y_end || 0.0) - this.y;
        }
        AnnoEdgeLineElement.prototype.height = function () {
            return Math.abs(this.dyEnd);
        };
        AnnoEdgeLineElement.prototype.width = function () {
            return Math.abs(this.dxEnd);
        };
        AnnoEdgeLineElement.prototype.render = function (g) {
            g.data = this.data;
            g.styleClass = this.styleClass;
            g.stroke = this.stroke;
            g.addEdgeLine(this.renderLayer, this.x, this.y, this.dxMiddle + this.x, this.dyMiddle + this.y, this.dxEnd + this.x, this.dyEnd + this.y, this.arrow);
            g.clearStyle();
        };
        return AnnoEdgeLineElement;
    }(AnnoElement));
    exports.AnnoEdgeLineElement = AnnoEdgeLineElement;
    var AnnoTextLine = (function (_super) {
        __extends(AnnoTextLine, _super);
        function AnnoTextLine() {
            _super.call(this);
            this.text = [];
            this.xs = [];
            this.textMeas = [];
            this.partials = {};
            this.textClass = "annotation-text";
            this.maxHeight = 0.0;
            this.w = 0.0;
            this.len = 0;
            this.partials[0] = 0.0;
        }
        AnnoTextLine.prototype.add = function (text, partiaLens, measurements) {
            for (var k = 0; k < partiaLens.length; k++) {
                this.partials[this.len + partiaLens[k]] = this.w + measurements[k].w;
            }
            var full = measurements[measurements.length - 1];
            this.xs.push(this.w);
            this.w += full.w;
            this.maxHeight = Math.max(full.h, this.maxHeight);
            this.len += text.length;
            this.text.push(text);
            this.textMeas.push(full);
        };
        AnnoTextLine.prototype.section = function (from, to) {
            return new BBox(this.x + this.partials[from], this.y, this.partials[to] - this.partials[from], this.maxHeight);
        };
        AnnoTextLine.prototype.length = function () {
            return this.len;
        };
        AnnoTextLine.prototype.width = function () {
            return this.w;
        };
        AnnoTextLine.prototype.height = function () {
            return this.maxHeight;
        };
        AnnoTextLine.prototype.render = function (g) {
            g.styleClass = this.textClass;
            var ys = [];
            for (var k = 0; k < this.xs.length; k++) {
                ys.push(this.y + (this.maxHeight - this.textMeas[k].h) - this.textMeas[k].y);
            }
            g.addTextSpanLine(this.renderLayer, this.text, this.xs, ys);
            g.clearStyle();
        };
        return AnnoTextLine;
    }(AnnoElement));
    exports.AnnoTextLine = AnnoTextLine;
    var AnnoCurlyElement = (function (_super) {
        __extends(AnnoCurlyElement, _super);
        function AnnoCurlyElement(x, y, w, h) {
            _super.call(this);
            this.curlyStyle = "annotation-curly";
            this.stroke = "";
            this.x = x || 0.0;
            this.y = y || 0.0;
            this.w = w || 0.0;
            this.h = h || 0.0;
        }
        AnnoCurlyElement.prototype.width = function () {
            return this.w;
        };
        AnnoCurlyElement.prototype.height = function () {
            return this.h;
        };
        AnnoCurlyElement.prototype.render = function (g) {
            g.styleClass = this.curlyStyle;
            g.stroke = this.stroke;
            g.addCurly(this.renderLayer, this.x, this.x + (this.w / 2.0), this.x + this.w, this.y, this.h);
            g.clearStyle();
        };
        return AnnoCurlyElement;
    }(AnnoElement));
    exports.AnnoCurlyElement = AnnoCurlyElement;
    var AnnoRectElement = (function (_super) {
        __extends(AnnoRectElement, _super);
        function AnnoRectElement(x, y, w, h, rx, ry) {
            _super.call(this);
            this.rx = 0.0;
            this.ry = 0.0;
            this.fill = "";
            this.stroke = "";
            this.styleClass = "";
            this.x = x || 0.0;
            this.y = y || 0.0;
            this.w = w || 0.0;
            this.h = h || 0.0;
            this.rx = rx || 0.0;
            this.ry = ry || 0.0;
        }
        AnnoRectElement.prototype.render = function (g) {
            g.fill = this.fill;
            g.stroke = this.stroke;
            g.styleClass = this.styleClass;
            g.addRect(this.renderLayer, this.x, this.y, this.w, this.h, this.rx, this.ry);
            g.clearStyle();
        };
        AnnoRectElement.prototype.width = function () {
            return this.w;
        };
        AnnoRectElement.prototype.height = function () {
            return this.h;
        };
        return AnnoRectElement;
    }(AnnoElement));
    exports.AnnoRectElement = AnnoRectElement;
    var AnnoPolygon = (function (_super) {
        __extends(AnnoPolygon, _super);
        function AnnoPolygon(x, y, points) {
            _super.call(this);
            this.fill = "";
            this.stroke = "";
            this.styleClass = "";
            this.points = [];
            this.x = x || 0.0;
            this.y = y || 0.0;
            var minX = Number.POSITIVE_INFINITY, maxX = Number.NEGATIVE_INFINITY;
            var minY = Number.POSITIVE_INFINITY, maxY = Number.NEGATIVE_INFINITY;
            for (var k = 0; k < points.length; k += 2) {
                minX = Math.min(minX, points[k]);
                maxX = Math.max(maxX, points[k]);
                minY = Math.min(minY, points[k + 1]);
                maxY = Math.max(maxY, points[k + 1]);
            }
            this.w = maxX - minX;
            this.h = maxY - minY;
            if (minX !== 0.0 || maxX !== 0.0) {
                for (var k = 0; k < points.length / 2; k += 2) {
                    points[k] -= minX;
                    points[k + 1] -= minY;
                }
            }
            this.points = points;
        }
        AnnoPolygon.prototype.render = function (g) {
            g.fill = this.fill;
            g.stroke = this.stroke;
            g.styleClass = this.styleClass;
            g.addPolygon(this.renderLayer, this.x, this.y, this.points);
            g.clearStyle();
        };
        AnnoPolygon.prototype.width = function () {
            return this.w;
        };
        AnnoPolygon.prototype.height = function () {
            return this.h;
        };
        return AnnoPolygon;
    }(AnnoElement));
    exports.AnnoPolygon = AnnoPolygon;
    var AnnoGroup = (function (_super) {
        __extends(AnnoGroup, _super);
        function AnnoGroup(x, y) {
            _super.call(this);
            this.elements = [];
            this.layerClasses = {};
            this.w = undefined;
            this.h = undefined;
            this.isolateGroup = false;
            this.x = x || 0.0;
            this.y = y || 0.0;
        }
        AnnoGroup.prototype.moveContent = function (dx, dy) {
            if (dx !== 0.0 || dy !== 0.0) {
                for (var _i = 0, _a = this.elements; _i < _a.length; _i++) {
                    var el = _a[_i];
                    el.x += dx;
                    el.y += dy;
                }
            }
        };
        AnnoGroup.prototype.applyTransformation = function () {
            if (this.y !== 0.0 || this.x !== 0.0) {
                this.moveContent(this.x, this.y);
                this.x = 0.0;
                this.y = 0.0;
            }
        };
        AnnoGroup.prototype.add = function (elem) {
            this.elements.push(elem);
            this.w = undefined;
            this.h = undefined;
        };
        AnnoGroup.prototype.width = function () {
            if (this.w === undefined) {
                var min_x = Number.POSITIVE_INFINITY;
                var max_x = Number.NEGATIVE_INFINITY;
                for (var _i = 0, _a = this.elements; _i < _a.length; _i++) {
                    var el = _a[_i];
                    max_x = Math.max(el.x + el.width(), max_x);
                    min_x = Math.min(el.x, min_x);
                }
                this.w = max_x - min_x;
            }
            return this.w;
        };
        AnnoGroup.prototype.height = function () {
            if (this.h === undefined) {
                var min_y = Number.POSITIVE_INFINITY;
                var max_y = Number.NEGATIVE_INFINITY;
                for (var _i = 0, _a = this.elements; _i < _a.length; _i++) {
                    var el = _a[_i];
                    max_y = Math.max(el.y + el.height(), max_y);
                    min_y = Math.min(el.y, min_y);
                }
                this.h = max_y - min_y;
            }
            return this.h;
        };
        AnnoGroup.prototype.bounds = function () {
            var min_y = Number.POSITIVE_INFINITY;
            var max_y = Number.NEGATIVE_INFINITY;
            var min_x = Number.POSITIVE_INFINITY;
            var max_x = Number.NEGATIVE_INFINITY;
            for (var _i = 0, _a = this.elements; _i < _a.length; _i++) {
                var el = _a[_i];
                max_x = Math.max(el.x + el.width(), max_x);
                min_x = Math.min(el.x, min_x);
                max_y = Math.max(el.y + el.height(), max_y);
                min_y = Math.min(el.y, min_y);
            }
            return new BBox(min_x, min_y, max_x - min_x, max_y - min_y);
        };
        AnnoGroup.prototype.compact = function () {
            var size = this.bounds();
            if (size.x !== 0.0 || size.y !== 0.0) {
                for (var _i = 0, _a = this.elements; _i < _a.length; _i++) {
                    var el = _a[_i];
                    el.x -= size.x;
                    el.y -= size.y;
                }
            }
        };
        AnnoGroup.prototype.render = function (g) {
            if (this.x !== 0.0 || this.y !== 0.0) {
                if (this.isolateGroup === true) {
                    g.push(this.x, this.y);
                }
                else {
                    g.pushTranslation(this.x, this.y);
                }
            }
            for (var _i = 0, _a = this.elements; _i < _a.length; _i++) {
                var el = _a[_i];
                el.render(g);
            }
            if (this.x !== 0.0 || this.y !== 0.0) {
                if (this.isolateGroup === true) {
                    g.pop();
                }
                else {
                    g.popTranslation();
                }
            }
        };
        return AnnoGroup;
    }(AnnoElement));
    exports.AnnoGroup = AnnoGroup;
});
define("annotations/flowdocument", ["require", "exports", "annotations/util", "annotations/componentmodel"], function (require, exports, util_1, componentmodel_1) {
    "use strict";
    var LabelFragment = (function () {
        function LabelFragment() {
            this.layer = "";
            this.id = "";
            this.label = "";
            this.popup = "";
            this.popupId = "";
            this.color = null;
            this.strokeColor = null;
            this.bbox = null;
        }
        return LabelFragment;
    }());
    var NodeLabelFragment = (function (_super) {
        __extends(NodeLabelFragment, _super);
        function NodeLabelFragment() {
            _super.apply(this, arguments);
            this.startpos = 0;
            this.endpos = 0;
        }
        return NodeLabelFragment;
    }(LabelFragment));
    var EdgeLabelFragment = (function (_super) {
        __extends(EdgeLabelFragment, _super);
        function EdgeLabelFragment() {
            _super.apply(this, arguments);
            this.head = "";
            this.tail = "";
            this.startline = 0;
            this.endline = 0;
        }
        return EdgeLabelFragment;
    }(LabelFragment));
    var Lines;
    (function (Lines) {
        Lines.nodeLayerPriority = {
            "node/Token": -2,
            "node/Sentence": Number.POSITIVE_INFINITY
        };
    })(Lines || (Lines = {}));
    var EdgeFragment = (function () {
        function EdgeFragment(parent, label) {
            this.bottomY = 0.0;
            this.leftAttachmentPt = { x: 0, y: 0 };
            this.rightAttachmentPt = { x: 0, y: 0 };
            this.arrowLeft = false;
            this.arrowRight = false;
            this.leftHasNode = false;
            this.rightHasNode = false;
            this.parent = parent;
            this.label = label;
            this.textElement = new componentmodel_1.AnnoTextElement(0, 0, label.label, label.bbox, "annotation-label");
        }
        EdgeFragment.prototype.closestPtByX = function (pt, pts) {
            var selectedPt = pts[0];
            var dist = Math.abs(pt.x - pts[0].x);
            for (var k = 1; k < pts.length; k++) {
                if (Math.abs(pt.x - pts[k].x) < dist) {
                    dist = Math.abs(pt.x - pts[k].x);
                    selectedPt = pts[k];
                }
            }
            return selectedPt;
        };
        EdgeFragment.prototype.optimizeLeft = function (pts) {
            this.leftAttachmentPt = this.closestPtByX(this.leftAttachmentPt, pts);
        };
        EdgeFragment.prototype.optimizeRight = function (pts) {
            this.rightAttachmentPt = this.closestPtByX(this.rightAttachmentPt, pts);
        };
        EdgeFragment.prototype.centerX = function () {
            var cx = this.textElement.width() / 2.0;
            cx += this.parent.parent.doc.style.labelPadding;
            cx += this.parent.parent.doc.style.minEdgeLineLength;
            cx += this.parent.parent.doc.style.edgeBendDistance;
            return Math.max(cx, this.leftAttachmentPt.x + (this.rightAttachmentPt.x - this.leftAttachmentPt.x) / 2.0);
        };
        EdgeFragment.prototype.width = function () {
            return this.rightX() - this.leftX();
        };
        EdgeFragment.prototype.leftX = function () {
            var cx = this.centerX();
            cx -= this.textElement.width() / 2.0;
            cx -= this.parent.parent.doc.style.labelPadding;
            cx -= this.parent.parent.doc.style.minEdgeLineLength;
            cx -= this.parent.parent.doc.style.edgeBendDistance;
            return Math.min(cx, this.leftAttachmentPt.x);
        };
        EdgeFragment.prototype.rightX = function () {
            var cx = this.centerX();
            cx += this.textElement.width() / 2.0;
            cx += this.parent.parent.doc.style.labelPadding;
            cx += this.parent.parent.doc.style.minEdgeLineLength;
            cx += this.parent.parent.doc.style.edgeBendDistance;
            return Math.max(cx, this.rightAttachmentPt.x);
        };
        EdgeFragment.prototype.height = function () {
            return this.textElement.height();
        };
        EdgeFragment.prototype.compile = function (labelPadding, bendDistance) {
            var edgeBox = new componentmodel_1.AnnoGroup();
            this.textElement.renderLayer = componentmodel_1.RenderLayer.LABEL;
            this.textElement.data = {
                "popup-title": this.label.tail + " → " + this.label.head + " : " + this.label.id,
                "popup-html": this.label.popup
            };
            var centerX = Math.max(this.centerX(), this.textElement.width() / 2.0 + labelPadding + bendDistance);
            this.textElement.attach({ x: this.centerX(), y: this.bottomY }, componentmodel_1.AnchorPoint.BOTTOM_CENTER);
            var leftAnchorPoint = this.textElement.anchor(componentmodel_1.AnchorPoint.LEFT_CENTER);
            var rightAnchorPoint = this.textElement.anchor(componentmodel_1.AnchorPoint.RIGHT_CENTER);
            leftAnchorPoint.x -= labelPadding;
            rightAnchorPoint.x += labelPadding;
            var leftLineXstart = this.leftX() + bendDistance;
            var leftLine = new componentmodel_1.AnnoLineElement(leftLineXstart, leftAnchorPoint.y, leftAnchorPoint.x - leftLineXstart, 0);
            var rightLine = new componentmodel_1.AnnoLineElement(rightAnchorPoint.x, rightAnchorPoint.y, this.rightX() - rightAnchorPoint.x - bendDistance, 0);
            leftLine.renderLayer = componentmodel_1.RenderLayer.EDGE_BACKGROUND;
            rightLine.renderLayer = componentmodel_1.RenderLayer.EDGE_BACKGROUND;
            leftLine.styleClass = "annotation-edge-line";
            rightLine.styleClass = "annotation-edge-line";
            leftLine.strokeWidth = undefined;
            rightLine.strokeWidth = undefined;
            edgeBox.add(this.textElement);
            edgeBox.add(leftLine);
            edgeBox.add(rightLine);
            if (this.leftHasNode === true) {
                var annoLine = new componentmodel_1.AnnoEdgeLineElement(leftLine.x, leftLine.y, leftLine.x - bendDistance, leftLine.y, this.leftAttachmentPt.x, this.leftAttachmentPt.y);
                annoLine.arrow = this.arrowLeft === true ? "end" : "";
                annoLine.renderLayer = componentmodel_1.RenderLayer.EDGE_BACKGROUND;
                annoLine.styleClass = "annotation-edge-line";
                edgeBox.add(annoLine);
            }
            else {
                this.leftAttachmentPt.y = leftLine.y;
                var annoLine = new componentmodel_1.AnnoEdgeLineElement(leftLine.x, leftLine.y, leftLine.x, leftLine.y, this.leftAttachmentPt.x, this.leftAttachmentPt.y);
                annoLine.arrow = this.arrowLeft === true ? "end" : "";
                annoLine.renderLayer = componentmodel_1.RenderLayer.EDGE_BACKGROUND;
                annoLine.styleClass = "annotation-edge-line";
                edgeBox.add(annoLine);
            }
            if (this.rightHasNode === true) {
                var rightLineAnchor = rightLine.anchor(componentmodel_1.AnchorPoint.RIGHT_CENTER);
                var annoLine = new componentmodel_1.AnnoEdgeLineElement(rightLineAnchor.x, rightLineAnchor.y, rightLineAnchor.x + bendDistance, rightLine.y, this.rightAttachmentPt.x, this.rightAttachmentPt.y);
                annoLine.arrow = this.arrowRight === true ? "end" : "";
                annoLine.renderLayer = componentmodel_1.RenderLayer.EDGE_BACKGROUND;
                annoLine.styleClass = "annotation-edge-line";
                edgeBox.add(annoLine);
            }
            else {
                this.rightAttachmentPt.y = rightLine.y;
                var annoLine = new componentmodel_1.AnnoEdgeLineElement(rightLine.x, rightLine.y, rightLine.x, rightLine.y, this.rightAttachmentPt.x, this.rightAttachmentPt.y);
                annoLine.arrow = this.arrowRight === true ? "end" : "";
                annoLine.renderLayer = componentmodel_1.RenderLayer.EDGE_BACKGROUND;
                annoLine.styleClass = "annotation-edge-line";
                edgeBox.add(annoLine);
            }
            return edgeBox;
        };
        return EdgeFragment;
    }());
    var LineSegment = (function () {
        function LineSegment(parent) {
            this.blocks = [];
            this.nodeLabels = [];
            this.edgeLabels = [];
            this.nodeId2idx = {};
            this.height = 0.0;
            this.masterBlock = new componentmodel_1.AnnoGroup();
            this.parent = parent;
        }
        LineSegment.prototype.start = function () {
            return this.blocks[0].startpos;
        };
        LineSegment.prototype.end = function () {
            return this.blocks[this.blocks.length - 1].endpos;
        };
        LineSegment.prototype.textExcerpt = function (lbl) {
            var excerpt = this.parent.doc.text.substring(lbl.startpos, lbl.endpos);
            if (lbl.endpos - lbl.startpos > 40) {
                excerpt = "\"" + excerpt.substring(0, 40) + "\"...";
            }
            else {
                excerpt = "\"" + excerpt + "\"";
            }
            return excerpt;
        };
        LineSegment.prototype.processExtensionMarkers = function (lbl, backgroundRect, labelBox) {
            var leftTri = lbl.startpos < this.start();
            var rightTri = lbl.endpos > this.end();
            if (leftTri || rightTri) {
                var triangleMargin = this.parent.doc.style.triangleMargin;
                var b = labelBox.bounds();
                if (leftTri) {
                    var pts = util_1.Util.leftTriangle(labelBox.height() * 0.5, labelBox.height() * 0.75);
                    var poly = new componentmodel_1.AnnoPolygon(0, 0, pts);
                    poly.renderLayer = componentmodel_1.RenderLayer.BACKGROUND;
                    poly.fill = backgroundRect.stroke;
                    poly.attach(backgroundRect.anchor(componentmodel_1.AnchorPoint.LEFT_CENTER), componentmodel_1.AnchorPoint.RIGHT_CENTER);
                    poly.x -= triangleMargin;
                    labelBox.add(poly);
                }
                if (rightTri) {
                    var pts = util_1.Util.rightTriangle(labelBox.height() * 0.5, labelBox.height() * 0.75);
                    var poly = new componentmodel_1.AnnoPolygon(0, 0, pts);
                    poly.renderLayer = componentmodel_1.RenderLayer.BACKGROUND;
                    poly.fill = backgroundRect.stroke;
                    poly.attach(backgroundRect.anchor(componentmodel_1.AnchorPoint.RIGHT_CENTER), componentmodel_1.AnchorPoint.LEFT_CENTER);
                    poly.x += triangleMargin;
                    labelBox.add(poly);
                }
            }
        };
        LineSegment.prototype.processNodeLabelBackground = function (lbl, labelPadding, labelBox) {
            var backgroundRect = new componentmodel_1.AnnoRectElement(-labelPadding, -labelPadding, labelBox.width() + labelPadding * 2, labelBox.height() + labelPadding * 2, 2.0, 1.0);
            backgroundRect.stroke = util_1.Util.rgb2hex(util_1.Util.hsl2rgb(lbl.strokeColor));
            backgroundRect.fill = util_1.Util.rgb2hex(util_1.Util.hsl2rgb(lbl.color));
            backgroundRect.renderLayer = componentmodel_1.RenderLayer.BACKGROUND;
            labelBox.add(backgroundRect);
            return backgroundRect;
        };
        LineSegment.prototype.processCurlyAttachment = function (lbl, blockBBox, curlyHeight, annotationBox) {
            var curlyObj = new componentmodel_1.AnnoCurlyElement(0, 0, blockBBox.w, curlyHeight);
            curlyObj.renderLayer = componentmodel_1.RenderLayer.LABEL;
            curlyObj.stroke = util_1.Util.rgb2hex(util_1.Util.hsl2rgb(lbl.strokeColor));
            curlyObj.attach(blockBBox.anchor(componentmodel_1.AnchorPoint.TOP_LEFT), componentmodel_1.AnchorPoint.BOTTOM_LEFT);
            annotationBox.add(curlyObj);
            return curlyObj;
        };
        LineSegment.prototype.lineBBox = function (line, startpos, endpos) {
            var rel_startpos = Math.max(this.start(), startpos) - this.start();
            var rel_endpos = Math.min(this.end(), endpos) - this.start();
            return line.section(rel_startpos, rel_endpos);
        };
        LineSegment.prototype.resolveLabelPositioning = function (annotationBoxLayer, labelBoxes) {
            for (var _i = 0, _a = Object.keys(annotationBoxLayer); _i < _a.length; _i++) {
                var key = _a[_i];
                annotationBoxLayer[key].sort(function (x, y) { return labelBoxes[x].x - labelBoxes[y].x; });
            }
            var labelLayer = [];
            var labelMargin = this.parent.doc.style.labelMargin;
            var layerEnd = [-labelMargin];
            var labelLayerKeys = Object.keys(annotationBoxLayer);
            labelLayerKeys.sort(function (x, y) { return (Lines.nodeLayerPriority[x] || 0) - (Lines.nodeLayerPriority[y] || 0); });
            for (var _b = 0, labelLayerKeys_1 = labelLayerKeys; _b < labelLayerKeys_1.length; _b++) {
                var key = labelLayerKeys_1[_b];
                for (var _c = 0, _d = annotationBoxLayer[key]; _c < _d.length; _c++) {
                    var labelAlloc = _d[_c];
                    var lbl = this.nodeLabels[labelAlloc];
                    var assigned = false;
                    for (var i = 0; i < layerEnd.length; i++) {
                        if (labelBoxes[labelAlloc].x >= (layerEnd[i] + labelMargin)) {
                            layerEnd[i] = labelBoxes[labelAlloc].x + labelBoxes[labelAlloc].width();
                            labelLayer[labelAlloc] = i;
                            assigned = true;
                            break;
                        }
                    }
                    if (assigned === false) {
                        labelLayer[labelAlloc] = layerEnd.length;
                        layerEnd.push(labelBoxes[labelAlloc].x + labelBoxes[labelAlloc].width());
                    }
                }
                for (var i = 0; i < layerEnd.length; i++) {
                    layerEnd[i] = Number.POSITIVE_INFINITY;
                }
            }
            return {
                layer: labelLayer,
                numLayers: layerEnd.length,
                priority: labelLayerKeys
            };
        };
        LineSegment.prototype.computeNodeLayerInfo = function (annotationBoxes, labelLayer, numLayers) {
            var labelMaxHeight = [];
            var labelLayerOffsets = [];
            for (var i = 0; i < numLayers; i++) {
                labelMaxHeight.push(0.0);
            }
            for (var i = 0; i < this.nodeLabels.length; i++) {
                labelMaxHeight[labelLayer[i]] = Math.max(labelMaxHeight[labelLayer[i]], annotationBoxes[i].height());
            }
            var labelHeight = 0.0;
            var offset = 0.0;
            for (var i = 0; i < numLayers; i++) {
                labelLayerOffsets.push(offset);
                offset -= labelMaxHeight[i];
                labelHeight += labelMaxHeight[i];
            }
            labelLayerOffsets.push(labelHeight);
            return { heights: labelMaxHeight, offsets: labelLayerOffsets, totalHeight: labelHeight };
        };
        LineSegment.prototype.placeLabels = function (line) {
            var annotationBoxes = [];
            var labelBoxes = [];
            var curlyHeight = this.parent.doc.style.curlyHeight;
            var labelPadding = this.parent.doc.style.labelPadding;
            var layer2labels = {};
            for (var _i = 0, _a = this.nodeLabels; _i < _a.length; _i++) {
                var lbl = _a[_i];
                var blockBBox = this.lineBBox(line, lbl.startpos, lbl.endpos);
                var annotationBox = new componentmodel_1.AnnoGroup();
                var labelBox = new componentmodel_1.AnnoGroup();
                var labelObj = new componentmodel_1.AnnoTextElement(0, 0, lbl.label, lbl.bbox, "annotation-label");
                labelObj.data = { "popup-title": this.textExcerpt(lbl) + " : " + lbl.id, "popup-html": lbl.popup };
                labelObj.renderLayer = componentmodel_1.RenderLayer.LABEL;
                labelBox.add(labelObj);
                var backgroundRect = this.processNodeLabelBackground(lbl, labelPadding, labelBox);
                this.processExtensionMarkers(lbl, backgroundRect, labelBox);
                labelBox.compact();
                annotationBox.add(labelBox);
                var curlyObj = this.processCurlyAttachment(lbl, blockBBox, curlyHeight, annotationBox);
                labelBox.attach(curlyObj.anchor(componentmodel_1.AnchorPoint.TOP_CENTER), componentmodel_1.AnchorPoint.BOTTOM_CENTER);
                labelBox.x = Math.max(0, labelBox.x);
                var idxes = layer2labels[lbl.layer];
                if (idxes === undefined) {
                    idxes = [];
                    layer2labels[lbl.layer] = idxes;
                }
                idxes.push(labelBoxes.length);
                labelBoxes.push(labelBox);
                annotationBoxes.push(annotationBox);
            }
            var labelPos = this.resolveLabelPositioning(layer2labels, labelBoxes);
            var labelInfo = this.computeNodeLayerInfo(annotationBoxes, labelPos.layer, labelPos.numLayers);
            for (var i = 0; i < this.nodeLabels.length; i++) {
                var dy = labelInfo.offsets[labelPos.layer[i]];
                annotationBoxes[i].y += dy;
                annotationBoxes[i].applyTransformation();
                this.masterBlock.add(annotationBoxes[i]);
            }
            return {
                annotationBoxes: annotationBoxes,
                labelBoxes: labelBoxes,
                binding: labelPos,
                info: labelInfo,
                layer2labels: layer2labels
            };
        };
        LineSegment.prototype.placeLabelBackgrounds = function (line, placement) {
            var totalHeight = placement.info.totalHeight;
            for (var _i = 0, _a = placement.binding.priority.reverse(); _i < _a.length; _i++) {
                var key = _a[_i];
                for (var _b = 0, _c = placement.layer2labels[key]; _b < _c.length; _b++) {
                    var i = _c[_b];
                    var lbl = this.nodeLabels[i];
                    var rel_startpos = Math.max(this.start(), lbl.startpos) - this.start();
                    var rel_endpos = Math.min(this.end(), lbl.endpos) - this.start();
                    var b = line.section(rel_startpos, rel_endpos);
                    var rect = new componentmodel_1.AnnoRectElement(b.x, b.y, b.w, b.h, 3, 2);
                    rect.renderLayer = componentmodel_1.RenderLayer.BACKGROUND;
                    rect.styleClass = "annotation-background";
                    rect.fill = util_1.Util.rgb2hex(util_1.Util.hsl2rgb(this.nodeLabels[i].color));
                    this.masterBlock.add(rect);
                }
            }
        };
        LineSegment.prototype.createTextLine = function () {
            var line = new componentmodel_1.AnnoTextLine();
            line.textClass = "annotation-text";
            for (var _i = 0, _a = this.blocks; _i < _a.length; _i++) {
                var block = _a[_i];
                line.add(block.text, block.lengths, block.measurements);
            }
            return line;
        };
        LineSegment.prototype.closestPtByX = function (pt, pts) {
            var selectedPt = pts[0];
            var dist = Math.abs(pt.x - pts[0].x);
            for (var k = 1; k < pts.length; k++) {
                if (Math.abs(pt.x - pts[k].x) < dist) {
                    dist = Math.abs(pt.x - pts[k].x);
                    selectedPt = pts[k];
                }
            }
            return selectedPt;
        };
        LineSegment.prototype.middlePoint = function (ptA, ptB) {
            var leftX = Math.min(ptA.x, ptB.x);
            var rightX = Math.max(ptA.x, ptB.x);
            var topY = Math.min(ptA.y, ptB.y);
            var bottomY = Math.max(ptA.y, ptB.y);
            return { x: leftX + (rightX - leftX) / 2.0, y: topY + (bottomY - topY) / 2.0 };
        };
        LineSegment.prototype.possiblePointsAtNode = function (box) {
            var middlePt = box.anchor(componentmodel_1.AnchorPoint.TOP_CENTER);
            return [
                this.middlePoint(box.anchor(componentmodel_1.AnchorPoint.TOP_LEFT), middlePt),
                middlePt,
                this.middlePoint(box.anchor(componentmodel_1.AnchorPoint.TOP_RIGHT), middlePt)
            ];
        };
        LineSegment.prototype.placeEdges = function (labelPlacement) {
            var bottomY = -labelPlacement.info.totalHeight;
            var edgeFragments = [];
            for (var _i = 0, _a = this.edgeLabels; _i < _a.length; _i++) {
                var edgeLbl = _a[_i];
                var headIdx = this.nodeId2idx[edgeLbl.head];
                var tailIdx = this.nodeId2idx[edgeLbl.tail];
                if (headIdx !== undefined && tailIdx !== undefined) {
                    var tailBox = labelPlacement.labelBoxes[tailIdx];
                    var headBox = labelPlacement.labelBoxes[headIdx];
                    var edgeFragment = new EdgeFragment(this, edgeLbl);
                    var tailPt = tailBox.anchor(componentmodel_1.AnchorPoint.TOP_CENTER);
                    var headPt = headBox.anchor(componentmodel_1.AnchorPoint.TOP_CENTER);
                    edgeFragment.leftAttachmentPt = headPt.x < tailPt.x ? headPt : tailPt;
                    edgeFragment.rightAttachmentPt = headPt.x < tailPt.x ? tailPt : headPt;
                    edgeFragment.leftHasNode = true;
                    edgeFragment.rightHasNode = true;
                    edgeFragment.arrowLeft = headPt.x < tailPt.x;
                    edgeFragment.arrowRight = !edgeFragment.arrowLeft;
                    edgeFragment.optimizeLeft(this.possiblePointsAtNode(headPt.x < tailPt.x ? headBox : tailBox));
                    edgeFragment.optimizeRight(this.possiblePointsAtNode(headPt.x < tailPt.x ? tailBox : headBox));
                    edgeFragments.push(edgeFragment);
                }
                else {
                    if (headIdx === undefined && tailIdx === undefined) {
                        var edgeFragment = new EdgeFragment(this, edgeLbl);
                        edgeFragment.leftAttachmentPt = { x: 0, y: 0 };
                        edgeFragment.rightAttachmentPt = { x: this.parent.width, y: 0 };
                        edgeFragment.leftHasNode = false;
                        edgeFragment.rightHasNode = false;
                        edgeFragment.arrowLeft = edgeLbl.startline < this.linenb;
                        edgeFragment.arrowRight = !edgeFragment.arrowLeft;
                        edgeFragments.push(edgeFragment);
                    }
                    else {
                        var edgeFragment = new EdgeFragment(this, edgeLbl);
                        var sourceIdx = headIdx === undefined ? tailIdx : headIdx;
                        var sourceBox = labelPlacement.labelBoxes[sourceIdx];
                        var targetPt = sourceBox.anchor(componentmodel_1.AnchorPoint.TOP_CENTER);
                        if (edgeLbl.startline < this.linenb) {
                            edgeFragment.leftAttachmentPt = { x: 0, y: 0 };
                            edgeFragment.rightAttachmentPt = targetPt;
                            edgeFragment.leftHasNode = false;
                            edgeFragment.rightHasNode = true;
                            edgeFragment.optimizeRight(this.possiblePointsAtNode(sourceBox));
                            edgeFragment.arrowLeft = (headIdx === undefined) ? true : false;
                            edgeFragment.arrowRight = !edgeFragment.arrowLeft;
                        }
                        else {
                            edgeFragment.leftHasNode = true;
                            edgeFragment.rightHasNode = false;
                            edgeFragment.leftAttachmentPt = targetPt;
                            edgeFragment.rightAttachmentPt = { x: this.parent.width, y: 0 };
                            edgeFragment.optimizeLeft(this.possiblePointsAtNode(sourceBox));
                            edgeFragment.arrowLeft = (headIdx === undefined) ? false : true;
                            edgeFragment.arrowRight = !edgeFragment.arrowLeft;
                        }
                        edgeFragments.push(edgeFragment);
                    }
                }
            }
            var maxHeight = this.resolveEdgePositioning(edgeFragments, bottomY);
            for (var k = 0; k < edgeFragments.length; k++) {
                this.masterBlock.add(edgeFragments[k].compile(this.parent.doc.style.labelPadding, this.parent.doc.style.edgeBendDistance));
            }
            return Math.max(0, maxHeight);
        };
        LineSegment.prototype.resolveEdgePositioning = function (edgeFragments, bottomY) {
            var allocOrder = [];
            var maxX = Number.NEGATIVE_INFINITY;
            for (var k = 0; k < edgeFragments.length; k++) {
                if (edgeFragments[k] !== null) {
                    allocOrder.push(k);
                    maxX = Math.max(maxX, edgeFragments[k].rightX());
                }
            }
            var layerAssignment = {};
            var layers;
            layers = [new util_1.Util.SparseGridSpatialIndex(0, maxX, Math.max(1, Math.floor(maxX / 25)))];
            allocOrder.sort(function (x, y) { return edgeFragments[x].width() - edgeFragments[y].width(); });
            for (var k = 0; k < allocOrder.length; k++) {
                var lbl = this.edgeLabels[allocOrder[k]];
                var assigned = false;
                var frag = edgeFragments[allocOrder[k]];
                var minX = frag.leftX();
                var maxX_1 = frag.rightX();
                for (var i = 0; i < layers.length; i++) {
                    if (layers[i].anyIntersection(minX, maxX_1) === false) {
                        layerAssignment[allocOrder[k]] = i;
                        assigned = true;
                        layers[i].add(minX, maxX_1, allocOrder[k]);
                        break;
                    }
                }
                if (assigned === false) {
                    layers.push(new util_1.Util.SparseGridSpatialIndex(0, maxX_1, Math.max(1, Math.floor(maxX_1 / 25))));
                    layers[layers.length - 1].add(minX, maxX_1, allocOrder[k]);
                    layerAssignment[allocOrder[k]] = layers.length - 1;
                }
            }
            var layerHeights = [];
            for (var k = 0; k < layers.length; k++) {
                layerHeights.push(Number.NEGATIVE_INFINITY);
            }
            for (var k = 0; k < allocOrder.length; k++) {
                var targetLayer = layerAssignment[allocOrder[k]];
                layerHeights[targetLayer] = Math.max(layerHeights[targetLayer], edgeFragments[allocOrder[k]].height());
            }
            var layerOffsets = [];
            var totalheight = this.parent.doc.style.edgeLayerMargin;
            var offset = bottomY - this.parent.doc.style.edgeLayerMargin;
            for (var k = 0; k < layers.length; k++) {
                layerOffsets.push(offset);
                offset -= layerHeights[k];
                totalheight += layerHeights[k];
            }
            for (var k = 0; k < allocOrder.length; k++) {
                edgeFragments[allocOrder[k]].bottomY = layerOffsets[layerAssignment[allocOrder[k]]];
            }
            return totalheight;
        };
        LineSegment.prototype.layout = function (style) {
            var line = this.createTextLine();
            var labelPlacement = this.placeLabels(line);
            var edgeTotalHeight = this.placeEdges(labelPlacement);
            this.placeLabelBackgrounds(line, labelPlacement);
            this.masterBlock.y = edgeTotalHeight + labelPlacement.info.totalHeight;
            this.height = line.height() + edgeTotalHeight + labelPlacement.info.totalHeight;
            this.masterBlock.add(line);
            this.masterBlock.applyTransformation();
        };
        LineSegment.prototype.render = function (g) {
            this.masterBlock.render(g);
        };
        return LineSegment;
    }());
    var Segment = (function () {
        function Segment(doc) {
            this.height = 0.0;
            this.blocks = [];
            this.lines = [];
            this.lineOffsetY = [];
            this.doc = doc;
        }
        Segment.prototype.layout = function () {
            this.lineOffsetY = [];
            this.height = this.doc.style.lineRowPadding;
            for (var _i = 0, _a = this.lines; _i < _a.length; _i++) {
                var line = _a[_i];
                this.lineOffsetY.push(this.height);
                line.layout(this.doc.style);
                this.height += this.doc.style.lineRowPadding + line.height;
            }
        };
        Segment.prototype.render = function (g) {
            for (var k = 0; k < this.lines.length; k++) {
                g.push(0, this.lineOffsetY[k]);
                this.lines[k].render(g);
                g.pop();
            }
            if (this.doc.showSegmentMargin === true) {
                var labelOffsetX = -this.doc.segmentMarginWidth - this.doc.style.segmentInnerPadding;
                labelOffsetX += (this.doc.segmentMarginWidth - this.marginMeas.w - this.doc.style.segmentMarginPaddingRight);
                g.styleClass = "annotation-margin";
                g.addText(componentmodel_1.RenderLayer.TEXT, this.marginText, labelOffsetX, this.lineOffsetY[0] + this.lines[0].height - (this.marginMeas.h + this.marginMeas.y));
            }
        };
        return Segment;
    }());
    var FlowDocument = (function () {
        function FlowDocument(parentElem, raw) {
            this.layers = {};
            this.segments = [];
            this.nodeLabels = [];
            this.edgeLabels = [];
            this.uniqueCoordinates = [];
            this.showSegmentMargin = true;
            this.segmentStart = undefined;
            this.segmentEnd = undefined;
            this.segStart = 0;
            this.segEnd = 0;
            this.boundLabels = {};
            this.style = {
                lineRowPadding: 10,
                segmentRowPadding: 0,
                segmentMarginPaddingLeft: 5,
                segmentMarginPaddingRight: 2,
                segmentInnerPadding: 10,
                showSegmentMarginLine: true,
                curlyHeight: 5,
                labelMargin: 2,
                labelPadding: 2,
                triangleMargin: 0,
                minEdgeLineLength: 5,
                edgeBendDistance: 10,
                edgeLayerMargin: 10
            };
            this.setTarget(parentElem);
            for (var _i = 0, _a = Object.keys(raw.nodeLayers); _i < _a.length; _i++) {
                var layer = _a[_i];
                this.layers[layer] = true;
            }
            this.segment(raw);
            this.measureAll(raw);
            this.colorizeLabels();
        }
        FlowDocument.prototype.setTarget = function (el) {
            this.parentElem = el;
            this.renderElem = document.createElementNS(componentmodel_1.Svg.NS, "svg");
            this.parentElem.appendChild(this.renderElem);
        };
        FlowDocument.prototype.setLayers = function (layerList) {
            this.layers = {};
            for (var _i = 0, layerList_1 = layerList; _i < layerList_1.length; _i++) {
                var layer = layerList_1[_i];
                this.layers[layer] = true;
            }
        };
        FlowDocument.prototype.partials = function (raw) {
            var allRanges = [];
            for (var _i = 0, _a = raw.nodeLayers; _i < _a.length; _i++) {
                var layer = _a[_i];
                for (var _b = 0, _c = layer.ranges; _b < _c.length; _b++) {
                    var range = _c[_b];
                    allRanges.push(range);
                }
            }
            allRanges.push(0);
            allRanges.push(raw.text.length);
            allRanges.sort(function (x, y) { return x - y; });
            var uniqueRanges = [];
            var last = -1;
            for (var _d = 0, allRanges_1 = allRanges; _d < allRanges_1.length; _d++) {
                var range = allRanges_1[_d];
                if (last !== range) {
                    uniqueRanges.push(range);
                    last = range;
                }
            }
            return uniqueRanges;
        };
        FlowDocument.prototype.segment = function (raw) {
            this.text = raw.text.replace(/\s/g, "\xa0");
            var re = /\s+/g;
            var partialRanges = this.partials(raw);
            var partialPos = 0;
            this.uniqueCoordinates = partialRanges;
            for (var i = 0; i < raw.segments.length; i += 2) {
                var seg = new Segment(this);
                seg.startpos = raw.segments[i];
                seg.endpos = raw.segments[i + 1];
                seg.text = raw.text.substring(seg.startpos, seg.endpos);
                seg.marginText = ((i / 2) + 1).toString(10);
                var m = void 0;
                var lastPosition = 0;
                while ((m = re.exec(seg.text)) !== null) {
                    var startloc = lastPosition + seg.startpos;
                    var endloc = m.index + m.length + seg.startpos;
                    seg.blocks.push({ startpos: startloc, endpos: endloc, text: this.text.substring(startloc, endloc), measurements: [], lengths: [] });
                    lastPosition = m.index + m.length;
                    if (m.index === re.lastIndex) {
                        re.lastIndex++;
                    }
                }
                if (lastPosition !== seg.text.length) {
                    seg.blocks.push({ startpos: lastPosition + seg.startpos, endpos: seg.text.length + seg.startpos, text: this.text.substring(lastPosition + seg.startpos, seg.startpos + seg.text.length), measurements: [], lengths: [] });
                }
                var k = 0;
                while (partialPos < partialRanges.length && k < seg.blocks.length) {
                    if (partialRanges[partialPos] <= seg.blocks[k].startpos) {
                        partialPos++;
                    }
                    else if (partialRanges[partialPos] > seg.blocks[k].endpos) {
                        k++;
                    }
                    else {
                        var length_1 = partialRanges[partialPos] - seg.blocks[k].startpos;
                        seg.blocks[k].lengths.push(length_1);
                        partialPos++;
                    }
                }
                for (var _i = 0, _a = seg.blocks; _i < _a.length; _i++) {
                    var block = _a[_i];
                    if (block.lengths.length > 0 && block.lengths[block.lengths.length - 1] !== block.endpos - block.startpos) {
                        block.lengths.push(block.endpos - block.startpos);
                    }
                    else if (block.lengths.length === 0) {
                        block.lengths.push(block.endpos - block.startpos);
                    }
                }
                this.segments.push(seg);
            }
        };
        FlowDocument.prototype.measureAll = function (raw) {
            var textMeasurements = [];
            var textClasses = [];
            for (var _i = 0, _a = this.segments; _i < _a.length; _i++) {
                var seg = _a[_i];
                for (var _b = 0, _c = seg.blocks; _b < _c.length; _b++) {
                    var block = _c[_b];
                    for (var _d = 0, _e = block.lengths; _d < _e.length; _d++) {
                        var len = _e[_d];
                        textMeasurements.push(this.text.substring(block.startpos, block.startpos + len));
                        textClasses.push("annotation-text");
                    }
                }
                textMeasurements.push(seg.marginText);
                textClasses.push("annotation-margin");
            }
            for (var _f = 0, _g = raw.nodeLayers; _f < _g.length; _f++) {
                var layer = _g[_f];
                for (var _h = 0, _j = Object.keys(layer.labels); _h < _j.length; _h++) {
                    var key = _j[_h];
                    textMeasurements.push(layer.labels[key]);
                    textClasses.push("annotation-label");
                }
                textMeasurements.push(layer.name);
                textClasses.push("annotation-label");
            }
            for (var _k = 0, _l = raw.edgeLayers; _k < _l.length; _k++) {
                var layer = _l[_k];
                for (var _m = 0, _o = Object.keys(layer.labels); _m < _o.length; _m++) {
                    var key = _o[_m];
                    textMeasurements.push(layer.labels[key]);
                    textClasses.push("annotation-label");
                }
                textMeasurements.push(layer.name);
                textClasses.push("annotation-label");
            }
            var measurements = componentmodel_1.Svg.measureTexts(this.renderElem, textMeasurements, textClasses);
            var textMap = measurements["annotation-text"];
            for (var _p = 0, _q = this.segments; _p < _q.length; _p++) {
                var seg = _q[_p];
                for (var _r = 0, _s = seg.blocks; _r < _s.length; _r++) {
                    var block = _s[_r];
                    for (var _t = 0, _u = block.lengths; _t < _u.length; _t++) {
                        var len = _u[_t];
                        block.measurements.push(textMap[this.text.substring(block.startpos, block.startpos + len)]);
                    }
                }
                seg.marginMeas = measurements["annotation-margin"][seg.marginText];
            }
            for (var _v = 0, _w = raw.nodeLayers; _v < _w.length; _v++) {
                var layer = _w[_v];
                for (var k = 0; k < layer.ranges.length / 2; k++) {
                    var labelfrag = new NodeLabelFragment();
                    labelfrag.id = layer.id[k];
                    labelfrag.layer = "node/" + layer.name;
                    labelfrag.label = layer.labels[labelfrag.id];
                    if (labelfrag.label === undefined) {
                        labelfrag.label = layer.name;
                    }
                    labelfrag.startpos = layer.ranges[k * 2];
                    labelfrag.endpos = layer.ranges[k * 2 + 1];
                    labelfrag.bbox = measurements["annotation-label"][labelfrag.label];
                    labelfrag.popup = layer.popups[labelfrag.id];
                    this.nodeLabels.push(labelfrag);
                }
            }
            for (var _x = 0, _y = raw.edgeLayers; _x < _y.length; _x++) {
                var layer = _y[_x];
                for (var k = 0; k < layer.id.length; k++) {
                    var labelfrag = new EdgeLabelFragment();
                    labelfrag.id = layer.id[k];
                    labelfrag.layer = "edge/" + layer.name;
                    labelfrag.label = layer.labels[labelfrag.id];
                    if (labelfrag.label === undefined) {
                        labelfrag.label = layer.name;
                    }
                    labelfrag.head = layer.head[k];
                    labelfrag.tail = layer.tail[k];
                    labelfrag.bbox = measurements["annotation-label"][labelfrag.label];
                    labelfrag.popup = layer.popups[labelfrag.id];
                    this.edgeLabels.push(labelfrag);
                }
            }
        };
        FlowDocument.prototype.colorizeLabels = function () {
            var uniqueLabels = {};
            for (var _i = 0, _a = this.nodeLabels; _i < _a.length; _i++) {
                var lbl = _a[_i];
                uniqueLabels[lbl.label] = true;
            }
            for (var _b = 0, _c = this.edgeLabels; _b < _c.length; _b++) {
                var lbl = _c[_b];
                uniqueLabels[lbl.label] = true;
            }
            var colors = util_1.Util.generatePastelColors(Object.keys(uniqueLabels).length);
            var strokeColors = [];
            var darkFactor = util_1.Util.hsl(1, 0.75, 0.5);
            for (var _d = 0, colors_1 = colors; _d < colors_1.length; _d++) {
                var clr = colors_1[_d];
                strokeColors.push(util_1.Util.mulHsl(clr, darkFactor));
            }
            var lblColor = {};
            var lblStrokeColor = {};
            var m = 0;
            for (var _e = 0, _f = Object.keys(uniqueLabels); _e < _f.length; _e++) {
                var lblKey = _f[_e];
                lblColor[lblKey] = colors[m];
                lblStrokeColor[lblKey] = strokeColors[m];
                m += 1;
            }
            for (var _g = 0, _h = this.nodeLabels; _g < _h.length; _g++) {
                var lbl = _h[_g];
                lbl.color = lblColor[lbl.label];
                lbl.strokeColor = lblStrokeColor[lbl.label];
            }
            for (var _j = 0, _k = this.edgeLabels; _j < _k.length; _j++) {
                var lbl = _k[_j];
                lbl.color = lblColor[lbl.label];
                lbl.strokeColor = lblStrokeColor[lbl.label];
            }
        };
        FlowDocument.prototype.lineflow = function () {
            var segmentMaxWidth = this.maxWidth;
            var maxMarginWidth = 0.0;
            if (this.showSegmentMargin === true) {
                for (var _i = 0, _a = this.segments; _i < _a.length; _i++) {
                    var seg = _a[_i];
                    maxMarginWidth = Math.max(maxMarginWidth, seg.marginMeas.w);
                }
                maxMarginWidth += this.style.segmentMarginPaddingLeft + this.style.segmentMarginPaddingRight;
            }
            this.segmentMarginWidth = maxMarginWidth;
            segmentMaxWidth -= maxMarginWidth + this.style.segmentInnerPadding * 2;
            var actualMaxWidth = segmentMaxWidth;
            var lineCnt = 0;
            for (var k = 0; k < this.segments.length; k++) {
                var seg = this.segments[k];
                for (var _b = 0, _c = seg.lines; _b < _c.length; _b++) {
                    var line_1 = _c[_b];
                    line_1.parent = undefined;
                }
                seg.width = segmentMaxWidth;
                seg.lines = [];
                var line = new LineSegment(seg);
                var widthAccum = 0.0;
                for (var k_1 = 0; k_1 < seg.blocks.length; k_1++) {
                    var block = seg.blocks[k_1];
                    var full = block.measurements[block.measurements.length - 1];
                    var width = full.w - full.x;
                    if (line.blocks.length === 0) {
                        line.blocks.push(block);
                        widthAccum += width;
                    }
                    else if (widthAccum + width > segmentMaxWidth) {
                        seg.lines.push(line);
                        line.linenb = lineCnt;
                        lineCnt += 1;
                        line = new LineSegment(seg);
                        widthAccum = 0.0;
                        line.blocks.push(block);
                        widthAccum += width;
                    }
                    else {
                        line.blocks.push(block);
                        widthAccum += width;
                    }
                    actualMaxWidth = Math.max(actualMaxWidth, widthAccum);
                }
                if (line.blocks.length !== 0) {
                    seg.lines.push(line);
                    line.linenb = lineCnt;
                    lineCnt += 1;
                }
            }
            this.actualMaxWidth = Math.max(this.maxWidth, actualMaxWidth + maxMarginWidth + this.style.segmentInnerPadding * 2);
        };
        FlowDocument.prototype.assignLabels = function () {
            var i = 0;
            var k = 0;
            var lines = [];
            for (var _i = 0, _a = this.segments; _i < _a.length; _i++) {
                var seg = _a[_i];
                for (var _b = 0, _c = seg.lines; _b < _c.length; _b++) {
                    var ln = _c[_b];
                    lines.push(ln);
                }
            }
            var mapping = {};
            while (i < lines.length && k < this.uniqueCoordinates.length) {
                if (lines[i].start() > this.uniqueCoordinates[k]) {
                    k++;
                    console.log("Should not have happend!");
                }
                else if (this.uniqueCoordinates[k] > lines[i].end()) {
                    i++;
                }
                else {
                    mapping[this.uniqueCoordinates[k]] = i;
                    k++;
                }
            }
            var nodeLineMapping = {};
            for (var _d = 0, _e = this.nodeLabels; _d < _e.length; _d++) {
                var lbl = _e[_d];
                if (this.layers[lbl.layer] === true) {
                    var startline = mapping[lbl.startpos];
                    var endline = mapping[lbl.endpos];
                    nodeLineMapping[lbl.id] = [startline, endline + 1];
                    for (var h = startline; h <= endline; h++) {
                        if (lines[h].start() < lbl.endpos && lines[h].end() > lbl.startpos) {
                            lines[h].nodeId2idx[lbl.id] = lines[h].nodeLabels.length;
                            lines[h].nodeLabels.push(lbl);
                        }
                    }
                }
            }
            for (var _f = 0, _g = this.edgeLabels; _f < _g.length; _f++) {
                var lbl = _g[_f];
                if (this.layers[lbl.layer] === true) {
                    var headStartEnd = nodeLineMapping[lbl.head];
                    var tailStartEnd = nodeLineMapping[lbl.tail];
                    if (headStartEnd !== undefined && tailStartEnd !== undefined) {
                        var startline = Math.min(headStartEnd[0], tailStartEnd[0]);
                        var endline = Math.max(headStartEnd[1], tailStartEnd[1]);
                        var actualStartLine = lines.length + 1;
                        var actualEndLine = -1;
                        for (var h = startline; h < endline; h++) {
                            if (lines[h].nodeId2idx[lbl.head] !== undefined || lines[h].nodeId2idx[lbl.tail] !== undefined) {
                                actualStartLine = Math.min(actualStartLine, h);
                                actualEndLine = Math.max(actualEndLine, h);
                            }
                        }
                        actualEndLine += 1;
                        lbl.startline = actualStartLine;
                        lbl.endline = actualEndLine + 1;
                        for (var h = actualStartLine; h < actualEndLine; h++) {
                            lines[h].edgeLabels.push(lbl);
                        }
                    }
                }
            }
        };
        FlowDocument.prototype.remove = function () {
            for (var _i = 0, _a = Object.keys(this.boundLabels); _i < _a.length; _i++) {
                var key = _a[_i];
                $(this.boundLabels[key]).empty();
            }
            this.boundLabels = {};
            this.renderElem.innerHTML = null;
        };
        FlowDocument.prototype.update = function () {
            this.remove();
            this.segStart = this.segmentStart || 0;
            this.segEnd = Math.min(this.segmentEnd || this.segments.length, this.segments.length);
            this.maxWidth = parseFloat(window.getComputedStyle(this.parentElem, null).width);
            this.lineflow();
            this.assignLabels();
            var segmentOffsetY = [];
            var accumHeight = this.style.segmentRowPadding;
            for (var k = this.segStart; k < this.segEnd; k++) {
                var seg = this.segments[k];
                seg.layout();
                segmentOffsetY.push(accumHeight);
                accumHeight += seg.height + this.style.segmentRowPadding;
            }
            this.height = accumHeight;
            $(this.renderElem).detach();
            this.renderElem.setAttribute("style", "width:" + this.actualMaxWidth + "px; height:" + this.height + "px;");
            this.renderElem.setAttribute("version", "1.2");
            this.renderElem.setAttribute("class", "annotation-box");
            this.renderElem.innerHTML = "<defs>" +
                "<marker id=\"arrow\" markerWidth=\"10\" markerHeight=\"10\" refx=\"9\" refy=\"3\" orient=\"auto\" markerUnits=\"userSpaceOnUse\">" +
                "<path d=\'M0,0 Q 3,3 0,6 L9,3 z\' fill=\'#000\' />" +
                "</marker>" +
                "</defs>";
            var g = new componentmodel_1.AnnoGraphics(this.renderElem);
            for (var k = this.segStart; k < this.segEnd; k++) {
                var seg = this.segments[k];
                g.layerClasses[componentmodel_1.RenderLayer.LABEL] = "annotation-segment";
                g.push(this.segmentMarginWidth + this.style.segmentInnerPadding, segmentOffsetY[k], "seg" + k);
                seg.render(g);
                g.pop();
            }
            if (this.showSegmentMargin === true) {
                g.clearStyle();
                g.styleClass = "annotation-margin-line";
                g.addLine(componentmodel_1.RenderLayer.BACKGROUND, this.segmentMarginWidth, 0, this.segmentMarginWidth, this.height);
            }
            g.write();
            this.boundLabels = {};
            var self = this;
            $(this.renderElem).find(".annotation-label").each(function (idx, el) {
                $(el).on("mouseenter", function (event) {
                    if (self.boundLabels[idx] === undefined) {
                        self.boundLabels[idx] = el;
                        $(this).qtip({
                            content: {
                                attr: "data-popup-html",
                                title: {
                                    text: function (event, api) {
                                        return $(this).attr("data-popup-title");
                                    },
                                    button: true
                                }
                            },
                            show: {
                                delay: 100,
                                ready: true
                            },
                            hide: {
                                fixed: true,
                                delay: 300
                            },
                            events: {
                                show: function (event, api) {
                                    $(api.elements.titlebar).on("click", function (event) {
                                        if (api.get("hide.event") !== false) {
                                            $(this).find(".qtip-title").prepend($("<i class=\"pin icon\"></i>"));
                                            api.set("hide.event", false);
                                        }
                                    });
                                    $(this).draggable({
                                        containment: "window",
                                        handle: api.elements.titlebar,
                                        drag: function (event, ui) {
                                            if (api.get("hide.event") !== false) {
                                                $(this).find(".qtip-title").prepend($("<i class=\"pin icon\"></i>"));
                                                api.set("hide.event", false);
                                            }
                                        }
                                    });
                                },
                                hide: function (event, api) {
                                    api.set('hide.event', 'mouseleave');
                                },
                                hidden: function (event, api) {
                                    $(this).find(".icon").remove();
                                }
                            },
                            position: {
                                my: "bottom center",
                                at: "top center",
                                viewport: $(window)
                            },
                            style: { classes: "qtip-rounded qtip-light qtip-shadow" }
                        });
                    }
                });
            });
            $(this.parentElem).append(this.renderElem);
        };
        return FlowDocument;
    }());
    exports.FlowDocument = FlowDocument;
});
define("constants", ["require", "exports"], function (require, exports) {
    "use strict";
    var ENTER_KEY = 13;
    exports.ENTER_KEY = ENTER_KEY;
    var ESCAPE_KEY = 27;
    exports.ESCAPE_KEY = ESCAPE_KEY;
});
define("AnnotationView", ["require", "exports", "react", "react-dom", "annotations/flowdocument"], function (require, exports, React, ReactDOM, flowdocument_1) {
    "use strict";
    var AnnotationView = (function (_super) {
        __extends(AnnotationView, _super);
        function AnnotationView(props) {
            var _this = this;
            _super.call(this, props);
            this.docChanged = true;
            this.showMoreListener = function (evt) { return _this.showMore(); };
            this.showAllListener = function (evt) { return _this.showAllToggle(); };
            this.lastActiveTimeout = undefined;
            this.windowResizerEventHandler = (function () { _this.windowResized(); });
            this.state = { ready: false, maxSegments: 10 };
        }
        AnnotationView.prototype.componentWillUnmount = function () {
            if (this.doc !== undefined) {
                this.doc.remove();
                this.renderElem.innerHTML = "";
            }
            this.renderElem = undefined;
            window.removeEventListener("resize", this.windowResizerEventHandler);
            var showMoreButton = ReactDOM.findDOMNode(this.refs["show-more"]);
            showMoreButton.removeEventListener("click", this.showMoreListener);
        };
        AnnotationView.prototype.componentWillReceiveProps = function (nextProps) {
            if (nextProps.doc !== undefined && this.props.doc !== nextProps.doc) {
                this.docChanged = true;
                this.setState({ doc: this.convert(nextProps.doc) });
            }
            else if (nextProps.doc === undefined) {
                this.docChanged = true;
                this.setState({ doc: undefined });
            }
        };
        AnnotationView.prototype.componentDidUpdate = function () {
            var _this = this;
            if (this.state.doc === undefined && this.props.doc !== undefined) {
                this.setState({ doc: this.convert(this.props.doc) });
            }
            if (this.state.doc === undefined && this.doc !== undefined) {
                this.doc.remove();
                this.doc = undefined;
                this.renderElem.innerHTML = "";
            }
            if (this.state.doc !== undefined) {
                var loaderElem_1 = document.createElement("div");
                loaderElem_1.setAttribute("class", "ui small text loader");
                loaderElem_1.innerText = "Rendering...";
                if (this.doc !== undefined) {
                    this.doc.remove();
                    this.renderElem.innerHTML = "";
                }
                this.renderElem.appendChild(loaderElem_1);
                loaderElem_1.style.display = "none";
                loaderElem_1.style.display = "block";
                this.renderElem.style.minHeight = "200px";
                if (this.docChanged === true) {
                    this.doc = new flowdocument_1.FlowDocument(this.renderElem, this.state.doc);
                    this.doc.showSegmentMargin = false;
                    this.docChanged = false;
                    this.doc.segmentStart = 0;
                }
                if ((this.props.showAllSegments || false) === true) {
                    this.doc.segmentEnd = this.doc.segments.length;
                }
                else {
                    this.doc.segmentEnd = this.state.maxSegments;
                }
                setTimeout(function () {
                    _this.doc.setLayers(_this.props.layers);
                    _this.doc.update();
                    _this.renderElem.removeChild(loaderElem_1);
                    _this.renderElem.style.minHeight = undefined;
                });
            }
        };
        AnnotationView.prototype.formatPropertyValue = function (text) {
            if (text === null) {
                return "null";
            }
            if (text.length >= 4 && text.substring(0, 4) === "urn:") {
                var wd = /urn:wikidata:([QP][0-9]+)/;
                var m = void 0;
                if ((m = wd.exec(text)) !== null) {
                    return "<a href=\"http://www.wikidata.org/wiki/" + m[1] + "\">" + text + "</a>";
                }
                var wp = /urn:wikipedia:([a-z]+):(.+)/;
                if ((m = wp.exec(text)) !== null) {
                    return "<a href=\"http://" + m[1] + ".wikipedia.org/wiki/" + encodeURIComponent(m[2]) + "\">" + he.encode(text) + "</a>";
                }
            }
            if (text.length > 5 && text.substring(0, 7).toLowerCase() === "http://") {
                return "<a href=\"" + text + "\">" + he.encode(text) + "</a>";
            }
            return text;
        };
        AnnotationView.prototype.formatProperties = function (properties) {
            var _this = this;
            var text = ["<table class='prop-table'><thead><tr>"];
            $.each(properties, function (key, value) {
                text.push("<th>" + he.encode(key) + "</th>");
            });
            text.push("</tr></thead><tbody><tr>");
            $.each(properties, function (key, value) {
                text.push("<td>" + _this.formatPropertyValue(value) + "</td>");
            });
            text.push("</tr></tbody></table>");
            return text.join("");
        };
        AnnotationView.prototype.showAllToggle = function () {
            if (this.state.doc !== undefined) {
                if (this.state.maxSegments < (this.state.doc.segments.length / 2)) {
                    this.setState({ maxSegments: this.state.doc.segments.length / 2 });
                }
                else {
                    this.setState({ maxSegments: 10 });
                }
            }
        };
        AnnotationView.prototype.abbreviateLayerName = function (layerName, usedNames) {
            var letters = [];
            var positions = [];
            var re = /([A-Z])/g;
            var m;
            while ((m = re.exec(layerName)) !== null) {
                if (m.index === re.lastIndex) {
                    re.lastIndex++;
                }
                letters.push(m[1]);
                positions.push(m.index);
            }
            var candidate = letters.join("");
            if (usedNames[candidate] === undefined) {
                return candidate;
            }
            candidate = letters.slice(0, letters.length - 1).join("") + layerName.substring(positions[positions.length - 1]);
            if (usedNames[candidate] === undefined) {
                return candidate;
            }
            return layerName;
        };
        AnnotationView.prototype.convert = function (doc) {
            var rawDoc = {
                text: doc.text,
                nodeLayers: [],
                edgeLayers: [],
                segments: []
            };
            var usedNames = {};
            var nodeId2fullId = {};
            for (var _i = 0, _a = Object.keys(doc.nodelayers); _i < _a.length; _i++) {
                var layername = _a[_i];
                var nodeLayer = {
                    name: layername,
                    id: [],
                    ranges: [],
                    labels: {},
                    popups: {}
                };
                var idprefix = this.abbreviateLayerName(layername, usedNames);
                usedNames[idprefix] = true;
                for (var _b = 0, _c = doc.nodelayers[layername]; _b < _c.length; _b++) {
                    var node = _c[_b];
                    var id = idprefix + node.id;
                    nodeLayer.id.push(id);
                    nodeLayer.ranges.push(node.start);
                    nodeLayer.ranges.push(node.end);
                    nodeLayer.labels[id] = node.label;
                    nodeLayer.popups[id] = this.formatProperties(node.properties);
                    nodeId2fullId[node.id] = id;
                }
                rawDoc.nodeLayers.push(nodeLayer);
            }
            for (var _d = 0, _e = Object.keys(doc.edgelayers); _d < _e.length; _d++) {
                var layername = _e[_d];
                var edgeLayer = {
                    name: layername,
                    id: [],
                    head: [],
                    tail: [],
                    labels: {},
                    popups: {}
                };
                var idprefix = this.abbreviateLayerName(layername, usedNames);
                usedNames[idprefix] = true;
                for (var _f = 0, _g = doc.edgelayers[layername]; _f < _g.length; _f++) {
                    var edge = _g[_f];
                    var id = idprefix + edge.id;
                    edgeLayer.id.push(id);
                    edgeLayer.head.push(nodeId2fullId[edge.head]);
                    edgeLayer.tail.push(nodeId2fullId[edge.tail]);
                    edgeLayer.labels[id] = edge.label;
                    edgeLayer.popups[id] = this.formatProperties(edge.properties);
                }
                rawDoc.edgeLayers.push(edgeLayer);
            }
            var re = /\n/g;
            var lastPosition = 0;
            var m;
            while ((m = re.exec(doc.text)) !== null) {
                if (m.index === re.lastIndex) {
                    re.lastIndex++;
                }
                if (lastPosition !== m.index) {
                    rawDoc.segments.push(lastPosition);
                    rawDoc.segments.push(m.index);
                }
                else {
                    rawDoc.segments.push(m.index);
                    rawDoc.segments.push(m.index + m.length);
                }
                lastPosition = m.index + m.length;
            }
            if (lastPosition !== doc.text.length) {
                rawDoc.segments.push(lastPosition);
                rawDoc.segments.push(doc.text.length);
            }
            return rawDoc;
        };
        AnnotationView.prototype.windowResized = function () {
            var _this = this;
            if (this.lastActiveTimeout !== undefined) {
                window.clearTimeout(this.lastActiveTimeout);
            }
            this.lastActiveTimeout = window.setTimeout(function () {
                _this.forceUpdate();
                _this.lastActiveTimeout = undefined;
            }, 300);
        };
        AnnotationView.prototype.showMore = function () {
            this.setState({ maxSegments: this.state.maxSegments + 10 });
        };
        AnnotationView.prototype.componentDidMount = function () {
            this.renderElem = ReactDOM.findDOMNode(this.refs["anno-component"]);
            var showMoreButton = ReactDOM.findDOMNode(this.refs["show-more"]);
            var showAllButton = ReactDOM.findDOMNode(this.refs["show-all"]);
            showMoreButton.addEventListener("click", this.showMoreListener);
            showAllButton.addEventListener("click", this.showAllListener);
            window.addEventListener("resize", this.windowResizerEventHandler, true);
            this.docChanged = true;
            this.componentDidUpdate();
        };
        AnnotationView.prototype.render = function () {
            var showMoreButtonVisible = false;
            if (this.state.doc !== undefined
                && (this.props.showAllSegments || false) === false
                && this.state.maxSegments < (this.state.doc.segments.length / 2)) {
                showMoreButtonVisible = true;
            }
            var showAllButton = this.state.doc !== undefined
                && (this.props.showAllSegments || false) === false
                && this.state.doc.segments.length > 20;
            return (React.createElement("div", null, 
                React.createElement("div", {ref: "anno-component"}), 
                React.createElement("div", {className: "two ui buttons"}, 
                    React.createElement("button", {ref: "show-more", className: "fluid ui button " + (showAllButton === true && showMoreButtonVisible === false ? "disabled" : ""), style: { display: (showAllButton || showMoreButtonVisible === true ? "block" : "none") }}, 
                        React.createElement("i", {className: "angle double down icon"}), 
                        "Show more..."), 
                    React.createElement("button", {ref: "show-all", style: { display: (showAllButton ? "block" : "none") }, className: "fluid ui button" + (showMoreButtonVisible === false ? " positive" : "")}, 
                        React.createElement("i", {className: "expand icon"}), 
                        "Show all"))));
        };
        return AnnotationView;
    }(React.Component));
    exports.AnnotationView = AnnotationView;
});
define("MultipleSelectDropdown", ["require", "exports", "react", "react-dom"], function (require, exports, React, ReactDOM) {
    "use strict";
    var MultipleSelectDropdown = (function (_super) {
        __extends(MultipleSelectDropdown, _super);
        function MultipleSelectDropdown(props) {
            _super.call(this, props);
            this.deltaVersion = 0;
            this.dropdownVisible = false;
            this.state = { selected: props.selected };
        }
        MultipleSelectDropdown.prototype.propagate = function () {
            var _this = this;
            window.setTimeout(function () { return _this.props.onSelected(_this.state.selected); }, 100);
        };
        MultipleSelectDropdown.prototype.componentWillReceiveProps = function (nextProps) {
            if (nextProps.selected !== undefined) {
                var layerselect = ReactDOM.findDOMNode(this.refs["annotation-layers"]);
                var el = $(layerselect);
                el.dropdown("set selected", nextProps.selected);
            }
        };
        MultipleSelectDropdown.prototype.componentDidMount = function () {
            var _this = this;
            var layerselect = ReactDOM.findDOMNode(this.refs["annotation-layers"]);
            var el = $(layerselect);
            el.dropdown({
                allowAdditions: true,
                onChange: function (selected) {
                    _this.state.selected = selected;
                    if (_this.dropdownVisible === false) {
                        _this.propagate();
                    }
                    else {
                        _this.deltaVersion += 1;
                    }
                },
                onShow: function (evt) {
                    _this.dropdownVisible = true;
                },
                onHide: function (evt) {
                    _this.dropdownVisible = false;
                    if (_this.deltaVersion > 0) {
                        _this.propagate();
                        _this.deltaVersion = 0;
                    }
                    return true;
                }
            });
            el.dropdown("set selected", this.props.selected);
        };
        MultipleSelectDropdown.prototype.render = function () {
            var options = [];
            $.each(this.props.options, function (key, value) {
                options.push(React.createElement("option", {key: key, value: key}, value));
            });
            return React.createElement("select", {ref: "annotation-layers", multiple: "true", className: "ui fluid dropdown"}, 
                React.createElement("option", {key: "__DEFAULT__", value: ""}, this.props.text), 
                options);
        };
        return MultipleSelectDropdown;
    }(React.Component));
    exports.MultipleSelectDropdown = MultipleSelectDropdown;
});
define("DocumentView", ["require", "exports", "react", "MultipleSelectDropdown", "AnnotationView"], function (require, exports, React, MultipleSelectDropdown_1, AnnotationView_1) {
    "use strict";
    var DocumentView = (function (_super) {
        __extends(DocumentView, _super);
        function DocumentView(props) {
            _super.call(this, props);
            this.state = { selectedLayers: [] };
        }
        DocumentView.prototype.computeFragments = function (datamodel) {
            if (datamodel.fragments === undefined) {
                datamodel.fragments = {};
                datamodel.fragmentlist = [];
                if (datamodel.nodelayers["Paragraph"]) {
                    $.each(datamodel.nodelayers["Paragraph"], function (key, value) {
                        datamodel.fragments[key] = { start: value.start, end: value.end };
                        datamodel.fragmentlist.push(key);
                    });
                }
                else if (datamodel.nodelayers["Sentence"]) {
                    $.each(datamodel.nodelayers["Sentence"], function (key, value) {
                        datamodel.fragments[key] = { start: value.start, end: value.end };
                        datamodel.fragmentlist.push(key);
                    });
                }
                else {
                    var key = 1;
                    var re = /((?:(?:.+)\n\s*)+\n\s*)|(.+)/g;
                    var m = void 0;
                    var last = 0;
                    if (datamodel.text.indexOf("\n\n") > 0) {
                        datamodel.text += "\n\n";
                    }
                    while ((m = re.exec(datamodel.text)) !== null) {
                        if (m.index === re.lastIndex) {
                            re.lastIndex++;
                        }
                        if (last !== m.index) {
                            datamodel.fragments[key] = { start: last, end: m.index };
                            datamodel.fragmentlist.push(key);
                            key += 1;
                            last = m.index;
                        }
                    }
                    datamodel.fragments[key] = { start: last, end: datamodel.text.length };
                    datamodel.fragmentlist.push(key);
                    datamodel.sentences = [];
                    var re2 = /(\n\s*)/gm;
                    last = 0;
                    while ((m = re2.exec(datamodel.text)) !== null) {
                        if (last !== m.index) {
                            datamodel.sentences.push(last);
                            datamodel.sentences.push(m.index);
                            last = m.index + m[1].length;
                        }
                    }
                    datamodel.sentences.push(last);
                    datamodel.sentences.push(datamodel.text.length);
                }
            }
        };
        DocumentView.prototype.componentWillReceiveProps = function (nextProps) {
            if (nextProps.annodata !== undefined) {
                this.computeFragments(nextProps.annodata);
            }
        };
        DocumentView.prototype.handleLayersChanged = function (selected) {
            this.setState({ selectedLayers: selected });
        };
        DocumentView.prototype.render = function () {
            if (this.props.annoview === true) {
                var options_1 = {};
                if (this.props.annodata) {
                    Object.keys(this.props.annodata.nodelayers).forEach(function (key) {
                        options_1["node/" + key] = key + " (Nodes)";
                    });
                    Object.keys(this.props.annodata.edgelayers).forEach(function (key) {
                        options_1["edge/" + key] = key + " (Edges)";
                    });
                }
                return React.createElement("div", null, 
                    React.createElement(MultipleSelectDropdown_1.MultipleSelectDropdown, {text: "Layers", onSelected: this.handleLayersChanged.bind(this), selected: this.state.selectedLayers, options: options_1}), 
                    React.createElement(AnnotationView_1.AnnotationView, {layers: this.state.selectedLayers, doc: this.props.annodata}));
            }
            else {
                var lines = [];
                var reg = /\n/g;
                var last = 0;
                var found = void 0;
                var i = 0;
                while (found = reg.exec(this.props.text)) {
                    lines.push(React.createElement("span", {key: "e" + (++i)}, this.props.text.substring(last, found.index)));
                    lines.push(React.createElement("br", {key: "e" + (++i)}));
                    last = found.index + found.length;
                }
                if (last !== this.props.text.length) {
                    lines.push(this.props.text.substring(last));
                }
                else {
                    lines.pop();
                }
                return React.createElement("p", null, lines);
            }
        };
        return DocumentView;
    }(React.Component));
    exports.DocumentView = DocumentView;
});
define("app", ["require", "exports", "react", "react-dom", "DocumentView"], function (require, exports, React, ReactDOM, DocumentView_1) {
    "use strict";
    var NlpApp = (function (_super) {
        __extends(NlpApp, _super);
        function NlpApp(props) {
            _super.call(this, props);
            this.wikiEditionFullName = { "en": "English", "sv": "Swedish", "ru": "Russian", "es": "Spanish", "de": "German", "fr": "French" };
            this.state = { annotating: false, text: "", config: "en/default" };
        }
        NlpApp.prototype.annotate_text = function (text) {
            var _this = this;
            this.setState({ annotating: true, doc: undefined });
            $.ajax("./" + this.state.config + "/api/annoviz", {
                data: text,
                processData: false,
                type: "POST",
                contentType: "text/plain; charset=utf-8"
            }).done(function (data) {
                var result = data;
                _this.setState({ annotating: false, doc: result });
            }).fail(function (xhr, textStatus, errorThrown) {
                _this.setState({ annotating: false });
                console.error(errorThrown);
            });
        };
        NlpApp.prototype.annotate_wikipedia = function (lang, title) {
            var _this = this;
            this.setState({ annotating: true, doc: undefined });
            var settings = {
                "async": true,
                "crossDomain": true,
                "url": "https://" + lang + ".wikipedia.org/api/rest_v1/page/html/" + encodeURIComponent(title),
                "method": "GET"
            };
            $.ajax(settings).done(function (response) {
                $.ajax("./" + _this.state.config + "/api/wikipedia/annoviz", {
                    data: response,
                    processData: false,
                    type: "POST",
                    contentType: "text/html; charset=utf-8"
                }).done(function (data) {
                    var result = data;
                    _this.setState({ annotating: false, doc: result });
                }).fail(function (xhr, textStatus, errorThrown) {
                    _this.setState({ annotating: false });
                    console.error(errorThrown);
                });
            });
        };
        NlpApp.prototype.annotate_click = function (e) {
            var mode = $(".modeselector .active").attr("data-tab");
            if (mode === "text") {
                var elem = ReactDOM.findDOMNode(this.refs["textholder"]);
                if (elem.value !== undefined && elem.value.length > 0) {
                    this.annotate_text(elem.value);
                }
            }
            else if (mode === "wikipedia") {
                var wp = ReactDOM.findDOMNode(this.refs["wikipedia-page"]);
                if (wp.value.length > 0) {
                    this.annotate_wikipedia(this.state.config.substring(0, 2), wp.value);
                }
            }
        };
        NlpApp.prototype.selectConfig = function (lang, config) {
            this.setState({ config: lang + "/" + config });
            var langselect = ReactDOM.findDOMNode(this.refs["lang-select"]);
            $(langselect).dropdown("set selected", "en/default");
        };
        NlpApp.prototype.bindSearch = function (searchelem, language) {
            $(searchelem).search("destroy");
            $(searchelem).search({
                searchFields: [
                    'title'
                ],
                minCharacters: 3,
                showNoResults: true,
                searchDelay: 300,
                apiSettings: {
                    dataType: "jsonp",
                    onResponse: function (wikipediaResponse) {
                        var response = {
                            results: []
                        };
                        for (var _i = 0, _a = wikipediaResponse.query.prefixsearch; _i < _a.length; _i++) {
                            var result = _a[_i];
                            response.results.push(result);
                        }
                        return response;
                    },
                    url: "//" + language + ".wikipedia.org/w/api.php?action=query&list=prefixsearch&format=json&pssearch={query}"
                }
            });
        };
        NlpApp.prototype.componentDidMount = function () {
            var _this = this;
            var setState = this.setState;
            this.router = Router({
                "/": setState.bind(this, { text: "" }),
                "/:lang/:config/": function (lang, config) { return _this.selectConfig(lang, config); }
            });
            this.router.init("/");
            var langselect = ReactDOM.findDOMNode(this.refs["lang-select"]);
            var searchbox = ReactDOM.findDOMNode(this.refs["wiki-search"]);
            $(langselect)
                .dropdown({
                allowCategorySelection: true,
                onChange: function (value, text, $selectedItem) {
                    _this.setState({ config: value });
                    _this.bindSearch(searchbox, value.substring(0, 2));
                }
            });
            $(langselect).dropdown("set selected", "en/default");
            this.bindSearch(searchbox, "en");
            $('.modeselector .item').tab();
        };
        NlpApp.prototype.render = function () {
            var _this = this;
            var langs = Object.keys(config_langs);
            var lang_objs = [];
            for (var _i = 0, langs_1 = langs; _i < langs_1.length; _i++) {
                var lang = langs_1[_i];
                var configs = [];
                for (var _a = 0, _b = config_langs[lang]; _a < _b.length; _a++) {
                    var config = _b[_a];
                    configs.push(React.createElement("div", {className: "item", "data-value": lang + "/" + config, key: "c_" + lang + "/" + config}, config_friendly[lang + "/" + config]));
                }
                lang_objs.push(React.createElement("div", {className: "item", "data-value": lang + "/default", key: "l_" + lang}, 
                    React.createElement("i", {className: "dropdown icon"}), 
                    React.createElement("span", {className: "text"}, lang), 
                    React.createElement("div", {className: "menu"}, configs)));
            }
            var wikiEdition = this.wikiEditionFullName[(this.state.config || "en").substring(0, 2)];
            return (React.createElement("div", null, 
                React.createElement("div", {className: "ui top attached tabular menu modeselector"}, 
                    React.createElement("a", {className: "active item", "data-tab": "text"}, "Text"), 
                    React.createElement("a", {className: "item", "data-tab": "wikipedia"}, "Wikipedia")), 
                React.createElement("div", {className: "ui bottom attached active tab segment modeselector", "data-tab": "text"}, 
                    React.createElement("div", {className: "ui form"}, 
                        React.createElement("div", {className: "field"}, 
                            React.createElement("textarea", {ref: "textholder"})
                        )
                    )
                ), 
                React.createElement("div", {className: "ui bottom attached tab segment modeselector", "data-tab": "wikipedia"}, 
                    React.createElement("div", {className: "ui form"}, 
                        React.createElement("div", {className: "field"}, 
                            React.createElement("label", null, 
                                "Page Title (", 
                                wikiEdition, 
                                " Wikipedia)"), 
                            React.createElement("div", {className: "ui search", ref: "wiki-search"}, 
                                React.createElement("div", {className: "ui icon input"}, 
                                    React.createElement("input", {className: "prompt", type: "text", ref: "wikipedia-page", placeholder: "Wikipedia page title..."}), 
                                    React.createElement("i", {className: "search icon"})), 
                                React.createElement("div", {className: "results"})))
                    )
                ), 
                React.createElement("div", {className: "ui form"}, 
                    React.createElement("div", {className: "field"}, 
                        React.createElement("div", {ref: "lang-select", className: "ui dropdown icon button"}, 
                            React.createElement("i", {className: "world icon"}), 
                            React.createElement("span", {className: "text"}, "Select Language"), 
                            React.createElement("div", {className: "menu"}, lang_objs)), 
                        React.createElement("button", {onClick: function (e) { return _this.annotate_click(e); }, className: ((this.state.annotating === true) ? "loading " : "") + "positive ui button"}, "Annotate"))
                ), 
                React.createElement("div", {className: "ui segment"}, 
                    React.createElement("div", {className: "ui " + (this.state.annotating ? "active " : "") + "inverted dimmer"}, 
                        React.createElement("div", {className: "ui large text loader"}, "Annotating...")
                    ), 
                    React.createElement("div", {style: { minHeight: "150pt" }}, 
                        React.createElement(DocumentView_1.DocumentView, {ref: "docview", annoview: this.state.doc !== undefined, annodata: this.state.doc, text: ""})
                    ))));
        };
        return NlpApp;
    }(React.Component));
    $(window).ready(function () {
        ReactDOM.render(React.createElement(NlpApp, null), document.getElementById("nlpapp"));
    });
});
define("DocumentFragment", ["require", "exports", "react", "AnnotationView"], function (require, exports, React, AnnotationView_2) {
    "use strict";
    var DocumentFragment = (function (_super) {
        __extends(DocumentFragment, _super);
        function DocumentFragment(props) {
            _super.call(this, props);
            this.state = { display: false };
        }
        DocumentFragment.prototype.handleChange = function (event) {
            var el = event.target;
            console.dir(el.checked);
            this.setState({ display: el.checked });
        };
        DocumentFragment.prototype.render = function () {
            var _this = this;
            var annoview = null;
            if (this.state.display === true) {
                annoview = React.createElement(AnnotationView_2.AnnotationView, {doc: this.props.entry, layers: this.props.layers});
            }
            return (React.createElement("div", {className: "paragraph"}, 
                React.createElement("div", {className: "ui checkbox"}, 
                    React.createElement("input", {type: "checkbox", className: "annotation-checkbox", onChange: function (e) { return _this.handleChange(e); }}), 
                    React.createElement("label", null, 
                        React.createElement("p", null, this.props.entry.text.substring(this.props.start, this.props.end))
                    )), 
                annoview));
        };
        return DocumentFragment;
    }(React.Component));
    exports.DocumentFragment = DocumentFragment;
});
