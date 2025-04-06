package org.example.smarthealthmanagmentsystem.Service;

import org.example.smarthealthmanagmentsystem.Entity.Role;
import org.example.smarthealthmanagmentsystem.Entity.User;
import org.example.smarthealthmanagmentsystem.Repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserDetailServiceImlTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserDetailServiceIml userDetailService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testLoadUserByUsername_UserFound() {
        // Arrange
        User mockUser = new User();
        mockUser.setEmail("doctor@example.com");
        mockUser.setPassword("encoded-password");

        Role role = new Role();
        role.setName("ROLE_DOCTOR");

        mockUser.setRoles(Set.of(role));

        when(userRepository.findByEmail("doctor@example.com")).thenReturn(Optional.of(mockUser));

        // Act
        UserDetails userDetails = userDetailService.loadUserByUsername("doctor@example.com");

        // Assert
        assertNotNull(userDetails);
        assertEquals("doctor@example.com", userDetails.getUsername());
        assertEquals("encoded-password", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_DOCTOR")));

        verify(userRepository).findByEmail("doctor@example.com");
    }

    @Test
    void testLoadUserByUsername_UserNotFound() {
        // Arrange
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        // Act & Assert
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class,
                () -> userDetailService.loadUserByUsername("unknown@example.com"));

        assertEquals("User not found with username: unknown@example.com", exception.getMessage());
        verify(userRepository).findByEmail("unknown@example.com");
    }
}
