package org.example.smarthealthmanagmentsystem.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.example.smarthealthmanagmentsystem.Entity.Patient;
import org.example.smarthealthmanagmentsystem.Entity.Role;
import org.example.smarthealthmanagmentsystem.Entity.User;
import org.example.smarthealthmanagmentsystem.ExceptionHandler.EmailAlreadyExistsException;
import org.example.smarthealthmanagmentsystem.ExceptionHandler.IdNotFoundException;
import org.example.smarthealthmanagmentsystem.ExceptionHandler.ResourceNotFoundException;
import org.example.smarthealthmanagmentsystem.Repository.PatientRepository;
import org.example.smarthealthmanagmentsystem.Repository.RoleRepository;
import org.example.smarthealthmanagmentsystem.Repository.UserRepository;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

class PatientServiceTest {

    @InjectMocks
    private PatientService patientService;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void registerPatient_Success() {
        Patient patient = createPatient();
        String email = patient.getUser().getEmail();

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(roleRepository.findByName("ROLE_PATIENT")).thenReturn(Optional.of(new Role(1, "ROLE_PATIENT")));
        when(passwordEncoder.encode(anyString())).thenReturn("encoded_password");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
        when(patientRepository.save(any(Patient.class))).thenAnswer(i -> i.getArgument(0));

        Patient savedPatient = patientService.registerPatient(patient);

        assertEquals(patient.getName(), savedPatient.getName());
        verify(userRepository).save(any(User.class));
        verify(patientRepository).save(any(Patient.class));
    }

    @Test
    void registerPatient_EmailAlreadyExists_ThrowsException() {
        Patient patient = createPatient();
        when(userRepository.findByEmail(patient.getUser().getEmail()))
                .thenReturn(Optional.of(new User()));

        assertThrows(EmailAlreadyExistsException.class, () -> patientService.registerPatient(patient));
    }

    @Test
    void getAllPatients_ReturnsList() {
        List<Patient> patients = List.of(createPatient());
        when(patientRepository.findAll()).thenReturn(patients);

        List<Patient> result = patientService.getAllPatients();
        assertEquals(1, result.size());
        verify(patientRepository).findAll();
    }

    @Test
    void getPatientById_Found() {
        Patient patient = createPatient();
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));

        Patient result = patientService.getPatientById(1L);
        assertEquals(patient.getName(), result.getName());
    }

    @Test
    void getPatientById_NotFound_ThrowsException() {
        when(patientRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(IdNotFoundException.class, () -> patientService.getPatientById(1L));
    }

    @Test
    void getCurrentPatientDetails_Success() {
        String email = "test@example.com";
        Patient patient = createPatient();
        patient.getUser().setEmail(email);

        mockSecurityContext(email);
        when(patientRepository.findByUserEmail(email)).thenReturn(Optional.of(patient));

        Patient result = patientService.getCurrentPatientDetails();
        assertEquals(patient.getName(), result.getName());
    }

    @Test
    void getCurrentPatientDetails_NotFound_ThrowsException() {
        String email = "test@example.com";
        mockSecurityContext(email);
        when(patientRepository.findByUserEmail(email)).thenReturn(Optional.empty());

        assertThrows(IdNotFoundException.class, () -> patientService.getCurrentPatientDetails());
    }

    @Test
    void editPatient_Success() {
        String email = "test@example.com";
        Patient updated = createPatient();
        updated.setId(1L);
        updated.setName("Updated Name");

        Patient existing = createPatient();
        existing.setId(1L);

        User user = new User();
        user.setEmail(email);

        mockSecurityContext(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(patientRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(patientRepository.save(any(Patient.class))).thenAnswer(i -> i.getArgument(0));

        Patient result = patientService.editPatient(updated);
        assertEquals("Updated Name", result.getName());
    }

    @Test
    void editPatient_NotFound_ThrowsException() {
        Patient updated = createPatient();
        updated.setId(1L);

        mockSecurityContext("email@example.com");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(new User()));
        when(patientRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> patientService.editPatient(updated));
    }

    // Utility methods
    private Patient createPatient() {
        Patient patient = new Patient();
        patient.setId(1L);
        patient.setName("John Doe");
        patient.setDateOfBirth(LocalDate.of(1990, 1, 1));
        patient.setGender("Male");
        patient.setBloodType("A+");
        patient.setContact("1234567890");

        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("password");
        patient.setUser(user);

        return patient;
    }

    private void mockSecurityContext(String email) {
        Authentication auth = mock(Authentication.class);
        SecurityContext context = mock(SecurityContext.class);
        when(auth.getName()).thenReturn(email);
        when(context.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(context);
    }
}
