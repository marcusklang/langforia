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
 
interface Map<T> {
    [K: string]: T;
}

interface IAnnotationNodeLayer {
  name : string; // Name of the node layer
  id: Array<string>; // The globally unique node id
  ranges : Array<number>; // The ranges using interleave encoding: start, end, ...
  labels: Map<string>; // The labels for the nodes
  popups : Map<string>; // HTML to be placed in a popup
}

interface IAnnotationEdgeLayer {
  name : string; // Name of the edge layer
  id : Array<string>; // The globally unique edge id
  head : Array<string>; // The target node id
  tail : Array<string>; // The source node id
  labels : Map<string>; // The visible label
  popups : Map<string>; // HTML to be placed in a popup.
}

interface IAnnotationDocument {
  text : string; // The document text
  nodeLayers : Array<IAnnotationNodeLayer>; // The node layers
  edgeLayers : Array<IAnnotationEdgeLayer>; // The edge layers
  segments : Array<number>; // Text segments, e.g. sentence, paragraph or lines.
}

interface INodeEntry {
  id : number;
  start : number;
  end : number;
  comment : string;
  label : string;
  properties : Map<string>;
}

interface IEdgeEntry {
  id : number;
  head : number;
  tail : number;
  comment : string;
  label : string;
  properties : Map<string>;
}

interface IDocEntry {
  uri : string;
  docid : string;
  title : string;
  text : string;
  sentences : Array<number>;
  tokens : Array<number>;
  nodelayers : Map<Array<INodeEntry>>;
  edgelayers : Map<Array<IEdgeEntry>>;
  edgeindex?: Map<any>;
  fragments?: Map<any>;
  fragmentlist? : Array<any>;
  properties : Map<string>;
  id2layer : Array<any>;
}

interface IAnnotationViewState {
  ready?: boolean;
  maxSegments?: number;
  doc?: IAnnotationDocument;
}

interface IAnnotationViewProps {
  doc?: IDocEntry;
  layers?: Array<string>;
  showMargin?: boolean;
  showAllSegments?:boolean;
}
