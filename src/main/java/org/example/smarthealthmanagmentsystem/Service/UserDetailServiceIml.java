package org.example.smarthealthmanagmentsystem.Service;

import org.example.smarthealthmanagmentsystem.Entity.User;
import org.example.smarthealthmanagmentsystem.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

@Service
public class UserDetailServiceIml implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(UserDetailServiceIml.class);

    @Autowired
    private UserRepository userRepository;

    // Loads user details for authentication based on username (email)
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> user = userRepository.findByEmail(username);

        if (user.isEmpty()) {
            logger.warn("User not found with username: {}", username);
            throw new UsernameNotFoundException("User not found with username: " + username);
        } else {
            logger.info("User found: {}", username);
            // Return Spring Security compatible User object
            return new org.springframework.security.core.userdetails.User(
                    user.get().getEmail(),
                    user.get().getPassword(),
                    user.get().getRoles()
            );
        }
    }
}
