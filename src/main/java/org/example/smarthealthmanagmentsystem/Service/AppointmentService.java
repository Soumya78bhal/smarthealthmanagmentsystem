package org.example.smarthealthmanagmentsystem.Service;

import java.nio.file.AccessDeniedException;
import java.util.List;

import org.example.smarthealthmanagmentsystem.Entity.Appointment;
import org.example.smarthealthmanagmentsystem.Entity.AppointmentStatus;
import org.example.smarthealthmanagmentsystem.Entity.Doctor;
import org.example.smarthealthmanagmentsystem.Entity.Patient;
import org.example.smarthealthmanagmentsystem.Entity.User;
import org.example.smarthealthmanagmentsystem.ExceptionHandler.IdNotFoundException;
import org.example.smarthealthmanagmentsystem.ExceptionHandler.UnauthorizedAccessException;
import org.example.smarthealthmanagmentsystem.Repository.AppointmentRepository;
import org.example.smarthealthmanagmentsystem.Repository.DoctorRepository;
import org.example.smarthealthmanagmentsystem.Repository.PatientRepository;
import org.example.smarthealthmanagmentsystem.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AppointmentService {

    @Autowired
    private AppointmentRepository appointmentRepository;
    @Autowired
    private DoctorRepository doctorRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PatientRepository patientRepository;

    // Only Doctor can schedule an appointment
    public Appointment scheduleAppointment(Appointment appointmentRequest) {
        String doctorEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByEmail(doctorEmail)
                .orElseThrow(() -> new IdNotFoundException("Doctor not found"));

        Doctor doctor = doctorRepository.findByUser(user)
                .orElseThrow(() -> new IdNotFoundException("Doctor profile not found"));

        Long patientId = appointmentRequest.getPatient().getId();
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new IdNotFoundException("Patient not found"));

        Appointment appointment = new Appointment();
        appointment.setDoctor(doctor);
        appointment.setPatient(patient);
        appointment.setAppointmentDate(appointmentRequest.getAppointmentDate());
        appointment.setAppointmentStatus(AppointmentStatus.SCHEDULED);
        appointment.setDescription(appointmentRequest.getDescription());

        return appointmentRepository.save(appointment);
    }

    // Admin or the Doctor who created the appointment can delete it
    public ResponseEntity<String> deleteAppointment(Long appointmentId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UnauthorizedAccessException("Unauthorized user"));

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IdNotFoundException("Appointment not found"));

        if (hasRole(user, "ROLE_ADMIN")) {
            appointmentRepository.delete(appointment);
            return ResponseEntity.ok("Appointment deleted by Admin");
        }

        if (hasRole(user, "ROLE_DOCTOR")) {
            Doctor doctor = doctorRepository.findByUser(user)
                    .orElseThrow(() -> new IdNotFoundException("Doctor not found"));
            if (appointment.getDoctor() != null
                    && appointment.getDoctor().getId() == (doctor.getId())) {
                appointmentRepository.delete(appointment);
                return ResponseEntity.ok("Appointment deleted by Doctor");
            } else {
                throw new UnauthorizedAccessException("You can delete only your own appointments");
            }
        }

        throw new UnauthorizedAccessException("You are not authorized to delete appointments");
    }

    // Get appointments based on current user's role
    public List<Appointment> getAppointmentsByRole() throws AccessDeniedException {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IdNotFoundException("User not found"));

        if (hasRole(user, "ROLE_ADMIN")) {
            return appointmentRepository.findAll();
        }

        if (hasRole(user, "ROLE_DOCTOR")) {
            Doctor doctor = doctorRepository.findByUser(user)
                    .orElseThrow(() -> new IdNotFoundException("Doctor not found"));
            return appointmentRepository.findByDoctor(doctor);
        }

        if (hasRole(user, "ROLE_PATIENT")) {
            Patient patient = patientRepository.findByUser(user)
                    .orElseThrow(() -> new IdNotFoundException("Patient not found"));
            return appointmentRepository.findByPatient(patient);
        }

        throw new UnauthorizedAccessException("Unauthorized access to appointments");
    }

    // Admin or Doctor (only their own) can edit appointment
    public Appointment editAppointment(Appointment updatedAppointment) {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        Appointment existingAppointment = appointmentRepository.findById(updatedAppointment.getId())
                .orElseThrow(() -> new IdNotFoundException("Appointment not found"));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IdNotFoundException("User not found"));

        boolean isAdmin = hasRole(user, "ROLE_ADMIN");
        boolean isDoctor = hasRole(user, "ROLE_DOCTOR");

        if (isDoctor) {
            Doctor doctor = doctorRepository.findByUser(user)
                    .orElseThrow(() -> new IdNotFoundException("Doctor not found"));
            if (existingAppointment.getDoctor().getId() != (doctor.getId())) {
                throw new UnauthorizedAccessException("Access denied: Cannot edit appointment of another doctor");
            }
        } else if (!isAdmin) {
            throw new UnauthorizedAccessException("Access denied: Only Admin or Doctor can edit appointment");
        }

        existingAppointment.setAppointmentDate(updatedAppointment.getAppointmentDate());
        existingAppointment.setAppointmentStatus(updatedAppointment.getAppointmentStatus());
        existingAppointment.setDescription(updatedAppointment.getDescription());

        return appointmentRepository.save(existingAppointment);
    }

    // Doctor can get their own appointments
    public List<Appointment> getAppointmentsForCurrentDoctor() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IdNotFoundException("User not found"));

        Doctor doctor = doctorRepository.findByUser(user)
                .orElseThrow(() -> new IdNotFoundException("Doctor not found"));

        return appointmentRepository.findByDoctor(doctor);
    }

    // Utility method to check if user has a specific role
    private boolean hasRole(User user, String roleName) {
        return user.getRoles().stream().anyMatch(r -> r.getName().equals(roleName));
    }
}
