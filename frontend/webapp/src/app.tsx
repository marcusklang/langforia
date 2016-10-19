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
 
/*jshint quotmark:false */
/*jshint white:false */
/*jshint trailing:false */
/*jshint newcap:false */
/*global React, Router*/

/// <reference path="../typings/browser.d.ts" />
/// <reference path="./interfaces.d.ts"/>

import * as React from "react";
import * as ReactDOM from "react-dom";
import {DocumentView} from "./DocumentView";

declare var Router;
declare var config_langs : Map<Array<String>>;
declare var config_friendly : Map<String>;

import { ENTER_KEY } from "./constants";

interface JQuery {
    dropdown() : JQuery;
    dropdown(opts? : any) : JQuery ;
    dropdown(cmd : string, opts? : any) : JQuery;
}

class NlpApp extends React.Component<IAppProps, IAppState> {

  public state : IAppState;
  private router : any;

  constructor(props : IAppProps) {
    super(props);
    this.state = { annotating: false, text: "", config:"en/default" };
  }

  public annotate_text(text : string) {
    this.setState({annotating: true, doc: undefined});

    $.ajax("./" + this.state.config + "/api/annoviz", {
      data: text,
      processData: false,
      type: "POST",
      contentType: "text/plain; charset=utf-8"
    }).done(data => {
        let result : IDocEntry = data;
        this.setState({annotating: false, doc: result});
    }).fail((xhr: JQueryXHR, textStatus: string, errorThrown: string) => {
      this.setState({annotating: false});
      console.error(errorThrown);
    });
  }

  private annotate_wikipedia(lang, title) {
    this.setState({annotating: true, doc: undefined});

    let settings = {
      "async": true,
      "crossDomain": true,
      "url": "https://" + lang + ".wikipedia.org/api/rest_v1/page/html/" + encodeURIComponent(title),
      "method": "GET"
    };

    $.ajax(settings).done((response) => {
      $.ajax("./" + this.state.config + "/api/wikipedia/annoviz", {
        data: response,
        processData: false,
        type: "POST",
        contentType: "text/html; charset=utf-8"
      }).done(data => {
          let result : IDocEntry = data;
          this.setState({annotating: false, doc: result});
      }).fail((xhr: JQueryXHR, textStatus: string, errorThrown: string) => {
        this.setState({annotating: false});
        console.error(errorThrown);
      });
    });
  }

  public annotate_click(e : MouseEvent) {
    // Determine mode:
    let mode = $(".modeselector .active").attr("data-tab");

    if(mode === "text") {
      let elem = ReactDOM.findDOMNode<HTMLTextAreaElement>(this.refs["textholder"]);
      if(elem.value !== undefined && elem.value.length > 0) {
        this.annotate_text(elem.value);
      }
    } else if(mode === "wikipedia") {
      let wp = ReactDOM.findDOMNode<HTMLInputElement>(this.refs["wikipedia-page"]);
      if(wp.value.length > 0) {
        this.annotate_wikipedia(this.state.config.substring(0,2), wp.value);
      }
    }
  }

  public selectConfig(lang : string, config : string) {
    this.setState({config: lang +"/"+ config});
    let langselect = ReactDOM.findDOMNode<HTMLDivElement>(this.refs["lang-select"]);
    $(langselect).dropdown("set selected", "en/default");
  }

  private bindSearch(searchelem, language) {
    $(searchelem).search("destroy");
    $(searchelem).search({
      searchFields   : [
        'title'
      ],
        minCharacters : 3,
        showNoResults: true,
        searchDelay: 300,
        apiSettings   : {
          dataType: "jsonp",
          onResponse: function(wikipediaResponse) {
            let response = {
                results: []
              }
            ;
            for(let result of wikipediaResponse.query.prefixsearch) {
              response.results.push(result);
            }

            return response;
          },
          url: "//"+language+".wikipedia.org/w/api.php?action=query&list=prefixsearch&format=json&pssearch={query}"
        }
      });
  }

  public componentDidMount() {
    let setState = this.setState;
    this.router = Router({
      "/": setState.bind(this, {text: ""}),
      "/:lang/:config/": (lang,config) => this.selectConfig(lang,config)
    });
    this.router.init("/");

    let langselect = ReactDOM.findDOMNode<HTMLDivElement>(this.refs["lang-select"]);
    let searchbox = ReactDOM.findDOMNode<HTMLDivElement>(this.refs["wiki-search"]);
    $(langselect)
      .dropdown({
        allowCategorySelection: true,
        onChange: (value, text, $selectedItem) => {
          this.setState({config: value});
          this.bindSearch(searchbox, value.substring(0,2));
        }
      });
    $(langselect).dropdown("set selected", "en/default");
    this.bindSearch(searchbox, "en");

    $('.modeselector .item').tab();
  }

  private wikiEditionFullName = {"en" : "English", "sv": "Swedish", "ru": "Russian", "es": "Spanish", "de":"German", "fr": "French"};

  public render() {
    let langs = Object.keys(config_langs);
    let lang_objs = [];
    for(let lang of langs) {
      let configs = [];
      for(let config of config_langs[lang]) {
        configs.push(
          <div className="item" data-value={lang + "/" + config} key={"c_" +lang + "/" + config}>{config_friendly[lang + "/" + config]}</div>
        );
      }

      lang_objs.push(<div className="item" data-value={lang + "/default"} key={"l_" + lang}>
          <i className="dropdown icon"></i>
          <span className="text">{lang}</span>
          <div className="menu">
            {configs}
          </div>
        </div>);
    }

    let wikiEdition = this.wikiEditionFullName[(this.state.config || "en").substring(0,2)];

    return (
      <div>
      <div className="ui top attached tabular menu modeselector">
        <a className="active item" data-tab="text">Text</a>
        <a className="item" data-tab="wikipedia">Wikipedia</a>
      </div>
      <div className="ui bottom attached active tab segment modeselector" data-tab="text">
        <div className="ui form">
          <div className="field">
            <textarea ref="textholder"></textarea>
          </div>
        </div>
      </div>
      <div className="ui bottom attached tab segment modeselector" data-tab="wikipedia">
        <div className="ui form">
          <div className="field">
            <label>Page Title ({wikiEdition} Wikipedia)</label>
            <div className="ui search" ref="wiki-search">
              <div className="ui icon input">
                <input className="prompt" type="text" ref="wikipedia-page" placeholder="Wikipedia page title..." />
                <i className="search icon"></i>
              </div>
              <div className="results"></div>
            </div>
          </div>
        </div>
      </div>
        <div className="ui form">
          <div className="field">
            <div ref="lang-select" className="ui dropdown icon button">
              <i className="world icon"></i>
              <span className="text">Select Language</span>
              <div className="menu">
                {lang_objs}
              </div>
            </div>
            <button onClick={(e : MouseEvent) => this.annotate_click(e)} className={((this.state.annotating === true) ? "loading " : "") + "positive ui button"}>Annotate</button>
          </div>
        </div>
        <div className="ui segment">
          <div className={"ui " + (this.state.annotating ? "active " : "") + "inverted dimmer"}>
            <div className="ui large text loader">Annotating...</div>
          </div>
          <div style={{minHeight: "150pt"}}>
          <DocumentView ref="docview" annoview={this.state.doc !== undefined} annodata={this.state.doc} text={""} />
          </div>
        </div>
      </div>
    );
  }
}

$(window).ready(function() {
  ReactDOM.render(
    <NlpApp/>,
    document.getElementById("nlpapp")
  );
});
