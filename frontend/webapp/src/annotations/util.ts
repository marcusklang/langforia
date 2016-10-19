/**
 *  This file is part of Langforia.
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

namespace Util {
  export class SpatialItem<T> {
    public id : number;
    public value : T;
    public minX : number;
    public maxX : number;

    constructor(id : number, minX : number, maxX : number, value : T) {
      this.id = id;
      this.value = value;
      this.minX = minX;
      this.maxX = maxX;
    }
  }

  class SpatialContainer<T> {
    public minX : number = Number.POSITIVE_INFINITY;
    public maxX : number = Number.NEGATIVE_INFINITY;
    public items : Array<SpatialItem<T>> = [];

    public add(item : SpatialItem<T>) {
      this.minX = Math.min(item.minX, this.minX);
      this.maxX = Math.max(item.maxX, this.maxX);
      this.items.push(item);
    }
  }

  export class SparseGridSpatialIndex<T> {
    private buckets : Map<SpatialContainer<T>> = {};
    private numBuckets : number;
    private minX : number;
    private maxX : number;
    private range : number;
    private idCnt : number = 0;

    constructor(minX : number, maxX : number, numBuckets : number) {
      this.minX = minX;
      this.maxX = maxX;
      this.range = maxX-minX;
      this.numBuckets = numBuckets;
    }

    public add(minX : number, maxX : number, elem : T) {
      let u = Math.floor((minX-this.minX) / this.range * this.numBuckets);
      let v = Math.floor((maxX-this.minX) / this.range * this.numBuckets);

      let startBucket = Math.min(this.numBuckets-1,Math.max(0,u));
      let endBucket = Math.min(this.numBuckets-1,Math.max(0,v))+1;

      for(let k = startBucket; k < endBucket; k++) {
        let container = this.buckets[k];
        if(container === undefined) {
          container = new SpatialContainer<T>();
          this.buckets[k] = container;
        }

        container.add(new SpatialItem<T>(this.idCnt, minX, maxX, elem));
        this.idCnt += 1;
      }
    }

    public anyIntersection(minX : number, maxX : number) : boolean {
      let u = Math.floor((minX-this.minX) / this.range * this.numBuckets);
      let v = Math.floor((maxX-this.minX) / this.range * this.numBuckets);

      let startBucket = Math.min(this.numBuckets-1,Math.max(0,u));
      let endBucket = Math.min(this.numBuckets-1,Math.max(0,v))+1;

      let added : Map<boolean> = {};
      let result : Array<SpatialItem<T>> = [];

      for(let k = startBucket; k < endBucket; k++) {
        let container = this.buckets[k];
        if(container !== undefined) {
          if(minX < container.maxX && maxX > container.minX) {
            // Possible Intersections
            for(let item of container.items) {
              // Check Intersections
              if(minX < item.maxX && maxX > item.minX) {
                return true;
              }
            }
          }
        }
      }

      return false;
    }

    public intersections(minX : number, maxX : number) : Array<SpatialItem<T>> {
      let u = Math.floor((minX-this.minX) / this.range * this.numBuckets);
      let v = Math.floor((maxX-this.minX) / this.range * this.numBuckets);

      let startBucket = Math.min(this.numBuckets-1,Math.max(0,u));
      let endBucket = Math.min(this.numBuckets-1,Math.max(0,u))+1;

      let added : Map<boolean> = {};
      let result : Array<SpatialItem<T>> = [];

      for(let k = startBucket; k < endBucket; k++) {
        let container = this.buckets[k];
        if(container !== undefined) {
          if(minX < container.maxX && maxX > container.minX) {
            // Possible Intersections
            for(let item of container.items) {
              if(added[item.id] !== undefined) {
                // Check Intersections
                if(minX < item.maxX && maxX > item.minX) {
                  result.push(item);
                  added[item.id] = true;
                }
              }
            }
          }
        }
      }

      return result;
    }
  }

  export function rgb(r,g,b) : IRGBColor {
    return {r: r, g: g, b: b};
  }

  export function hsl(h,s,l) : IHSLColor {
    return {h: h, s: s, l: l};
  }

  export function mixRgb(x : IRGBColor,y : IRGBColor) : IRGBColor {
    return {r: (x.r + y.r) / 2.0,
            g: (x.g + y.g) / 2.0,
            b: (x.b + y.b) / 2.0};
  }

  export function rightTriangle(w : number, h : number) : Array<number> {
    return [0,0, 0,h, w,h/2];
  }

  export function leftTriangle(w : number, h : number) : Array<number> {
    return [w,0, w,h, 0,h/2];
  }

  export function shuffle(array) {
    let cnt = array.length;
    while (cnt > 0) {
        let idx = Math.floor(Math.random() * cnt);
        cnt--;

        let temp = array[cnt];
        array[cnt] = array[idx];
        array[idx] = temp;
    }

    return array;
  }

  export function randomRgb() : IRGBColor {
    return {
      r: Math.random(),
      g: Math.random(),
      b: Math.random()
    };
  }

  export function darkerHsl(hsl : IHSLColor, amount? : number) : IHSLColor {
    let fact = amount || 0.5;
    fact = 1.0 - fact;
    return {h: hsl.h, s: hsl.s, l: hsl.l * fact};
  }

  export function mulHsl(x : IHSLColor, y : IHSLColor) : IHSLColor {
    return {h: x.h * y.h, s: x.s*y.s, l: x.l * y.l };
  }

  export function generatePastelColors(n) : Array<IHSLColor> {
    if(n < 180) {
      let result : Array<IHSLColor> = [];
      let offset = Math.random()*360.0;
      let deltaOffset = 360.0/n;
      for(let k = 0; k < n; k++) {
        let startOffset = offset + (deltaOffset*k);
        let h = startOffset + (deltaOffset/2.0) + (2.0*Math.random()-1.0) * (deltaOffset/8.0);
        if(h >= 360.0) {
          h -= 360.0;
        }

        let s = Math.random()*0.5+0.5;
        let l = 0.85;
        result.push(hsl(h,s,l));
      }

      return shuffle(result);
    } else {
      let result : Array<IHSLColor> = [];
      let white = rgb(1.0,1.0,1.0);
      for(let i = 0; i < n; i++) {
        result.push({
          h: Math.random()*360.0,
          s: Math.random()*0.5+0.5,
          l: Math.random()*0.125+0.75
        });
      }

      return result;
    }
  }

  export interface IRGBColor {
    r: number;
    g: number;
    b : number;
  }

  export interface IHSLColor {
    h: number;
    s: number;
    l: number;
  }

  /**
   * Converts an HSL color value to RGB. Conversion formula
   * adapted from http://en.wikipedia.org/wiki/HSL_color_space.
   * Assumes h, s, and l are contained in the set [0, 1] and
   * returns r, g, and b in the set [0, 1].
   *
   * Source: http://stackoverflow.com/questions/2353211/hsl-to-rgb-color-conversion
   *
   * @param   {number}  h       The hue
   * @param   {number}  s       The saturation
   * @param   {number}  l       The lightness
   * @return  {Array}           The RGB representation
   */
  export function hsl2rgb(hc : IHSLColor) : IRGBColor {
      let h = hc.h/360.0, s = hc.s, l = hc.l;
      let r, g, b;

      if(s === 0) {
          r = g = b = l; // achromatic
      } else {
          let hue2rgb = function hue2rgb(p, q, t){
              if(t < 0) t += 1;
              if(t > 1) t -= 1;
              if(t < 1/6) return p + (q - p) * 6 * t;
              if(t < 1/2) return q;
              if(t < 2/3) return p + (q - p) * (2/3 - t) * 6;
              return p;
          };

          let q = l < 0.5 ? l * (1 + s) : l + s - l * s;
          let p = 2 * l - q;
          r = hue2rgb(p, q, h + 1/3);
          g = hue2rgb(p, q, h);
          b = hue2rgb(p, q, h - 1/3);
      }

      return rgb(r,g,b);
  }

  export function rgb2hsl(c : IRGBColor) : IHSLColor {
    let r = c.r;
    let g = c.g;
    let b = c.b;
    let cmax = Math.max(r,Math.max(g,b));
    let cmin = Math.min(r,Math.min(g,b));
    let delta = cmax-cmin;
    let eps = 1e-7;
    let h = 0.0;
    let l = (cmax+cmin)/2.0;
    let s = 0.0;

    if(delta >= -eps && delta < eps) {
        h = 0.0;
    }
    else if(cmax === r) {
      h = (((g-b) / delta) % 6.0)*60.0;
      s = delta/(1.0-Math.floor(2.0*l-1.0));
    }
    else if(cmax === g) {
      h = (((b-r) / delta) + 2)*60.0;
      s = delta/(1.0-Math.floor(2.0*l-1.0));
    }
    else if(cmax === b) {
      h = (((r-g) / delta) + 4)*60.0;
      s = delta/(1.0-Math.floor(2.0*l-1.0));
    }

    return {
      h: h,
      s: s,
      l: l
    };
  }

  export function hex2rgb(hex) : IRGBColor {
    let r,g,b;
    if(hex.length === 3) {
      r = hex[0]+hex[0];
      g = hex[1]+hex[1];
      g = hex[2]+hex[2];
    }
    else if(hex.length !== 6) {
      throw new Error("Invalid hex passed to hex2rgb!");
    }

    let rv = parseInt(r,16) / 255.0;
    let gv = parseInt(g,16) / 255.0;
    let bv = parseInt(b,16) / 255.0;

    return {r: rv, g: gv, b: bv};
  }

  export function rgb2hex(c : IRGBColor) : string {
    let r = Math.round(c.r * 255).toString(16);
    let g = Math.round(c.g * 255).toString(16);
    let b = Math.round(c.b * 255).toString(16);
    return "#" +
           (r.length > 1 ? r : "0" + r) +
           (g.length > 1 ? g : "0" + g) +
           (b.length > 1 ? b : "0" + b);
  }

  // Create curly path
  export function curlyPath(x_start, x_middle, x_end, y_top, height) {
    return "M" + x_start + " " + (y_top + height) +
           "C" + x_start + " " + y_top            + "," +
                 x_middle + " " + (y_top+height)  + "," +
                 x_middle + " " + y_top +
           "C" + x_middle + " " + (y_top + height) + "," +
                 x_end    + " " + y_top            + "," +
                 x_end    + " " + (y_top + height);
  }

  export function closestIndex (num, arr) {
      let mid;
      let lo = 0;
      let hi = arr.length - 1;
      while (hi - lo > 1) {
          mid = Math.floor ((lo + hi) / 2);
          if (arr[mid] < num) {
              lo = mid;
          } else {
              hi = mid;
          }
      }
      if (num - arr[lo] <= arr[hi] - num) {
          return lo;
      }
      return hi;
  }
}

export { Util };
