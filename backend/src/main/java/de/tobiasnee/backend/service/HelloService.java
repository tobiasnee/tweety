package de.tobiasnee.backend.service;

import org.springframework.stereotype.Service;

@Service
public class HelloService {

    public String getHello(){
        return "Hello Mini-Twitter";
    }
}
