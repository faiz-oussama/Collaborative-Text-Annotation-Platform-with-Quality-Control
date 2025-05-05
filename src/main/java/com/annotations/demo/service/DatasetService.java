package com.annotations.demo.service;

import com.annotations.demo.entity.Dataset;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface DatasetService {
    List<Dataset> findAllDatasets();
    Dataset findDatasetByName(String name);
    Dataset findDatasetById(Long id);
    void SaveDataset(Dataset dataset);
    Dataset createDataset(String name, String description, MultipartFile file, String classRaw);
    void deleteDataset(Long id);
}
