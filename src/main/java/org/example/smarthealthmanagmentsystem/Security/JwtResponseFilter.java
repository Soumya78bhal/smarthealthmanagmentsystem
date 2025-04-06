package org.example.smarthealthmanagmentsystem.Security;

import org.example.smarthealthmanagmentsystem.Security.JwtUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public class JwtResponseFilter extends BasicAuthenticationFilter {

    private final JwtUtil jwtUtil;

    public JwtResponseFilter(JwtUtil jwtUtil) {

        super(authenticationManager -> null);
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        // Retrieve authentication from the security context
        Authentication authentication = getAuthentication();

        // If the user is authenticated and credentials are available
        if (authentication != null && authentication.isAuthenticated() && authentication.getCredentials() != null) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            // Generate a new JWT token for the user
            String token = jwtUtil.generateToken(userDetails);

            // Add the JWT token to the response header
            response.setHeader("Authorization", token);
        }

        // Continue with the filter chain
        chain.doFilter(request, response);
    }

    // Helper method to get current authentication
    private Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }
}
