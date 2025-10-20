package co.santiago.services;

import org.springframework.stereotype.Service;

@Service
public class PingService {

    public String ping() {
        return "pom";
    }
}
