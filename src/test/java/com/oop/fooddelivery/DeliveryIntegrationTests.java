package com.oop.fooddelivery;

import com.oop.fooddelivery.model.Delivery;
import com.oop.fooddelivery.model.Order;
import com.oop.fooddelivery.model.OrderStatus;
import com.oop.fooddelivery.repository.DeliveryRepository;
import com.oop.fooddelivery.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@AutoConfigureMockMvc
class DeliveryIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DeliveryRepository deliveryRepository;

    @Autowired
    private OrderRepository orderRepository;

    private Order testOrder;

    @BeforeEach
    void prepareTestData() {
        // Delivery requires a REAL Order, so seed one for each test.
        deliveryRepository.deleteAll();
        testOrder = createReadyOrder();
    }

    private Order createReadyOrder() {
        Order order = new Order();
        order.setRestaurantName("Test Restaurant");
        order.setOrderDate(LocalDate.of(2026, 10, 9));
        order.setStatus(OrderStatus.READY);
        order.setDeliveryAddress("Colombo");
        order.calculateTotalAmount();
        return orderRepository.save(order);
    }

    // MockMvc exercises the actual MVC and Spring Security filter chains,
    // without requiring a real network connection or a test admin in MySQL.
    private record TestResponse(int statusCode, String body) { }

    private TestResponse request(String method, String path, String body)
            throws Exception {
        String url = "/foodflow" + path;
        MockHttpServletRequestBuilder builder;

        if ("POST".equals(method)) {
            builder = post(url)
                    .contextPath("/foodflow")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .with(csrf());

            // Convert URL-encoded form fields into request parameters.
            // URLDecoder also turns '+' into spaces for delivery statuses.
            if (body != null && !body.isBlank()) {
                for (String field : body.split("&")) {
                    String[] pair = field.split("=", 2);
                    String key = URLDecoder.decode(pair[0], StandardCharsets.UTF_8);
                    String value = pair.length > 1
                            ? URLDecoder.decode(pair[1], StandardCharsets.UTF_8)
                            : "";
                    builder.param(key, value);
                }
            }
        } else {
            builder = get(url).contextPath("/foodflow");
        }

        // These are ADMIN management tests; authenticate each request.
        builder.with(user("admin@foodflow.test").roles("ADMIN"));

        MvcResult result = mockMvc.perform(builder).andReturn();
        return new TestResponse(
                result.getResponse().getStatus(),
                result.getResponse().getContentAsString()
        );
    }

    private String validForm(Long orderId, String person) {
        return "orderId=" + orderId
                + "&deliveryPersonName=" + person
                + "&phoneNumber=0771234567"
                + "&deliveryAddress=Colombo"
                + "&deliveryDate=2026-10-09"
                + "&deliveryStatus=Pending";
    }

    private String validForm(String person) {
        return validForm(testOrder.getOrderId(), person);
    }

    @Test
    void pagesAndAssetsRenderUnderContextPath() throws Exception {
        String[] paths = {
                "/", "/dashboard", "/customer", "/customer/add",
                "/restaurant", "/restaurant/add", "/food", "/food/add",
                "/delivery", "/delivery/add", "/css/dashboard.css",
                "/css/style.css"
        };

        for (String path : paths) {
            TestResponse response = request("GET", path, "");
            assertEquals(200, response.statusCode(), "Failed to load: " + path);
        }

        assertTrue(request("GET", "/dashboard", "")
                .body().contains("href=\"/foodflow/delivery\""));

        assertTrue(request("GET", "/delivery/add", "")
                .body().contains("action=\"/foodflow/delivery/add\""));

        assertTrue(request("GET", "/food", "")
                .body().contains("Food Item List"));
    }

    @Test
    void createEditAndDeleteDelivery() throws Exception {
        assertEquals(302,
                request("POST", "/delivery/add", validForm("Kasun")).statusCode());

        Delivery saved = deliveryRepository.findAll().getFirst();
        assertEquals(testOrder.getOrderId(), saved.getOrderId());
        assertEquals(LocalDate.of(2026, 10, 9), saved.getDeliveryDate());

        Long id = saved.getDeliveryId();
        assertTrue(request("GET", "/delivery", "").body().contains("Kasun"));
        assertEquals(200,
                request("GET", "/delivery/edit/" + id, "").statusCode());

        assertEquals(302,
                request("POST", "/delivery/edit/" + id,
                        validForm("Amaya")).statusCode());

        assertEquals("Amaya", deliveryRepository.findById(id)
                .orElseThrow().getDeliveryPersonName());

        assertEquals(405,
                request("GET", "/delivery/delete/" + id, "").statusCode());
        assertTrue(deliveryRepository.existsById(id));

        assertEquals(302,
                request("POST", "/delivery/delete/" + id, "").statusCode());
        assertEquals(0, deliveryRepository.count());
    }

    @Test
    void invalidFormsRenderErrorsWithoutChangingDatabase() throws Exception {
        TestResponse response = request(
                "POST", "/delivery/add", "orderId=0&deliveryStatus=Unknown");

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("Order ID must be greater than zero."));
        assertEquals(0, deliveryRepository.count());

        assertEquals(200,
                request("POST", "/delivery/add",
                        validForm("Kasun").replace("2026-10-09", "bad-date"))
                        .statusCode());
        assertEquals(0, deliveryRepository.count());

        Delivery saved = deliveryRepository.save(
                new Delivery(testOrder.getOrderId(), "Kasun", "0771234567",
                        "Colombo", LocalDate.of(2026, 10, 9), "Pending"));

        assertEquals(200,
                request("POST", "/delivery/edit/" + saved.getDeliveryId(),
                        validForm("Amaya")
                                .replace("orderId=" + testOrder.getOrderId(), "orderId=0"))
                        .statusCode());

        assertEquals("Kasun", deliveryRepository.findById(saved.getDeliveryId())
                .orElseThrow().getDeliveryPersonName());
    }

    @Test
    void missingIdsAndSubmittedIdsCannotCreateOrOverwriteUnexpectedRecords()
            throws Exception {
        assertEquals(302,
                request("GET", "/delivery/edit/999999", "").statusCode());
        assertEquals(302,
                request("POST", "/delivery/edit/999999",
                        validForm("Kasun")).statusCode());
        assertEquals(0, deliveryRepository.count());

        Delivery original = deliveryRepository.save(
                new Delivery(testOrder.getOrderId(), "Original", "0771234567",
                        "Colombo", LocalDate.of(2026, 10, 9), "Pending"));

        // Use a DIFFERENT real Order for the second Delivery.
        // The browser-supplied deliveryId must not overwrite the first record.
        Order secondOrder = createReadyOrder();
        assertEquals(302,
                request("POST", "/delivery/add",
                        validForm(secondOrder.getOrderId(), "New")
                                + "&deliveryId=" + original.getDeliveryId())
                        .statusCode());

        assertEquals(2, deliveryRepository.count());
        assertEquals("Original", deliveryRepository.findById(original.getDeliveryId())
                .orElseThrow().getDeliveryPersonName());

        // Assigning a third Delivery to the first Order must be REJECTED.
        TestResponse duplicate = request(
                "POST", "/delivery/add", validForm("Duplicate"));
        assertEquals(200, duplicate.statusCode());
        assertEquals(2, deliveryRepository.count());
    }

    @Test
    void deliveryStatusUpdatesItsOrder() throws Exception {
        Delivery delivery = deliveryRepository.save(
                new Delivery(testOrder.getOrderId(), "Kasun", "0771234567",
                        "Colombo", LocalDate.of(2026, 10, 9), "Assigned"));
        Long deliveryId = delivery.getDeliveryId();

        String outForDelivery = validForm("Kasun")
                .replace("deliveryStatus=Pending", "deliveryStatus=Out+for+Delivery");
        assertEquals(302,
                request("POST", "/delivery/edit/" + deliveryId,
                        outForDelivery).statusCode());
        assertEquals(OrderStatus.OUT_FOR_DELIVERY,
                orderRepository.findById(testOrder.getOrderId())
                        .orElseThrow().getStatus());

        String delivered = validForm("Kasun")
                .replace("deliveryStatus=Pending", "deliveryStatus=Delivered");
        assertEquals(302,
                request("POST", "/delivery/edit/" + deliveryId,
                        delivered).statusCode());
        assertEquals(OrderStatus.DELIVERED,
                orderRepository.findById(testOrder.getOrderId())
                        .orElseThrow().getStatus());

        // Final state: going backwards is invalid.
        assertEquals(200,
                request("POST", "/delivery/edit/" + deliveryId,
                        outForDelivery).statusCode());
        assertEquals(OrderStatus.DELIVERED,
                orderRepository.findById(testOrder.getOrderId())
                        .orElseThrow().getStatus());
    }
}
