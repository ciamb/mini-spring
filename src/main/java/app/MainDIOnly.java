package app;

import app.core.HelloService;
import mini.spring.core.ContainerOfCiamb;

public class MainDIOnly {
    public static void main(String[] args) {
        // creo il container
        ContainerOfCiamb containerOfCiamb = new ContainerOfCiamb("app");
        // recupero l'istanza di HelloService dal container
        HelloService helloService = containerOfCiamb.getCiambBean(HelloService.class);
        // stampo il ciao
        System.out.println(helloService.hello("Fratello"));
    }
}
