package tests;

import client.ScooterApiClient;
import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import model.Courier;
import model.CourierCredentials;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import util.TestData;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;

public class CourierCreateTest {
    private ScooterApiClient client;
    private Courier courier;
    private Integer courierId;

    @Before
    public void setUp() {
        client = new ScooterApiClient();
        courier = TestData.randomCourier();
    }

    @After
    public void tearDown() {
        Integer idToDelete = courierId;
        if (idToDelete == null) {
            idToDelete = tryLoginAndGetCourierId();
        }
        if (idToDelete != null) {
            client.deleteCourier(idToDelete);
        }
    }

    @Test
    @DisplayName("Курьера можно создать")
    public void courierCanBeCreated() {
        client.createCourier(courier)
                .then()
                .statusCode(201)
                .body("ok", equalTo(true));

        courierId = client.loginCourier(CourierCredentials.from(courier))
                .then()
                .statusCode(200)
                .extract()
                .path("id");
    }

    @Test
    @DisplayName("Нельзя создать двух одинаковых курьеров")
    public void duplicateCourierCannotBeCreated() {
        client.createCourier(courier)
                .then()
                .statusCode(201)
                .body("ok", equalTo(true));

        courierId = client.loginCourier(CourierCredentials.from(courier))
                .then()
                .statusCode(200)
                .extract()
                .path("id");

        client.createCourier(courier)
                .then()
                .statusCode(409)
                .body("message", notNullValue());
    }

    @Test
    @DisplayName("Для создания курьера нужно передать логин")
    public void courierCannotBeCreatedWithoutLogin() {
        client.createCourier(courierBodyWithout("login"))
                .then()
                .statusCode(400)
                .body("message", notNullValue());
    }

    @Test
    @DisplayName("Для создания курьера нужно передать пароль")
    public void courierCannotBeCreatedWithoutPassword() {
        client.createCourier(courierBodyWithout("password"))
                .then()
                .statusCode(400)
                .body("message", notNullValue());
    }

    @Test
    @DisplayName("Если создать пользователя с существующим логином, возвращается ошибка")
    public void courierWithExistingLoginReturnsError() {
        Courier courierWithSameLogin = new Courier(
                courier.getLogin(),
                TestData.PASSWORD + TestData.uniqueSuffix(),
                TestData.FIRST_NAME
        );

        client.createCourier(courier)
                .then()
                .statusCode(201)
                .body("ok", equalTo(true));

        courierId = client.loginCourier(CourierCredentials.from(courier))
                .then()
                .statusCode(200)
                .extract()
                .path("id");

        client.createCourier(courierWithSameLogin)
                .then()
                .statusCode(409)
                .body("message", notNullValue());
    }

    @Step("Подготовить тело создания курьера без поля: {fieldName}")
    private Map<String, String> courierBodyWithout(String fieldName) {
        Map<String, String> body = new HashMap<>();
        body.put("login", courier.getLogin());
        body.put("password", courier.getPassword());
        body.put("firstName", courier.getFirstName());
        body.remove(fieldName);
        return body;
    }

    @Step("Получить id созданного курьера для удаления")
    private Integer tryLoginAndGetCourierId() {
        Response response = client.loginCourier(CourierCredentials.from(courier));
        if (response.statusCode() == 200) {
            return response.path("id");
        }
        return null;
    }
}
