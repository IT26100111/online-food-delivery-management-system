package com.oop.fooddelivery.service;

import com.oop.fooddelivery.model.Customer;
import com.oop.fooddelivery.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository repository;

    private CustomerService service;

    @BeforeEach
    void setUp() {
        service = new CustomerService(repository);
    }

    @Test
    void shouldSaveCustomerAndTrimValues() {
        Customer customer = new Customer(
                "  Amal Perera  ", "  amal@example.com ",
                " 0771234567 ", "  12 Main Street  ", " Active "
        );
        when(repository.save(customer)).thenReturn(customer);

        Customer saved = service.saveCustomer(customer);

        assertSame(customer, saved);
        assertEquals("Amal Perera", saved.getFullName());
        assertEquals("amal@example.com", saved.getEmail());
        assertEquals("0771234567", saved.getPhoneNumber());
        verify(repository).save(customer);
    }

    @Test
    void shouldReturnAllCustomersForBlankSearch() {
        Customer customer = new Customer();
        when(repository.findAll()).thenReturn(List.of(customer));

        assertEquals(List.of(customer), service.searchCustomers("  "));
        verify(repository).findAll();
    }

    @Test
    void shouldSearchByNameOrEmail() {
        Customer customer = new Customer();
        when(repository.findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                "amal", "amal"
        )).thenReturn(List.of(customer));

        assertEquals(List.of(customer), service.searchCustomers(" amal "));
        verify(repository).findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                "amal", "amal"
        );
    }

    @Test
    void shouldUpdateStoredEntityNotFormObject() {
        Customer stored = new Customer("Old Name", "old@example.com", "0771234567",
                "10 Old Road", "Active");
        Customer form = new Customer("New Name", "new@example.com", "0712345678",
                "20 New Road", "Inactive");
        when(repository.findById(8L)).thenReturn(Optional.of(stored));
        when(repository.save(stored)).thenReturn(stored);

        Customer result = service.updateCustomer(8L, form);

        assertSame(stored, result);
        assertNotSame(form, result);
        assertEquals("New Name", stored.getFullName());
        assertEquals("Inactive", stored.getStatus());
        verify(repository).save(stored);
        verify(repository, never()).save(form);
    }

    @Test
    void shouldRejectDeletingCustomerWithOrders() {
        when(repository.existsById(12L)).thenReturn(true);
        when(repository.countOrdersForCustomer(12L)).thenReturn(2L);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> service.deleteCustomer(12L));
        assertTrue(ex.getMessage().contains("existing orders"));
        verify(repository, never()).deleteById(12L);
    }

    @Test
    void shouldDeleteCustomerWithoutOrders() {
        when(repository.existsById(15L)).thenReturn(true);
        when(repository.countOrdersForCustomer(15L)).thenReturn(0L);

        service.deleteCustomer(15L);

        verify(repository).deleteById(15L);
    }

    @Test
    void shouldRejectDeletingUnknownCustomer() {
        when(repository.existsById(99L)).thenReturn(false);

        assertThrows(IllegalArgumentException.class,
                () -> service.deleteCustomer(99L));
        verify(repository, never()).deleteById(99L);
    }
}
