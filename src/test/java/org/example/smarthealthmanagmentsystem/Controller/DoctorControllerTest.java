package org.example.smarthealthmanagmentsystem.Controller;

import org.example.smarthealthmanagmentsystem.Entity.Doctor;
import org.example.smarthealthmanagmentsystem.Service.AppointmentService;
import org.example.smarthealthmanagmentsystem.Service.DoctorService;
import org.example.smarthealthmanagmentsystem.Service.PatientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DoctorControllerTest {

    @InjectMocks
    private DoctorController doctorController;

    @Mock
    private DoctorService doctorService;

    @Mock
    private AppointmentService appointmentService;

    @Mock
    private PatientService patientService;

    private Doctor doctor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        doctor = new Doctor();
        doctor.setId(1L);
        doctor.setName("Dr. House");
        doctor.setSpeciality("Diagnostics");
    }

    @Test
    void testCreateDoctor_success() {
        when(doctorService.saveDoctor(any())).thenReturn(doctor);

        ResponseEntity<Doctor> response = doctorController.createDoctor(doctor);

        assertEquals("Dr. House", response.getBody().getName());
        verify(doctorService, times(1)).saveDoctor(doctor);
    }

    @Test
    void testGetAllDoctors_success() {
        when(doctorService.getAllDoctors()).thenReturn(List.of(doctor));

        List<Doctor> doctors = doctorController.getAllDoctors();

        assertEquals(1, doctors.size());
        assertEquals("Dr. House", doctors.get(0).getName());
        verify(doctorService, times(1)).getAllDoctors();
    }

    @Test
    void testGetMyDoctorProfile_success() {
        when(doctorService.getCurrentDoctorDetails()).thenReturn(doctor);

        ResponseEntity<Doctor> response = doctorController.getMyDoctorProfile();

        assertEquals("Dr. House", response.getBody().getName());
        verify(doctorService, times(1)).getCurrentDoctorDetails();
    }
}
