package org.example.smarthealthmanagmentsystem.Controller;

import org.example.smarthealthmanagmentsystem.Entity.Patient;
import org.example.smarthealthmanagmentsystem.Service.PatientService;
import org.example.smarthealthmanagmentsystem.Service.PrescriptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PatientControllerTest {

    @Mock
    private PatientService patientService;

    @Mock
    private PrescriptionService prescriptionService;

    @InjectMocks
    private PatientController patientController;

    private Patient patient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        patient = new Patient();
        patient.setId(1L);
        patient.setName("Jane Doe");
        patient.setGender("Female");
        patient.setBloodType("A+");
        patient.setContact("1234567890");
        patient.setDateOfBirth(LocalDate.of(1990, 1, 1));
    }

    @Test
    void testRegisterPatient() {
        when(patientService.registerPatient(any(Patient.class))).thenReturn(patient);

        ResponseEntity<Patient> response = patientController.registerPatient(patient);

        assertEquals("Jane Doe", response.getBody().getName());
        verify(patientService, times(1)).registerPatient(patient);
    }

    @Test
    void testGetAllPatient() {
        List<Patient> patients = Collections.singletonList(patient);
        when(patientService.getAllPatients()).thenReturn(patients);

        ResponseEntity<List<Patient>> response = patientController.getAllPatient();


        assertEquals(1, response.getBody().size());
        verify(patientService, times(1)).getAllPatients();
    }

    @Test
    void testGetPatientById() {
        when(patientService.getPatientById(1L)).thenReturn(patient);

        ResponseEntity<Patient> response = patientController.getPatientById(1L);


        assertEquals(1L, response.getBody().getId());
        verify(patientService, times(1)).getPatientById(1L);
    }

    @Test
    void testGetMyDetails() {
        when(patientService.getCurrentPatientDetails()).thenReturn(patient);

        ResponseEntity<Patient> response = patientController.getMyDetails();


        assertEquals("Jane Doe", response.getBody().getName());
        verify(patientService, times(1)).getCurrentPatientDetails();
    }

    @Test
    void testEditPatient() {
        when(patientService.editPatient(any(Patient.class))).thenReturn(patient);

        ResponseEntity<Patient> response = patientController.editPatient(patient);


        assertEquals("Jane Doe", response.getBody().getName());
        verify(patientService, times(1)).editPatient(patient);
    }
}
