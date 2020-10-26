# pfa-iris-kafka #

Frank Polet
October 25 2020

## Introduction ##

According to some industry standards (source : LightBend ) there are mainly 4 model serving architectures:

1. Embedding model as code, deployed into a stream engine
1. Model as data easier dynamic updates
1. Model serving as a service use a separate service, access from the streaming  engine
1. Dynamically controlled streams one way to implement model as data in a streaming engine

The purpose of this example is to illustrate dynamically controlled streams by showing how to operate a ML model as data (in PFA format) in a real time pipeline using kafka streams.

This PoC is based on the famous iris classification ml model.

The way how the PFA has been built is out of scope. We use the iris one as per example to illustrate the operating part.

## Description ##
We use two input kafka topics:

- "model"
- "iris"

The "model" topic stores the PFA json information of the ML model. It can be loaded using the *ModelPublisher.java* class

The "iris" topic contains the real time information on iris features to be checked. Record format is csv and looks like this:

*`sepal_length,sepal_width,petal_length,petal_width,class`*

We use two output topics:

- "iris-virginica"
- "iris-others"

The ML model classifies the input data.

The kafka streams topology is composed of two elements: 

- the model stream. It tracks in real time any model change and make sure new model is used as soon as received

- the iris input stream. It tracks in real time any new iris record, classifies it as soon as received and, based on classification result, put the record in "iris-virginica" topic or in "iris-others".

**Notes**: 

The PFA management part information can be found at https://github.com/modelop/hadrian



## Tests  ##

    



  