package tests;

import client.ScooterApiClient;
import io.qameta.allure.junit4.DisplayName;
import model.Order;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import util.TestData;

import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.CoreMatchers.notNullValue;

@RunWith(Parameterized.class)
public class OrderCreateTest {
    private final String[] colors;
    private ScooterApiClient client;

    public OrderCreateTest(String[] colors) {
        this.colors = colors;
    }

    @Parameterized.Parameters(name = "Цвета заказа: {0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {new String[]{"BLACK"}},
                {new String[]{"GREY"}},
                {new String[]{"BLACK", "GREY"}},
                {new String[]{}},
                {null}
        });
    }

    @Before
    public void setUp() {
        client = new ScooterApiClient();
    }

    @Test
    @DisplayName("При создании заказа тело ответа содержит track")
    public void orderCanBeCreatedWithColorOptions() {
        Order order = TestData.orderWithColors(colors);

        client.createOrder(order)
                .then()
                .statusCode(201)
                .body("track", notNullValue());
    }
}

