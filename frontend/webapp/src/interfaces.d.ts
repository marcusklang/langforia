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

interface IDocumentViewProps {
  text : string;
  annoview: boolean;
  annodata? : IDocEntry;
}

interface IDocumentViewState {
  selectedLayers: Array<string>;
}

interface IDocumentFragmentState {
  display?: boolean;
}

interface IDocumentFragmentProps {
  entry : IDocEntry;
  start: number;
  end: number;
  layers: Array<string>;
}

interface Map<T> {
    [K: string]: T;
}

interface IAnnotationState {

}

interface IMultipleSelectDropdownState {
  selected? : Array<string>;
}

interface IMultipleSelectDropdownProps {
  text: string;
  options: Map<string>;
  selected?: Array<string>;
  onSelected: any;
}

interface IAnnotationProps {
  data: IAnnotationDocument;
}

interface IAppProps {

}

interface IAppState {
  doc?: IDocEntry;
  config?: string;
  annotating?: boolean;
  text?: string;
}

/// <reference path="./annotations.d"/>
