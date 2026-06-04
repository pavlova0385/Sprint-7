package util;

import model.Courier;
import model.Order;

import java.time.LocalDate;
import java.util.UUID;

public final class TestData {
    public static final String PASSWORD = "4325";
    public static final String FIRST_NAME = "cdsre";
    public static final String WRONG_VALUE = "wrong_value";

    private TestData() {
    }

    public static Courier randomCourier() {
        return new Courier("asdewq_" + uniqueSuffix(), PASSWORD, FIRST_NAME);
    }

    public static Order orderWithColors(String[] colors) {
        return new Order(
                "Anna",
                "Ivanova",
                "Moscow, Tverskaya 1",
                "4",
                "+79990001122",
                3,
                LocalDate.now().plusDays(1).toString(),
                "Handle with care",
                colors
        );
    }

    public static String uniqueSuffix() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 10);
    }
}

