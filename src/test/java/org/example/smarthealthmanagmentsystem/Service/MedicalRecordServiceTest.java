package org.example.smarthealthmanagmentsystem.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.example.smarthealthmanagmentsystem.Entity.Doctor;
import org.example.smarthealthmanagmentsystem.Entity.MedicalRecord;
import org.example.smarthealthmanagmentsystem.Entity.Patient;
import org.example.smarthealthmanagmentsystem.Entity.Role;
import org.example.smarthealthmanagmentsystem.Entity.User;
import org.example.smarthealthmanagmentsystem.ExceptionHandler.UnauthorizedAccessException;
import org.example.smarthealthmanagmentsystem.Repository.DoctorRepository;
import org.example.smarthealthmanagmentsystem.Repository.MedicalRecordRepository;
import org.example.smarthealthmanagmentsystem.Repository.PatientRepository;
import org.example.smarthealthmanagmentsystem.Repository.PrescriptionRepository;
import org.example.smarthealthmanagmentsystem.Repository.UserRepository;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
class MedicalRecordServiceTest {

    @InjectMocks
    private MedicalRecordService medicalRecordService;

    @Mock
    private MedicalRecordRepository medicalRecordRepository;
    @Mock
    private PrescriptionRepository prescriptionRepository;
    @Mock
    private DoctorRepository doctorRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PatientRepository patientRepository;

    private User mockUser;
    private Role rolePatient;
    private Role roleDoctor;
    private Patient patient;
    private Doctor doctor;

    @BeforeEach
    void setUp() throws IOException {
        medicalRecordService = new MedicalRecordService();
        MockitoAnnotations.openMocks(this);

        mockUser = new User();
        mockUser.setEmail("test@example.com");

        rolePatient = new Role();
        rolePatient.setName("ROLE_PATIENT");

        roleDoctor = new Role();
        roleDoctor.setName("ROLE_DOCTOR");

        patient = new Patient();
        patient.setId(1L);
        patient.setUser(mockUser);

        doctor = new Doctor();
        doctor.setId(1L);

        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken(mockUser.getEmail(), null)
        );
    }

    @Test
    void testGetAllMedicalRecords_asPatient() {
        mockUser.setRoles(Set.of(rolePatient));
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(mockUser));
        when(patientRepository.findByUser(mockUser)).thenReturn(Optional.of(patient));

        MedicalRecord record = new MedicalRecord();
        record.setId(1L);
        record.setPatient(patient);
        when(medicalRecordRepository.findByPatient(patient)).thenReturn(List.of(record));

        List<MedicalRecord> result = medicalRecordService.getAllMedicalRecords();

        assertEquals(1, result.size());
        verify(medicalRecordRepository).findByPatient(patient);
    }

    @Test
    void testGetAllMedicalRecords_asDoctor() {
        mockUser.setRoles(Set.of(roleDoctor));
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(mockUser));

        when(medicalRecordRepository.findAll()).thenReturn(List.of(new MedicalRecord()));

        List<MedicalRecord> result = medicalRecordService.getAllMedicalRecords();

        assertEquals(1, result.size());
        verify(medicalRecordRepository).findAll();
    }

    @Test
    void testSaveMedicalRecordWithFile_success() throws IOException {
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "hello".getBytes());

        MedicalRecord record = new MedicalRecord();
        record.setDoctor(doctor);
        record.setPatient(patient);

        when(doctorRepository.findById(anyLong())).thenReturn(Optional.of(doctor));
        when(patientRepository.findById(anyLong())).thenReturn(Optional.of(patient));
        when(medicalRecordRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        MedicalRecord result = medicalRecordService.saveMedicalRecordWithFile(record, file);

        assertNotNull(result.getMedicalDocuments());
        assertEquals(doctor, result.getDoctor());
        assertEquals(patient, result.getPatient());
    }

    @Test
    void testGetMedicalRecordWithFileById_success() throws IOException {
        MedicalRecord record = new MedicalRecord();
        record.setId(1L);
        record.setMedicalDocuments("test.txt");
        record.setPatient(patient);

        mockUser.setRoles(Set.of(rolePatient));
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(mockUser));
        when(medicalRecordRepository.findById(1L)).thenReturn(Optional.of(record));
        when(patientRepository.findByUser(mockUser)).thenReturn(Optional.of(patient));

        // create dummy file
        Path filePath = Paths.get("uploads/test.txt");
        Files.createDirectories(filePath.getParent());
        Files.write(filePath, "Test Content".getBytes());

        ResponseEntity<?> response = medicalRecordService.getMedicalRecordWithFileById(1L);

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody() instanceof Resource);

        Files.deleteIfExists(filePath); // cleanup
    }

    @Test
    void testGetMedicalRecordWithFileById_unauthorized() {
        Patient anotherPatient = new Patient();
        anotherPatient.setId(99L);

        MedicalRecord record = new MedicalRecord();
        record.setId(1L);
        record.setMedicalDocuments("test.txt");
        record.setPatient(anotherPatient);

        mockUser.setRoles(Set.of(rolePatient));
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(mockUser));
        when(medicalRecordRepository.findById(1L)).thenReturn(Optional.of(record));
        when(patientRepository.findByUser(mockUser)).thenReturn(Optional.of(patient));

        assertThrows(UnauthorizedAccessException.class, ()
                -> medicalRecordService.getMedicalRecordWithFileById(1L));
    }
}
