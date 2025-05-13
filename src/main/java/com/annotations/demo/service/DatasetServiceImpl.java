package com.annotations.demo.service;

import com.annotations.demo.entity.ClassPossible;
import com.annotations.demo.entity.CoupleText;
import com.annotations.demo.entity.Dataset;
import com.annotations.demo.repository.ClassPossibleRepository;
import com.annotations.demo.repository.CoupleTextRepository;
import com.annotations.demo.repository.DatasetRepository;
import com.annotations.demo.service.DatasetService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class DatasetServiceImpl implements DatasetService {

    private static final String UPLOAD_DIR = "uploads/datasets";

    @Autowired
    private final DatasetRepository datasetRepository;
    @Autowired
    private final ClassPossibleRepository classPossibleRepository;
    @Autowired
    private CoupleTextRepository coupleTextRepository;

    public DatasetServiceImpl(DatasetRepository datasetRepository, ClassPossibleRepository classPossibleRepository) {
        this.datasetRepository = datasetRepository;
        this.classPossibleRepository = classPossibleRepository;
    }

    @Override
    public List<Dataset> findAllDatasets() {
        return datasetRepository.findAll();
    }
    @Override
    public Dataset findDatasetByName(String name) {
        return datasetRepository.findByName(name);
    }
    @Override
    public Dataset findDatasetById(Long id) {
        return datasetRepository.findById(id).orElse(null);
    }


    @Override
    public void ParseDataset(Dataset dataset) {
        final int MAX_ROWS = 1000;
        final int BATCH_SIZE = 25;

        String filename = dataset.getFilePath();
        if (filename == null || filename.isEmpty()) {
            throw new IllegalArgumentException("Dataset has no associated file");
        }

        Path filePath = Paths.get(filename);

        if (!Files.exists(filePath)) {
            File resourceFile = new File(filename);
            if (resourceFile.exists()) {
                filePath = resourceFile.toPath();
            } else {
                throw new RuntimeException("File not found: " + filePath.toString());
            }
        }

        try (InputStream fileInputStream = new FileInputStream(filePath.toFile());
             Workbook workbook = WorkbookFactory.create(fileInputStream)) {

            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            int rowCount = 0;
            List<CoupleText> batch = new ArrayList<>();

            // Skip header
            if (rowIterator.hasNext()) rowIterator.next();

            while (rowIterator.hasNext() && rowCount < MAX_ROWS) {
                Row row = rowIterator.next();
                Cell text1Cell = row.getCell(0);
                Cell text2Cell = row.getCell(1);

                if (text1Cell == null || text2Cell == null) continue;

                String text1 = text1Cell.getStringCellValue().trim();
                String text2 = text2Cell.getStringCellValue().trim();

                CoupleText couple = new CoupleText();
                couple.setText_1(text1);
                couple.setText_2(text2);
                couple.setDataset(dataset);

                batch.add(couple);
                rowCount++;

                if (batch.size() == BATCH_SIZE) {
                    coupleTextRepository.saveAll(batch);
                    batch.clear();
                }
            }

            // Save remaining rows if any
            if (!batch.isEmpty()) {
                coupleTextRepository.saveAll(batch);
            }

        } catch (IOException e) {
            throw new RuntimeException("Error reading Excel file", e);
        }
    }



    @Override
    @Transactional
    public Dataset createDataset(String name, String description, MultipartFile file, String classesRaw) throws IOException {
        Dataset dataset = new Dataset();
        dataset.setName(name);
        dataset.setDescription(description);

        if (file != null && !file.isEmpty()) {
            File uploadDirFile = new File(UPLOAD_DIR);
            if (!uploadDirFile.exists()) {
                uploadDirFile.mkdirs();
            }

            // Generate a unique filename to avoid collisions
            String originalFilename = file.getOriginalFilename();
            String uniqueFilename = UUID.randomUUID() + "_" + originalFilename;

            // Create the complete file path
            Path targetLocation = Paths.get(UPLOAD_DIR, uniqueFilename).toAbsolutePath();

            // Actually save the file to disk
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // Store the absolute path in the dataset
            dataset.setFilePath(targetLocation.toString());
            dataset.setFileType(file.getContentType());
        }

        // Handle classes
        Set<ClassPossible> classSet = Arrays.stream(classesRaw.split(";"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(className -> {
                    ClassPossible cp = new ClassPossible();
                    cp.setTextClass(className);
                    cp.setDataset(dataset);
                    return cp;
                })
                .collect(Collectors.toSet());

        dataset.setClassesPossibles(classSet);

        // Save the dataset to get an ID assigned
        return datasetRepository.save(dataset);
    }


    @Override
    public void SaveDataset(Dataset dataset) {
        datasetRepository.save(dataset);
    }

    @Override
    public void deleteDataset(Long id) {
        datasetRepository.deleteById(id);
    }

    @Override
    public long countDatasets() {
        return datasetRepository.count();
    }



}