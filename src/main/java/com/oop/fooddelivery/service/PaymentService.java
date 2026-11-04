package com.oop.fooddelivery.service;

import com.oop.fooddelivery.model.Order;
import com.oop.fooddelivery.model.Payment;
import com.oop.fooddelivery.model.PaymentMethod;
import com.oop.fooddelivery.model.PaymentStatus;
import com.oop.fooddelivery.payment.PaymentProcessor;
import com.oop.fooddelivery.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;

@Service
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final OrderService orderService;
    private final List<PaymentProcessor> processors;

    public PaymentService(PaymentRepository paymentRepository,
                          OrderService orderService,
                          List<PaymentProcessor> processors) {
        this.paymentRepository = paymentRepository;
        this.orderService = orderService;
        this.processors = processors;
    }

    @Transactional
    public Payment createPayment(Long orderId, PaymentMethod method) {
        if (orderId == null) throw new IllegalArgumentException("Choose an order.");
        if (method == null) throw new IllegalArgumentException("Choose a payment method.");
        Order order = orderService.getOrderById(orderId);
        if (order == null) throw new IllegalArgumentException("Order not found.");
        if (paymentRepository.existsByOrder_OrderIdAndPaymentStatus(orderId, PaymentStatus.PAID)) {
            throw new IllegalArgumentException("This order already has a paid payment.");
        }

        PaymentProcessor processor = processors.stream()
                .filter(p -> p.supports() == method)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported payment method."));

        Payment payment = new Payment(order, method, LocalDate.now());
        PaymentStatus initial = processor.initialStatus();
        if (initial != PaymentStatus.PENDING && !payment.changeStatus(initial)) {
            throw new IllegalStateException("Invalid initial payment status.");
        }
        return paymentRepository.save(payment);
    }

    public List<Order> getOrders() {
        return orderService.getAllOrders();
    }

    @Transactional(readOnly = true)
    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Payment getPaymentById(Long id) {
        return paymentRepository.findById(id).orElse(null);
    }

    @Transactional
    public void changePaymentStatus(Long id, PaymentStatus requestedStatus) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found."));
        if (requestedStatus == PaymentStatus.PAID &&
                paymentRepository.existsByOrder_OrderIdAndPaymentStatus(
                        payment.getOrder().getOrderId(), PaymentStatus.PAID)
                && payment.getPaymentStatus() != PaymentStatus.PAID) {
            throw new IllegalArgumentException("This order already has a paid payment.");
        }
        if (!payment.changeStatus(requestedStatus)) {
            throw new IllegalArgumentException("Invalid payment status transition.");
        }
        paymentRepository.save(payment);
    }

    @Transactional
    public void deletePayment(Long id) {
        if (paymentRepository.existsById(id)) {
            paymentRepository.deleteById(id);
        }
    }
}
