package org.example.smarthealthmanagmentsystem.Service;

import org.example.smarthealthmanagmentsystem.Entity.Patient;
import org.example.smarthealthmanagmentsystem.Entity.Role;
import org.example.smarthealthmanagmentsystem.Entity.User;
import org.example.smarthealthmanagmentsystem.ExceptionHandler.EmailAlreadyExistsException;
import org.example.smarthealthmanagmentsystem.ExceptionHandler.IdNotFoundException;
import org.example.smarthealthmanagmentsystem.ExceptionHandler.ResourceNotFoundException;
import org.example.smarthealthmanagmentsystem.Repository.PatientRepository;
import org.example.smarthealthmanagmentsystem.Repository.RoleRepository;
import org.example.smarthealthmanagmentsystem.Repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class PatientService {

    private static final Logger logger = LoggerFactory.getLogger(PatientService.class);

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    // PasswordEncoder should not be both @Autowired and instantiated here
    @Autowired
    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(); // ⚠️ Remove either @Autowired or initialization

    public Patient registerPatient(Patient patient) {
        String email = patient.getUser().getEmail();
        logger.info("Registering new patient with email: {}", email);

        // Check for duplicate email
        if (userRepository.findByEmail(email).isPresent()) {
            logger.warn("Email already exists: {}", email);
            throw new EmailAlreadyExistsException("Email already in use");
        }

        // Fetch patient role from DB
        Role patientRole = roleRepository.findByName("ROLE_PATIENT")
                .orElseThrow(() -> new ResourceNotFoundException("Patient role not found"));

        // Create and encode user details
        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(patient.getUser().getPassword()));
        user.setRoles(Collections.singleton(patientRole));

        user = userRepository.save(user);

        // Assign user to patient and save
        patient.setUser(user);

        logger.info("Patient registered successfully: {}", patient.getName());
        return patientRepository.save(patient);
    }

    public List<Patient> getAllPatients() {
        logger.info("Fetching all patients");
        return (List<Patient>) patientRepository.findAll();
    }

    public Patient getPatientById(long id) {
        logger.info("Fetching patient by ID: {}", id);
        return patientRepository.findById(id)
                .orElseThrow(() -> new IdNotFoundException("Patient not found with ID: " + id));
    }

    public Patient getCurrentPatientDetails() {
        // Get authenticated user's email
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        logger.info("Fetching current patient details for email: {}", email);
        return patientRepository.findByUserEmail(email)
                .orElseThrow(() -> new IdNotFoundException("Current user is not a patient"));
    }

    public Patient editPatient(Patient updatedPatient) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        logger.info("Editing patient with ID: {}", updatedPatient.getId());

        // Ensure current user exists
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Load existing patient by ID
        Patient existingPatient = patientRepository.findById(updatedPatient.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with ID: " + updatedPatient.getId()));

        // Update editable fields
        existingPatient.setName(updatedPatient.getName());
        existingPatient.setDateOfBirth(updatedPatient.getDateOfBirth());
        existingPatient.setGender(updatedPatient.getGender());
        existingPatient.setBloodType(updatedPatient.getBloodType());
        existingPatient.setContact(updatedPatient.getContact());

        logger.info("Patient updated successfully: {}", existingPatient.getName());
        return patientRepository.save(existingPatient);
    }
}
