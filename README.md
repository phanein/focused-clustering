# Focused Clustering and Outlier Detection in Large Attributed Graphs

Bryan Perozzi

## Disclaimers

1. This code is very "research", and so is probably more useful as an example than a product
1. Distance Metric Learning based on original code from Eric Xing, [available here]( http://www.cs.cmu.edu/~epxing/papers/Old_papers/code_Metric_online.tar.gz)

## Implementation Overview

There are two programs.  The first is a matlab script which learns a distance metric and reweighs the input graph.  The second is a java program which extracts communities & outliers from the reweighted graph

## Running

An example batch file <code>focusco.bat</code> shows how to use the matlab program from the command line.  (it'll run the whole thing soon)

## Installation

### Requirements
1. A recent version of Matlab
2. Java 6+

### Setup
The only required step should be to build the java, e.g. with maven:

<code>
$ cd java_src

$ mvn clean install
</code>

