package com.camping.tests.config;

import io.cucumber.core.backend.ObjectFactory;
import io.cucumber.picocontainer.PicoFactory;

public class TestConfigurationModule {

    public static void configureContainer(ObjectFactory objectFactory) {
        if (objectFactory instanceof PicoFactory) {
            PicoFactory picoFactory = (PicoFactory) objectFactory;
            picoFactory.addClass(TestConfiguration.class);
        }
    }
}
