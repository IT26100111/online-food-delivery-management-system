package com.oop.fooddelivery;

import com.oop.fooddelivery.model.Delivery;
import com.oop.fooddelivery.repository.DeliveryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DeliveryIntegrationTests {
    @LocalServerPort
    private int port;
    @Autowired
    private DeliveryRepository repository;
    private final HttpClient client = HttpClient.newHttpClient();

    @BeforeEach
    void clearDeliveries() {
        repository.deleteAll();
    }

    private HttpResponse<String> request(String method, String path, String body) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/foodflow" + path));
        if (method.equals("POST")) {
            builder.header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(body));
        } else {
            builder.GET();
        }
        return client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }

    private String validForm(String person) {
        return "orderId=101&deliveryPersonName=" + person + "&phoneNumber=0771234567"
                + "&deliveryAddress=Colombo&deliveryDate=2026-10-09&deliveryStatus=Pending";
    }

    @Test
    void pagesAndAssetsRenderUnderContextPath() throws Exception {
        for (String path : new String[]{"/", "/dashboard", "/customer", "/customer/add", "/customer/edit",
                "/restaurant", "/restaurant/add", "/restaurant/edit", "/food", "/food/add", "/food/edit",
                "/delivery", "/delivery/add", "/css/dashboard.css", "/css/style.css"}) {
            assertEquals(200, request("GET", path, "").statusCode(), path);
        }
        assertTrue(request("GET", "/dashboard", "").body().contains("href=\"/foodflow/delivery\""));
        assertTrue(request("GET", "/delivery/add", "").body().contains("action=\"/foodflow/delivery/add\""));
        assertTrue(request("GET", "/food", "").body().contains("Food Item List"));
    }

    @Test
    void createEditAndDeleteDelivery() throws Exception {
        assertEquals(302, request("POST", "/delivery/add", validForm("Kasun")).statusCode());
        Delivery saved = repository.findAll().getFirst();
        assertEquals(LocalDate.of(2026, 10, 9), saved.getDeliveryDate());
        Long id = saved.getDeliveryId();
        assertTrue(request("GET", "/delivery", "").body().contains("Kasun"));
        assertEquals(200, request("GET", "/delivery/edit/" + id, "").statusCode());
        assertEquals(302, request("POST", "/delivery/edit/" + id, validForm("Amaya")).statusCode());
        assertEquals("Amaya", repository.findById(id).orElseThrow().getDeliveryPersonName());
        assertEquals(405, request("GET", "/delivery/delete/" + id, "").statusCode());
        assertTrue(repository.existsById(id));
        assertEquals(302, request("POST", "/delivery/delete/" + id, "").statusCode());
        assertEquals(0, repository.count());
    }

    @Test
    void invalidFormsRenderErrorsWithoutChangingDatabase() throws Exception {
        HttpResponse<String> response = request("POST", "/delivery/add", "orderId=0&deliveryStatus=Unknown");
        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("Order ID must be greater than zero."));
        assertEquals(0, repository.count());
        assertEquals(200, request("POST", "/delivery/add", validForm("Kasun").replace("2026-10-09", "bad-date")).statusCode());
        assertEquals(0, repository.count());
        Delivery saved = repository.save(new Delivery(101L, "Kasun", "0771234567", "Colombo",
                LocalDate.of(2026, 10, 9), "Pending"));
        assertEquals(200, request("POST", "/delivery/edit/" + saved.getDeliveryId(),
                validForm("Amaya").replace("orderId=101", "orderId=0")).statusCode());
        assertEquals("Kasun", repository.findById(saved.getDeliveryId()).orElseThrow().getDeliveryPersonName());
    }

    @Test
    void missingIdsAndSubmittedIdsCannotCreateOrOverwriteUnexpectedRecords() throws Exception {
        assertEquals(302, request("GET", "/delivery/edit/999999", "").statusCode());
        assertEquals(302, request("POST", "/delivery/edit/999999", validForm("Kasun")).statusCode());
        assertEquals(0, repository.count());
        Delivery original = repository.save(new Delivery(101L, "Original", "0771234567", "Colombo",
                LocalDate.of(2026, 10, 9), "Pending"));
        assertEquals(302, request("POST", "/delivery/add", validForm("New")
                + "&deliveryId=" + original.getDeliveryId()).statusCode());
        assertEquals(2, repository.count());
        assertEquals("Original", repository.findById(original.getDeliveryId()).orElseThrow().getDeliveryPersonName());
    }
}
