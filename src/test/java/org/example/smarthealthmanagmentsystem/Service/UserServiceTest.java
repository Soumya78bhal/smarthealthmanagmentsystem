package org.example.smarthealthmanagmentsystem.Service;

import java.util.List;
import java.util.Optional;

import org.example.smarthealthmanagmentsystem.Entity.Role;
import org.example.smarthealthmanagmentsystem.Entity.User;
import org.example.smarthealthmanagmentsystem.ExceptionHandler.ResourceNotFoundException;
import org.example.smarthealthmanagmentsystem.Repository.UserRepository;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleService roleService;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User mockUser;
    private Role mockRole;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        mockUser = new User();
        mockUser.setId(1);
        mockUser.setEmail("user@example.com");
        mockUser.setPassword("password");

        mockRole = new Role();
        mockRole.setId(1);
        mockRole.setName("ROLE_PATIENT");
    }

    @Test
    void testGetAllUsers() {
        List<User> users = List.of(mockUser);
        when(userRepository.findAll()).thenReturn(users);

        List<User> result = userService.getAllUsers();

        assertEquals(1, result.size());
        assertEquals("user@example.com", result.get(0).getEmail());
    }

    @Test
    void testGetUserById_Success() {
        when(userRepository.findById(1)).thenReturn(Optional.of(mockUser));

        User result = userService.getUserById(1L);

        assertNotNull(result);
        assertEquals("user@example.com", result.getEmail());
    }

    @Test
    void testGetUserById_NotFound() {
        when(userRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(1L));
    }

    @Test
    void testCreateUser_Success() {
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        when(roleService.findByName("ROLE_PATIENT")).thenReturn(mockRole);
        when(userRepository.save(any(User.class))).thenReturn(mockUser);

        User inputUser = new User();
        inputUser.setEmail("user@example.com");
        inputUser.setPassword("password");

        User savedUser = userService.createUser(inputUser, "ROLE_PATIENT");

        assertEquals("user@example.com", savedUser.getEmail());
        verify(passwordEncoder).encode("password");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testDeleteUser_Success() {
        when(userRepository.existsById(1)).thenReturn(true);

        userService.deleteUser(1L);

        verify(userRepository).existsById(1);
        verify(userRepository).deleteById(1);
    }

    @Test
    void testDeleteUser_UserNotFound() {
        when(userRepository.existsById(1)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> userService.deleteUser(1L));

        verify(userRepository).existsById(1);
        verify(userRepository, never()).deleteById(anyInt());
    }

}
