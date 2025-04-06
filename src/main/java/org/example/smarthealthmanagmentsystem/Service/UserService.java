package org.example.smarthealthmanagmentsystem.Service;

import org.example.smarthealthmanagmentsystem.Entity.Role;
import org.example.smarthealthmanagmentsystem.Entity.User;
import org.example.smarthealthmanagmentsystem.ExceptionHandler.ResourceNotFoundException;
import org.example.smarthealthmanagmentsystem.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class UserService {

    @Autowired
    private final UserRepository userRepository;

    @Autowired
    private RoleService roleService;

    private final PasswordEncoder passwordEncoder;

    // Constructor injection for required components
    public UserService(UserRepository userRepository, RoleService roleService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleService = roleService;
        this.passwordEncoder = passwordEncoder;
    }

    // Fetch all users from the database
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // Retrieve a user by their ID
    public User getUserById(Long id) {
        return userRepository.findById(Math.toIntExact(id))
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
    }

    // Create a new user with the specified role
    public User createUser(User user, String roleType) {
        user.setPassword(passwordEncoder.encode(user.getPassword())); // Encrypt the password

        Role role = roleService.findByName(roleType); // Get role by name
        if (role == null) {
            throw new ResourceNotFoundException("Role not found: " + roleType);
        }

        user.setRoles(Set.of(role)); // Assign role to user
        return userRepository.save(user); // Save user to DB
    }

    // Delete a user by ID
    public void deleteUser(Long id) {
        if (!userRepository.existsById(Math.toIntExact(id))) {
            throw new ResourceNotFoundException("User not found with ID: " + id);
        }
        userRepository.deleteById(Math.toIntExact(id));
    }
}
