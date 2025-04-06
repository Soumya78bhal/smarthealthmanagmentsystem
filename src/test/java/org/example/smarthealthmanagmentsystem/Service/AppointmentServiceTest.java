package org.example.smarthealthmanagmentsystem.Service;

import org.example.smarthealthmanagmentsystem.Entity.*;
import org.example.smarthealthmanagmentsystem.ExceptionHandler.IdNotFoundException;
import org.example.smarthealthmanagmentsystem.ExceptionHandler.UnauthorizedAccessException;
import org.example.smarthealthmanagmentsystem.Repository.AppointmentRepository;
import org.example.smarthealthmanagmentsystem.Repository.DoctorRepository;
import org.example.smarthealthmanagmentsystem.Repository.PatientRepository;
import org.example.smarthealthmanagmentsystem.Repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AppointmentServiceTest {

    @InjectMocks
    private AppointmentService appointmentService;

    @Mock
    private AppointmentRepository appointmentRepository;
    @Mock
    private DoctorRepository doctorRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PatientRepository patientRepository;

    @Mock
    private SecurityContext securityContext;
    @Mock
    private Authentication authentication;

    private User mockUser;
    private Doctor mockDoctor;
    private Patient mockPatient;
    private Appointment mockAppointment;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Setup authentication
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("doctor@example.com");

        // Mock User
        mockUser = new User();
        mockUser.setEmail("doctor@example.com");
        Role doctorRole = new Role();
        doctorRole.setName("ROLE_DOCTOR");
        mockUser.setRoles(Set.of(doctorRole));

        // Mock Doctor
        mockDoctor = new Doctor();
        mockDoctor.setId(1L);
        mockDoctor.setUser(mockUser);

        // Mock Patient
        mockPatient = new Patient();
        mockPatient.setId(2L);
        mockPatient.setUser(mockUser);

        // Mock Appointment
        mockAppointment = new Appointment();
        mockAppointment.setId(10L);
        mockAppointment.setDoctor(mockDoctor);
        mockAppointment.setPatient(mockPatient);
        mockAppointment.setAppointmentDate(LocalDateTime.now());
        mockAppointment.setAppointmentStatus(AppointmentStatus.SCHEDULED);
        mockAppointment.setDescription("Check-up");
    }

    @Test
    void testScheduleAppointment() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(mockUser));
        when(doctorRepository.findByUser(any())).thenReturn(Optional.of(mockDoctor));
        when(patientRepository.findById(anyLong())).thenReturn(Optional.of(mockPatient));
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(mockAppointment);

        Appointment result = appointmentService.scheduleAppointment(mockAppointment);

        assertNotNull(result);
        assertEquals(AppointmentStatus.SCHEDULED, result.getAppointmentStatus());
    }

    @Test
    void testDeleteAppointmentByAdmin() {
        Role adminRole = new Role();
        adminRole.setName("ROLE_ADMIN");
        mockUser.setRoles(Set.of(adminRole));

        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(mockUser));
        when(appointmentRepository.findById(10L)).thenReturn(Optional.of(mockAppointment));

        ResponseEntity<String> response = appointmentService.deleteAppointment(10L, "admin@example.com");
        assertEquals("Appointment deleted by Admin", response.getBody());
    }

    @Test
    void testDeleteAppointmentByDoctor() {
        when(userRepository.findByEmail("doctor@example.com")).thenReturn(Optional.of(mockUser));
        when(appointmentRepository.findById(10L)).thenReturn(Optional.of(mockAppointment));
        when(doctorRepository.findByUser(mockUser)).thenReturn(Optional.of(mockDoctor));

        ResponseEntity<String> response = appointmentService.deleteAppointment(10L, "doctor@example.com");
        assertEquals("Appointment deleted by Doctor", response.getBody());
    }

    @Test
    void testGetAppointmentsByRoleAsDoctor() throws AccessDeniedException {
        when(userRepository.findByEmail(any())).thenReturn(Optional.of(mockUser));
        when(doctorRepository.findByUser(any())).thenReturn(Optional.of(mockDoctor));
        when(appointmentRepository.findByDoctor(any())).thenReturn(List.of(mockAppointment));

        List<Appointment> appointments = appointmentService.getAppointmentsByRole();
        assertFalse(appointments.isEmpty());
    }

    @Test
    void testEditAppointmentAsDoctor() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(mockUser));
        when(doctorRepository.findByUser(any())).thenReturn(Optional.of(mockDoctor));
        when(appointmentRepository.findById(10L)).thenReturn(Optional.of(mockAppointment));
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(mockAppointment);

        mockAppointment.setDescription("Updated description");
        Appointment result = appointmentService.editAppointment(mockAppointment);
        assertEquals("Updated description", result.getDescription());
    }

    @Test
    void testGetAppointmentsForCurrentDoctor() {
        when(userRepository.findByEmail(any())).thenReturn(Optional.of(mockUser));
        when(doctorRepository.findByUser(any())).thenReturn(Optional.of(mockDoctor));
        when(appointmentRepository.findByDoctor(any())).thenReturn(List.of(mockAppointment));

        List<Appointment> result = appointmentService.getAppointmentsForCurrentDoctor();
        assertEquals(1, result.size());
    }
}
