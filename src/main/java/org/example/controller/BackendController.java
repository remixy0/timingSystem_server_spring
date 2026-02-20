package org.example.controller;


import lombok.Getter;
import org.example.model.Effort;
import org.example.service.Service;
import org.springframework.stereotype.Repository;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;

@Controller
public class BackendController {
    Service service = new Service();

    @GetMapping("/efforts")
    public String getEfforts(Model model) {
        var allEfforts = service.getEfforts();
        model.addAttribute("efforts", allEfforts);
        return "efforts-html.html";
    }

//    @GetMapping("/efforts2")
//    public String getEfforts2(Model model) {
//        var allEfforts = service.getEfforts();
//        model.addAttribute("efforts", allEfforts);
//        return "efforts-html.html";
//    }



    @ResponseBody
    @PostMapping("/effort")
    public String addNewEffort(@RequestBody Effort effort) {
        service.addEffort(effort);
        return "added new effort";
    }


    @GetMapping("/effort2")
    public String addEffort(Model model) {
        return "add-effort.html";
    }



}

