package org.example.smarthealthmanagmentsystem.Controller;

import org.example.smarthealthmanagmentsystem.Entity.Appointment;
import org.example.smarthealthmanagmentsystem.Service.AppointmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AppointmentControllerTest {

    @InjectMocks
    private AppointmentController appointmentController;

    @Mock
    private AppointmentService appointmentService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    private Appointment appointment;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        appointment = new Appointment();
        appointment.setId(1L);
        appointment.setDescription("General Checkup");
        appointment.setAppointmentDate(LocalDateTime.now());
    }

    @Test
    void testGetAllAppointments_success() throws Exception {
        when(appointmentService.getAppointmentsByRole()).thenReturn(List.of(appointment));

        List<Appointment> result = appointmentController.getAllAppointments();

        assertEquals(1, result.size());
        assertEquals("General Checkup", result.get(0).getDescription());
        verify(appointmentService, times(1)).getAppointmentsByRole();
    }

    @Test
    void testGetAllAppointments_accessDenied() throws Exception {
        when(appointmentService.getAppointmentsByRole()).thenThrow(new AccessDeniedException("Access Denied"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            appointmentController.getAllAppointments();
        });

        assertEquals("java.nio.file.AccessDeniedException: Access Denied", exception.getMessage());
    }

    @Test
    void testScheduleAppointment_success() {
        when(appointmentService.scheduleAppointment(any())).thenReturn(appointment);

        ResponseEntity<Appointment> response = appointmentController.scheduleAppointment(appointment);

        assertEquals("General Checkup", response.getBody().getDescription());
        verify(appointmentService).scheduleAppointment(appointment);
    }

    @Test
    void testDeleteAppointment_success() {
        String userEmail = "doctor@example.com";

        // Mock security context
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(userEmail);
        SecurityContextHolder.setContext(securityContext);

        when(appointmentService.deleteAppointment(1L, userEmail)).thenReturn(ResponseEntity.ok("Deleted"));

        ResponseEntity<String> response = appointmentController.deleteAppointment(1L);

        assertEquals("Deleted", response.getBody());
        verify(appointmentService).deleteAppointment(1L, userEmail);
    }

    @Test
    void testEditAppointment_success() {
        when(appointmentService.editAppointment(any())).thenReturn(appointment);

        ResponseEntity<Appointment> response = appointmentController.editAppointment(appointment);

        assertEquals("General Checkup", response.getBody().getDescription());
        verify(appointmentService).editAppointment(appointment);
    }
}
