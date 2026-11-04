package com.oop.fooddelivery.service;
import com.oop.fooddelivery.model.AdminAccount;
import com.oop.fooddelivery.repository.AdminAccountRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AdminAccountService {

    private final AdminAccountRepository adminAccountRepository;

    private final PasswordEncoder passwordEncoder =
            new BCryptPasswordEncoder();

    public AdminAccountService(AdminAccountRepository adminAccountRepository) {
        this.adminAccountRepository = adminAccountRepository;
    }

    public boolean adminExists() {
        return adminAccountRepository.count() > 0;
    }
    public AdminAccount saveAdmin(AdminAccount admin) {
        admin.setPassword(passwordEncoder.encode(admin.getPassword()));
        return adminAccountRepository.save(admin);
    }


    public Optional<AdminAccount> getAdminByEmail(String email) {
        return adminAccountRepository.findByEmail(email);
    }

    public boolean checkPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
}
