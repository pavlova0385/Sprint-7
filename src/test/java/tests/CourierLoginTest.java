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
import static org.apache.http.HttpStatus.SC_CREATED;
import static org.apache.http.HttpStatus.SC_NOT_FOUND;
import static org.apache.http.HttpStatus.SC_OK;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;

public class CourierLoginTest {
    private static final String NOT_ENOUGH_DATA_MESSAGE = "Недостаточно данных для входа";
    private static final String ACCOUNT_NOT_FOUND_MESSAGE = "Учетная запись не найдена";

    private ScooterApiClient client;
    private Courier courier;
    private Integer courierId;

    @Before
    public void setUp() {
        client = new ScooterApiClient();
        courier = TestData.randomCourier();
        createCourier();
    }

    @After
    public void tearDown() {
        loginCreatedCourier();

        if (courierId != null) {
            client.deleteCourier(courierId);
        }
    }

    @Test
    @DisplayName("Курьер может авторизоваться")
    @Description("Проверяем, что созданный курьер может авторизоваться, запрос возвращает 200 и id")
    public void courierCanLogin() {
        courierId = client.loginCourier(CourierCredentials.from(courier))
                .then()
                .statusCode(SC_OK)
                .body("id", notNullValue())
                .extract()
                .path("id");
    }

    @Test
    @DisplayName("Для авторизации нужно передать логин")
    @Description("Проверяем, что авторизация без login возвращает 400 и корректный текст ошибки")
    public void courierCannotLoginWithoutLogin() {
        client.loginCourier(loginBodyWithout("login"))
                .then()
                .statusCode(SC_BAD_REQUEST)
                .body("message", equalTo(NOT_ENOUGH_DATA_MESSAGE));
    }

    @Test
    @DisplayName("Система вернет ошибку при неверном логине")
    @Description("Проверяем, что авторизация с неверным login возвращает 404 и корректный текст ошибки")
    public void courierCannotLoginWithWrongLogin() {
        client.loginCourier(new CourierCredentials(courier.getLogin() + TestData.WRONG_VALUE, courier.getPassword()))
                .then()
                .statusCode(SC_NOT_FOUND)
                .body("message", equalTo(ACCOUNT_NOT_FOUND_MESSAGE));
    }

    @Test
    @DisplayName("Система вернет ошибку при неверном пароле")
    @Description("Проверяем, что авторизация с неверным password возвращает 404 и корректный текст ошибки")
    public void courierCannotLoginWithWrongPassword() {
        client.loginCourier(new CourierCredentials(courier.getLogin(), courier.getPassword() + TestData.WRONG_VALUE))
                .then()
                .statusCode(SC_NOT_FOUND)
                .body("message", equalTo(ACCOUNT_NOT_FOUND_MESSAGE));
    }

    @Test
    @DisplayName("Если авторизоваться под несуществующим пользователем, возвращается ошибка")
    @Description("Проверяем, что авторизация несуществующего курьера возвращает 404 и корректный текст ошибки")
    public void nonExistentCourierCannotLogin() {
        CourierCredentials credentials = new CourierCredentials(
                "not_existing_" + TestData.uniqueSuffix(),
                TestData.PASSWORD
        );

        client.loginCourier(credentials)
                .then()
                .statusCode(SC_NOT_FOUND)
                .body("message", equalTo(ACCOUNT_NOT_FOUND_MESSAGE));
    }

    @Step("Создать тестового курьера")
    private void createCourier() {
        client.createCourier(courier)
                .then()
                .statusCode(SC_CREATED);
    }

    @Step("Подготовить JSON тела авторизации без поля: {fieldName}")
    private String loginBodyWithout(String fieldName) {
        Map<String, String> body = new HashMap<>();
        body.put("login", courier.getLogin());
        body.put("password", courier.getPassword());
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