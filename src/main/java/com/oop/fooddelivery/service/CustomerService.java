package com.oop.fooddelivery.service;

import com.oop.fooddelivery.model.Customer;
import com.oop.fooddelivery.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    // Constructor injection: Spring supplies the repository implementation.
    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    // Also used by guest checkout; keep the existing method signature.
    @Transactional
    public Customer saveCustomer(Customer customer) {
        if (customer == null) {
            throw new IllegalArgumentException("Customer details are required.");
        }
        normalizeCustomerDetails(customer);
        return customerRepository.save(customer);
    }

    @Transactional(readOnly = true)
    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Customer> searchCustomers(String query) {
        if (query == null || query.isBlank()) {
            return customerRepository.findAll();
        }
        String term = query.trim();
        return customerRepository
                .findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                        term, term
                );
    }

    @Transactional(readOnly = true)
    public Customer getCustomerById(Long id) {
        return id == null ? null : customerRepository.findById(id).orElse(null);
    }

    // Never save the browser's detached form object over an existing row.
    // Load the existing customer and copy only editable properties.
    @Transactional
    public Customer updateCustomer(Long id, Customer formCustomer) {
        if (formCustomer == null) {
            throw new IllegalArgumentException("Customer details are required.");
        }
        Customer existing = customerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found."));

        existing.setFullName(formCustomer.getFullName());
        existing.setEmail(formCustomer.getEmail());
        existing.setPhoneNumber(formCustomer.getPhoneNumber());
        existing.setAddress(formCustomer.getAddress());
        existing.setStatus(formCustomer.getStatus());
        normalizeCustomerDetails(existing);
        return customerRepository.save(existing);
    }

    @Transactional
    public void deleteCustomer(Long id) {
        if (id == null || !customerRepository.existsById(id)) {
            throw new IllegalArgumentException("Customer not found.");
        }
        if (customerRepository.countOrdersForCustomer(id) > 0) {
            throw new IllegalStateException(
                    "Cannot delete this customer because they have existing orders."
            );
        }
        customerRepository.deleteById(id);
    }

    private void normalizeCustomerDetails(Customer customer) {
        if (customer.getFullName() != null) {
            customer.setFullName(customer.getFullName().trim());
        }
        if (customer.getEmail() != null) {
            customer.setEmail(customer.getEmail().trim());
        }
        if (customer.getPhoneNumber() != null) {
            customer.setPhoneNumber(customer.getPhoneNumber().trim());
        }
        if (customer.getAddress() != null) {
            customer.setAddress(customer.getAddress().trim());
        }
        if (customer.getStatus() != null) {
            customer.setStatus(customer.getStatus().trim());
        }
    }
}
