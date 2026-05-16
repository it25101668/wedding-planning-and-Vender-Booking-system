package com.wms.service;

import com.wms.entity.Booking;
import com.wms.entity.Customer;
import com.wms.repository.BookingRepository;
import com.wms.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
@org.springframework.transaction.annotation.Transactional
public class ProfileService {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private BookingRepository bookingRepository;

    // =================== CUSTOMER OPERATIONS ===================

    public Optional<Customer> customerLogin(String email, String password) {
        Optional<Customer> customer = customerRepository.findByEmail(email);
        if (customer.isPresent() && customer.get().getPassword().equals(password)) {
            return customer;
        }
        return Optional.empty();
    }

    public Customer addCustomer(Customer customer) {
        if (customerRepository.existsByEmail(customer.getEmail())) {
            throw new RuntimeException("Email already registered: " + customer.getEmail());
        }
        return customerRepository.save(customer);
    }

    public Customer updateCustomerProfile(Long id, Customer updatedCustomer) {
        Customer existing = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found: " + id));
        existing.setFirstName(updatedCustomer.getFirstName());
        existing.setLastName(updatedCustomer.getLastName());
        existing.setPhone(updatedCustomer.getPhone());
        existing.setAddress(updatedCustomer.getAddress());
        
        if (updatedCustomer.getEmail() != null && !updatedCustomer.getEmail().isEmpty() 
            && !existing.getEmail().equals(updatedCustomer.getEmail())) {
            if (customerRepository.existsByEmail(updatedCustomer.getEmail())) {
                throw new RuntimeException("Email already registered to another account.");
            }
            existing.setEmail(updatedCustomer.getEmail());
        }
        if (updatedCustomer.getPassword() != null && !updatedCustomer.getPassword().isEmpty()) {
            existing.setPassword(updatedCustomer.getPassword());
        }
        if (updatedCustomer.getProfilePic() != null && !updatedCustomer.getProfilePic().isEmpty()) {
            existing.setProfilePic(updatedCustomer.getProfilePic());
        }
        return customerRepository.save(existing);
    }

    public Optional<Customer> getCustomerById(Long id) {
        return customerRepository.findById(id);
    }

    public List<Customer> getAllCustomers() {
        return customerRepository.findRecentCustomers();
    }

    public List<Customer> getRecentCustomers() {
        return customerRepository.findRecentCustomers();
    }

    public void deleteCustomer(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found: " + id));
        // Delete related bookings first to avoid FK constraint violation
        List<Booking> bookings = bookingRepository.findByCustomerOrderByBookingDateDesc(customer);
        bookingRepository.deleteAll(bookings);
        customerRepository.deleteById(id);
    }

    public List<Booking> getCustomerBookings(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found: " + customerId));
        return bookingRepository.findByCustomerWithPackage(customer);
    }

    public long getTotalCustomers() {
        return customerRepository.countNonAdminCustomers();
    }
}
