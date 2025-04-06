package org.example.smarthealthmanagmentsystem.Repository;

import org.example.smarthealthmanagmentsystem.Entity.MedicalRecord;
import org.example.smarthealthmanagmentsystem.Entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {
    List<MedicalRecord> findByPatient(Patient patient);
}
