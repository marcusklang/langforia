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
import {MultipleSelectDropdown} from "./MultipleSelectDropdown";
import {AnnotationView} from "./AnnotationView";

class DocumentView extends React.Component<IDocumentViewProps, IDocumentViewState> {
  constructor(props : IDocumentViewProps) {
    super(props);
    this.state = {selectedLayers: []};
  }

  private computeFragments(datamodel : IDocEntry) {
    if(datamodel.fragments === undefined) {
      datamodel.fragments = {};
      datamodel.fragmentlist = [];
      if(datamodel.nodelayers["Paragraph"]) {
          $.each(datamodel.nodelayers["Paragraph"], (key, value) => {
              datamodel.fragments[key] = {start: value.start, end: value.end};
              datamodel.fragmentlist.push(key);
          });
      }
      else if(datamodel.nodelayers["Sentence"]) {
          $.each(datamodel.nodelayers["Sentence"], (key, value) => {
              datamodel.fragments[key] = {start: value.start, end: value.end};
              datamodel.fragmentlist.push(key);
          });
      } else {
          let key = 1;
          // Paragraph splitter
          let re = /((?:(?:.+)\n\s*)+\n\s*)|(.+)/g;
          let m;
          let last = 0;

          if(datamodel.text.indexOf("\n\n") > 0) {
              datamodel.text += "\n\n";
          }

          while ((m = re.exec(datamodel.text)) !== null) {
               if (m.index === re.lastIndex) {
                   re.lastIndex++;
               }

              if(last !== m.index) {
                  datamodel.fragments[key] = {start: last, end: m.index};
                  datamodel.fragmentlist.push(key);
                  key += 1;

                  last = m.index;
              }
          }

          datamodel.fragments[key] = {start: last, end: datamodel.text.length};
          datamodel.fragmentlist.push(key);

          datamodel.sentences = [];
          let re2 = /(\n\s*)/gm;
          last = 0;

          while ((m = re2.exec(datamodel.text)) !== null) {
              if(last !== m.index) {
                  datamodel.sentences.push(last);
                  datamodel.sentences.push(m.index);
                  last = m.index + m[1].length;
              }
          }

          datamodel.sentences.push(last);
          datamodel.sentences.push(datamodel.text.length);
      }
    }
  }

  public componentWillReceiveProps(nextProps : IDocumentViewProps) {
    if(nextProps.annodata !== undefined) {
      this.computeFragments(nextProps.annodata);
    }
  }

  public handleLayersChanged(selected) {
    this.setState({selectedLayers: selected});
  }

  public render() {
    if(this.props.annoview === true) {
      let options : Map<string> = {};
      // let fragments = [];

      if(this.props.annodata) {
        Object.keys(this.props.annodata.nodelayers).forEach(key => {
          options["node/" + key] = key + " (Nodes)";
        });

        Object.keys(this.props.annodata.edgelayers).forEach(key => {
          options["edge/" + key] = key + " (Edges)";
        });
      }

      return <div>
        <MultipleSelectDropdown text="Layers" onSelected={this.handleLayersChanged.bind(this)} selected={this.state.selectedLayers} options={options} />
        <AnnotationView layers={this.state.selectedLayers} doc={this.props.annodata} />
      </div>;
    } else {
      let lines = [];
      let reg = /\n/g;
      let last = 0;
      let found : RegExpExecArray;
      let i = 0;
      while(found = reg.exec(this.props.text)) {
          lines.push(<span key={"e" + (++i)}>{this.props.text.substring(last, found.index)}</span>);
          lines.push(<br key={"e" + (++i)}/>);

        last = found.index + found.length;
      }

      if(last !== this.props.text.length) {
        lines.push(this.props.text.substring(last));
      } else {
        lines.pop();
      }

      return <p>{lines}</p>;
    }
  }
}

export { DocumentView }
