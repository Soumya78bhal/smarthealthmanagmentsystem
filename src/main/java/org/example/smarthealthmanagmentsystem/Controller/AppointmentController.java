package org.example.smarthealthmanagmentsystem.Controller;

import org.example.smarthealthmanagmentsystem.Entity.Appointment;
import org.example.smarthealthmanagmentsystem.Service.AppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.List;

@RestController
@RequestMapping("/appointments")
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    @GetMapping("/getAllAppointments")
    public List<Appointment> getAllAppointments() {
        // Get appointments based on user role
        try {
            return appointmentService.getAppointmentsByRole();
        } catch (AccessDeniedException e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/scheduleAppointment")
    public ResponseEntity<Appointment> scheduleAppointment(@RequestBody Appointment appointmentRequest) {
        // Schedule a new appointment
        return ResponseEntity.ok(appointmentService.scheduleAppointment(appointmentRequest));
    }

    @DeleteMapping("/deleteAppointment/{id}")
    public ResponseEntity<String> deleteAppointment(@PathVariable Long id) {
        // Delete appointment by ID with user email for authorization
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        return appointmentService.deleteAppointment(id, userEmail);
    }

    @PutMapping("/editAppointment")
    public ResponseEntity<Appointment> editAppointment(@RequestBody Appointment updatedAppointment) {
        // Edit and update an existing appointment
        Appointment updated = appointmentService.editAppointment(updatedAppointment);
        return ResponseEntity.ok(updated);
    }
}
