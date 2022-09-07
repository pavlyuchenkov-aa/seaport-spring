package com.example.service2;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import com.example.service1.Timetable;
import com.example.service3.Stats;
import com.example.service2.JsonConvertor;

@RestController
@RequestMapping("/")
public class Service2Controller {

    @GetMapping(value = "/service2/createfile")
    @ResponseStatus(HttpStatus.OK)
    public File getJson() {
        RestTemplate restTemplate = new RestTemplate();
        Timetable timetable = restTemplate.getForObject("http://localhost:8081/service1", Timetable.class);

        ObjectWriter writer = new ObjectMapper().writer().withDefaultPrettyPrinter();
        try {
            writer.writeValue(new File("timetable.json"), timetable);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return new File("timetable.json");
    }

    @GetMapping(value = "/service2/gettimetable")
    public Timetable getScheduleFromJson(@RequestParam("filename") String fileName) {

        if (!Files.exists(Path.of(fileName))) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "This file doesn't exist");
        }
        else {
            Timetable timetable = new Timetable();
            ObjectMapper reader = new ObjectMapper();
            try {
                timetable = reader.readValue(new File(fileName), new TypeReference<>() {});
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            JsonConvertor.addConsoleShip(timetable);

            return timetable;
        }

    }

    @PostMapping(value = "/service2/result")
    @ResponseStatus(HttpStatus.CREATED)
    public void formJson(@RequestBody Stats stats) {
        ObjectWriter writer = new ObjectMapper().writer().withDefaultPrettyPrinter();
        try {
            writer.writeValue(new File("stats.json"), stats);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }


}
