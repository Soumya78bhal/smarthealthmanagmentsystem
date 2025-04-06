package org.example.smarthealthmanagmentsystem.Repository;

import org.example.smarthealthmanagmentsystem.Entity.Doctor;
import org.example.smarthealthmanagmentsystem.Entity.Patient;
import org.example.smarthealthmanagmentsystem.Entity.Prescription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {
    List<Prescription> findByDoctor(Doctor doctor);

    List<Prescription> findByPatient(Patient patient);
}
