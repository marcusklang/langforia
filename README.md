# langforia
Language pipelines

## What?
This is a library for building language pipelines.

## Prerequisites

 * JDK 8
 * Maven 3+

## How to use/install?

1. Clone this repo recursivly
2. Download the bigger models from [semantica](http://semantica.cs.lth.se/langforia/external.zip) and unpack its content to directory external
3. Run external/install.sh - installs model dependencies to your local maven repository
4. mvn install - to install the library into your local maven repo
5. Depend on the libraries or test the webserver demo in the **frontend** directory.

## Example of usage

In directory **frontend** there is a webserver implementation that can be used to test out what is available and visualize the results.

