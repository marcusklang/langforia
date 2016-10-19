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
import {AnnotationView} from "./AnnotationView";

class DocumentFragment extends React.Component<IDocumentFragmentProps, IDocumentFragmentState> {
  constructor(props: IDocumentFragmentProps) {
    super(props);
    this.state = {display: false};
  }

  public handleChange(event) {
    let el : HTMLInputElement = event.target;
    console.dir(el.checked);
    this.setState({display: el.checked});
  }

  public render() {
    let annoview = null;
    if(this.state.display === true) {
      annoview = <AnnotationView doc={this.props.entry} layers={this.props.layers} />;
    }

    return (<div className="paragraph">
      <div className="ui checkbox">
         <input type="checkbox" className="annotation-checkbox" onChange={e => this.handleChange(e)} />
         <label>
            <p>{this.props.entry.text.substring(this.props.start, this.props.end)}</p>
         </label>
      </div>
      {annoview}
      </div>);
  }

}

export {DocumentFragment};
