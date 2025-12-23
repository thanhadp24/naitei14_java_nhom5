package vn.sun.public_service_manager.service;

import vn.sun.public_service_manager.entity.ServiceType;
import vn.sun.public_service_manager.repository.ServiceTypeRepository;
import vn.sun.public_service_manager.repository.ServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ServiceTypeService {

    private final ServiceTypeRepository serviceTypeRepository;
    private final ServiceRepository serviceRepository;

    public List<ServiceType> getAllServiceTypes() {
        return serviceTypeRepository.findAll();
    }

    public List<ServiceType> getServiceTypesByDepartmentId(Long departmentId) {
        return serviceRepository.findDistinctServiceTypesByDepartmentId(departmentId);
    }

    public Optional<ServiceType> getServiceTypeById(Long id) {
        return serviceTypeRepository.findById(id);
    }

    @Transactional
    public ServiceType saveServiceType(ServiceType serviceType) {

        String category = serviceType.getCategory().trim();
        serviceType.setCategory(category);
        Optional<ServiceType> existingType = serviceTypeRepository.findByCategoryIgnoreCase(category);

        if (existingType.isPresent()) {
            Long existingId = existingType.get().getId();
            if (serviceType.getId() == null || !existingId.equals(serviceType.getId())) {
                throw new DataIntegrityViolationException("Category '" + category + "' already exists.");
            }
        }
        try {
            return serviceTypeRepository.save(serviceType);
        } catch (DataIntegrityViolationException e) {
            throw new DataIntegrityViolationException("Failed to save due to database constraint violation.");
        }
    }

    @Transactional
    public void deleteServiceType(Long id) throws IllegalArgumentException, IllegalStateException {

        if (!serviceTypeRepository.existsById(id)) {
            throw new IllegalArgumentException("ServiceType ID " + id + " not found.");
        }
        long servicesCount = serviceRepository.countByServiceTypeId(id);

        if (servicesCount > 0) {
            throw new IllegalStateException("Cannot delete ServiceType ID " + id +
                    " because it is currently linked to " + servicesCount + " service(s).");
        }

        serviceTypeRepository.deleteById(id);
    }

    /**
     * Export all service types to CSV
     */
    public void exportServiceTypesToCsv(Writer writer) {
        List<ServiceType> serviceTypes = serviceTypeRepository.findAll();

        try {
            // Write UTF-8 BOM for Excel recognition
            writer.write('\ufeff');

            // Header
            writer.write("ID,Category,Description\n");

            // Write data
            for (ServiceType serviceType : serviceTypes) {
                writer.write(String.format("%d,%s,%s\n",
                        serviceType.getId(),
                        escapeCSV(serviceType.getCategory()),
                        escapeCSV(serviceType.getDescription())));
            }

            writer.flush();

        } catch (IOException e) {
            throw new RuntimeException("Lỗi khi xuất CSV: " + e.getMessage(), e);
        }
    }

    /**
     * Import service types from CSV file
     */
    @Transactional
    public Map<String, Object> importServiceTypesFromCsv(MultipartFile file) throws IOException {
        List<String> errors = new ArrayList<>();
        List<String> success = new ArrayList<>();
        int rowNumber = 0;
        int totalRows = 0;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            // Read first line as header
            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new RuntimeException("File CSV rỗng!");
            }

            // Remove BOM if present
            if (headerLine.startsWith("\uFEFF")) {
                headerLine = headerLine.substring(1);
            }

            // Validate header
            String expectedHeader = "category,description";
            String normalizedHeader = headerLine.toLowerCase().trim().replaceAll("\\s+", "");
            if (!normalizedHeader.equals(expectedHeader.toLowerCase())) {
                throw new RuntimeException(
                        "File CSV không đúng định dạng! Cần có: " + expectedHeader + " nhưng nhận được: " + headerLine);
            }

            // Read data lines
            String line;
            while ((line = reader.readLine()) != null) {
                rowNumber++;
                totalRows++;

                if (line.trim().isEmpty()) {
                    continue; // Skip empty lines
                }

                try {
                    // Parse CSV line (handle quoted values)
                    String[] values = parseCSVLine(line);

                    if (values.length < 1) {
                        errors.add("Dòng " + rowNumber + ": Thiếu dữ liệu bắt buộc (category)");
                        continue;
                    }

                    String category = values[0].trim();
                    String description = values.length > 1 ? values[1].trim() : "";

                    // Validate required fields
                    if (category.isEmpty()) {
                        errors.add("Dòng " + rowNumber + ": Category không được để trống");
                        continue;
                    }

                    if (category.length() > 150) {
                        errors.add("Dòng " + rowNumber + ": Category không được vượt quá 150 ký tự");
                        continue;
                    }

                    // Check duplicate category
                    if (serviceTypeRepository.findByCategoryIgnoreCase(category).isPresent()) {
                        errors.add("Dòng " + rowNumber + ": Category '" + category + "' đã tồn tại");
                        continue;
                    }

                    // Create new ServiceType
                    ServiceType serviceType = new ServiceType();
                    serviceType.setCategory(category);
                    serviceType.setDescription(description);

                    // Save service type
                    serviceTypeRepository.save(serviceType);
                    success.add("Dòng " + rowNumber + ": Import service type '" + category + "' thành công");

                } catch (Exception e) {
                    errors.add("Dòng " + rowNumber + ": " + e.getMessage());
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi đọc file CSV: " + e.getMessage(), e);
        }

        // Prepare response
        Map<String, Object> response = new HashMap<>();
        response.put("total", totalRows);
        response.put("success", success.size());
        response.put("failed", errors.size());
        response.put("errors", errors);
        response.put("successMessages", success);

        return response;
    }

    /**
     * Helper method to parse CSV line with quoted values
     */
    private String[] parseCSVLine(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);

            if (ch == '"') {
                // Check for escaped quotes ("")
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++; // Skip next quote
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (ch == ',' && !inQuotes) {
                result.add(current.toString().trim());
                current = new StringBuilder();
            } else {
                current.append(ch);
            }
        }
        result.add(current.toString().trim());

        return result.toArray(new String[0]);
    }

    /**
     * Helper method to escape CSV values
     */
    private String escapeCSV(String value) {
        if (value == null) {
            return "";
        }
        // If value contains comma, newline, or quotes, wrap in quotes and escape quotes
        if (value.contains(",") || value.contains("\n") || value.contains("\"")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
