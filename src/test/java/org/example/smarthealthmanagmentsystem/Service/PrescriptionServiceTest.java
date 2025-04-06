package org.example.smarthealthmanagmentsystem.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import org.example.smarthealthmanagmentsystem.Entity.Doctor;
import org.example.smarthealthmanagmentsystem.Entity.Patient;
import org.example.smarthealthmanagmentsystem.Entity.Prescription;
import org.example.smarthealthmanagmentsystem.Entity.Role;
import org.example.smarthealthmanagmentsystem.Entity.User;
import org.example.smarthealthmanagmentsystem.ExceptionHandler.ResourceNotFoundException;
import org.example.smarthealthmanagmentsystem.ExceptionHandler.UnauthorizedAccessException;
import org.example.smarthealthmanagmentsystem.Repository.AppointmentRepository;
import org.example.smarthealthmanagmentsystem.Repository.DoctorRepository;
import org.example.smarthealthmanagmentsystem.Repository.PatientRepository;
import org.example.smarthealthmanagmentsystem.Repository.PrescriptionRepository;
import org.example.smarthealthmanagmentsystem.Repository.UserRepository;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

class PrescriptionServiceTest {

    @InjectMocks
    private PrescriptionService prescriptionService;

    @Mock
    private PrescriptionRepository prescriptionRepository;
    @Mock
    private AppointmentRepository appointmentRepository;
    @Mock
    private DoctorRepository doctorRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PatientRepository patientRepository;

    private User doctorUser;
    private Doctor doctor;
    private Patient patient;
    private Prescription prescription;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        doctorUser = new User();
        doctorUser.setEmail("doctor@example.com");
        doctorUser.setRoles(Set.of(new Role("ROLE_DOCTOR")));

        doctor = new Doctor();
        doctor.setId(1L);
        doctor.setUser(doctorUser);

        patient = new Patient();
        patient.setId(2L);

        prescription = new Prescription();
        prescription.setMedicationList(Collections.singleton("Med A"));
        prescription.setDosageInstructions("Once a day");
        prescription.setValidUntil(LocalDateTime.now().plusDays(10));
        prescription.setPatient(patient);
    }

    private void mockSecurityContext(String email) {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(email);

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void testAddPrescriptionSuccess() {
        mockSecurityContext("doctor@example.com");

        when(userRepository.findByEmail("doctor@example.com")).thenReturn(Optional.of(doctorUser));
        when(doctorRepository.findByUser(doctorUser)).thenReturn(Optional.of(doctor));
        when(patientRepository.findById(2L)).thenReturn(Optional.of(patient));

        ResponseEntity<String> response = prescriptionService.addPrescription(prescription);

        assertEquals("Prescription saved successfully.", response.getBody());
        verify(prescriptionRepository, times(1)).save(any(Prescription.class));
    }

    @Test
    void testAddPrescriptionUnauthorizedUser() {
        mockSecurityContext("unauth@example.com");
        when(userRepository.findByEmail("unauth@example.com")).thenReturn(Optional.empty());

        UnauthorizedAccessException ex = assertThrows(UnauthorizedAccessException.class,
                () -> prescriptionService.addPrescription(prescription));
        assertEquals("User not authenticated", ex.getMessage());
    }

    @Test
    void testAddPrescriptionNotDoctor() {
        mockSecurityContext("user@example.com");

        User nonDoctorUser = new User();
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(nonDoctorUser));
        when(doctorRepository.findByUser(nonDoctorUser)).thenReturn(Optional.empty());

        UnauthorizedAccessException ex = assertThrows(UnauthorizedAccessException.class,
                () -> prescriptionService.addPrescription(prescription));
        assertEquals("Only doctors can prescribe", ex.getMessage());
    }

    @Test
    void testAddPrescriptionMissingPatientId() {
        mockSecurityContext("doctor@example.com");
        prescription.setPatient(null); // no patient set

        when(userRepository.findByEmail("doctor@example.com")).thenReturn(Optional.of(doctorUser));
        when(doctorRepository.findByUser(doctorUser)).thenReturn(Optional.of(doctor));

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> prescriptionService.addPrescription(prescription));
        assertEquals("Patient ID is required in the prescription", ex.getMessage());
    }

    @Test
    void testAddPrescriptionPatientNotFound() {
        mockSecurityContext("doctor@example.com");

        when(userRepository.findByEmail("doctor@example.com")).thenReturn(Optional.of(doctorUser));
        when(doctorRepository.findByUser(doctorUser)).thenReturn(Optional.of(doctor));
        when(patientRepository.findById(2L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> prescriptionService.addPrescription(prescription));
        assertEquals("Patient not found with ID: 2", ex.getMessage());
    }
}
