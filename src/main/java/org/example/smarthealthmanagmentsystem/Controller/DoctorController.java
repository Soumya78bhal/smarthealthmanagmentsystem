package org.example.smarthealthmanagmentsystem.Controller;

import org.example.smarthealthmanagmentsystem.Entity.Doctor;
import org.example.smarthealthmanagmentsystem.Service.AppointmentService;
import org.example.smarthealthmanagmentsystem.Service.DoctorService;
import org.example.smarthealthmanagmentsystem.Service.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/doctors")
public class DoctorController {

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private PatientService patientService;

    @Autowired
    private DoctorService doctorService;

    @PostMapping("/create")
    public ResponseEntity<Doctor> createDoctor(@RequestBody Doctor reqDoctor) {
        // Create a new doctor profile
        Doctor doctor = doctorService.saveDoctor(reqDoctor);
        return ResponseEntity.ok(doctor);
    }

    @GetMapping("/getAllDoctor")
    public List<Doctor> getAllDoctors() {
        // Get list of all doctors
        return doctorService.getAllDoctors();
    }

    @GetMapping("/me")
    public ResponseEntity<Doctor> getMyDoctorProfile() {
        // Get currently authenticated doctor's profile
        return ResponseEntity.ok(doctorService.getCurrentDoctorDetails());
    }
}
