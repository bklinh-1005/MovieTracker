package com.movietracker.controller;

import com.movietracker.entity.Role;
import com.movietracker.entity.User;
import com.movietracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@RequestParam String username,
                               @RequestParam String email,
                               @RequestParam String fullName,
                               @RequestParam String password,
                               Model model) {
        if (userRepository.existsByUsername(username.trim())) {
            model.addAttribute("error", "Tên đăng nhập đã tồn tại, vui lòng chọn tên khác!");
            return "register";
        }
        User user = User.builder()
                .username(username.trim())
                .email(email.trim())
                .fullName(fullName.trim())
                .password(passwordEncoder.encode(password))
                .role(Role.USER)
                .build();
        userRepository.save(user);
        return "redirect:/login?registered";
    }
}