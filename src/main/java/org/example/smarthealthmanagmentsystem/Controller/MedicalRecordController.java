package org.example.smarthealthmanagmentsystem.Controller;

import java.io.IOException;
import java.util.List;

import org.example.smarthealthmanagmentsystem.Entity.MedicalRecord;
import org.example.smarthealthmanagmentsystem.Service.MedicalRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/medicalRecord")
public class MedicalRecordController {

    @Autowired
    private MedicalRecordService medicalRecordService;

    // Upload a medical record along with a file
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MedicalRecord> uploadMedicalRecord(
            @RequestPart("file") MultipartFile file,
            @RequestPart("medicalRecord") String medicalRecordJson
    ) throws IOException {

        MedicalRecord medicalRecord = new ObjectMapper().readValue(medicalRecordJson, MedicalRecord.class);
        return ResponseEntity.ok(medicalRecordService.saveMedicalRecordWithFile(medicalRecord, file));
    }

    // Get all medical records based on user role
    @GetMapping("/getAllRecord")
    public ResponseEntity<List<MedicalRecord>> getAllMedicalRecords() {
        return ResponseEntity.ok(medicalRecordService.getAllMedicalRecords());

    }

    // Get medical record by ID (includes file if permitted)
    @GetMapping("getMedicalRecordFileById/{id}")
    public ResponseEntity<?> getMedicalRecordWithFile(@PathVariable Long id) {
        return medicalRecordService.getMedicalRecordWithFileById(id);
    }

}
