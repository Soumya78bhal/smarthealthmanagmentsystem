package org.example.smarthealthmanagmentsystem.Service;

import org.example.smarthealthmanagmentsystem.Entity.Doctor;
import org.example.smarthealthmanagmentsystem.Entity.Role;
import org.example.smarthealthmanagmentsystem.Entity.User;
import org.example.smarthealthmanagmentsystem.ExceptionHandler.EmailAlreadyExistsException;
import org.example.smarthealthmanagmentsystem.ExceptionHandler.IdNotFoundException;
import org.example.smarthealthmanagmentsystem.ExceptionHandler.RoleNotFoundException;
import org.example.smarthealthmanagmentsystem.Repository.DoctorRepository;
import org.example.smarthealthmanagmentsystem.Repository.RoleRepository;
import org.example.smarthealthmanagmentsystem.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class DoctorService {

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // Retrieve doctor by ID (if exists)
    public Optional<Doctor> getDoctorById(Long id) {
        return doctorRepository.findById(id);
    }

    // Get currently authenticated doctor's details
    public Doctor getCurrentDoctorDetails() {
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        return doctorRepository.findByUserEmail(name)
                .orElseThrow(() -> new IdNotFoundException("Current user is not a doctor"));
    }

    // Save a new doctor account with ROLE_DOCTOR
    public Doctor saveDoctor(Doctor doctor) {
        String email = doctor.getUser().getEmail();
        String password = doctor.getUser().getPassword();

        if (userRepository.findByEmail(email).isPresent()) {
            throw new EmailAlreadyExistsException("User with this email already exists: " + email);
        }

        Role doctorRole = roleRepository.findByName("ROLE_DOCTOR")
                .orElseThrow(() -> new RoleNotFoundException("Role 'ROLE_DOCTOR' not found"));

        // Create user and encode password
        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRoles(Collections.singleton(doctorRole));
        user = userRepository.save(user);

        // Link user to doctor and save doctor
        doctor.setUser(user);
        return doctorRepository.save(doctor);
    }

    // Get list of all doctors
    public List<Doctor> getAllDoctors() {
        return doctorRepository.findAll();
    }
}
