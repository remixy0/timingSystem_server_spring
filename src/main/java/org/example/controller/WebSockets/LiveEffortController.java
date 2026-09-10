package org.example.controller.WebSockets;

import org.example.model.DTOs.EffortDTO;
import org.example.service.Service;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.Collections;
import java.util.List;

@Controller
public class LiveEffortController {

    private final Service service;

    public LiveEffortController(Service service) {
        this.service = service;
    }

    @MessageMapping("/get-my-efforts")
    @SendToUser("/queue/efforts")
    public List<EffortDTO> getMyEfforts(Principal principal) {
        if (principal == null) {
            return Collections.emptyList();
        }

        String username = principal.getName();
        return service.getEffortsDTO(username);
    }
}