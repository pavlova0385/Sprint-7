package tests;

import client.ScooterApiClient;
import com.google.gson.Gson;
import io.qameta.allure.Description;
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

import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_CONFLICT;
import static org.apache.http.HttpStatus.SC_CREATED;
import static org.apache.http.HttpStatus.SC_OK;
import static org.hamcrest.CoreMatchers.equalTo;

public class CourierCreateTest {
    private static final String NOT_ENOUGH_DATA_MESSAGE = "Недостаточно данных для создания учетной записи";
    private static final String LOGIN_ALREADY_EXISTS_MESSAGE = "Этот логин уже используется. Попробуйте другой.";

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
        loginCreatedCourier();

        if (courierId != null) {
            client.deleteCourier(courierId);
        }
    }

    @Test
    @DisplayName("Курьера можно создать")
    @Description("Проверяем, что курьера можно создать с валидными login, password и firstName")
    public void courierCanBeCreated() {
        client.createCourier(courier)
                .then()
                .statusCode(SC_CREATED)
                .body("ok", equalTo(true));
    }

    @Test
    @DisplayName("Нельзя создать двух одинаковых курьеров")
    @Description("Проверяем, что повторное создание курьера с теми же данными возвращает ошибку")
    public void duplicateCourierCannotBeCreated() {
        client.createCourier(courier)
                .then()
                .statusCode(SC_CREATED)
                .body("ok", equalTo(true));

        client.createCourier(courier)
                .then()
                .statusCode(SC_CONFLICT)
                .body("message", equalTo(LOGIN_ALREADY_EXISTS_MESSAGE));
    }

    @Test
    @DisplayName("Для создания курьера нужно передать логин")
    @Description("Проверяем, что запрос без login возвращает ошибку с корректным текстом")
    public void courierCannotBeCreatedWithoutLogin() {
        client.createCourier(courierBodyWithout("login"))
                .then()
                .statusCode(SC_BAD_REQUEST)
                .body("message", equalTo(NOT_ENOUGH_DATA_MESSAGE));
    }

    @Test
    @DisplayName("Для создания курьера нужно передать пароль")
    @Description("Проверяем, что запрос без password возвращает ошибку с корректным текстом")
    public void courierCannotBeCreatedWithoutPassword() {
        client.createCourier(courierBodyWithout("password"))
                .then()
                .statusCode(SC_BAD_REQUEST)
                .body("message", equalTo(NOT_ENOUGH_DATA_MESSAGE));
    }

    @Test
    @DisplayName("Если создать пользователя с существующим логином, возвращается ошибка")
    @Description("Проверяем, что создание курьера с уже существующим login возвращает ошибку")
    public void courierWithExistingLoginReturnsError() {
        Courier courierWithSameLogin = new Courier(
                courier.getLogin(),
                TestData.PASSWORD + TestData.uniqueSuffix(),
                TestData.FIRST_NAME
        );

        client.createCourier(courier)
                .then()
                .statusCode(SC_CREATED)
                .body("ok", equalTo(true));

        client.createCourier(courierWithSameLogin)
                .then()
                .statusCode(SC_CONFLICT)
                .body("message", equalTo(LOGIN_ALREADY_EXISTS_MESSAGE));
    }

    @Step("Подготовить JSON тела создания курьера без поля: {fieldName}")
    private String courierBodyWithout(String fieldName) {
        Map<String, String> body = new HashMap<>();
        body.put("login", courier.getLogin());
        body.put("password", courier.getPassword());
        body.put("firstName", courier.getFirstName());
        body.remove(fieldName);
        return new Gson().toJson(body);
    }

    @Step("Авторизоваться созданным курьером для получения id")
    private void loginCreatedCourier() {
        if (courierId != null) {
            return;
        }

        Response response = client.loginCourier(CourierCredentials.from(courier));
        if (response.statusCode() == SC_OK) {
            courierId = response.path("id");
        }
    }
}