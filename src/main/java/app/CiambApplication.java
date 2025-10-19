package app;

import mini.spring.core.ContainerOfCiamb;
import mini.spring.web.HttpServerOfCiamb;

import java.io.IOException;

public class CiambApplication {
    public static void main(String[] args) throws IOException {
        String scanningBasePackage = "app";
        ContainerOfCiamb containerOfCiamb = new ContainerOfCiamb(scanningBasePackage);
        HttpServerOfCiamb httpServerOfCiamb = new HttpServerOfCiamb(containerOfCiamb);
        httpServerOfCiamb.scanControllers(scanningBasePackage);
        httpServerOfCiamb.startServer(9800);
    }
}
