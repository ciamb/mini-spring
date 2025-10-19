package app.web;

import app.core.HelloService;
import mini.spring.annotations.ComponentByCiamb;
import mini.spring.annotations.InjectByCiamb;
import mini.spring.web.ControllerByCiamb;
import mini.spring.web.GetByCiamb;

@ComponentByCiamb
@ControllerByCiamb(basePath = "/api")
public class HelloByCiambController {

    private final HelloService helloService;

    @InjectByCiamb
    public HelloByCiambController(HelloService helloService) {
        this.helloService = helloService;
    }

    @GetByCiamb(path = "/ping")
    public String ping() {
        return "pong 🏓";
    }

    @GetByCiamb(path = "/hello")
    public String hello(String name) {
        return helloService.hello(name);
    }
}
