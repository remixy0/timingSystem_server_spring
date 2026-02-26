package org.example.controller;


import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import org.example.model.Athlete;
import org.example.model.Effort;
import org.example.service.Service;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Repository;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Controller
public class BackendController {
    Service service = new Service();
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    @GetMapping(path = "/efforts", produces = MediaType.TEXT_HTML_VALUE)
    public String getEfforts(Model model) {
        var allEfforts = service.getEfforts();
        model.addAttribute("efforts", allEfforts);

        return "dashboard2";
    }

    @GetMapping(path = "/efforts", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribeToEfforts() {
        SseEmitter emitter = new SseEmitter(600000L); // timeout 10 minut
        emitters.add(emitter);

        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError((e) -> emitters.remove(emitter));

        return emitter;
    }


    @ResponseBody
    @PostMapping("/effort")
    public String addNewEffort(@RequestBody Effort effort) {
        service.addEffort(effort);

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send("ODSWIEZ");
            } catch (IOException e) {
                emitters.remove(emitter);
            }
        }
        return "added new effort";
    }

    @ResponseBody
    @PostMapping("/athlete")
    public String addNewAthlete(@RequestBody Athlete athlete) {
        service.addAthlete(athlete);

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send("ODSWIEZ");
            } catch (IOException e) {
                emitters.remove(emitter);
            }
        }
        return "added new athlete";
    }

}

