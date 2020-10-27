// The Portable Format Analytic ( PFA model )
// https://modelop.github.io/Hadrian
// need to add "hadrian-standalone-0.8.1-jar-with-dependencies.jar" as dependency
/*
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
******************************             <-- initial model loaded
*** Initializing model ... ***
******************************
==> input: 5.4,3.9,1.7,0.4,Iris-setosa
==> Result: "Iris-setosa"
==> input: 6.3,3.3,6.0,2.5,Iris-virginica
==> Result: "Iris-virginica"
==> input: 5.7,2.8,4.1,1.3,Iris-versicolor
==> Result: "Iris-versicolor"
******************************              <-- load new model
*** Initializing model ... ***
******************************
==> input: 6.3,3.3,6.0,2.5,Iris-virginica   <-- test with new model
==> Result: "Iris-virginica"

Process finished with exit code -1



 */
package pfademo;


import com.opendatagroup.hadrian.jvmcompiler.PFAEngine;
import com.opendatagroup.antinous.pfainterface.PFAEngineFactory;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

public class PFAMain {

    private PFAEngineFactory factory = null;
    private PFAEngine<Object, Object> engine = null;
    //---------------------------------------------------------------------------------------------

    public PFAMain() {

    }

    //---------------------------------------------------------------------------------------------

    public static void main(String[] args) {
        PFAMain pfamain = new PFAMain();
        // pfamain.test();
        pfamain.streamSetup();
    }

    //---------------------------------------------------------------------------------------------
    // test without kafka
    private void test() {
        try {
            this.initModel("pfa-model.json");
            /*
            factory = new PFAEngineFactory();
            String modelJSON = readModel("pfa-model.json");
            PFAEngine<Object, Object> engine = factory.engineFromJson(modelJSON);
            */

            // test with data from csv
            List<String> dataLines = Files.readAllLines(Paths.get("Iris.csv"));
            for (String dataLine : dataLines) {
                // System.out.println(dataLine); // (sepal_length,sepal_width,petal_length,petal_width,class)
                if (dataLine.startsWith("sepal_length")) continue;
                System.out.println("--------------");
                String[] columns = dataLine.split(",");
                String inputJson = "{\n" +
                        "    \"sepal_length_cm\": " + columns[0] + ",\n" +
                        "    \"sepal_width_cm\": " + columns[1] + ",\n" +
                        "    \"petal_length_cm\": " + columns[2] + ",\n" +
                        "    \"petal_width_cm\": " + columns[3] + ",\n" +
                        "    \"class\": \"" + columns[4] + "\"\n" +
                        "}";
                System.out.println("==> input: " + dataLine);
                Object output = engine.action(engine.jsonInput(inputJson));
                System.out.println("==> Result: " + engine.jsonOutput(output));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //---------------------------------------------------------------------------------------------

    private void streamSetup() {
        Properties streamsConfig = new Properties();
        // The name must be unique on the Kafka cluster
        streamsConfig.put(StreamsConfig.APPLICATION_ID_CONFIG, UUID.randomUUID().toString()); //"jpmml-example");
        // Brokers
        streamsConfig.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        // SerDes for key and value
        streamsConfig.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        streamsConfig.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());

        StreamsBuilder builder = buildTopology(streamsConfig, "model", "iris", "iris-virginica", "iris-others");
        KafkaStreams streams = new KafkaStreams(builder.build(), streamsConfig);
        streams.start();

        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
    }

    //---------------------------------------------------------------------------------------------
    // filter data based on their category

    private StreamsBuilder buildTopology(Properties streamsConfig, String modelTopic, String inputTopic, String okTopic, String koTopic) {
        StreamsBuilder builder = new StreamsBuilder();

        // model stream - initialize when new record received
        KStream<String, String> model = builder.stream(modelTopic, Consumed.with(Serdes.String(), Serdes.String()))
                .mapValues(value -> initModel(value));

        // data stream - classify when new record received
        KStream<String, String> dataStream[] = builder.stream(inputTopic, Consumed.with(Serdes.String(), Serdes.String()))
                .map((key, value) -> classifyInputData(key, value))
                .branch((key, value) -> testCondition(key, value),
                        (key, value) -> true);
        dataStream[0].to(okTopic, Produced.with(Serdes.String(), Serdes.String()));
        dataStream[1].to(koTopic, Produced.with(Serdes.String(), Serdes.String()));
        return builder;
    }

    //---------------------------------------------------------------------------------------------

    public static String readModel(String path) {
        try {
            byte[] encoded = Files.readAllBytes(Paths.get(path));
            return new String(encoded);
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    //---------------------------------------------------------------------------------------------

    private String initModel(String model) {
        try {
            System.out.println("******************************");
            System.out.println("*** Initializing model ... ***");

            this.factory = new PFAEngineFactory();
            String modelJSON = readModel("pfa-model.json");
            this.engine = factory.engineFromJson(modelJSON);


            System.out.println("******************************");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return model;
    }


    //---------------------------------------------------------------------------------------------

    private KeyValue<String, String> classifyInputData(String key, String value) {
        if ( (value == null) || (value.length() == 0) ) return new KeyValue<>("n/a", value);
        String[] columns = value.split(",");
        String inputJson = "{\n" +
                "    \"sepal_length_cm\": " + columns[0] + ",\n" +
                "    \"sepal_width_cm\": " + columns[1] + ",\n" +
                "    \"petal_length_cm\": " + columns[2] + ",\n" +
                "    \"petal_width_cm\": " + columns[3] + ",\n" +
                "    \"class\": \"" + columns[4] + "\"\n" +
                "}";
        System.out.println("==> input: " + value);
        Object output = engine.action(engine.jsonInput(inputJson));
        String category =  engine.jsonOutput(output);
        System.out.println("==> Result: " + category);

        return new KeyValue<>(category, value);
    }
    //---------------------------------------------------------------------------------------------
    // filtering condition for target topic ...

    private boolean testCondition(String key, String value) {
        if (key == null) return false;
        // ok should extract result from json ... taking some shortcut here ...
        return key.contains("virginica");
    }

    //---------------------------------------------------------------------------------------------

}
