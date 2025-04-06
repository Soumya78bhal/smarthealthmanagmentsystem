package org.example.smarthealthmanagmentsystem.Service;

import org.example.smarthealthmanagmentsystem.Entity.Role;
import org.example.smarthealthmanagmentsystem.ExceptionHandler.ResourceNotFoundException;
import org.example.smarthealthmanagmentsystem.Repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RoleServiceTest {

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private RoleService roleService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFindByName_Success() {
        // Arrange
        Role role = new Role();
        role.setId(1);
        role.setName("ROLE_DOCTOR");

        when(roleRepository.findByName("ROLE_DOCTOR")).thenReturn(Optional.of(role));

        // Act
        Role result = roleService.findByName("ROLE_DOCTOR");

        // Assert
        assertNotNull(result);
        assertEquals("ROLE_DOCTOR", result.getName());
        verify(roleRepository).findByName("ROLE_DOCTOR");
    }

    @Test
    void testFindByName_RoleNotFound() {
        // Arrange
        when(roleRepository.findByName("ROLE_UNKNOWN")).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> roleService.findByName("ROLE_UNKNOWN"));

        assertEquals("Role not found with name: ROLE_UNKNOWN", exception.getMessage());
        verify(roleRepository).findByName("ROLE_UNKNOWN");
    }
}
