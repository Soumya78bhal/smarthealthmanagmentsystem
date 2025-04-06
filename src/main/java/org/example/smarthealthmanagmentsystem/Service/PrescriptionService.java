package org.example.smarthealthmanagmentsystem.Service;

import java.time.LocalDateTime;
import java.util.List;

import org.example.smarthealthmanagmentsystem.Entity.Doctor;
import org.example.smarthealthmanagmentsystem.Entity.Patient;
import org.example.smarthealthmanagmentsystem.Entity.Prescription;
import org.example.smarthealthmanagmentsystem.Entity.User;
import org.example.smarthealthmanagmentsystem.ExceptionHandler.ResourceNotFoundException;
import org.example.smarthealthmanagmentsystem.ExceptionHandler.UnauthorizedAccessException;
import org.example.smarthealthmanagmentsystem.Repository.AppointmentRepository;
import org.example.smarthealthmanagmentsystem.Repository.DoctorRepository;
import org.example.smarthealthmanagmentsystem.Repository.PatientRepository;
import org.example.smarthealthmanagmentsystem.Repository.PrescriptionRepository;
import org.example.smarthealthmanagmentsystem.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class PrescriptionService {

    @Autowired
    private PrescriptionRepository prescriptionRepository;
    @Autowired
    private AppointmentRepository appointmentRepository;
    @Autowired
    private DoctorRepository doctorRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PatientRepository patientRepository;

    public ResponseEntity<String> addPrescription(Prescription prescription) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        // Fetch authenticated user
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedAccessException("User not authenticated"));

        // Ensure user is a doctor
        Doctor doctor = doctorRepository.findByUser(user)
                .orElseThrow(() -> new UnauthorizedAccessException("Only doctors can prescribe"));

        // Validate patient ID
        Long patientId = prescription.getPatient() != null ? prescription.getPatient().getId() : null;
        if (patientId == null) {
            throw new ResourceNotFoundException("Patient ID is required in the prescription");
        }

        // Fetch patient entity
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with ID: " + patientId));

        // Set related entities and date
        prescription.setDoctor(doctor);
        prescription.setPatient(patient);
        prescription.setPrescribedDate(LocalDateTime.now());

        prescriptionRepository.save(prescription);

        return ResponseEntity.ok("Prescription saved successfully.");
    }

    public List<Prescription> getPrescriptionsForCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        // Fetch authenticated user
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        // Role-based prescription access
        boolean isDoctor = user.getRoles().stream().anyMatch(role -> role.getName().equals("ROLE_DOCTOR"));
        boolean isPatient = user.getRoles().stream().anyMatch(role -> role.getName().equals("ROLE_PATIENT"));
        boolean isAdmin = user.getRoles().stream().anyMatch(role -> role.getName().equals("ROLE_ADMIN"));

        if (isDoctor) {
            Doctor doctor = doctorRepository.findByUser(user)
                    .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));
            return prescriptionRepository.findByDoctor(doctor);
        } else if (isPatient) {
            Patient patient = patientRepository.findByUser(user)
                    .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));
            return prescriptionRepository.findByPatient(patient);
        } else if (isAdmin) {
            return prescriptionRepository.findAll();
        } else {
            throw new UnauthorizedAccessException("You are not authorized to view prescriptions");
        }
    }

    public Prescription editPrescription(Prescription updatedPrescription) {
        String doctorEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        // Fetch doctor by authenticated email
        Doctor doctor = doctorRepository.findByUserEmail(doctorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with email: " + doctorEmail));

        // Fetch prescription to edit
        Prescription existing = prescriptionRepository.findById(updatedPrescription.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Prescription not found with ID: " + updatedPrescription.getId()));

        // Ensure the prescription belongs to the logged-in doctor
        if (existing.getDoctor().getId() != (doctor.getId())) {
            throw new UnauthorizedAccessException("You are not authorized to edit this prescription");
        }

        // Update editable fields
        existing.setDosageInstructions(updatedPrescription.getDosageInstructions());
        existing.setMedicationList(updatedPrescription.getMedicationList());
        existing.setPrescribedDate(updatedPrescription.getPrescribedDate());
        existing.setValidUntil(updatedPrescription.getValidUntil());

        return prescriptionRepository.save(existing);
    }
}
