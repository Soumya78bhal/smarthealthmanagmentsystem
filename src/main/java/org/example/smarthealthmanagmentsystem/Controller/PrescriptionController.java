package org.example.smarthealthmanagmentsystem.Controller;

import java.util.List;

import org.example.smarthealthmanagmentsystem.Entity.Prescription;
import org.example.smarthealthmanagmentsystem.Service.PrescriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/prescription")
public class PrescriptionController {

    @Autowired
    private PrescriptionService prescriptionService;

    // Add a new prescription (Doctor only)
    @PostMapping("/addPrescription")
    public ResponseEntity<String> addPrescription(@RequestBody Prescription prescription) {
        return prescriptionService.addPrescription(prescription);
    }

    // Get prescriptions for the current user (Doctor sees own patients, Patient sees own)
    @GetMapping("/getAllPrescriptions")
    public ResponseEntity<List<Prescription>> getMyPrescriptions() {
        List<Prescription> prescriptions = prescriptionService.getPrescriptionsForCurrentUser();
        return ResponseEntity.ok(prescriptions);
    }

    // Edit an existing prescription
    @PutMapping("/edit")
    public ResponseEntity<Prescription> editPrescription(@RequestBody Prescription updatedPrescription) {
        Prescription updated = prescriptionService.editPrescription(updatedPrescription);
        return ResponseEntity.ok(updated);
    }
}
