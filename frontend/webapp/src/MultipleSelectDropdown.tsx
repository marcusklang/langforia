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

class MultipleSelectDropdown extends React.Component<IMultipleSelectDropdownProps, IMultipleSelectDropdownState> {
  constructor(props : IMultipleSelectDropdownProps) {
    super(props);
    this.state = { selected: props.selected };
  }

  private deltaVersion : number = 0;

  public propagate() {
    window.setTimeout(() => this.props.onSelected(this.state.selected), 100);
  }

  private dropdownVisible : boolean = false;

  public componentWillReceiveProps(nextProps : IMultipleSelectDropdownProps) {
    if(nextProps.selected !== undefined) {
      let layerselect = ReactDOM.findDOMNode<HTMLSelectElement>(this.refs["annotation-layers"]);
      let el = $(layerselect);
      el.dropdown("set selected", nextProps.selected);
    }
  }

  public componentDidMount() {
    let layerselect = ReactDOM.findDOMNode<HTMLSelectElement>(this.refs["annotation-layers"]);
    let el = $(layerselect);
    el.dropdown({
          allowAdditions: true,
          onChange: selected => {
            this.state.selected = selected;
            if(this.dropdownVisible === false) {
              this.propagate();
            } else {
              this.deltaVersion += 1;
            }
          },
          onShow: evt => {
              this.dropdownVisible = true;
          },
          onHide: evt => {
            this.dropdownVisible = false;
            if(this.deltaVersion > 0) {
              this.propagate();
              this.deltaVersion = 0;
            }
            return true;
          }
    });

    el.dropdown("set selected", this.props.selected);
  }

  public render() {
    let options = [];
    $.each(this.props.options, (key: any, value: any) => {
      options.push(<option key={key} value={key}>{value}</option>);
    });

    return <select ref={"annotation-layers"} multiple="true" className="ui fluid dropdown">
        <option key={"__DEFAULT__"} value="">{this.props.text}</option>
        {options}
    </select>;
  }
}

export {MultipleSelectDropdown}
