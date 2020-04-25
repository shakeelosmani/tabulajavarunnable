package Application;

import Controllers.ExtractController;
import TabulaExtractor.Extract;
import org.apache.commons.io.FileUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import java.io.File;
import java.io.IOException;

@SpringBootApplication
@ComponentScan(basePackageClasses = ExtractController.class)
public class Main {

    public static void main(String[] args) {

        SpringApplication.run(Main.class, args);

    }

}
