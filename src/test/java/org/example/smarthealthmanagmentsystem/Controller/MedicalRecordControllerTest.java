package org.example.smarthealthmanagmentsystem.Controller;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.example.smarthealthmanagmentsystem.Entity.MedicalRecord;
import org.example.smarthealthmanagmentsystem.Service.MedicalRecordService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;

class MedicalRecordControllerTest {

    @InjectMocks
    private MedicalRecordController medicalRecordController;

    @Mock
    private MedicalRecordService medicalRecordService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllMedicalRecords_Success() {
        // Arrange
        List<MedicalRecord> records = List.of(new MedicalRecord(), new MedicalRecord());
        when(medicalRecordService.getAllMedicalRecords()).thenReturn(records);

        // Act
        ResponseEntity<List<MedicalRecord>> response = medicalRecordController.getAllMedicalRecords();

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getBody()).hasSize(2);
    }

    @Test
    void testUploadMedicalRecord_Success() throws Exception {
        // Arrange
        MedicalRecord inputRecord = new MedicalRecord();
        inputRecord.setId(1L);
        inputRecord.setMedicalDocuments("file.pdf");

        String medicalRecordJson = new ObjectMapper().writeValueAsString(inputRecord);

        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("file.pdf");
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream("dummy content".getBytes()));

        // Mock service behavior
        when(medicalRecordService.saveMedicalRecordWithFile(any(MedicalRecord.class), any(MultipartFile.class)))
                .thenReturn(inputRecord);

        // Act
        ResponseEntity<MedicalRecord> response = medicalRecordController.uploadMedicalRecord(file, medicalRecordJson);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getBody()).isEqualTo(inputRecord);
    }

    @Test
    void testGetMedicalRecordWithFile_Success() throws IOException {
        // Arrange
        Long id = 1L;
        ByteArrayResource mockFile = new ByteArrayResource("dummy data".getBytes()) {
            @Override
            public String getFilename() {
                return "test.pdf";
            }
        };

        // IMPORTANT: Declare ResponseEntity with explicit type
        ResponseEntity<ByteArrayResource> mockResponse = ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(mockFile);

        // Fix: Use explicit type when mocking
        when(medicalRecordService.getMedicalRecordWithFileById(id))
                .thenReturn((ResponseEntity) mockResponse);

        // Act
        ResponseEntity<?> response = medicalRecordController.getMedicalRecordWithFile(id);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getBody()).isInstanceOf(ByteArrayResource.class);
        assertThat(((ByteArrayResource) response.getBody()).getFilename()).isEqualTo("test.pdf");
    }
}
