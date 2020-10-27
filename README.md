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


    -- create topic
    
    C:\frank\apache-kafka-2.4.1\bin\windows>kafka-topics.bat --create --zookeeper localhost:2181 --replication-factor 1 --partitions 2 --topic iris
    Created topic iris.
    
    C:\frank\apache-kafka-2.4.1\bin\windows>kafka-topics.bat --create --zookeeper localhost:2181 --replication-factor 1 --partitions 2 --topic iris-virginica
    Created topic iris-virginica.
    
    C:\frank\apache-kafka-2.4.1\bin\windows>kafka-topics.bat --create --zookeeper localhost:2181 --replication-factor 1 --partitions 2 --topic iris-others
    Created topic iris-others.
    
    C:\frank\apache-kafka-2.4.1\bin\windows>kafka-topics.bat --create --zookeeper localhost:2181 --replication-factor 1 --partitions 2 --topic model
    Created topic model.
    
    -- run model publisher
    
    -- check model topic
    C:\frank\apache-kafka-2.4.1\bin\windows>kafka-console-consumer.bat --bootstrap-server localhost:9092 --from-beginning --topic model
    {
      "input": {"type": "record",
    "name": "Iris",
    "fields": [
      {"name": "sepal_length_cm", "type": "double"},
      {"name": "sepal_width_cm", "type": "double"},
      {"name": "petal_length_cm", "type": "double"},
      {"name": "petal_width_cm", "type": "double"},
      {"name": "class", "type": "string"}
    ]},
      "output": "string",
      "action": [
    {"if": {"<": ["input.petal_length_cm", 2.5]},
      "then": {"string": "Iris-setosa"},
      "else":
      {"if": {"<": ["input.petal_length_cm", 4.8]},
    "then": {"string": "Iris-versicolor"},
    "else":
    {"if": {"<": ["input.petal_width_cm", 1.7]},
      "then": {"string": "Iris-versicolor"},
      "else": {"string": "Iris-virginica"}}
      }
    }
      ]
    }
    Processed a total of 1 messages
    Terminer le programme de commandes (O/N) ? o
    
    -- produce some data
    C:\frank\apache-kafka-2.4.1\bin\windows>kafka-console-producer.bat --broker-list localhost:9092 --topic iris
    >5.4,3.9,1.7,0.4,Iris-setosa
    >6.3,3.3,6.0,2.5,Iris-virginica
    >5.7,2.8,4.1,1.3,Iris-versicolor
    
    -- check results
    C:\frank\apache-kafka-2.4.1\bin\windows>kafka-console-consumer.bat --bootstrap-server localhost:9092 --from-beginning --topic iris
    5.4,3.9,1.7,0.4,Iris-setosa
    6.3,3.3,6.0,2.5,Iris-virginica
    5.7,2.8,4.1,1.3,Iris-versicolor
    Processed a total of 3 messages
    Terminer le programme de commandes (O/N)Â ? o
    
    C:\frank\apache-kafka-2.4.1\bin\windows>kafka-console-consumer.bat --bootstrap-server localhost:9092 --from-beginning --topic iris-others
    5.4,3.9,1.7,0.4,Iris-setosa
    5.7,2.8,4.1,1.3,Iris-versicolor
    Processed a total of 2 messages
    Terminer le programme de commandes (O/N)Â ? o
    
    C:\frank\apache-kafka-2.4.1\bin\windows>kafka-console-consumer.bat --bootstrap-server localhost:9092 --from-beginning --topic iris-virginica
    6.3,3.3,6.0,2.5,Iris-virginica
    Processed a total of 1 messages
    Terminer le programme de commandes (O/N)Â ? o
    
    
    -- program output
    ****************************** <-- initial model loaded
    *** Initializing model ... ***
    ******************************
    ==> input: 5.4,3.9,1.7,0.4,Iris-setosa
    ==> Result: "Iris-setosa"
    ==> input: 6.3,3.3,6.0,2.5,Iris-virginica
    ==> Result: "Iris-virginica"
    ==> input: 5.7,2.8,4.1,1.3,Iris-versicolor
    ==> Result: "Iris-versicolor"
    ******************************  <-- load new model
    *** Initializing model ... ***
    ******************************
    ==> input: 6.3,3.3,6.0,2.5,Iris-virginica   <-- test with new model
    ==> Result: "Iris-virginica"
    
    Process finished with exit code -1
    