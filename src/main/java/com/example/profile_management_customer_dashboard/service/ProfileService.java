package com.example.profile_management_customer_dashboard.service;

import com.example.profile_management_customer_dashboard.entity.Customer;
import com.example.profile_management_customer_dashboard.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ProfileService {

    @Autowired
    private CustomerRepository customerRepository;

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
        return customerRepository.findAll();
    }

    public List<Customer> getRecentCustomers() {
        return customerRepository.findRecentCustomers();
    }

    public void deleteCustomer(Long id) {
        customerRepository.deleteById(id);
    }

    public long getTotalCustomers() {
        return customerRepository.countNonAdminCustomers();
    }
}
