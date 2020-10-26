// The Portable Format Analytic ( PFA model )
// https://modelop.github.io/Hadrian
// need to add "hadrian-standalone-0.8.1-jar-with-dependencies.jar" as dependency

package pfademo;


import com.opendatagroup.hadrian.jvmcompiler.PFAEngine;
import com.opendatagroup.antinous.pfainterface.PFAEngineFactory;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class PFAMain {

    //---------------------------------------------------------------------------------------------

    public PFAMain() {

    }

    //---------------------------------------------------------------------------------------------

    public static String readModel(String path) {
        try {
            byte[] encoded = Files.readAllBytes(Paths.get(path));
            return new String(encoded);
        }
        catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    //---------------------------------------------------------------------------------------------

    public static void main(String[] args) {
        try {
            PFAEngineFactory factory = new PFAEngineFactory();
            String modelJSON = readModel("pfa-model.json");
            PFAEngine<Object, Object> engine = factory.engineFromJson(modelJSON);

            // test with data from csv
            List<String> dataLines = Files.readAllLines(Paths.get("Iris.csv"));
            for (String dataLine : dataLines) {
                // System.out.println(dataLine); // (sepal_length,sepal_width,petal_length,petal_width,class)
                if (dataLine.startsWith("sepal_length")) continue;
                System.out.println("--------------");
                String[] columns = dataLine.split(",");
                String inputJson = "{\n" +
                        "    \"sepal_length_cm\": "+columns[0]+",\n" +
                        "    \"sepal_width_cm\": "+columns[1]+",\n" +
                        "    \"petal_length_cm\": "+columns[2]+",\n" +
                        "    \"petal_width_cm\": "+columns[3]+",\n" +
                        "    \"class\": \""+columns[4]+"\"\n" +
                        "}";
                System.out.println("==> input: "+dataLine);
                Object output = engine.action(engine.jsonInput(inputJson));
                System.out.println("==> Result: " +engine.jsonOutput(output));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //---------------------------------------------------------------------------------------------

}
