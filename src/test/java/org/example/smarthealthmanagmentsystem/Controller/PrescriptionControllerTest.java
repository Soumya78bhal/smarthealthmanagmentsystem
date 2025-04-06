package org.example.smarthealthmanagmentsystem.Controller;

import org.example.smarthealthmanagmentsystem.Entity.Doctor;
import org.example.smarthealthmanagmentsystem.Entity.Patient;
import org.example.smarthealthmanagmentsystem.Entity.Prescription;
import org.example.smarthealthmanagmentsystem.Service.PrescriptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PrescriptionControllerTest {

    @Mock
    private PrescriptionService prescriptionService;

    @InjectMocks
    private PrescriptionController prescriptionController;

    private Prescription testPrescription;

    @BeforeEach
    void setUpTestEnvironment() {
        MockitoAnnotations.openMocks(this);

        Doctor doctor = new Doctor();
        doctor.setId(1L);
        doctor.setName("Dr. Mock");

        Patient patient = new Patient();
        patient.setId(2L);
        patient.setName("Patient Mock");

        testPrescription = new Prescription();
        testPrescription.setId(100L);
        testPrescription.setDoctor(doctor);
        testPrescription.setPatient(patient);
        testPrescription.setMedicationList(Set.of("Paracetamol", "Amoxicillin"));
        testPrescription.setDosageInstructions("Take after meals");
        testPrescription.setPrescribedDate(LocalDateTime.now());
        testPrescription.setValidUntil(LocalDateTime.now().plusDays(10));
    }

    @Test
    void AddPrescriptionTest() {
        when(prescriptionService.addPrescription(testPrescription))
                .thenReturn(ResponseEntity.ok("Prescription added successfully"));

        ResponseEntity<String> response = prescriptionController.addPrescription(testPrescription);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Prescription added successfully", response.getBody());
        verify(prescriptionService, times(1)).addPrescription(testPrescription);
    }

    @Test
    void GetAllPrescriptionsTest() {
        when(prescriptionService.getPrescriptionsForCurrentUser()).thenReturn(List.of(testPrescription));

        ResponseEntity<List<Prescription>> response = prescriptionController.getMyPrescriptions();


        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(2L, response.getBody().get(0).getPatient().getId());
        verify(prescriptionService, times(1)).getPrescriptionsForCurrentUser();
    }

    @Test
    void EditPrescriptionTest() {
        when(prescriptionService.editPrescription(testPrescription)).thenReturn(testPrescription);

        ResponseEntity<Prescription> response = prescriptionController.editPrescription(testPrescription);


        assertEquals("Take after meals", response.getBody().getDosageInstructions());
        assertEquals(Set.of("Paracetamol", "Amoxicillin"), response.getBody().getMedicationList());
        verify(prescriptionService, times(1)).editPrescription(testPrescription);
    }
}
