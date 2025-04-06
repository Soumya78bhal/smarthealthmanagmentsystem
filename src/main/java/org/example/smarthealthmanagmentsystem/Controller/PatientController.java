package org.example.smarthealthmanagmentsystem.Controller;

import java.util.List;
import java.util.Optional;

import org.example.smarthealthmanagmentsystem.Entity.Patient;
import org.example.smarthealthmanagmentsystem.Service.PatientService;
import org.example.smarthealthmanagmentsystem.Service.PrescriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/patients")
public class PatientController {

    @Autowired
    private PatientService patientService;

    @Autowired
    private PrescriptionService prescriptionService;

    // Register a new patient
    @PostMapping("/register")
    public ResponseEntity<Patient> registerPatient(@RequestBody Patient patient) {
        Patient savedPatient = patientService.registerPatient(patient);
        return ResponseEntity.ok(savedPatient);
    }

    // Get all patients (admin/doctor only)
    @GetMapping("/getAllPatient")
    public ResponseEntity<List<Patient>> getAllPatient() {
        return ResponseEntity.of(Optional.ofNullable(patientService.getAllPatients()));
    }

    // Get patient by ID
    @GetMapping("/getPatientById/{id}")
    public ResponseEntity<Patient> getPatientById(@PathVariable long id) {
        return ResponseEntity.ok(patientService.getPatientById(id));
    }

    // Get currently authenticated patient's details
    @GetMapping("/getMyDetails")
    public ResponseEntity<Patient> getMyDetails() {
        return ResponseEntity.ok(patientService.getCurrentPatientDetails());
    }

    // Edit patient profile
    @PutMapping("/edit")
    public ResponseEntity<Patient> editPatient(@RequestBody Patient updatedPatient) {
        Patient patient = patientService.editPatient(updatedPatient);
        return ResponseEntity.ok(patient);
    }
}
