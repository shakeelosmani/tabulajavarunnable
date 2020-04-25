package Application;

import TabulaExtractor.Extract;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class Main {

    public static void main(String[] args) {

        File inputFile = new File("data.txt");

        String input = "";

        try {
            input = FileUtils.readFileToString(inputFile);
        } catch (IOException e) {
            e.printStackTrace();
        }


        Extract extract = new Extract();
        String json = extract.extractTable(input);
        System.out.println(json);
    }

}
