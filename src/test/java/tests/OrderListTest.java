package tests;

import client.ScooterApiClient;
import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import org.junit.Before;
import org.junit.Test;

import static org.apache.http.HttpStatus.SC_OK;
import static org.hamcrest.CoreMatchers.notNullValue;

public class OrderListTest {
    private ScooterApiClient client;

    @Before
    public void setUp() {
        client = new ScooterApiClient();
    }

    @Test
    @DisplayName("В тело ответа возвращается список заказов")
    @Description("Проверяем, что запрос списка заказов возвращает 200, а тело ответа содержит orders")
    public void getOrdersReturnsOrdersList() {
        client.getOrders()
                .then()
                .statusCode(SC_OK)
                .body("orders", notNullValue());
    }
}