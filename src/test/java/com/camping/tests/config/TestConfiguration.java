package com.camping.tests.config;

public class TestConfiguration {
    private final String adminBaseUrl;
    private final String kioskBaseUrl;
    private final String reservationBaseUrl;

    public TestConfiguration() {
        this.adminBaseUrl = getConfigValue("ADMIN_BASE_URL", "http://localhost:18082");
        this.kioskBaseUrl = getConfigValue("KIOSK_BASE_URL", "http://localhost:18081");
        this.reservationBaseUrl = getConfigValue("RESERVATION_BASE_URL", "http://localhost:18083");
    }

    private String getConfigValue(String key, String defaultValue) {
        return System.getProperty(key, System.getenv().getOrDefault(key, defaultValue));
    }

    public String getAdminBaseUrl() {
        return adminBaseUrl;
    }

    public String getKioskBaseUrl() {
        return kioskBaseUrl;
    }

    public String getReservationBaseUrl() {
        return reservationBaseUrl;
    }
}
