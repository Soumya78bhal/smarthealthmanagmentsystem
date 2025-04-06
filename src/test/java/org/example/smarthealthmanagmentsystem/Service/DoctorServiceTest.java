package org.example.smarthealthmanagmentsystem.Service;

import org.example.smarthealthmanagmentsystem.Entity.Doctor;
import org.example.smarthealthmanagmentsystem.Entity.Role;
import org.example.smarthealthmanagmentsystem.Entity.User;
import org.example.smarthealthmanagmentsystem.ExceptionHandler.EmailAlreadyExistsException;
import org.example.smarthealthmanagmentsystem.ExceptionHandler.IdNotFoundException;
import org.example.smarthealthmanagmentsystem.ExceptionHandler.RoleNotFoundException;
import org.example.smarthealthmanagmentsystem.Repository.DoctorRepository;
import org.example.smarthealthmanagmentsystem.Repository.RoleRepository;
import org.example.smarthealthmanagmentsystem.Repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DoctorServiceTest {

    @InjectMocks
    private DoctorService doctorService;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    private Doctor doctor;
    private User user;
    private Role role;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("doctor@example.com");

        user = new User();
        user.setEmail("doctor@example.com");
        user.setPassword("plainPassword");

        doctor = new Doctor();
        doctor.setName("Dr. Smith");
        doctor.setSpeciality("Cardiology");
        doctor.setExperienceYears(10);
        doctor.setUser(user);

        role = new Role();
        role.setName("ROLE_DOCTOR");
    }

    @Test
    void testSaveDoctor_Success() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(roleRepository.findByName("ROLE_DOCTOR")).thenReturn(Optional.of(role));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
        when(doctorRepository.save(any(Doctor.class))).thenAnswer(i -> i.getArgument(0));

        Doctor savedDoctor = doctorService.saveDoctor(doctor);

        assertNotNull(savedDoctor.getUser());
        assertEquals("doctor@example.com", savedDoctor.getUser().getEmail());
        assertEquals("Dr. Smith", savedDoctor.getName());
    }

    @Test
    void testSaveDoctor_EmailAlreadyExists() {
        when(userRepository.findByEmail("doctor@example.com")).thenReturn(Optional.of(user));

        assertThrows(EmailAlreadyExistsException.class, () -> doctorService.saveDoctor(doctor));
    }

    @Test
    void testSaveDoctor_RoleNotFound() {
        when(userRepository.findByEmail("doctor@example.com")).thenReturn(Optional.empty());
        when(roleRepository.findByName("ROLE_DOCTOR")).thenReturn(Optional.empty());

        assertThrows(RoleNotFoundException.class, () -> doctorService.saveDoctor(doctor));
    }

    @Test
    void testGetDoctorById_Found() {
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        Optional<Doctor> result = doctorService.getDoctorById(1L);

        assertTrue(result.isPresent());
        assertEquals("Dr. Smith", result.get().getName());
    }

    @Test
    void testGetDoctorById_NotFound() {
        when(doctorRepository.findById(1L)).thenReturn(Optional.empty());
        Optional<Doctor> result = doctorService.getDoctorById(1L);

        assertFalse(result.isPresent());
    }

    @Test
    void testGetCurrentDoctorDetails_Success() {
        when(doctorRepository.findByUserEmail("doctor@example.com")).thenReturn(Optional.of(doctor));

        Doctor result = doctorService.getCurrentDoctorDetails();

        assertNotNull(result);
        assertEquals("Dr. Smith", result.getName());
    }

    @Test
    void testGetCurrentDoctorDetails_NotDoctor() {
        when(doctorRepository.findByUserEmail("doctor@example.com")).thenReturn(Optional.empty());

        assertThrows(IdNotFoundException.class, () -> doctorService.getCurrentDoctorDetails());
    }

    @Test
    void testGetAllDoctors() {
        when(doctorRepository.findAll()).thenReturn(List.of(doctor));
        List<Doctor> result = doctorService.getAllDoctors();

        assertEquals(1, result.size());
        assertEquals("Dr. Smith", result.get(0).getName());
    }
}
