package edu.gatech.cs6310.project7.controller;

import edu.gatech.cs6310.project7.model.CommandResult;
import edu.gatech.cs6310.project7.service.UniversitySystem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;


/**
 * Controller to handle all requests from the Admin page.
 */
@Slf4j
@RestController
@RequestMapping("/api/weka")
public class WekaController {


    private final UniversitySystem system;

    @Autowired
    public WekaController(final UniversitySystem system) {
        this.system = system;
    }

    @RequestMapping(value = "/analysis", method = RequestMethod.GET)
    public ResponseEntity wekaAnalysis() throws Exception {
        CommandResult result;
        result =  system.generateWekaTable();
        if (!result.getStatus()) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(result.getMessage());
        }
        System.out.println(result.getMessage());
        result =  system.wekaAnalysis();

        if (!result.getStatus()) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(result.getMessage());
        }

        String modelText = (String) result.getResults().get("model");
        String rules = modelText.substring(modelText.indexOf("Best rules found:") + 17);
        String[] lines = rules.trim().split(System.getProperty("line.separator"));
        Map<String, String> rulesMap = new HashMap<>();

        for (String line : lines) {
            try {
                String courseId = line.substring(line.indexOf('.') + 2, line.indexOf('='));
                String correlationId = line.substring((line.indexOf("==> ") + 4), line.lastIndexOf("="));
                if (!rulesMap.containsKey(courseId)) {
                    rulesMap.put(courseId, correlationId);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return ResponseEntity.ok(rulesMap);
    }

}
