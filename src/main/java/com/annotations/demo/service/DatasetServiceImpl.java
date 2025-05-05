package com.annotations.demo.service;

import com.annotations.demo.entity.ClassPossible;
import com.annotations.demo.entity.CoupleText;
import com.annotations.demo.entity.Dataset;
import com.annotations.demo.repository.ClassPossibleRepository;
import com.annotations.demo.repository.CoupleTextRepository;
import com.annotations.demo.repository.DatasetRepository;
import com.annotations.demo.service.DatasetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class DatasetServiceImpl implements DatasetService {

    @Value("${upload.dir}")
    private String uploadDir;

    @Autowired
    private final DatasetRepository datasetRepository;
    @Autowired
    private final ClassPossibleRepository classPossibleRepository;

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
    @Transactional
    public Dataset createDataset(String name, String description, MultipartFile file, String classesRaw) {
        Dataset dataset = new Dataset();
        dataset.setName(name);
        dataset.setDescription(description);

        // 1. Handle file upload
        if (file != null && !file.isEmpty()) {
            String filename = file.getOriginalFilename();
            dataset.setFilePath("public/" + filename);
            dataset.setFileType(file.getContentType());
        }

        System.out.println(classesRaw);
        // 2. Handle class creation
        Set<ClassPossible> classSet = Arrays.stream(classesRaw.split(";"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(className -> {
                    System.out.println("Parsed class: '" + className + "'");
                    ClassPossible cp = new ClassPossible();
                    cp.setTextClass(className);
                    cp.setDataset(dataset);
                    return cp;
                })
                .collect(Collectors.toSet());

        dataset.setClassesPossibles(classSet);
        System.out.println(classSet);

        return dataset;
    }

    @Override
    public void SaveDataset(Dataset dataset) {
        datasetRepository.save(dataset);
    }

    @Override
    public void deleteDataset(Long id) {
        datasetRepository.deleteById(id);
    }


}
