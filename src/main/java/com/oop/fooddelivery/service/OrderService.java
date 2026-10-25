package com.oop.fooddelivery.service;

import com.oop.fooddelivery.model.Order;
import com.oop.fooddelivery.model.OrderItem;
import com.oop.fooddelivery.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderService {

    //constructor dependency injection
    private final OrderRepository orderRepository;


    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }


    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }


    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElse(null);
    }


    public Order saveOrder(Order order) {
        return orderRepository.save(order);
    }


    public void deleteOrder(Long id) {
        orderRepository.deleteById(id);
    }

    public void removeOrderItem(Long orderId, Long itemId){
        Order order = getOrderById(orderId);

        if(order == null){
            return;
        }

        OrderItem itemToRemove = null;

        for(OrderItem item: order.getOrderItems()){
            if(item.getOrderItemId().equals(itemId)){
                itemToRemove = item;
                break;
            }
        }
        if(itemToRemove != null){
            order.removeOrderItem(itemToRemove);
            orderRepository.save(order);
        }
    }
}