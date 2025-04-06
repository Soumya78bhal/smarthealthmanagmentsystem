package org.example.smarthealthmanagmentsystem.Service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;

import org.example.smarthealthmanagmentsystem.Entity.Doctor;
import org.example.smarthealthmanagmentsystem.Entity.MedicalRecord;
import org.example.smarthealthmanagmentsystem.Entity.Patient;
import org.example.smarthealthmanagmentsystem.Entity.User;
import org.example.smarthealthmanagmentsystem.ExceptionHandler.FileStorageException;
import org.example.smarthealthmanagmentsystem.ExceptionHandler.ResourceNotFoundException;
import org.example.smarthealthmanagmentsystem.ExceptionHandler.UnauthorizedAccessException;
import org.example.smarthealthmanagmentsystem.Repository.DoctorRepository;
import org.example.smarthealthmanagmentsystem.Repository.MedicalRecordRepository;
import org.example.smarthealthmanagmentsystem.Repository.PatientRepository;
import org.example.smarthealthmanagmentsystem.Repository.PrescriptionRepository;
import org.example.smarthealthmanagmentsystem.Repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class MedicalRecordService {

    private static final Logger logger = LoggerFactory.getLogger(MedicalRecordService.class);

    @Autowired
    private MedicalRecordRepository medicalRecordRepository;
    @Autowired
    private PrescriptionRepository prescriptionRepository;
    @Autowired
    private DoctorRepository doctorRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PatientRepository patientRepository;

    // Define the directory to store uploaded medical documents
    private final Path storageLocation = Paths.get("uploads").toAbsolutePath().normalize();

    // Ensure the upload directory exists
    public MedicalRecordService() throws IOException {
        Files.createDirectories(storageLocation);
    }

    // Fetch all medical records based on user role
    public List<MedicalRecord> getAllMedicalRecords() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        boolean isPatient = user.getRoles().stream().anyMatch(role -> role.getName().equals("ROLE_PATIENT"));
        boolean isDoctorOrAdmin = user.getRoles().stream().anyMatch(role
                -> role.getName().equals("ROLE_DOCTOR") || role.getName().equals("ROLE_ADMIN"));

        List<MedicalRecord> records;

        if (isPatient) {
            // Patient can only view their own records
            Patient patient = patientRepository.findByUser(user)
                    .orElseThrow(() -> new ResourceNotFoundException("Patient not found for user: " + email));
            records = medicalRecordRepository.findByPatient(patient);
        } else if (isDoctorOrAdmin) {
            // Doctor and Admin can view all records
            records = medicalRecordRepository.findAll();
        } else {
            throw new UnauthorizedAccessException("Access denied.");
        }

        return records;
    }

    // Save medical record with attached document
    public MedicalRecord saveMedicalRecordWithFile(MedicalRecord medicalRecord, MultipartFile file) {
        try {
            // Generate unique filename and store file
            String filename = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path targetLocation = storageLocation.resolve(filename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // Set file name and timestamps
            medicalRecord.setMedicalDocuments(filename);
            medicalRecord.setCreatedAt(LocalDateTime.now());
            medicalRecord.setUpdatedAt(LocalDateTime.now());

            // Validate doctor and patient existence
            Doctor doctor = doctorRepository.findById(medicalRecord.getDoctor().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with ID: " + medicalRecord.getDoctor().getId()));
            Patient patient = patientRepository.findById(medicalRecord.getPatient().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Patient not found with ID: " + medicalRecord.getPatient().getId()));

            // Set references and save
            medicalRecord.setDoctor(doctor);
            medicalRecord.setPatient(patient);

            return medicalRecordRepository.save(medicalRecord);

        } catch (IOException ex) {
            logger.error("File storage failed", ex);
            throw new FileStorageException("Failed to store file", ex);
        }
    }

    // Retrieve medical record and return attached file as a response
    public ResponseEntity<?> getMedicalRecordWithFileById(Long id) {
        MedicalRecord record = medicalRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medical Record not found with ID: " + id));

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        boolean isPatient = user.getRoles().stream().anyMatch(role -> role.getName().equals("ROLE_PATIENT"));

        // If user is patient, validate access to their own record
        if (isPatient) {
            Patient patient = patientRepository.findByUser(user)
                    .orElseThrow(() -> new ResourceNotFoundException("Patient not found for user: " + email));

            if (!record.getPatient().getId().equals(patient.getId())) {
                throw new UnauthorizedAccessException("You are not authorized to access this record.");
            }
        }

        try {
            // Load file from storage
            Path filePath = storageLocation.resolve(record.getMedicalDocuments()).normalize();
            Resource file = new UrlResource(filePath.toUri());

            if (!file.exists() || !file.isReadable()) {
                throw new FileStorageException("File not found or not readable: " + record.getMedicalDocuments());
            }

            // Return file as download response
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + file.getFilename() + "\"")
                    .body(file);

        } catch (MalformedURLException e) {
            throw new FileStorageException("Invalid file path: " + record.getMedicalDocuments(), e);
        }
    }
}
