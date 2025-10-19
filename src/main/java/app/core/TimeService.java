package app.core;

import mini.spring.annotations.ComponentByCiamb;

import java.time.ZonedDateTime;

@ComponentByCiamb
public class TimeService {

    public String nowIso() {
        return ZonedDateTime.now().toString();
    }
}
