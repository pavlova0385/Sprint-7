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

import static org.hamcrest.CoreMatchers.notNullValue;

public class CourierLoginTest {
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
    @DisplayName("Курьер может авторизоваться")
    public void courierCanLogin() {
        createCourier();

        courierId = client.loginCourier(CourierCredentials.from(courier))
                .then()
                .statusCode(200)
                .body("id", notNullValue())
                .extract()
                .path("id");
    }

    @Test
    @DisplayName("Для авторизации нужно передать логин")
    public void courierCannotLoginWithoutLogin() {
        createCourier();

        client.loginCourier(loginBodyWithout("login"))
                .then()
                .statusCode(400)
                .body("message", notNullValue());

        courierId = loginAndGetCourierId();
    }

    @Test
    @DisplayName("Система вернет ошибку при неверном логине")
    public void courierCannotLoginWithWrongLogin() {
        createCourier();

        client.loginCourier(new CourierCredentials(courier.getLogin() + TestData.WRONG_VALUE, courier.getPassword()))
                .then()
                .statusCode(404)
                .body("message", notNullValue());

        courierId = loginAndGetCourierId();
    }

    @Test
    @DisplayName("Система вернет ошибку при неверном пароле")
    public void courierCannotLoginWithWrongPassword() {
        createCourier();

        client.loginCourier(new CourierCredentials(courier.getLogin(), courier.getPassword() + TestData.WRONG_VALUE))
                .then()
                .statusCode(404)
                .body("message", notNullValue());

        courierId = loginAndGetCourierId();
    }

    @Test
    @DisplayName("Если авторизоваться под несуществующим пользователем, возвращается ошибка")
    public void nonExistentCourierCannotLogin() {
        CourierCredentials credentials = new CourierCredentials(
                "not_existing_" + TestData.uniqueSuffix(),
                TestData.PASSWORD
        );

        client.loginCourier(credentials)
                .then()
                .statusCode(404)
                .body("message", notNullValue());
    }

    @Step("Создать тестового курьера")
    private void createCourier() {
        client.createCourier(courier)
                .then()
                .statusCode(201);
    }

    @Step("Авторизоваться тестовым курьером и получить id")
    private Integer loginAndGetCourierId() {
        return client.loginCourier(CourierCredentials.from(courier))
                .then()
                .statusCode(200)
                .extract()
                .path("id");
    }

    @Step("Подготовить тело авторизации без поля: {fieldName}")
    private Map<String, String> loginBodyWithout(String fieldName) {
        Map<String, String> body = new HashMap<>();
        body.put("login", courier.getLogin());
        body.put("password", courier.getPassword());
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
