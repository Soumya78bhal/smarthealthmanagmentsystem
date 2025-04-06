package org.example.smarthealthmanagmentsystem.Controller;

import org.example.smarthealthmanagmentsystem.Entity.Role;
import org.example.smarthealthmanagmentsystem.Entity.User;
import org.example.smarthealthmanagmentsystem.Service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private User mockUser;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        mockUser = new User();
        mockUser.setId(1);
        mockUser.setEmail("mock@example.com");
        mockUser.setPassword("password");

        Role mockRole = new Role();
        mockRole.setName("PATIENT");
        Set<Role> roleSet = new HashSet<>();
        roleSet.add(mockRole);
        mockUser.setRoles(roleSet);
    }

    @Test
    void getAllUsersTest() {
        List<User> userList = Collections.singletonList(mockUser);
        when(userService.getAllUsers()).thenReturn(userList);

        ResponseEntity<List<User>> response = userController.getAllUsers();


        assertEquals(1, response.getBody().size());
        assertEquals("mock@example.com", response.getBody().get(0).getEmail());

        verify(userService, times(1)).getAllUsers();
    }

    @Test
    void getUserByIdTest() {
        when(userService.getUserById(1L)).thenReturn(mockUser);

        ResponseEntity<?> response = userController.getUserById(1L);


        assertInstanceOf(User.class, response.getBody());
        assertEquals("mock@example.com", ((User) response.getBody()).getEmail());

        verify(userService, times(1)).getUserById(1L);
    }

    @Test
    void createUserTest() {
        when(userService.createUser(mockUser, "PATIENT")).thenReturn(mockUser);

        ResponseEntity<User> response = userController.createUser(mockUser, "PATIENT");


        assertEquals("mock@example.com", response.getBody().getEmail());

        verify(userService, times(1)).createUser(mockUser, "PATIENT");
    }

    @Test
    void deleteUserTest() {
        doNothing().when(userService).deleteUser(1L);

        ResponseEntity<?> response = userController.deleteUser(1L);


        assertEquals("User deleted successfully", response.getBody());

        verify(userService, times(1)).deleteUser(1L);
    }
}
