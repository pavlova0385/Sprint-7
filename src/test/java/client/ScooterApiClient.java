package client;

import io.qameta.allure.Step;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import model.Courier;
import model.CourierCredentials;
import model.Order;

import java.util.Map;

import static io.restassured.RestAssured.given;

public class ScooterApiClient {
    private static final String BASE_URI = "https://qa-scooter.praktikum-services.ru";
    private static final String COURIER_PATH = "/api/v1/courier";
    private static final String LOGIN_PATH = "/api/v1/courier/login";
    private static final String ORDERS_PATH = "/api/v1/orders";

    static {
        RestAssured.baseURI = BASE_URI;
    }

    @Step("Создать курьера")
    public Response createCourier(Courier courier) {
        return given()
                .header("Content-type", "application/json")
                .body(courier)
                .when()
                .post(COURIER_PATH);
    }

    @Step("Создать курьера с телом запроса: {body}")
    public Response createCourier(Map<String, String> body) {
        return given()
                .header("Content-type", "application/json")
                .body(body)
                .when()
                .post(COURIER_PATH);
    }

    @Step("Авторизоваться курьером")
    public Response loginCourier(CourierCredentials credentials) {
        return given()
                .header("Content-type", "application/json")
                .body(credentials)
                .when()
                .post(LOGIN_PATH);
    }

    @Step("Авторизоваться курьером с телом запроса: {body}")
    public Response loginCourier(Map<String, String> body) {
        return given()
                .header("Content-type", "application/json")
                .body(body)
                .when()
                .post(LOGIN_PATH);
    }

    @Step("Удалить курьера с id: {courierId}")
    public Response deleteCourier(int courierId) {
        return given()
                .when()
                .delete(COURIER_PATH + "/" + courierId);
    }

    @Step("Создать заказ")
    public Response createOrder(Order order) {
        return given()
                .header("Content-type", "application/json")
                .body(order)
                .when()
                .post(ORDERS_PATH);
    }

    @Step("Получить список заказов")
    public Response getOrders() {
        return given()
                .when()
                .get(ORDERS_PATH);
    }
}