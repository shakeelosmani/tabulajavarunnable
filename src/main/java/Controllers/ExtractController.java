package Controllers;


import TabulaExtractor.Extract;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController @RequestMapping("/")
public class ExtractController {

    @PostMapping("/extract")
    public String index(@RequestBody String jsonInput) {
        Extract extract = new Extract();
        String json = extract.extractTable(jsonInput);

        return json;
    }

}
