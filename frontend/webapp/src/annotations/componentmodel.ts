/**
 *  This file is part of langforia.
 *
 *  Langforia is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Langforia is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Langforia.  If not, see <http://www.gnu.org/licenses/>.
 */
 
/// <reference path="../../typings/browser.d.ts" />

import {Util} from "./util.ts";

export interface IPoint {
  x : number;
  y : number;
}

export enum AnchorPoint {
  LEFT_TOP,
  LEFT_CENTER,
  LEFT_BOTTOM,
  RIGHT_TOP,
  RIGHT_CENTER,
  RIGHT_BOTTOM,
  TOP_LEFT,
  TOP_CENTER,
  TOP_RIGHT,
  BOTTOM_LEFT,
  BOTTOM_CENTER,
  BOTTOM_RIGHT,
  CENTER
};

// Bounding box
export class BBox {
  public x : number;
  public y : number;
  public w : number;
  public h: number;

  constructor(x? : number, y? : number, w? : number, h? : number) {
    this.x = x || 0.0;
    this.y = y || 0.0;
    this.w = w || 0.0;
    this.h = h || 0.0;
  }

  public minX() : number {
    return this.x;
  }

  public maxX() : number {
    return this.x+this.w;
  }

  public minY() : number {
    return this.y;
  }

  public maxY() : number {
    return this.y+this.h;
  }

  public anchor(point : AnchorPoint) : IPoint {
    switch(point) {
      case AnchorPoint.TOP_LEFT:
      case AnchorPoint.LEFT_TOP:
        return {x: this.x, y : this.y};
      case AnchorPoint.LEFT_CENTER:
        return {x: this.x, y : this.y+(this.h/2.0)};
      case AnchorPoint.BOTTOM_LEFT:
      case AnchorPoint.LEFT_BOTTOM:
        return {x: this.x, y : this.y+this.h};
      case AnchorPoint.TOP_CENTER:
        return {x: this.x+(this.w/2.0), y : this.y};
      case AnchorPoint.TOP_RIGHT:
      case AnchorPoint.RIGHT_TOP:
        return {x: this.x+this.w, y : this.y};
      case AnchorPoint.RIGHT_CENTER:
        return {x: this.x+this.w, y : this.y+(this.h/2.0)};
      case AnchorPoint.RIGHT_BOTTOM:
      case AnchorPoint.BOTTOM_RIGHT:
        return {x: this.x+this.w, y : this.y+this.h};
      case AnchorPoint.CENTER:
        return {x: this.x+(this.w/2.0), y : this.y+(this.h/2.0)};
      case AnchorPoint.BOTTOM_CENTER:
        return {x: this.x+(this.w/2.0), y : this.y+this.h};
    }
  }

  public translate(dx : number, dy : number) : BBox {
    let box = new BBox();
    box.x = this.x +dx;
    box.y = this.y +dy;
    box.w = this.w;
    box.h = this.h;

    return box;
  }
}

export enum RenderLayer {
  EDGE_BACKGROUND,
  BACKGROUND,
  HIGHLIGHT_BACKGROUND,
  TEXT,
  LABEL
};

export namespace Svg {
  export let NS = "http://www.w3.org/2000/svg";

  export function measureTexts(renderElem : SVGElement, texts : Array<string>, classes : Array<string>) : Map<Map<BBox>> {
    let measurements : Map<Map<BBox>> = {};
    let elems : Map<Map<SVGTextElement>> = {};

    for(let i = 0; i < texts.length; i++) {
      let text = texts[i];
      let clazz = classes[i];
      if(measurements[clazz] === undefined) {
        measurements[clazz] = {};
        elems[clazz] = {};
      }

      if(measurements[clazz][text] === undefined) {
        measurements[clazz][text] = new BBox();
      }
    }

    // Create a holder element that does not require a dom refresh
    let g = <SVGGElement>document.createElementNS(NS, "g");

    for(let clazzkey of Object.keys(measurements)) {
      for(let textkey of Object.keys(measurements[clazzkey])) {
        let el = <SVGTextElement>document.createElementNS(NS, "text");
        el.setAttribute("x", "0.0");
        el.setAttribute("y", "0.0");
        el.setAttribute("class", clazzkey);
        el.appendChild(document.createTextNode(textkey));
        g.appendChild(el);
        elems[clazzkey][textkey] = el;
      }
    }

    // Insert element and extract measurement results
    renderElem.appendChild(g);

    for(let clazzkey of Object.keys(measurements)) {
      for(let key of Object.keys(measurements[clazzkey])) {
        let bbox = elems[clazzkey][key].getBBox();
        let store = measurements[clazzkey][key];
        store.x = bbox.x;
        store.y = bbox.y;
        store.w = bbox.width;
        store.h = bbox.height;
      }
    }

    // Remove the element
    renderElem.removeChild(g);

    return measurements;
  }
}


let RenderLayers : Array<RenderLayer> = [
  RenderLayer.EDGE_BACKGROUND,
  RenderLayer.BACKGROUND,
  RenderLayer.HIGHLIGHT_BACKGROUND,
  RenderLayer.TEXT,
  RenderLayer.LABEL
];

let RenderLayerClasses : Array<string> = [
  "annotation-layer-edge-background",
  "annotation-layer-background",
  "annotation-layer-highlight",
  "annotation-layer-text",
  "annotation-layer-label"
];

export class AnnoGraphics {
  public renderElem : SVGElement;
  public layers : Map<SVGGElement> = {};
  public stack : Array<Map<SVGGElement>> = [];

  constructor(renderElem : SVGElement) {
    this.renderElem = renderElem;
    for(let k = 0; k < RenderLayers.length; k++) {
      let layer = RenderLayers[k];
      let layerClass = RenderLayerClasses[k];
      this.layers[layer] = document.createElementNS(Svg.NS, "g") as SVGGElement;
      this.layers[layer].setAttribute("class", layerClass);
    }
  }

  public write() {
    for(let layer of RenderLayers) {
      this.renderElem.appendChild(this.layers[layer]);
    }
  }

  public fill : string = "";
  public stroke : string = "";
  public styleClass : string = "";
  public data : Map<string> = {};
  public layerClasses = {};
  public id : string;

  public clearStyle() {
    this.stroke = "";
    this.fill = "";
    this.styleClass = "";
  }

  private translateX : number = 0;
  private translateY : number = 0;
  private translationStack : Array<number> = [];

  public pushTranslation(translateX: number, translateY :number) {
    this.translationStack.push(this.translateX);
    this.translationStack.push(this.translateY);
    this.translateX += translateX;
    this.translateY += translateY;
  }

  public popTranslation() {
    this.translateY = this.translationStack.pop();
    this.translateX = this.translationStack.pop();
  }

  public push(translateX? : number, translateY? : number, id? : string) {
    translateX = translateX || 0.0;
    translateY = translateY || 0.0;

    translateX += this.translateX;
    translateY += this.translateY;

    this.translationStack.push(this.translateX);
    this.translationStack.push(this.translateY);
    this.translateX = 0.0;
    this.translateY = 0.0;

    this.stack.push(this.layers);
    this.layers = {};
    for(let k = 0; k < RenderLayers.length; k++) {
      let layer = RenderLayers[k];
      let layerClass = RenderLayerClasses[k];

      let el = document.createElementNS(Svg.NS, "g") as SVGGElement;
      if(this.layerClasses[layer] !== undefined) {
        el.setAttribute("class", layerClass + " " + this.layerClasses[layer]);
      } else {
        el.setAttribute("class", layerClass);
      }

      if(id !== undefined) {
        el.setAttribute("id", id);
      }

      if(translateX !== 0.0 || translateY !== 0.0) {
        el.setAttribute("transform", "translate("+ translateX.toString(10) + ", "+ translateY.toString(10) +")");
      }

      this.layers[layer] = el;
    }

    this.layerClasses = {};
  }

  public addText(layer : RenderLayer, text : string, x : number, y : number) {
    let el : SVGTextElement = document.createElementNS(Svg.NS, "text") as SVGTextElement;
    if(this.styleClass !== "") {
      el.setAttribute("class", this.styleClass);
    }
    el.setAttribute("x", (x+this.translateX).toString(10));
    el.setAttribute("y", (y+this.translateY).toString(10));

    for(let key of Object.keys(this.data)) {
      el.setAttribute("data-" + key, this.data[key]);
    }
    this.data = {};
    el.appendChild(document.createTextNode(text));
    this.layers[layer].appendChild(el);
  }

  public addTextSpanLine(layer : RenderLayer, text : Array<string>, x : Array<number>, y : Array<number>) {
    let el : SVGTextElement = document.createElementNS(Svg.NS, "text") as SVGTextElement;
    if(this.styleClass !== "") {
      el.setAttribute("class", this.styleClass);
    }
    el.setAttribute("x", "0");
    el.setAttribute("y", "0");

    for(let key of Object.keys(this.data)) {
      el.setAttribute("data-" + key, this.data[key]);
    }
    this.data = {};

    for(let k = 0; k < text.length; k++) {
      let tspan = document.createElementNS(Svg.NS, "tspan") as SVGTSpanElement;
      tspan.setAttribute("x", (x[k]+this.translateX).toString(10));
      tspan.setAttribute("y", (y[k]+this.translateY).toString(10));
      tspan.appendChild(document.createTextNode(text[k]));

      el.appendChild(tspan);
    }

    this.layers[layer].appendChild(el);
  }

  public addRect(layer : RenderLayer, x : number, y : number, w : number, h : number, rx : number, ry : number) {
    let rect = <SVGRectElement>document.createElementNS(Svg.NS, "rect");
    if(this.styleClass !== "") {
      rect.setAttribute("class", this.styleClass);
    }

    rect.setAttribute("x", (x+this.translateX).toString(10));
    rect.setAttribute("y", (y+this.translateY).toString(10));
    rect.setAttribute("width", w.toString(10));
    rect.setAttribute("height", h.toString(10));

    for(let key of Object.keys(this.data)) {
      rect.setAttribute("data-" + key, this.data[key]);
    }
    this.data = {};

    if(rx !== 0.0 || ry !== 0.0) {
      rect.setAttribute("rx", rx.toString(10));
      rect.setAttribute("ry", ry.toString(10));
    }

    if(this.stroke !== "") {
      rect.setAttribute("stroke", this.stroke);
    }
    if(this.fill !== "") {
      rect.setAttribute("fill", this.fill);
    }

    this.layers[layer].appendChild(rect);
  }

  public addEdgeLine(layer : RenderLayer, x_start : number, y_start : number, x_middle : number, y_middle,  x_end : number, y_end : number, arrow? : string) {
    x_start += this.translateX;
    x_middle += this.translateX;
    x_end += this.translateX;

    y_start += this.translateY;
    y_middle += this.translateY;
    y_end += this.translateY;

    let path = "M " + x_start + "," + y_start;
    if(Math.abs(x_middle - x_end) < 1e-5 && Math.abs(y_middle - y_end) < 1e-5 ) {
      path += " L " + x_end +"," +y_end;
    } else {
      path += " Q " + x_middle + "," + y_middle + " " + x_end + "," + y_end;
    }

    let pathel = document.createElementNS(Svg.NS, "path") as SVGPathElement;
    pathel.setAttribute("d", path);
    if(this.styleClass !== "") {
      pathel.setAttribute("class", this.styleClass);
    }

    if((arrow || "") === "end") {
      pathel.setAttribute("marker-end","url(#arrow)");
    }

    if(this.stroke !== "") {
      pathel.setAttribute("stroke", this.stroke);
    }
    if(this.fill !== "") {
      pathel.setAttribute("fill", this.fill);
    }

    this.layers[layer].appendChild(pathel);
  }

  public addCurly(layer : RenderLayer, x_left : number, x_middle : number, x_right : number, y_top : number, height : number) {
    x_left += this.translateX;
    x_middle += this.translateX;
    x_right += this.translateX;

    y_top += this.translateY;

    let path = Util.curlyPath(x_left,x_middle,x_right, y_top, height);
    let pathel = document.createElementNS(Svg.NS, "path") as SVGPathElement;
    pathel.setAttribute("d", path);
    if(this.styleClass !== "") {
      pathel.setAttribute("class", this.styleClass);
    }

    if(this.stroke !== "") {
      pathel.setAttribute("stroke", this.stroke);
    }
    if(this.fill !== "") {
      pathel.setAttribute("fill", this.fill);
    }

    this.layers[layer].appendChild(pathel);
  }

  public addLine(layer : RenderLayer, x1 : number, y1 : number, x2 : number, y2 :number, strokeWidth? : number) {
    let lineel = document.createElementNS(Svg.NS, "line");
    lineel.setAttribute("x1", (x1+this.translateX).toString(10));
    lineel.setAttribute("y1", (y1+this.translateY).toString(10));
    lineel.setAttribute("x2", (x2+this.translateX).toString(10));
    lineel.setAttribute("y2", (y2+this.translateY).toString(10));
    if(strokeWidth !== undefined) {
      lineel.setAttribute("stroke-width", strokeWidth.toString(10));
    }

    if(this.stroke !== "") {
      lineel.setAttribute("stroke", this.stroke);
    }

    if(this.styleClass !== "") {
      lineel.setAttribute("class", this.styleClass);
    }
    this.layers[layer].appendChild(lineel);
  }

  public addPolygon(layer : RenderLayer, x : number, y : number, pts : Array<number>) {
    x += this.translateX;
    y += this.translateY;

    let poly = document.createElementNS(Svg.NS, "polygon");

    let items : Array<string> = [];
    for(let k = 0; k < pts.length; k += 2) {
      items.push((pts[k]+x).toString() + "," + (pts[k+1]+y).toString());
    }

    poly.setAttribute("points", items.join(" "));
    if(this.styleClass !== "") {
      poly.setAttribute("class", this.styleClass);
    }

    if(this.stroke !== "") {
      poly.setAttribute("stroke", this.stroke);
    }

    if(this.fill !== "") {
      poly.setAttribute("fill", this.fill);
    }

    this.layers[layer].appendChild(poly);
  }

  public pop() {
      let parent = this.stack.pop();
      for(let layer of RenderLayers) {
        if(this.layers[layer].hasChildNodes) {
          parent[layer].appendChild(this.layers[layer]);
        }
      }
      this.layers = parent;
      this.popTranslation();
  }
}

export abstract class AnnoElement {
  public renderLayer : RenderLayer = RenderLayer.TEXT;
  public x : number = 0.0;
  public y : number = 0.0;
  public data : Map<string> = {};

  public translate(dx : number, dy : number) : AnnoElement {
    this.x = this.x+dx;
    this.y = this.y+dy;
    return this;
  }

  public abstract render(g : AnnoGraphics);
  public abstract width() : number;
  public abstract height() : number;

  public attach(anchor : IPoint, point : AnchorPoint) : AnnoElement {
    switch(point) {
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
        this.y = anchor.y-this.height()/2.0;
        break;
      }
      case AnchorPoint.BOTTOM_LEFT:
      case AnchorPoint.LEFT_BOTTOM:
      {
        this.x = anchor.x;
        this.y = anchor.y-this.height();
        break;
      }
      case AnchorPoint.TOP_CENTER:
      {
        this.x = anchor.x - this.width()/2.0;
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
        this.y = anchor.y - this.height()/2;
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
  }

  public anchor(point : AnchorPoint) : IPoint {
    switch(point) {
      case AnchorPoint.TOP_LEFT:
      case AnchorPoint.LEFT_TOP:
        return {x: this.x, y : this.y};
      case AnchorPoint.LEFT_CENTER:
        return {x: this.x, y : this.y+(this.height()/2.0)};
      case AnchorPoint.BOTTOM_LEFT:
      case AnchorPoint.LEFT_BOTTOM:
        return {x: this.x, y : this.y+this.height()};
      case AnchorPoint.TOP_CENTER:
        return {x: this.x+(this.width()/2.0), y : this.y};
      case AnchorPoint.TOP_RIGHT:
      case AnchorPoint.RIGHT_TOP:
        return {x: this.x+this.width(), y : this.y};
      case AnchorPoint.RIGHT_CENTER:
        return {x: this.x+this.width(), y : this.y+(this.height()/2.0)};
      case AnchorPoint.RIGHT_BOTTOM:
      case AnchorPoint.BOTTOM_RIGHT:
        return {x: this.x+this.width(), y : this.y+this.height()};
      case AnchorPoint.CENTER:
        return {x: this.x+(this.width()/2.0), y : this.y+(this.height()/2.0)};
      case AnchorPoint.BOTTOM_CENTER:
        return {x: this.x+(this.width()/2.0), y : this.y+this.height()};
    }
  }
}

export class AnnoTextElement extends AnnoElement {
  public text : string;
  public textMeas : BBox;
  public textClass : string;

  constructor(x? : number, y? : number, text?: string, textMeas?: BBox, textClass? : string) {
    super();
    this.x = x || 0.0;
    this.y = y || 0.0;
    this.text = text || "";
    this.textMeas = textMeas || new BBox();
    this.textClass = textClass || "annotation-text";
  }

  public width() : number {
    return this.textMeas.w;
  }

  public height() : number {
    return this.textMeas.h;
  }

  public render(g : AnnoGraphics) {
    g.data = this.data;
    g.styleClass = this.textClass;
    g.addText(this.renderLayer, this.text, this.x, this.y-this.textMeas.y);
    g.clearStyle();
  }
}

export class AnnoLineElement extends AnnoElement {
  public x2 : number = 0.0;
  public y2 : number = 0.0;
  public strokeWidth = 1.0;
  public stroke : string = "";
  public styleClass : string = "";

  constructor(x1? : number, y1? : number, x2?: number, y2?: number) {
    super();
    this.x = x1 || 0.0;
    this.y = y1 || 0.0;
    this.x2 = x2 || 0.0;
    this.y2 = y2 || 0.0;
  }

  public height() : number {
    return Math.abs(this.y2);
  }

  public width() : number {
    return Math.abs(this.x2);
  }

  public render(g : AnnoGraphics) {
    g.data = this.data;
    g.styleClass = this.styleClass;
    g.stroke = this.stroke;
    g.addLine(this.renderLayer, this.x, this.y, this.x2+this.x, this.y2+this.y, this.strokeWidth);
    g.clearStyle();
  }
}

export class AnnoEdgeLineElement extends AnnoElement {
  public dxMiddle : number = 0.0;
  public dyMiddle : number = 0.0;
  public dxEnd : number = 0.0;
  public dyEnd : number = 0.0;
  public strokeWidth = 1.0;
  public stroke : string = "";
  public styleClass : string = "";
  public arrow : string = "";

  constructor(x? : number, y? : number, x_middle?: number, y_middle?: number, x_end? : number, y_end? : number) {
    super();
    this.x = x || 0.0;
    this.y = y || 0.0;
    this.dxMiddle = (x_middle || 0.0) - this.x;
    this.dyMiddle = (y_middle || 0.0) - this.y;
    this.dxEnd = (x_end || 0.0) - this.x;
    this.dyEnd = (y_end || 0.0) - this.y;
  }

  public height() : number {
    return Math.abs(this.dyEnd);
  }

  public width() : number {
    return Math.abs(this.dxEnd);
  }

  public render(g : AnnoGraphics) {
    g.data = this.data;
    g.styleClass = this.styleClass;
    g.stroke = this.stroke;
    g.addEdgeLine(this.renderLayer, this.x, this.y, this.dxMiddle+this.x, this.dyMiddle+this.y, this.dxEnd+this.x, this.dyEnd+this.y, this.arrow);
    g.clearStyle();
  }
}

export class AnnoTextLine extends AnnoElement {
  public text : Array<string> = [];
  public xs : Array<number> = [];
  public textMeas : Array<BBox> = [];
  public partials : Map<number> = {};
  public textClass : string = "annotation-text";
  public maxHeight : number = 0.0;
  public w : number = 0.0;
  public len : number = 0;

  constructor() {
    super();
    this.partials[0] = 0.0;
  }

  public add(text : string, partiaLens : Array<number>, measurements : Array<BBox>) {
    for(let k = 0; k < partiaLens.length; k++) {
      this.partials[this.len+partiaLens[k]] = this.w + measurements[k].w;
    }

    let full = measurements[measurements.length-1];

    this.xs.push(this.w);
    this.w += full.w;
    this.maxHeight = Math.max(full.h, this.maxHeight);
    this.len += text.length;
    this.text.push(text);
    this.textMeas.push(full);
  }

  public section(from : number, to : number) : BBox {
    return new BBox(
      this.x+this.partials[from],
      this.y,
      this.partials[to]-this.partials[from],
      this.maxHeight
    );
  }

  public length() : number {
    return this.len;
  }

  public width() : number {
    return this.w;
  }

  public height() : number {
    return this.maxHeight;
  }

  public render(g : AnnoGraphics) {
    g.styleClass = this.textClass;
    let ys : Array<number> = [];

    for(let k = 0; k < this.xs.length; k++) {
      ys.push(this.y+(this.maxHeight-this.textMeas[k].h)-this.textMeas[k].y);
    }

    g.addTextSpanLine(this.renderLayer, this.text, this.xs, ys);
    g.clearStyle();
  }
}

export class AnnoCurlyElement extends AnnoElement {
  public curlyStyle : string = "annotation-curly";
  public w : number;
  public h : number;
  public stroke : string = "";

  constructor(x?: number, y?: number, w?: number, h?: number) {
    super();
    this.x = x || 0.0;
    this.y = y || 0.0;
    this.w = w || 0.0;
    this.h = h || 0.0;
  }

  public width() : number {
    return this.w;
  }

  public height() : number {
    return this.h;
  }

  public render(g : AnnoGraphics) {
    g.styleClass = this.curlyStyle;
    g.stroke = this.stroke;
    g.addCurly(this.renderLayer, this.x, this.x+(this.w/2.0), this.x+this.w, this.y, this.h );
    g.clearStyle();
  }
}

export class AnnoRectElement extends AnnoElement {
  public w : number;
  public h : number;
  public rx : number = 0.0;
  public ry : number = 0.0;
  public fill : string = "";
  public stroke : string = "";
  public styleClass : string = "";

  constructor(x? : number, y? : number, w?: number, h?: number, rx?:number, ry?:number) {
    super();
    this.x = x || 0.0;
    this.y = y || 0.0;
    this.w = w || 0.0;
    this.h = h || 0.0;
    this.rx = rx || 0.0;
    this.ry = ry || 0.0;
  }

  public render(g : AnnoGraphics) {
    g.fill = this.fill;
    g.stroke = this.stroke;
    g.styleClass = this.styleClass;
    g.addRect(this.renderLayer, this.x, this.y ,this.w, this.h, this.rx, this.ry);
    g.clearStyle();
  }

  public width() : number {
    return this.w;
  }

  public height() : number {
    return this.h;
  }
}

export class AnnoPolygon extends AnnoElement {
  public w : number;
  public h : number;
  public fill : string = "";
  public stroke : string = "";
  public styleClass : string = "";
  private points : Array<number> = [];

  constructor(x? : number, y? : number, points? : Array<number> ) {
    super();
    this.x = x || 0.0;
    this.y = y || 0.0;
    let minX = Number.POSITIVE_INFINITY, maxX = Number.NEGATIVE_INFINITY;
    let minY = Number.POSITIVE_INFINITY, maxY = Number.NEGATIVE_INFINITY;

    for(let k = 0; k < points.length; k += 2) {
      minX = Math.min(minX, points[k]);
      maxX = Math.max(maxX, points[k]);
      minY = Math.min(minY, points[k+1]);
      maxY = Math.max(maxY, points[k+1]);
    }

    this.w = maxX - minX;
    this.h = maxY - minY;

    if(minX !== 0.0 || maxX !== 0.0) {
      for(let k = 0; k < points.length/2; k += 2) {
        points[k] -= minX;
        points[k+1] -= minY;
      }
    }

    this.points = points;
  }

  public render(g : AnnoGraphics) {
    g.fill = this.fill;
    g.stroke = this.stroke;
    g.styleClass = this.styleClass;
    g.addPolygon(this.renderLayer, this.x, this.y, this.points);
    g.clearStyle();
  }

  public width() : number {
    return this.w;
  }

  public height() : number {
    return this.h;
  }
}

export class AnnoGroup extends AnnoElement {
  protected elements : Array<AnnoElement> = [];
  public layerClasses : Map<string> = {};
  private w : number = undefined;
  private h : number = undefined;
  private isolateGroup : boolean = false;

  constructor(x?:number, y?:number) {
    super();
    this.x = x || 0.0;
    this.y = y || 0.0;
  }

  public moveContent(dx : number, dy : number) {
    if(dx !== 0.0 || dy !== 0.0) {
      for(let el of this.elements) {
        el.x += dx;
        el.y += dy;
      }
    }
  }

  public applyTransformation() {
    if(this.y !== 0.0 || this.x !== 0.0) {
        this.moveContent(this.x,this.y);
        this.x = 0.0;
        this.y = 0.0;
    }
  }

  public add(elem : AnnoElement) {
    this.elements.push(elem);
    this.w = undefined;
    this.h = undefined;
  }

  public width() : number {
    if(this.w === undefined) {
      let min_x = Number.POSITIVE_INFINITY;
      let max_x = Number.NEGATIVE_INFINITY;
      for(let el of this.elements) {
        max_x = Math.max(el.x+el.width(), max_x);
        min_x = Math.min(el.x, min_x);
      }

      this.w = max_x - min_x;
    }

    return this.w;
  }

  public height() : number {
    if(this.h === undefined) {
      let min_y = Number.POSITIVE_INFINITY;
      let max_y = Number.NEGATIVE_INFINITY;
      for(let el of this.elements) {
        max_y = Math.max(el.y+el.height(), max_y);
        min_y = Math.min(el.y, min_y);
      }

      this.h = max_y - min_y;
    }

    return this.h;
  }

  public bounds() : BBox {
    let min_y = Number.POSITIVE_INFINITY;
    let max_y = Number.NEGATIVE_INFINITY;
    let min_x = Number.POSITIVE_INFINITY;
    let max_x = Number.NEGATIVE_INFINITY;
    for(let el of this.elements) {
      max_x = Math.max(el.x+el.width(), max_x);
      min_x = Math.min(el.x, min_x);
      max_y = Math.max(el.y+el.height(), max_y);
      min_y = Math.min(el.y, min_y);
    }

    return new BBox(min_x, min_y, max_x-min_x, max_y-min_y);
  }

  public compact() {
    let size = this.bounds();
    if(size.x !== 0.0 || size.y !== 0.0) {
      for(let el of this.elements) {
        el.x -= size.x;
        el.y -= size.y;
      }
    }
  }

  public render(g : AnnoGraphics) {
    if(this.x !== 0.0 || this.y !== 0.0) {
      if(this.isolateGroup === true) {
        g.push(this.x, this.y);
      } else {
        g.pushTranslation(this.x,this.y);
      }
    }

    for(let el of this.elements) {
      el.render(g);
    }

    if(this.x !== 0.0 || this.y !== 0.0) {
      if(this.isolateGroup === true) {
        g.pop();
      } else {
        g.popTranslation();
      }
    }
  }
}
