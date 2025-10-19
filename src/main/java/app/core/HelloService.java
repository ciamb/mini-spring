package app.core;

import mini.spring.annotations.ComponentByCiamb;
import mini.spring.annotations.InjectByCiamb;

@ComponentByCiamb
public class HelloService {

    private final TimeService timeService;

    @InjectByCiamb
    public HelloService(TimeService timeService) {
        this.timeService = timeService;
    }

    public String hello(String name) {
        if (name == null) {
            name = "Fratellone";
        }
        return "Hi " + name + " @ " + timeService.nowIso();
    }
}
