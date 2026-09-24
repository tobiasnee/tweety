package de.tobiasnee.backend.controller;

import de.tobiasnee.backend.service.HelloService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class HelloController {

    private final HelloService helloService;

    @GetMapping("/hello")
    public String hello(){
        return helloService.getHello();
    }
}
