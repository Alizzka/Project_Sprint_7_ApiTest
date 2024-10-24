// С методом для завершения заказа по id
package CreateOrderTest;
import io.qameta.allure.Step;
import io.qameta.allure.internal.shadowed.jackson.databind.ObjectMapper;
import io.qameta.allure.internal.shadowed.jackson.databind.SerializationFeature;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import java.util.List;
import java.util.Map;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

public class OrderClient {
    private static final String CREATE_ORDERS = "/api/v1/orders";
    private static final String CANCEL_ORDER = "/api/v1/orders/finish";
    private static final String GET_ORDER_BY_TRACK = "/api/v1/orders/track";

    // Метод для создания заказа
    @Step("Creating an order")
    public static Response createNewOrder(OrderCreate orderCreate) {
        Response response = given()
                .spec(Specifications.requestSpec())
                .header("Content-type", "application/json")
                .body(orderCreate)
                .post(CREATE_ORDERS);
        // Проверка кода ответа
        if (response.getStatusCode() != 201) {
            throw new RuntimeException("Ошибка при создании заказа. Код ответа: " + response.getStatusCode() + ", Тело ответа: " + response.asString());
        }
        // Проверка тела ответа на наличие поля track
        JsonPath jsonPath = new JsonPath(response.asString());
        Integer trackNumber = jsonPath.get("track");
        if (trackNumber == null) {
            throw new RuntimeException("Ошибка при создании заказа. Поле 'track' отсутствует в ответе. Тело ответа: " + response.asString());
        }
        // Вывод информации о создании заказа
        System.out.println("Заказ создан. Код ответа: " + response.getStatusCode() + ", Тело ответа: " + response.asString() + ", номер заказа: " + trackNumber);
        return response;
    }

    // Метод для сравнивнения ожидаемого кода ответа с фактическим, проверяет наличие трек-номера заказа
    @Step("Comparing the expected response code with the actual one")
    public static void comparingSuccessfulOrderSet(Response response, int responseCode) {
        response.then().assertThat().body("track", not(0)).and().statusCode(responseCode);
    }

    // Метод для получения Id заказа по трек-номеру заказа
    @Step("Get order ID by track")
    public static String getOrderId(Response response) {
        // Получение трек-номера заказа
        String trackNumber = response.then().extract().body().asString();
        JsonPath jsonPath = new JsonPath(trackNumber);
        // Запрос на получение ID заказа по трек-номеру
        Response trackResponse = given()
                .spec(Specifications.requestSpec())
                .header("Content-type", "application/json")
                .get(GET_ORDER_BY_TRACK + "?track=" + jsonPath.getString("track"));
        // Проверка успешного получения ID заказа
        if (trackResponse.getStatusCode() != 200) {
            throw new RuntimeException("Ошибка при получении ID заказа. Код ответа: " + trackResponse.getStatusCode() + ", Тело ответа: " + trackResponse.asString());
        }
        // Извлечение ID заказа из ответа
        JsonPath orderJson = new JsonPath(trackResponse.asString());
        String orderId = orderJson.getString("id");
        if (orderId == null) {
            throw new RuntimeException("Ошибка: ID заказа отсутствует в ответе. Тело ответа: " + trackResponse.asString());
        }
        return orderId;
    }

    // Метод для завершения заказа по Id
    @Step("Closing an order by ID")
    public static Response deleteOrder(String id) {
        Response response = given()
                .spec(Specifications.requestSpec())
                .header("Content-type", "application/json")
                .put(CANCEL_ORDER + "?id=" + id);
        // Проверка кода ответа
        if (response.getStatusCode() != 200) {
            throw new RuntimeException("Ошибка при удалении заказа. Код ответа: " + response.getStatusCode() + ", Тело ответа: " + response.asString());
        }
        // Вывод информации об успешном удалении
        System.out.println("Заказ удалён. Код ответа: " + response.getStatusCode() + ", Тело ответа: " + response.asString());
        return response;
    }

    // Метод для проверки успешности завершения заказа, сравнивая ожидаемого и фактического кода ответа и содержимого тела ответа.
    @Step("Comparison of expected order closure response code with actual one")
    public static void comparingSuccessfulOrderCancel(Response response, int expectedResponseCode) {
        if (response.getStatusCode() != expectedResponseCode) {
            throw new AssertionError("Ожидаемый код ответа: " + expectedResponseCode + ", Фактический код ответа: " + response.getStatusCode());
        }
        response.then().assertThat().body("ok", equalTo(true)).and().statusCode(expectedResponseCode);
    }

    // Метод для получения списка заказов и проверки статуса ответа
    @Step("Get Orders List")
    public static Response getAllOrders() {
        Response response = given()
                .spec(Specifications.requestSpec())
                .header("Content-type", "application/json")
                .get(CREATE_ORDERS);
        if (response.getStatusCode() != 200) {
            throw new RuntimeException("Ошибка при получении списка заказов. Код ответа: " + response.getStatusCode() + ", Тело ответа: " + response.asString());
        }
        JsonPath jsonPath = new JsonPath(response.asString());
        List<Map<String, Object>> orders = jsonPath.getList("orders");
        if (orders == null || orders.isEmpty()) {
            throw new RuntimeException("Ошибка: список заказов пуст.");
        }
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
            String ordersJson = objectMapper.writeValueAsString(orders);
            System.out.println("Список заказов получен. Код ответа: " + response.getStatusCode() + ", Тело ответа: " + response.asString());
            System.out.println(ordersJson);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при преобразовании списка заказов в JSON: " + e.getMessage());
        }
        return response;
    }
}

// С методом для завершения заказа по трек-номеру заказа
/*package CreateOrderTest;
import io.qameta.allure.Step;
import io.qameta.allure.internal.shadowed.jackson.databind.ObjectMapper;
import io.qameta.allure.internal.shadowed.jackson.databind.SerializationFeature;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import java.util.List;
import java.util.Map;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

public class OrderClient {
    private static final String CREATE_ORDERS = "/api/v1/orders";
    private static final String CANCEL_ORDER = "/api/v1/orders/finish";

    // Метод для создания заказа
    @Step("Creating an order")
    public static Response createNewOrder(OrderCreate orderCreate) {
        Response response = given()
                .spec(Specifications.requestSpec())
                .header("Content-type", "application/json")
                .body(orderCreate)
                .post(CREATE_ORDERS);
        // Проверка кода ответа
        if (response.getStatusCode() != 201) {
            throw new RuntimeException("Ошибка при создании заказа. Код ответа: " + response.getStatusCode() + ", Тело ответа: " + response.asString());
        }
        // Проверка тела ответа на наличие поля track
        JsonPath jsonPath = new JsonPath(response.asString());
        Integer trackNumber = jsonPath.get("track");
        if (trackNumber == null) {
            throw new RuntimeException("Ошибка при создании заказа. Поле 'track' отсутствует в ответе. Тело ответа: " + response.asString());
        }
        // Вывод информации о создании заказа
        System.out.println("Заказ создан. Код ответа: " + response.getStatusCode() + ", Тело ответа: " + response.asString() + ", номер заказа: " + trackNumber);
        return response;
    }

    // Метод для получения трек-номера заказа
    @Step("Get order thrack")
    public static String getOrderTrack(Response response){
        String trackNumber = response.then().extract().body().asString();
        JsonPath jsonPath = new JsonPath(trackNumber);
        return jsonPath.getString("track");
    }

    // Метод для сравнения ожидаемого кода ответа с фактическим, проверяет наличие трек-номера заказа
    @Step("Comparing the expected response code with the actual one")
    public static void comparingSuccessfulOrderSet(Response response, int responseCode){
        response.then().assertThat().body("track", not(0)).and().statusCode(responseCode);
    }

    // Метод для завершения заказа по трек-номеру заказа
    @Step("Closing an order by tracking number")
    public static Response deleteOrder(String track) {
        Response response = given()
                .spec(Specifications.requestSpec())
                .header("Content-type", "application/json")
                .put(CANCEL_ORDER + "?track=" + track);
        // Проверка кода ответа
        if (response.getStatusCode() != 200) {
            throw new RuntimeException("Ошибка при удалении заказа. Код ответа: " + response.getStatusCode() + ", Тело ответа: " + response.asString());
        }
        // Вывод информации об успешном удалении
        System.out.println("Заказ удалён. Код ответа: " + response.getStatusCode() + ", Тело ответа: " + response.asString());
        return response;
    }

    // Метод для проверки успешности завершения заказа, сравнивая ожидаемого и фактического кода ответа и содержимого тела ответа.
    @Step("Comparison of expected order closure response code with actual one")
    public static void comparingSuccessfulOrderCancel(Response response, int expectedResponseCode) {
        // Проверяем, что код ответа соответствует ожидаемому
        if (response.getStatusCode() != expectedResponseCode) {
            throw new AssertionError("Ожидаемый код ответа: " + expectedResponseCode + ", Фактический код ответа: " + response.getStatusCode());
        }
        response.then().assertThat().body("ok", equalTo(true)).and().statusCode(expectedResponseCode);
    }

    // Метод для получения списка заказов и проверки статуса ответа
    @Step("Get Orders List")
    public static Response getAllOrders() {
        Response response = given()
                .spec(Specifications.requestSpec())
                .header("Content-type", "application/json")
                .get(CREATE_ORDERS);
        // Проверка кода ответа
        if (response.getStatusCode() != 200) {
            throw new RuntimeException("Ошибка при получении списка заказов. Код ответа: " + response.getStatusCode() + ", Тело ответа: " + response.asString());
        }
        // Проверка наличия списка заказов
        JsonPath jsonPath = new JsonPath(response.asString());
        List<Map<String, Object>> orders = jsonPath.getList("orders"); // Получаем список объектов заказов
        if (orders == null || orders.isEmpty()) {
            throw new RuntimeException("Ошибка: список заказов пуст.");
        }
        // Форматируем список заказов в JSON с переносами строк
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.enable(SerializationFeature.INDENT_OUTPUT); // Включаем форматирование с переносами строк
            String ordersJson = objectMapper.writeValueAsString(orders);

            // Вывод информации о полученных заказах в формате JSON
            System.out.println("Список заказов получен. Код ответа: " + response.getStatusCode() + ", Тело ответа: " + response.asString());
            System.out.println("Список заказов в формате JSON: \n" + ordersJson); // Добавляем перенос строки перед выводом
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при форматировании списка заказов в JSON: " + e.getMessage());
        }
        return response;
    }
}*/




