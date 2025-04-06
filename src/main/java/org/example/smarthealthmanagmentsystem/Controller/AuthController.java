package org.example.smarthealthmanagmentsystem.Controller;

import jakarta.servlet.http.HttpServletResponse;
import org.example.smarthealthmanagmentsystem.Security.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final JwtUtil jwtUtil;

    public AuthController(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @GetMapping("/getToken")
    public ResponseEntity<?> generateTokenForAuthenticatedUser(HttpServletResponse response) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated() &&
                authentication.getPrincipal() instanceof UserDetails) {

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            // Generate a new JWT token
            String token = jwtUtil.generateToken(userDetails);

            // Optionally, return it in the body too
            return ResponseEntity.ok(Map.of("token", token));
        }

        return ResponseEntity.status(401).body("User not authenticated");
    }
}

