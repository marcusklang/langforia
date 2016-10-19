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
/// <reference path="../interfaces.d.ts"/>

import {Util} from "./util";
import {
   AnnoElement,
   AnnoGraphics,
   AnnoGroup,
   AnnoLineElement,
   AnnoTextLine,
   AnnoEdgeLineElement,
   AnnoRectElement,
   AnnoCurlyElement,
   AnnoTextElement,
   AnnoPolygon,
   BBox,
   RenderLayer,
   AnchorPoint,
   Svg,
   IPoint
} from "./componentmodel";

// Styles
interface Style {
  lineRowPadding: number; // Distance between lines, top padding
  segmentRowPadding: number; // Distance between segments, top padding
  segmentMarginPaddingLeft : number; // Distance left/right padding on segment id margin.
  segmentMarginPaddingRight : number; // Distance left/right padding on segment id margin.
  showSegmentMarginLine: boolean; // Render the segment line or not.
  segmentInnerPadding: number; // sets the segment padding
  curlyHeight: number; // the height of curly pointers
  labelMargin: number; // margin for placement of labels, i.e. don't place labels closer than this.
  labelPadding: number; // inner padding for label rect
  triangleMargin: number; // continuation triangle margin
  minEdgeLineLength: number; // the minimum length of edge line
  edgeBendDistance: number; // the maximum bend distance
  edgeLayerMargin: number; // minimum distance between node annotations and edge annotations.
}

class LabelFragment {
  public layer : string = "";
  public id : string = "";
  public label : string = "";
  public popup : string = "";
  public popupId : string = "";
  public color : Util.IHSLColor = null;
  public strokeColor : Util.IHSLColor = null;
  public bbox : BBox = null;
}

class NodeLabelFragment extends LabelFragment {
  public startpos : number = 0;
  public endpos : number = 0;
}

class EdgeLabelFragment extends LabelFragment {
  public head : string = "";
  public tail : string = "";
  public startline : number = 0;
  public endline : number = 0;
}

interface TextBlock {
  startpos : number;
  endpos : number;
  text : string;
  measurements : Array<BBox>;
  lengths : Array<number>;
}

interface INodeLayerInfo {
  heights: Array<number>;
  offsets : Array<number>;
  totalHeight : number;
}

interface INodeLayerBinding {
  layer: Array<number>;
  numLayers : number;
  priority : Array<string>;
}

interface INodeLayerPlacement {
  info: INodeLayerInfo;
  binding: INodeLayerBinding;
  layer2labels : Map<Array<number>>;
  annotationBoxes : Array<AnnoGroup>;
  labelBoxes : Array<AnnoGroup>;
}

namespace Lines {
  export let nodeLayerPriority : Map<number> = {
    "node/Token" : -2,
    "node/Sentence": Number.POSITIVE_INFINITY
  };
}

class EdgeFragment {
  private parent : LineSegment;
  private label : EdgeLabelFragment;
  private textElement : AnnoTextElement;

  public bottomY : number = 0.0;

  public leftAttachmentPt : IPoint = {x:0,y:0};
  public rightAttachmentPt : IPoint = {x:0,y:0};

  public arrowLeft : boolean = false;
  public arrowRight : boolean = false;

  public leftHasNode : boolean = false;
  public rightHasNode : boolean = false;

  constructor(parent : LineSegment, label : EdgeLabelFragment) {
    this.parent = parent;
    this.label = label;
    this.textElement = new AnnoTextElement(0,0,label.label, label.bbox, "annotation-label");
  }

  private closestPtByX(pt : IPoint, pts : Array<IPoint>) : IPoint {
    let selectedPt = pts[0];
    let dist = Math.abs(pt.x-pts[0].x);
    for(let k = 1; k < pts.length; k++) {
      if(Math.abs(pt.x-pts[k].x) < dist) {
        dist = Math.abs(pt.x-pts[k].x);
        selectedPt = pts[k];
      }
    }
    return selectedPt;
  }

  public optimizeLeft(pts : Array<IPoint>) {
    this.leftAttachmentPt = this.closestPtByX(this.leftAttachmentPt, pts);
  }

  public optimizeRight(pts : Array<IPoint>) {
    this.rightAttachmentPt = this.closestPtByX(this.rightAttachmentPt, pts);
  }

  public centerX() : number {
    let cx = this.textElement.width()/2.0;
    cx += this.parent.parent.doc.style.labelPadding;
    cx += this.parent.parent.doc.style.minEdgeLineLength;
    cx += this.parent.parent.doc.style.edgeBendDistance;

    return Math.max(cx, this.leftAttachmentPt.x + (this.rightAttachmentPt.x - this.leftAttachmentPt.x) / 2.0);
  }

  public width() : number {
    return this.rightX() - this.leftX();
  }

  public leftX() : number {
    let cx = this.centerX();
    cx -= this.textElement.width()/2.0;
    cx -= this.parent.parent.doc.style.labelPadding;
    cx -= this.parent.parent.doc.style.minEdgeLineLength;
    cx -= this.parent.parent.doc.style.edgeBendDistance;

    return Math.min(cx, this.leftAttachmentPt.x);
  }

  public rightX() : number {
    let cx = this.centerX();
    cx += this.textElement.width()/2.0;
    cx += this.parent.parent.doc.style.labelPadding;
    cx += this.parent.parent.doc.style.minEdgeLineLength;
    cx += this.parent.parent.doc.style.edgeBendDistance;

    return Math.max(cx, this.rightAttachmentPt.x);
  }

  public height() : number {
    return this.textElement.height();
  }

  public compile(labelPadding : number, bendDistance : number) : AnnoGroup {
    let edgeBox = new AnnoGroup();
    this.textElement.renderLayer = RenderLayer.LABEL;
    this.textElement.data = {
      "popup-title": this.label.tail + " → " + this.label.head + " : " + this.label.id,
      "popup-html": this.label.popup
    };

    let centerX = Math.max(this.centerX(), this.textElement.width()/2.0+labelPadding+bendDistance);

    this.textElement.attach({x: this.centerX(), y: this.bottomY}, AnchorPoint.BOTTOM_CENTER);

    let leftAnchorPoint = this.textElement.anchor(AnchorPoint.LEFT_CENTER);
    let rightAnchorPoint = this.textElement.anchor(AnchorPoint.RIGHT_CENTER);

    leftAnchorPoint.x -= labelPadding;
    rightAnchorPoint.x += labelPadding;

    let leftLineXstart = this.leftX()+bendDistance;

    let leftLine = new AnnoLineElement(leftLineXstart, leftAnchorPoint.y, leftAnchorPoint.x-leftLineXstart, 0);
    let rightLine = new AnnoLineElement(rightAnchorPoint.x, rightAnchorPoint.y, this.rightX()-rightAnchorPoint.x-bendDistance, 0);

    leftLine.renderLayer = RenderLayer.EDGE_BACKGROUND;
    rightLine.renderLayer = RenderLayer.EDGE_BACKGROUND;

    leftLine.styleClass = "annotation-edge-line";
    rightLine.styleClass = "annotation-edge-line";

    leftLine.strokeWidth = undefined;
    rightLine.strokeWidth = undefined;

    edgeBox.add(this.textElement);
    edgeBox.add(leftLine);
    edgeBox.add(rightLine);

    if(this.leftHasNode === true) {
      let annoLine = new AnnoEdgeLineElement(leftLine.x, leftLine.y, leftLine.x-bendDistance, leftLine.y, this.leftAttachmentPt.x, this.leftAttachmentPt.y);
      annoLine.arrow = this.arrowLeft === true ? "end" : "";
      annoLine.renderLayer = RenderLayer.EDGE_BACKGROUND;
      annoLine.styleClass = "annotation-edge-line";
      edgeBox.add(annoLine);
    } else {
      // Adjust height on left side
      this.leftAttachmentPt.y = leftLine.y;

      let annoLine = new AnnoEdgeLineElement(leftLine.x, leftLine.y, leftLine.x, leftLine.y, this.leftAttachmentPt.x, this.leftAttachmentPt.y);
      annoLine.arrow = this.arrowLeft === true ? "end" : "";
      annoLine.renderLayer = RenderLayer.EDGE_BACKGROUND;
      annoLine.styleClass = "annotation-edge-line";
      edgeBox.add(annoLine);
    }

    if(this.rightHasNode === true) {
      let rightLineAnchor = rightLine.anchor(AnchorPoint.RIGHT_CENTER);
      let annoLine = new AnnoEdgeLineElement(rightLineAnchor.x, rightLineAnchor.y, rightLineAnchor.x+bendDistance, rightLine.y, this.rightAttachmentPt.x, this.rightAttachmentPt.y);
      annoLine.arrow = this.arrowRight === true ? "end" : "";
      annoLine.renderLayer = RenderLayer.EDGE_BACKGROUND;
      annoLine.styleClass = "annotation-edge-line";
      edgeBox.add(annoLine);
    } else {
      // Adjust height on right side
      this.rightAttachmentPt.y = rightLine.y;

      let annoLine = new AnnoEdgeLineElement(rightLine.x, rightLine.y, rightLine.x, rightLine.y, this.rightAttachmentPt.x, this.rightAttachmentPt.y);
      annoLine.arrow = this.arrowRight === true ? "end" : "";
      annoLine.renderLayer = RenderLayer.EDGE_BACKGROUND;
      annoLine.styleClass = "annotation-edge-line";
      edgeBox.add(annoLine);
    }

    return edgeBox;
  }
}

class LineSegment {
  public parent : Segment;
  constructor(parent : Segment) {
    this.parent = parent;
  }

  public linenb : number;
  public blocks : Array<TextBlock> = [];
  public nodeLabels : Array<NodeLabelFragment> = [];
  public edgeLabels : Array<EdgeLabelFragment> = [];
  public nodeId2idx : Map<number> = {};
  public height : number = 0.0;
  public masterBlock : AnnoGroup = new AnnoGroup();

  public start() : number {
    return this.blocks[0].startpos;
  }

  public end() : number {
    return this.blocks[this.blocks.length-1].endpos;
  }

  private textExcerpt(lbl : NodeLabelFragment) {
    let excerpt = this.parent.doc.text.substring(lbl.startpos,lbl.endpos);
    if(lbl.endpos-lbl.startpos > 40) {
      excerpt = "\"" + excerpt.substring(0,40) + "\"...";
    } else {
      excerpt = "\"" + excerpt + "\"";
    }
    return excerpt;
  }

  private processExtensionMarkers(lbl : NodeLabelFragment, backgroundRect : AnnoRectElement, labelBox : AnnoGroup) {
    let leftTri = lbl.startpos < this.start();
    let rightTri = lbl.endpos > this.end();

    if(leftTri || rightTri) {
      let triangleMargin = this.parent.doc.style.triangleMargin;
      let b = labelBox.bounds();
      if(leftTri) {
        let pts = Util.leftTriangle(labelBox.height()*0.5,labelBox.height()*0.75);
        let poly = new AnnoPolygon(0,0,pts);
        poly.renderLayer = RenderLayer.BACKGROUND;
        poly.fill = backgroundRect.stroke;

        poly.attach(backgroundRect.anchor(AnchorPoint.LEFT_CENTER), AnchorPoint.RIGHT_CENTER);
        poly.x -= triangleMargin;
        labelBox.add(poly);
      }

      if(rightTri) {
        let pts = Util.rightTriangle(labelBox.height()*0.5,labelBox.height()*0.75);
        let poly = new AnnoPolygon(0,0,pts);
        poly.renderLayer = RenderLayer.BACKGROUND;
        poly.fill = backgroundRect.stroke;

        poly.attach(backgroundRect.anchor(AnchorPoint.RIGHT_CENTER), AnchorPoint.LEFT_CENTER);
        poly.x += triangleMargin;
        labelBox.add(poly);
      }
    }
  }

  private processNodeLabelBackground(lbl : NodeLabelFragment, labelPadding : number, labelBox : AnnoGroup) : AnnoRectElement {
    let backgroundRect = new AnnoRectElement(-labelPadding, -labelPadding, labelBox.width()+labelPadding*2, labelBox.height()+labelPadding*2, 2.0, 1.0);
    backgroundRect.stroke = Util.rgb2hex(Util.hsl2rgb(lbl.strokeColor));
    backgroundRect.fill = Util.rgb2hex(Util.hsl2rgb(lbl.color));
    backgroundRect.renderLayer = RenderLayer.BACKGROUND;

    labelBox.add(backgroundRect);
    return backgroundRect;
  }

  private processCurlyAttachment(lbl : NodeLabelFragment, blockBBox : BBox, curlyHeight : number, annotationBox : AnnoGroup) : AnnoCurlyElement {
    let curlyObj = new AnnoCurlyElement(0,0, blockBBox.w, curlyHeight);
    curlyObj.renderLayer = RenderLayer.LABEL;
    curlyObj.stroke = Util.rgb2hex(Util.hsl2rgb(lbl.strokeColor));

    curlyObj.attach(blockBBox.anchor(AnchorPoint.TOP_LEFT), AnchorPoint.BOTTOM_LEFT);
    annotationBox.add(curlyObj);

    return curlyObj;
  }

  private lineBBox(line : AnnoTextLine, startpos : number, endpos : number) : BBox {
    let rel_startpos = Math.max(this.start(), startpos)-this.start();
    let rel_endpos = Math.min(this.end(), endpos)-this.start();

    return line.section(rel_startpos, rel_endpos);
  }

  private resolveLabelPositioning(annotationBoxLayer : Map<Array<number>>, labelBoxes : Array<AnnoGroup>) : INodeLayerBinding {
    for(let key of Object.keys(annotationBoxLayer)) {
      annotationBoxLayer[key].sort((x,y) => labelBoxes[x].x-labelBoxes[y].x);
    }

    let labelLayer = [];
    let labelMargin = this.parent.doc.style.labelMargin;
    let layerEnd : Array<number> = [-labelMargin];

    let labelLayerKeys = Object.keys(annotationBoxLayer);
    labelLayerKeys.sort((x,y) => (Lines.nodeLayerPriority[x] || 0) - (Lines.nodeLayerPriority[y] || 0) );

    for(let key of labelLayerKeys) {
      for(let labelAlloc of annotationBoxLayer[key]) {
      // for(let labelAlloc of labelAllocOrder)
        let lbl = this.nodeLabels[labelAlloc];
        let assigned = false;

        for(let i = 0; i < layerEnd.length; i++) {
          if(labelBoxes[labelAlloc].x >= (layerEnd[i] + labelMargin)) {
            layerEnd[i] = labelBoxes[labelAlloc].x + labelBoxes[labelAlloc].width();
            labelLayer[labelAlloc] = i;
            assigned = true;
            break;
          }
        }

        if(assigned === false) {
          labelLayer[labelAlloc] = layerEnd.length;
          layerEnd.push(labelBoxes[labelAlloc].x + labelBoxes[labelAlloc].width());
        }
      }

      for(let i = 0; i < layerEnd.length; i++) {
        layerEnd[i] = Number.POSITIVE_INFINITY;
      }
    }

    // 4. Invert ordering of layers
    /*for(let i = 0; i < labelLayer.length; i++) {
      labelLayer[i] = layerEnd.length-labelLayer[i]-1;
    }*/

    return {
      layer:labelLayer,
      numLayers: layerEnd.length,
      priority: labelLayerKeys
    };
  }

  private computeNodeLayerInfo(annotationBoxes : Array<AnnoGroup>, labelLayer : Array<number>, numLayers : number) : INodeLayerInfo {
    // 5. Compute layer height.
    let labelMaxHeight = [];
    let labelLayerOffsets = [];
    for(let i = 0; i < numLayers; i++) {
      labelMaxHeight.push(0.0);
    }

    for(let i = 0; i < this.nodeLabels.length; i++) {
      labelMaxHeight[labelLayer[i]] = Math.max(
        labelMaxHeight[labelLayer[i]],
          annotationBoxes[i].height()
      );
    }

    let labelHeight = 0.0;
    let offset = 0.0;
    for(let i = 0; i < numLayers; i++) {
      labelLayerOffsets.push(offset);
      offset -= labelMaxHeight[i];
      labelHeight += labelMaxHeight[i];
    }
    labelLayerOffsets.push(labelHeight);

    return {heights:labelMaxHeight, offsets:labelLayerOffsets, totalHeight: labelHeight};
  }

  private placeLabels(line : AnnoTextLine) : INodeLayerPlacement {
    // 1. Compute label fragment x-offsets
    let annotationBoxes : Array<AnnoGroup> = [];
    let labelBoxes : Array<AnnoGroup> = [];

    let curlyHeight = this.parent.doc.style.curlyHeight;
    let labelPadding = this.parent.doc.style.labelPadding;

    let layer2labels : Map<Array<number>> = {};

    for(let lbl of this.nodeLabels) {
      let blockBBox = this.lineBBox(line, lbl.startpos, lbl.endpos);

      let annotationBox = new AnnoGroup();
      let labelBox = new AnnoGroup();

      let labelObj = new AnnoTextElement(0,0,lbl.label,lbl.bbox,"annotation-label");
      labelObj.data = {"popup-title": this.textExcerpt(lbl) + " : " + lbl.id, "popup-html": lbl.popup };
      labelObj.renderLayer = RenderLayer.LABEL;
      labelBox.add(labelObj);

      let backgroundRect = this.processNodeLabelBackground(lbl, labelPadding, labelBox);
      this.processExtensionMarkers(lbl, backgroundRect, labelBox);
      labelBox.compact();

      annotationBox.add(labelBox);

      let curlyObj = this.processCurlyAttachment(lbl, blockBBox, curlyHeight, annotationBox);
      labelBox.attach(curlyObj.anchor(AnchorPoint.TOP_CENTER), AnchorPoint.BOTTOM_CENTER);
      labelBox.x = Math.max(0, labelBox.x);

      let idxes = layer2labels[lbl.layer];
      if(idxes === undefined) {
        idxes = [];
        layer2labels[lbl.layer] = idxes;
      }

      idxes.push(labelBoxes.length);

      labelBoxes.push(labelBox);
      annotationBoxes.push(annotationBox);
    }

    let labelPos = this.resolveLabelPositioning(layer2labels, labelBoxes);
    let labelInfo = this.computeNodeLayerInfo(annotationBoxes, labelPos.layer, labelPos.numLayers);

    for(let i = 0; i < this.nodeLabels.length; i++) {
      let dy = labelInfo.offsets[labelPos.layer[i]];

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
  }

  private placeLabelBackgrounds(line : AnnoTextLine, placement : INodeLayerPlacement) {
    let totalHeight = placement.info.totalHeight;

    for(let key of placement.binding.priority.reverse()) {
      for(let i of placement.layer2labels[key]) {
        let lbl = this.nodeLabels[i];
        let rel_startpos = Math.max(this.start(), lbl.startpos)-this.start();
        let rel_endpos = Math.min(this.end(), lbl.endpos)-this.start();
        let b = line.section(rel_startpos, rel_endpos);

        let rect = new AnnoRectElement(b.x, b.y, b.w, b.h, 3,2);
        rect.renderLayer = RenderLayer.BACKGROUND;
        rect.styleClass = "annotation-background";
        rect.fill = Util.rgb2hex(Util.hsl2rgb(this.nodeLabels[i].color));

        this.masterBlock.add(rect);
      }
    }
  }

  private createTextLine() : AnnoTextLine {
    let line = new AnnoTextLine();
    line.textClass = "annotation-text";
    for(let block of this.blocks) {
      line.add(block.text, block.lengths, block.measurements);
    }
    return line;
  }

  private closestPtByX(pt : IPoint, pts : Array<IPoint>) : IPoint {
    let selectedPt = pts[0];
    let dist = Math.abs(pt.x-pts[0].x);
    for(let k = 1; k < pts.length; k++) {
      if(Math.abs(pt.x-pts[k].x) < dist) {
        dist = Math.abs(pt.x-pts[k].x);
        selectedPt = pts[k];
      }
    }
    return selectedPt;
  }

  private middlePoint(ptA : IPoint, ptB : IPoint) : IPoint {
    let leftX = Math.min(ptA.x,ptB.x);
    let rightX = Math.max(ptA.x,ptB.x);
    let topY = Math.min(ptA.y,ptB.y);
    let bottomY = Math.max(ptA.y,ptB.y);

    return {x: leftX+(rightX-leftX)/2.0, y: topY+(bottomY-topY)/2.0};
  }

  private possiblePointsAtNode(box : AnnoElement) : Array<IPoint> {
    let middlePt = box.anchor(AnchorPoint.TOP_CENTER);
    return [
      this.middlePoint(box.anchor(AnchorPoint.TOP_LEFT), middlePt),
      middlePt,
      this.middlePoint(box.anchor(AnchorPoint.TOP_RIGHT), middlePt)
    ];
  }

  private placeEdges(labelPlacement : INodeLayerPlacement) : number {
    let bottomY = -labelPlacement.info.totalHeight;
    // let edgeBoxes : Array<AnnoGroup> = [];
    let edgeFragments : Array<EdgeFragment> = [];

    for(let edgeLbl of this.edgeLabels) {
      let headIdx = this.nodeId2idx[edgeLbl.head];
      let tailIdx = this.nodeId2idx[edgeLbl.tail];

      if(headIdx !== undefined && tailIdx !== undefined) {
        // Both labels exists on the same line.
        let tailBox = labelPlacement.labelBoxes[tailIdx];
        let headBox = labelPlacement.labelBoxes[headIdx];

        let edgeFragment = new EdgeFragment(this, edgeLbl);

        let tailPt = tailBox.anchor(AnchorPoint.TOP_CENTER);
        let headPt = headBox.anchor(AnchorPoint.TOP_CENTER);

        edgeFragment.leftAttachmentPt = headPt.x < tailPt.x ? headPt : tailPt;
        edgeFragment.rightAttachmentPt = headPt.x < tailPt.x ? tailPt : headPt;

        edgeFragment.leftHasNode = true;
        edgeFragment.rightHasNode = true;

        edgeFragment.arrowLeft = headPt.x < tailPt.x;
        edgeFragment.arrowRight = !edgeFragment.arrowLeft;

        edgeFragment.optimizeLeft(this.possiblePointsAtNode(headPt.x < tailPt.x ? headBox : tailBox));
        edgeFragment.optimizeRight(this.possiblePointsAtNode(headPt.x < tailPt.x ? tailBox : headBox));

        edgeFragments.push(edgeFragment);

      } else {
        // Line bypass
        if(headIdx === undefined && tailIdx === undefined) {
          let edgeFragment = new EdgeFragment(this, edgeLbl);

          edgeFragment.leftAttachmentPt = {x: 0, y : 0};
          edgeFragment.rightAttachmentPt = {x: this.parent.width, y: 0};

          edgeFragment.leftHasNode = false;
          edgeFragment.rightHasNode = false;

          edgeFragment.arrowLeft = edgeLbl.startline < this.linenb;
          edgeFragment.arrowRight = !edgeFragment.arrowLeft;

          edgeFragments.push(edgeFragment);

          // Add fragments
        } else {
          let edgeFragment = new EdgeFragment(this, edgeLbl);

          let sourceIdx = headIdx === undefined ? tailIdx : headIdx;
          let sourceBox = labelPlacement.labelBoxes[sourceIdx];

          let targetPt = sourceBox.anchor(AnchorPoint.TOP_CENTER);

          // Bind to left or right?
          if(edgeLbl.startline < this.linenb) {
            // left side
            edgeFragment.leftAttachmentPt = {x: 0, y: 0};
            edgeFragment.rightAttachmentPt = targetPt;

            edgeFragment.leftHasNode = false;
            edgeFragment.rightHasNode = true;

            edgeFragment.optimizeRight(this.possiblePointsAtNode(sourceBox));

            edgeFragment.arrowLeft = (headIdx === undefined) ? true : false;
            edgeFragment.arrowRight = !edgeFragment.arrowLeft;
          } else {
            // Right side

            edgeFragment.leftHasNode = true;
            edgeFragment.rightHasNode = false;

            edgeFragment.leftAttachmentPt = targetPt;
            edgeFragment.rightAttachmentPt = {x: this.parent.width, y: 0};

            edgeFragment.optimizeLeft(this.possiblePointsAtNode(sourceBox));

            edgeFragment.arrowLeft = (headIdx === undefined) ? false : true;
            edgeFragment.arrowRight = !edgeFragment.arrowLeft;
          }

          edgeFragments.push(edgeFragment);
        }
      }
    }

    let maxHeight = this.resolveEdgePositioning(edgeFragments, bottomY);
    for(let k = 0; k < edgeFragments.length; k++) {
      this.masterBlock.add(edgeFragments[k].compile(this.parent.doc.style.labelPadding, this.parent.doc.style.edgeBendDistance));
    }

    return Math.max(0,maxHeight);
  }

  private resolveEdgePositioning(edgeFragments : Array<EdgeFragment>, bottomY : number) : number {
    let allocOrder : Array<number> = [];
    let maxX = Number.NEGATIVE_INFINITY;
    for(let k = 0; k < edgeFragments.length; k++) {
      if(edgeFragments[k] !== null) {
        allocOrder.push(k);
        maxX = Math.max(maxX, edgeFragments[k].rightX());
      }
    }

    let layerAssignment : Map<number> = {};
    let layers : Array<Util.SparseGridSpatialIndex<number>>;
    layers = [ new Util.SparseGridSpatialIndex<number>(0, maxX, Math.max(1,Math.floor(maxX / 25))) ];

    allocOrder.sort((x,y) => edgeFragments[x].width()-edgeFragments[y].width());
    for(let k = 0; k < allocOrder.length; k++) {
      let lbl = this.edgeLabels[allocOrder[k]];
      let assigned = false;

      let frag = edgeFragments[allocOrder[k]];

      let minX = frag.leftX();
      let maxX = frag.rightX();

      for(let i = 0; i < layers.length; i++) {
        // Can be placed?
        if(layers[i].anyIntersection(minX, maxX) === false) {
          layerAssignment[allocOrder[k]] = i;
          assigned = true;
          layers[i].add(minX, maxX, allocOrder[k]);
          break;
        }
      }

      if(assigned === false) {
        layers.push(new Util.SparseGridSpatialIndex<number>(0, maxX, Math.max(1,Math.floor(maxX / 25))));
        layers[layers.length-1].add(minX, maxX, allocOrder[k]);
        layerAssignment[allocOrder[k]] = layers.length-1;
      }
    }

    let layerHeights : Array<number> = [];
    for(let k = 0; k < layers.length; k++) {
      layerHeights.push(Number.NEGATIVE_INFINITY);
    }

    for(let k = 0; k < allocOrder.length; k++) {
      let targetLayer = layerAssignment[allocOrder[k]];
      layerHeights[targetLayer] = Math.max(layerHeights[targetLayer], edgeFragments[allocOrder[k]].height());
    }

    let layerOffsets : Array<number> = [];

    let totalheight = this.parent.doc.style.edgeLayerMargin;
    let offset = bottomY-this.parent.doc.style.edgeLayerMargin;
    for(let k = 0; k < layers.length; k++) {
      layerOffsets.push(offset);
      offset -= layerHeights[k];
      totalheight += layerHeights[k];
    }

    // Move content relative to bottomY
    for(let k = 0; k < allocOrder.length; k++) {
      edgeFragments[allocOrder[k]].bottomY = layerOffsets[layerAssignment[allocOrder[k]]];
      // edgeBoxes[allocOrder[k]].applyTransformation();
    }

    return totalheight;
  }

  public layout(style : Style) {
    let line = this.createTextLine();
    let labelPlacement = this.placeLabels(line);
    let edgeTotalHeight = this.placeEdges(labelPlacement);

    this.placeLabelBackgrounds(line, labelPlacement);

    this.masterBlock.y = edgeTotalHeight + labelPlacement.info.totalHeight;
    this.height = line.height() + edgeTotalHeight + labelPlacement.info.totalHeight;

    this.masterBlock.add(line);
    this.masterBlock.applyTransformation();
  }

  public render(g : AnnoGraphics) {
    this.masterBlock.render(g);
  }
}

class Segment {
  public doc : FlowDocument;
  public marginText : string;
  public marginMeas : BBox;
  public startpos : number;
  public endpos : number;
  public text : string;
  public height : number = 0.0;
  public width : number;

  public blocks : Array<TextBlock> = [];
  public lines : Array<LineSegment> = [];
  public lineOffsetY : Array<number> = [];

  constructor(doc : FlowDocument) {
    this.doc = doc;
  }

  public layout() {
    this.lineOffsetY = [];
    this.height = this.doc.style.lineRowPadding;
    for(let line of this.lines) {
      this.lineOffsetY.push(this.height); // padding
      line.layout(this.doc.style);
      this.height += this.doc.style.lineRowPadding + line.height;
    }
  }

  public render(g : AnnoGraphics) {
    for(let k = 0; k < this.lines.length; k++) {
      g.push(0, this.lineOffsetY[k]);
      this.lines[k].render(g);
      g.pop();
    }

    if(this.doc.showSegmentMargin === true) {
      let labelOffsetX = -this.doc.segmentMarginWidth-this.doc.style.segmentInnerPadding;
      labelOffsetX    += (this.doc.segmentMarginWidth - this.marginMeas.w - this.doc.style.segmentMarginPaddingRight);

      g.styleClass = "annotation-margin";
      g.addText(RenderLayer.TEXT, this.marginText, labelOffsetX, this.lineOffsetY[0]+this.lines[0].height-(this.marginMeas.h+this.marginMeas.y));
    }
  }
}

class FlowDocument {
  private layers : Map<boolean> = {};
  private renderElem : SVGElement;
  private parentElem : HTMLDivElement;

  public maxWidth : number;
  public actualMaxWidth : number;
  public height : number;
  public segmentMarginWidth : number;

  public segments : Array<Segment> = [];
  public nodeLabels : Array<NodeLabelFragment> = [];
  public edgeLabels : Array<EdgeLabelFragment> = [];
  public uniqueCoordinates : Array<number> = [];
  public text : string;
  public showSegmentMargin : boolean = true;
  public segmentStart : number = undefined;
  public segmentEnd : number = undefined;

  private segStart : number = 0;
  private segEnd : number = 0;

  private boundLabels : Map<SVGTextElement> = {};

  public style : Style = {
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

  constructor(parentElem : HTMLDivElement, raw : IAnnotationDocument) {
    this.setTarget(parentElem);

    for(let layer of Object.keys(raw.nodeLayers)) {
      this.layers[layer] = true;
    }

    this.segment(raw);
    this.measureAll(raw);
    this.colorizeLabels();
  }

  public setTarget(el : HTMLDivElement) {
    this.parentElem = el;
    this.renderElem = document.createElementNS(Svg.NS, "svg") as SVGElement;
    this.parentElem.appendChild(this.renderElem);
  }

  public setLayers(layerList? : Array<string>) {
    this.layers = {};
    for(let layer of layerList) {
      this.layers[layer] = true;
    }
  }

  // Computes all partial coordinates, i.e. all unique text coordinates.
  private partials(raw : IAnnotationDocument) : Array<number> {
    let allRanges : Array<number> = [];

    for(let layer of raw.nodeLayers) {
      for(let range of layer.ranges) {
        allRanges.push(range);
      }
    }

    allRanges.push(0);
    allRanges.push(raw.text.length);

    allRanges.sort((x,y) => x-y);

    let uniqueRanges : Array<number> = [];
    let last = -1;
    for(let range of allRanges) {
      if(last !== range) {
        uniqueRanges.push(range);
        last = range;
      }
    }

    return uniqueRanges;
  }

  // Extracts segments, and extracts all text partials with each segment
  private segment(raw : IAnnotationDocument) {
    this.text = raw.text.replace(/\s/g, "\xa0");
    let re : RegExp = /\s+/g;

    let partialRanges = this.partials(raw);
    let partialPos = 0;

    this.uniqueCoordinates = partialRanges;

    for(let i = 0; i < raw.segments.length; i += 2) {
      let seg = new Segment(this);
      seg.startpos = raw.segments[i];
      seg.endpos = raw.segments[i+1];
      seg.text = raw.text.substring(seg.startpos, seg.endpos);
      seg.marginText = ((i/2)+1).toString(10);

      // 1. Build smaller text blocks (memory/performance optimization)
      let m : RegExpExecArray;
      let lastPosition = 0;
      while ((m = re.exec(seg.text)) !== null) {
        let startloc = lastPosition + seg.startpos;
        let endloc = m.index + m.length + seg.startpos;
        seg.blocks.push({startpos: startloc, endpos: endloc, text: this.text.substring(startloc, endloc), measurements: [], lengths: []});

        lastPosition = m.index + m.length;

        if (m.index === re.lastIndex) {
            re.lastIndex++;
        }
      }

      if(lastPosition !== seg.text.length) {
          seg.blocks.push({startpos: lastPosition + seg.startpos, endpos: seg.text.length + seg.startpos, text: this.text.substring(lastPosition+ seg.startpos, seg.startpos+seg.text.length), measurements: [], lengths: []});
      }

      // 2. Extract all text block partials
      let k = 0;
      while(partialPos < partialRanges.length && k < seg.blocks.length) {
        if(partialRanges[partialPos] <= seg.blocks[k].startpos) {
          partialPos++;
        } else if(partialRanges[partialPos] > seg.blocks[k].endpos) {
          k++;
        } else {
          let length = partialRanges[partialPos] - seg.blocks[k].startpos;
          seg.blocks[k].lengths.push(length);
          partialPos++;
        }
      }

      for(let block of seg.blocks){
        if(block.lengths.length > 0 && block.lengths[block.lengths.length-1] !== block.endpos-block.startpos) {
          block.lengths.push(block.endpos-block.startpos);
        } else if(block.lengths.length === 0) {
          block.lengths.push(block.endpos-block.startpos);
        }
      }

      this.segments.push(seg);
    }
  }

  private measureAll(raw : IAnnotationDocument) {
    // 1. Measure all text block partials and segment margins
    let textMeasurements = [];
    let textClasses = [];
    for(let seg of this.segments) {
      for(let block of seg.blocks) {
        for(let len of block.lengths) {
          textMeasurements.push(this.text.substring(block.startpos, block.startpos+len));
          textClasses.push("annotation-text");
        }
      }

      textMeasurements.push(seg.marginText);
      textClasses.push("annotation-margin");
    }

    // 2. Measure all labels
    for(let layer of raw.nodeLayers) {
      for(let key of Object.keys(layer.labels)) {
        textMeasurements.push(layer.labels[key]);
        textClasses.push("annotation-label");
      }

      textMeasurements.push(layer.name);
      textClasses.push("annotation-label");
    }

    for(let layer of raw.edgeLayers) {
      for(let key of Object.keys(layer.labels)) {
        textMeasurements.push(layer.labels[key]);
        textClasses.push("annotation-label");
      }

      textMeasurements.push(layer.name);
      textClasses.push("annotation-label");
    }

    // 3. Do measurements
    let measurements = Svg.measureTexts(this.renderElem, textMeasurements, textClasses);

    // 4. Save measurements
    let textMap = measurements["annotation-text"];
    for(let seg of this.segments) {
      for(let block of seg.blocks) {
        for(let len of block.lengths) {
          block.measurements.push(textMap[this.text.substring(block.startpos, block.startpos+len)]);
        }
      }
      seg.marginMeas = measurements["annotation-margin"][seg.marginText];
    }

    for(let layer of raw.nodeLayers) {
      for(let k = 0; k < layer.ranges.length/2; k++) {
        let labelfrag = new NodeLabelFragment();
        labelfrag.id = layer.id[k];
        labelfrag.layer = "node/" + layer.name;
        labelfrag.label = layer.labels[labelfrag.id];
        if(labelfrag.label === undefined) {
          labelfrag.label = layer.name;
        }

        labelfrag.startpos = layer.ranges[k*2];
        labelfrag.endpos = layer.ranges[k*2+1];
        labelfrag.bbox = measurements["annotation-label"][labelfrag.label];
        labelfrag.popup = layer.popups[labelfrag.id];
        this.nodeLabels.push(labelfrag);
      }
    }

    for(let layer of raw.edgeLayers) {
      for(let k = 0; k < layer.id.length; k++) {
        let labelfrag = new EdgeLabelFragment();
        labelfrag.id = layer.id[k];
        labelfrag.layer = "edge/" + layer.name;
        labelfrag.label = layer.labels[labelfrag.id];
        if(labelfrag.label === undefined) {
          labelfrag.label = layer.name;
        }

        labelfrag.head = layer.head[k];
        labelfrag.tail = layer.tail[k];
        labelfrag.bbox = measurements["annotation-label"][labelfrag.label];
        labelfrag.popup = layer.popups[labelfrag.id];
        this.edgeLabels.push(labelfrag);
      }
    }
  }

  private colorizeLabels() {
    // 2. Colorize
    let uniqueLabels = {};
    for(let lbl of this.nodeLabels) {
      uniqueLabels[lbl.label] = true;
    }

    for(let lbl of this.edgeLabels) {
      uniqueLabels[lbl.label] = true;
    }

    let colors = Util.generatePastelColors(Object.keys(uniqueLabels).length);
    let strokeColors : Array<Util.IHSLColor> = [];

    let darkFactor = Util.hsl(1,0.75,0.5);
    for(let clr of colors) {
      strokeColors.push(Util.mulHsl(clr, darkFactor));
    }

    let lblColor = {};
    let lblStrokeColor = {};
    let m = 0;
    for(let lblKey of Object.keys(uniqueLabels)) {
      lblColor[lblKey] = colors[m];
      lblStrokeColor[lblKey] = strokeColors[m];
      m += 1;
    }

    for(let lbl of this.nodeLabels) {
      lbl.color = lblColor[lbl.label];
      lbl.strokeColor = lblStrokeColor[lbl.label];
    }

    for(let lbl of this.edgeLabels) {
      lbl.color = lblColor[lbl.label];
      lbl.strokeColor = lblStrokeColor[lbl.label];
    }
  }

  // Computes line flow, splits segments into multiple segments
  private lineflow() {
    let segmentMaxWidth = this.maxWidth;
    let maxMarginWidth = 0.0;
    if(this.showSegmentMargin === true) {
      for(let seg of this.segments) {
        maxMarginWidth = Math.max(maxMarginWidth, seg.marginMeas.w);
      }

      maxMarginWidth += this.style.segmentMarginPaddingLeft+this.style.segmentMarginPaddingRight;
    }

    this.segmentMarginWidth = maxMarginWidth;
    segmentMaxWidth -= maxMarginWidth + this.style.segmentInnerPadding*2;

    let actualMaxWidth = segmentMaxWidth;

    let lineCnt = 0;

    for(let k =0; k < this.segments.length; k++) {
      let seg = this.segments[k];
      for(let line of seg.lines) {
        line.parent = undefined;
      }

      seg.width = segmentMaxWidth;
      seg.lines = [];

      let line = new LineSegment(seg);
      let widthAccum = 0.0;
      for(let k = 0; k < seg.blocks.length; k++) {
        let block = seg.blocks[k];
        let full = block.measurements[block.measurements.length-1];
        let width = full.w-full.x;

        if(line.blocks.length === 0) {
          line.blocks.push(block);
          widthAccum += width;
        } else if(widthAccum + width > segmentMaxWidth) {
          // split line
          seg.lines.push(line);
          line.linenb = lineCnt;
          lineCnt += 1;
          line = new LineSegment(seg);
          widthAccum = 0.0;

          line.blocks.push(block);
          widthAccum += width;
        } else {
          line.blocks.push(block);
          widthAccum += width;
        }

        actualMaxWidth = Math.max(actualMaxWidth, widthAccum);
      }

      if(line.blocks.length !== 0) {
        seg.lines.push(line);
        line.linenb = lineCnt;
        lineCnt += 1;
      }
    }

    this.actualMaxWidth = Math.max(this.maxWidth, actualMaxWidth + maxMarginWidth + this.style.segmentInnerPadding*2);
  }

  // Assigns labels to lines
  private assignLabels() {
    // 1. Find all line - label overlaps and assign label fragments
    //    to each line that has an overlap or cover.

    let i = 0;
    let k = 0;

    let lines : Array<LineSegment> = [];
    for(let seg of this.segments) {
      for(let ln of seg.lines) {
        lines.push(ln);
      }
    }

    let mapping = {};

    while(i < lines.length && k < this.uniqueCoordinates.length) {
      if(lines[i].start() > this.uniqueCoordinates[k]) {
        k++;
        console.log("Should not have happend!");
      }
      else if(this.uniqueCoordinates[k] > lines[i].end()) {
        i++;
      }
      else {
        mapping[this.uniqueCoordinates[k]] = i;
        k++;
      }
    }

    let nodeLineMapping : Map<Array<number>> = {};

    for(let lbl of this.nodeLabels) {
      if(this.layers[lbl.layer] === true) {
        let startline = mapping[lbl.startpos];
        let endline = mapping[lbl.endpos];

        nodeLineMapping[lbl.id] = [startline, endline+1];

        for(let h = startline; h <= endline; h++) {
          // Intersection test
          if(lines[h].start() < lbl.endpos && lines[h].end() > lbl.startpos) {
            lines[h].nodeId2idx[lbl.id] = lines[h].nodeLabels.length;
            lines[h].nodeLabels.push(lbl);
          }
        }
      }
    }

    for(let lbl of this.edgeLabels) {
      if(this.layers[lbl.layer] === true) {
        let headStartEnd = nodeLineMapping[lbl.head];
        let tailStartEnd = nodeLineMapping[lbl.tail];

        if(headStartEnd !== undefined && tailStartEnd !== undefined) {

          let startline = Math.min(headStartEnd[0], tailStartEnd[0]);
          let endline = Math.max(headStartEnd[1], tailStartEnd[1]);

          let actualStartLine = lines.length+1;
          let actualEndLine = -1;

          for(let h = startline; h < endline; h++) {
            if(lines[h].nodeId2idx[lbl.head] !== undefined || lines[h].nodeId2idx[lbl.tail] !== undefined) {
              actualStartLine = Math.min(actualStartLine,h);
              actualEndLine = Math.max(actualEndLine,h);
            }
          }

          actualEndLine += 1;
          lbl.startline = actualStartLine;
          lbl.endline = actualEndLine+1;

          for(let h = actualStartLine; h < actualEndLine; h++) {
            lines[h].edgeLabels.push(lbl);
          }
        }
      }
    }
  }

  public remove() {
      for(let key of Object.keys(this.boundLabels)) {
        $(this.boundLabels[key]).empty();
      }
      this.boundLabels = {};
      this.renderElem.innerHTML = null;
  }

  public update() {
    this.remove();

    this.segStart = this.segmentStart || 0;
    this.segEnd = Math.min(this.segmentEnd || this.segments.length, this.segments.length);

    this.maxWidth = parseFloat(window.getComputedStyle(this.parentElem, null).width);
    this.lineflow();
    this.assignLabels();

    let segmentOffsetY : Array<number> = [];

    let accumHeight = this.style.segmentRowPadding;

    for(let k = this.segStart; k < this.segEnd; k++) {
      let seg = this.segments[k];
      seg.layout();
      segmentOffsetY.push(accumHeight);
      accumHeight += seg.height + this.style.segmentRowPadding;
    }
    this.height = accumHeight;

    $(this.renderElem).detach();
    this.renderElem.setAttribute("style", "width:" + this.actualMaxWidth + "px; height:" + this.height + "px;");
    this.renderElem.setAttribute("version", "1.2");
    this.renderElem.setAttribute("class", "annotation-box");
    this.renderElem.innerHTML = "<defs>"+
        "<marker id=\"arrow\" markerWidth=\"10\" markerHeight=\"10\" refx=\"9\" refy=\"3\" orient=\"auto\" markerUnits=\"userSpaceOnUse\">" +
          "<path d=\'M0,0 Q 3,3 0,6 L9,3 z\' fill=\'#000\' />"+
        "</marker>"+
    "</defs>";

    let g : AnnoGraphics = new AnnoGraphics(this.renderElem);
    for(let k = this.segStart; k < this.segEnd; k++) {
      let seg = this.segments[k];
      g.layerClasses[RenderLayer.LABEL] = "annotation-segment";
      g.push(this.segmentMarginWidth+this.style.segmentInnerPadding, segmentOffsetY[k], "seg" + k);
      seg.render(g);
      g.pop();
    }

    if(this.showSegmentMargin === true) {
      g.clearStyle();
      g.styleClass = "annotation-margin-line";
      g.addLine(RenderLayer.BACKGROUND, this.segmentMarginWidth, 0, this.segmentMarginWidth, this.height);
    }

    g.write();

    this.boundLabels = {};
    let self = this;

    $(this.renderElem).find(".annotation-label").each(function (idx, el) {
      $(el).on("mouseenter", function(event) {
        if(self.boundLabels[idx] === undefined) {
          self.boundLabels[idx] = el as SVGTextElement;
          $(this).qtip({
            content: {
              attr: "data-popup-html", // Tell qTip2 to look inside this attr for its content
              title: {
                text: function(event, api) {
                  // Retrieve content from ALT attribute of the $('.selector') element
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
                      $(api.elements.titlebar).on("click", function(event) {
                        if(api.get("hide.event") !== false) {
                          $(this).find(".qtip-title").prepend($("<i class=\"pin icon\"></i>"));
                          api.set("hide.event", false);
                        }
                      });
                      $(this).draggable({
                          containment: "window",
                          handle: api.elements.titlebar,
                          drag: function (event, ui) {
                            if(api.get("hide.event") !== false) {
                              $(this).find(".qtip-title").prepend($("<i class=\"pin icon\"></i>"));
                              api.set("hide.event", false);
                            }
                          }
                      });
                  },
                  hide: function (event, api) {
                    api.set('hide.event', 'mouseleave');
                  },
                  hidden: function(event, api) {
                    $(this).find(".icon").remove();
                  }
              },
              position: {
                  my: "bottom center",  // Position my top left...
                  at: "top center", // at the bottom right of...
                  viewport: $(window)
              },
              style: { classes: "qtip-rounded qtip-light qtip-shadow" }
          });
        }
      });
    });

    $(this.parentElem).append(this.renderElem);
  }
}

export { FlowDocument };
