package org.example.controller;


import org.example.model.Effort;
import org.springframework.stereotype.Repository;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;

@Controller
public class BackendController {
    Repository repository = new Repository();

    @GetMapping("/efforts")
    public String getEfforts(Model model) {
        var allSensors = repository.getEfforts();
        model.addAttribute("sensors", allSensors);
        return "efforts-html.html";
    }



    @ResponseBody
    @PostMapping("/effort")
    public String addNewEffort(@RequestBody Effort effort) {

        return "added new effort";
    }



}

