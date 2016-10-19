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
 
/// <reference path="../typings/browser.d.ts" />
/// <reference path="./interfaces.d.ts"/>

import * as React from "react";
import * as ReactDOM from "react-dom";
import {FlowDocument} from "./annotations/flowdocument";

declare namespace he {
  function encode(raw : string, opts?: any) : string;
  function decode(raw : string, opts?: any) : string;
}

class AnnotationView extends React.Component<IAnnotationViewProps, IAnnotationViewState> {
  private doc : FlowDocument;
  private renderElem : HTMLDivElement;
  private docChanged : boolean = true;
  private showMoreListener = (evt) => this.showMore();
  private showAllListener = (evt) => this.showAllToggle();

  constructor(props : IAnnotationViewProps) {
    super(props);
    this.state = { ready: false, maxSegments: 10 };
  }

  public componentWillUnmount() {
    if(this.doc !== undefined) {
      this.doc.remove();
      this.renderElem.innerHTML = "";
    }

    this.renderElem = undefined;
    window.removeEventListener("resize", this.windowResizerEventHandler);

    let showMoreButton = ReactDOM.findDOMNode<HTMLButtonElement>(this.refs["show-more"]);
    showMoreButton.removeEventListener("click", this.showMoreListener);
  }

  public componentWillReceiveProps(nextProps : IAnnotationViewProps) {
    if(nextProps.doc !== undefined && this.props.doc !== nextProps.doc) {
      this.docChanged = true;
      this.setState({doc: this.convert(nextProps.doc) });
    }
    else if(nextProps.doc === undefined) {
      this.docChanged = true;
      this.setState({doc: undefined });
    }
  }

  public componentDidUpdate() {
    if(this.state.doc === undefined && this.props.doc !== undefined) {
      this.setState({doc: this.convert(this.props.doc) });
    }

    if(this.state.doc === undefined && this.doc !== undefined) {
      this.doc.remove();
      this.doc = undefined;
      this.renderElem.innerHTML = "";
    }

    if(this.state.doc !== undefined) {
      let loaderElem = document.createElement("div");
      loaderElem.setAttribute("class", "ui small text loader");
      loaderElem.innerText = "Rendering...";

      if(this.doc !== undefined) {
        this.doc.remove();
        this.renderElem.innerHTML = "";
      }

      this.renderElem.appendChild(loaderElem);
      loaderElem.style.display = "none";
      loaderElem.style.display = "block";
      this.renderElem.style.minHeight = "200px";

      if(this.docChanged === true) {
        this.doc = new FlowDocument(this.renderElem, this.state.doc);
        this.doc.showSegmentMargin = false;
        this.docChanged = false;
        this.doc.segmentStart = 0;
      }

      if((this.props.showAllSegments || false) === true) {
        this.doc.segmentEnd = this.doc.segments.length;
      } else  {
        this.doc.segmentEnd = this.state.maxSegments;
      }

      setTimeout(() => {
        this.doc.setLayers(this.props.layers);
        this.doc.update();
        this.renderElem.removeChild(loaderElem);
        this.renderElem.style.minHeight = undefined;
      });
    }
  }

  private formatPropertyValue(text : string) : string {
    if(text === null) {
      return "null";
    }

    if(text.length >= 4 && text.substring(0,4) === "urn:") {
      let wd = /urn:wikidata:([QP][0-9]+)/;
      let m;

      if ((m = wd.exec(text)) !== null) {
          return "<a href=\"http://www.wikidata.org/wiki/" + m[1] + "\">" + text + "</a>";
      }

      let wp = /urn:wikipedia:([a-z]+):(.+)/;
      if((m = wp.exec(text)) !== null) {
        return "<a href=\"http://" + m[1] + ".wikipedia.org/wiki/" + encodeURIComponent(m[2]) + "\">" + he.encode(text) + "</a>";
      }
    }

    if(text.length > 5 && text.substring(0,7).toLowerCase() === "http://") {
      return "<a href=\"" + text + "\">" + he.encode(text) + "</a>";
    }

    return text;
  }

  private formatProperties(properties) : string {
      let text = ["<table class='prop-table'><thead><tr>"];

      $.each(properties, (key, value) => {
          text.push("<th>" + he.encode(key) + "</th>");
      });

      text.push("</tr></thead><tbody><tr>");

      $.each(properties, (key, value) => {
          text.push("<td>" + this.formatPropertyValue(value) + "</td>");
      });

      text.push("</tr></tbody></table>");

      return text.join("");
  }

  private showAllToggle() {
    if(this.state.doc !== undefined) {
      if(this.state.maxSegments < (this.state.doc.segments.length / 2)) {
        this.setState({maxSegments: this.state.doc.segments.length / 2});
      } else {
        this.setState({maxSegments: 10});
      }
    }
  }

  private abbreviateLayerName(layerName, usedNames : Map<boolean>) : string {
    let letters = [];
    let positions = [];
    let re = /([A-Z])/g;
    let m : RegExpExecArray;

    while ((m = re.exec(layerName)) !== null) {
        if (m.index === re.lastIndex) {
            re.lastIndex++;
        }

        letters.push(m[1]);
        positions.push(m.index);
    }

    // 1. Find all uppercase letters
    let candidate = letters.join("");
    if(usedNames[candidate] === undefined) {
      return candidate;
    }

    // 2. Use the last full uppercase
    candidate = letters.slice(0,letters.length-1).join("") + layerName.substring(positions[positions.length-1]);
    if(usedNames[candidate] === undefined) {
      return candidate;
    }

    // 3. Full name
    return layerName;
  }

  private convert(doc : IDocEntry) : IAnnotationDocument {
    let rawDoc : IAnnotationDocument = {
      text: doc.text,
      nodeLayers: [],
      edgeLayers: [],
      segments: []
    };

    let usedNames : Map<boolean> = {};
    let nodeId2fullId : Map<string> = {};

    for(let layername of Object.keys(doc.nodelayers)) {
      let nodeLayer : IAnnotationNodeLayer = {
        name: layername,
        id: [],
        ranges: [],
        labels: {},
        popups: {}
      };

      let idprefix = this.abbreviateLayerName(layername, usedNames);
      usedNames[idprefix] = true;

      for(let node of doc.nodelayers[layername]) {
        let id = idprefix + node.id;
        nodeLayer.id.push(id);
        nodeLayer.ranges.push(node.start);
        nodeLayer.ranges.push(node.end);
        nodeLayer.labels[id] = node.label;
        nodeLayer.popups[id] = this.formatProperties(node.properties);
        nodeId2fullId[node.id] = id;
      }

      rawDoc.nodeLayers.push(nodeLayer);
    }

    for(let layername of Object.keys(doc.edgelayers)) {
      let edgeLayer : IAnnotationEdgeLayer = {
        name: layername,
        id: [],
        head: [],
        tail: [],
        labels: {},
        popups: {}
      };

      let idprefix = this.abbreviateLayerName(layername, usedNames);
      usedNames[idprefix] = true;

      for(let edge of doc.edgelayers[layername]) {
        let id = idprefix + edge.id;
        edgeLayer.id.push(id);
        edgeLayer.head.push(nodeId2fullId[edge.head]);
        edgeLayer.tail.push(nodeId2fullId[edge.tail]);
        edgeLayer.labels[id] = edge.label;
        edgeLayer.popups[id] = this.formatProperties(edge.properties);
      }

      rawDoc.edgeLayers.push(edgeLayer);
    }

    // Find segments
    let re = /\n/g;
    let lastPosition = 0;
    let m;
    while ((m = re.exec(doc.text)) !== null) {
        if (m.index === re.lastIndex) {
            re.lastIndex++;
        }

        if(lastPosition !== m.index) {
          rawDoc.segments.push(lastPosition);
          rawDoc.segments.push(m.index);
        } else {
          rawDoc.segments.push(m.index);
          rawDoc.segments.push(m.index+m.length);
        }

        lastPosition = m.index+m.length;
        // View your result using the m-variable.
        // eg m[0] etc.
    }

    if(lastPosition !== doc.text.length) {
      rawDoc.segments.push(lastPosition);
      rawDoc.segments.push(doc.text.length);
    }

    return rawDoc;
  }

  private lastActiveTimeout : number = undefined;

  public windowResized() {
    if(this.lastActiveTimeout !== undefined) {
      window.clearTimeout(this.lastActiveTimeout);
    }

    this.lastActiveTimeout = window.setTimeout(() => {
        this.forceUpdate();
        this.lastActiveTimeout = undefined;
    }, 300);
  }

  private windowResizerEventHandler = (() => {this.windowResized();});

  private showMore() {
    this.setState({maxSegments: this.state.maxSegments+10});
  }

  public componentDidMount() {
    this.renderElem = ReactDOM.findDOMNode<HTMLDivElement>(this.refs["anno-component"]);
    let showMoreButton = ReactDOM.findDOMNode<HTMLButtonElement>(this.refs["show-more"]);
    let showAllButton = ReactDOM.findDOMNode<HTMLButtonElement>(this.refs["show-all"]);
    showMoreButton.addEventListener("click", this.showMoreListener);
    showAllButton.addEventListener("click", this.showAllListener);

    window.addEventListener("resize", this.windowResizerEventHandler, true);
    this.docChanged = true;
    this.componentDidUpdate();
  }

  public render() {
    let showMoreButtonVisible = false;
    if(this.state.doc !== undefined
      && (this.props.showAllSegments || false) === false
      && this.state.maxSegments < (this.state.doc.segments.length / 2)) {
      showMoreButtonVisible = true;
    }

    let showAllButton = this.state.doc !== undefined
     && (this.props.showAllSegments || false) === false
     && this.state.doc.segments.length > 20;

    return (<div>
      <div ref="anno-component"></div>
        <div className="two ui buttons">
          <button ref="show-more" className={"fluid ui button " + (showAllButton === true && showMoreButtonVisible === false ? "disabled" : "")} style={{display:(showAllButton || showMoreButtonVisible === true ? "block" : "none")}}>
            <i className="angle double down icon"></i>
              Show more...
          </button>
          <button ref="show-all" style={{display:(showAllButton ? "block" : "none")}} className={"fluid ui button" + (showMoreButtonVisible === false ? " positive" : "") }>
            <i className="expand icon"></i>Show all
          </button>
        </div>
      </div>);
  }
}

export { AnnotationView };
