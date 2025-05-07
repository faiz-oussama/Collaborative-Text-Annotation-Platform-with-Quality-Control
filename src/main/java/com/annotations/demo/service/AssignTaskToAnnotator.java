package com.annotations.demo.service;


import com.annotations.demo.entity.Annotateur;
import com.annotations.demo.entity.CoupleText;
import com.annotations.demo.entity.Dataset;
import com.annotations.demo.entity.Task;
import com.annotations.demo.repository.DatasetRepository;
import com.annotations.demo.repository.TaskRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;


@Service
@Transactional
public class AssignTaskToAnnotator {

    private final CoupleTextServiceImpl coupleTextServiceImpl;
    private final TaskRepository taskeRepository;

    public AssignTaskToAnnotator(CoupleTextServiceImpl coupleTextServiceImpl, TaskRepository taskeRepository) {
        this.coupleTextServiceImpl = coupleTextServiceImpl;
        this.taskeRepository = taskeRepository;
    }


    public void assignTaskToAnnotator(List<Annotateur> annotateurList, Dataset dataset, Date deadline) {

        //getting parameters I will be using
        Long datasetId = dataset.getId();
        List<CoupleText> coupleTextList = coupleTextServiceImpl.findAllCoupleTextsByDatasetId(datasetId);

        // Shuffle to ensure randomness
        Collections.shuffle(coupleTextList);

        int total = coupleTextList.size();
        int annotatorCount = annotateurList.size();
        int batchSize = total / annotatorCount;
        int remaining = total % annotatorCount;

        int index = 0;
        for (Annotateur annotator : annotateurList) {
            int currentBatchSize = batchSize + (remaining-- > 0 ? 1 : 0);

            List<CoupleText> subList = coupleTextList.subList(index, index + currentBatchSize);

            // Create new task for the annotator
            Task tache = new Task();
            tache.setCouples(new ArrayList<>(subList));
            tache.setAnnotateur(annotator);
            tache.setDataset(dataset);
            tache.setDateLimite(deadline);// If needed
            taskeRepository.save(tache);

            index += currentBatchSize;
        }
    }
}
