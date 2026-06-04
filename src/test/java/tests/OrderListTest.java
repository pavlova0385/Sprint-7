package tests;

import client.ScooterApiClient;
import io.qameta.allure.junit4.DisplayName;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.notNullValue;

public class OrderListTest {
    private ScooterApiClient client;

    @Before
    public void setUp() {
        client = new ScooterApiClient();
    }

    @Test
    @DisplayName("В тело ответа возвращается список заказов")
    public void getOrdersReturnsOrdersList() {
        client.getOrders()
                .then()
                .statusCode(200)
                .body("orders", notNullValue());
    }
}
