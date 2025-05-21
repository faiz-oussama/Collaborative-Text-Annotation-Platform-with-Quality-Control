
package com.annotations.demo.controller;

import com.annotations.demo.entity.Annotateur;
import com.annotations.demo.entity.Annotation;
import com.annotations.demo.entity.Dataset;
import com.annotations.demo.service.*;
import com.annotations.demo.service.interfaces.AnnotationService;
import com.annotations.demo.service.interfaces.DatasetService;
import com.annotations.demo.service.interfaces.TaskService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVWriter;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Date;

@Controller
@RequestMapping("/admin")
public class StatisticsController {
    private final TaskService taskService;
    private final AnnotateurService annotateurService;
    private final DatasetService datasetService;
    private final AnnotationService annotationService;
    private final UserService userService;

    @Autowired
    public StatisticsController(TaskService taskService,
                                AnnotateurService annotateurService,
                                DatasetService datasetService,
                                AnnotationService annotationService, UserService userService, InterAnnotatorAgreement interAnnotatorAgreement) {
        this.taskService = taskService;
        this.annotateurService = annotateurService;
        this.datasetService = datasetService;
        this.annotationService = annotationService;
        this.userService = userService;
    }

    @GetMapping("/overview")
    public String showStatistics(Model model) throws JsonProcessingException {
        // 1. Gather basic statistics
        long totalAnnotations = annotationService.countTotalAnnotations();
        long activeTasks = taskService.countActiveTasks();
        long totalDatasets = datasetService.countDatasets();
        long totalAnnotateurs = annotateurService.countActiveAnnotateurs();

        model.addAttribute("totalAnnotations", totalAnnotations);
        model.addAttribute("activeTasks", activeTasks);
        model.addAttribute("totalDatasets", totalDatasets);
        model.addAttribute("totalAnnotateurs", totalAnnotateurs);

        // 2. Dataset progress data
        Map<String, Object> datasetsProgressData = new HashMap<>();
        ObjectMapper objectMapper = new ObjectMapper();
        List<String> datasetNames = new ArrayList<>();
        List<Integer> totalCouples = new ArrayList<>();
        List<Integer> annotatedCouples = new ArrayList<>();
        List<Dataset> datasets = datasetService.findAllDatasets();

        for (Dataset dataset : datasetService.findAllDatasets()) {
            datasetNames.add(dataset.getName());
            totalCouples.add(dataset.getCoupleTexts().size());
            annotatedCouples.add(annotationService.countAnnotationsByDataset(dataset.getId()));
        }

        datasetsProgressData.put("labels", datasetNames);
        datasetsProgressData.put("totalCouples", totalCouples);
        datasetsProgressData.put("annotatedCouples", annotatedCouples);
        //pass the map as a JSON for the script retrieval
        String datasetsProgressJson = objectMapper.writeValueAsString(datasetsProgressData);
        model.addAttribute("datasetsProgressJson", datasetsProgressJson);
        model.addAttribute("datasets",  datasets);


        // 3. Class distribution data
        Map<String, Integer> classDistributionMap = new HashMap<>();

        for (Dataset dataset : datasetService.findAllDatasets()) {
            for (Annotation annotation : annotationService.findAllAnnotationsByDataset(dataset.getId())) {
                String className = annotation.getChosenClass();
                classDistributionMap.put(className, classDistributionMap.getOrDefault(className, 0) + 1);
            }
        }

        // Convert to labels and data arrays
        List<String> labels = new ArrayList<>(classDistributionMap.keySet());
        List<Integer> data = labels.stream()
                .map(classDistributionMap::get)
                .collect(Collectors.toList());

        // Prepare JSON structure
        Map<String, Object> classDistributionChartData = new HashMap<>();
        classDistributionChartData.put("labels", labels);
        classDistributionChartData.put("data", data);

        String classDistributionJson = objectMapper.writeValueAsString(classDistributionChartData);
        model.addAttribute("classDistributionJson", classDistributionJson);



        // 4. Annotator performance data
        List<Annotateur> annotators = annotateurService.findAllActive();
        List<String> performanceLabels = new ArrayList<>();
        List<Integer> completionRates = new ArrayList<>();

        for (Annotateur annotator : annotators) {
            Long totalAssigned = taskService.countAssignedCouples(annotator);
            System.out.println("totalAssigned: " + totalAssigned);
            int totalAnnotated = annotationService.findAllAnnotationsByUser(annotator).size();
            System.out.println("totalAnnotated: " + totalAnnotated);
            int completionRate = (totalAssigned == 0) ? 0 : (int) ((totalAnnotated / (double) totalAssigned) * 100);

            performanceLabels.add(annotator.getNom());
            completionRates.add(completionRate);
        }

        Map<String, Object> performanceChartData = new HashMap<>();
        performanceChartData.put("labels", performanceLabels);
        performanceChartData.put("completionRates", completionRates);


        String performanceMapJson = objectMapper.writeValueAsString(performanceChartData);
        model.addAttribute("annotatorPerformanceData", performanceMapJson);

        //current user name
        String currentUserName = StringUtils.capitalize(userService.getCurrentUserName());
        model.addAttribute("currentUserName", currentUserName);

        return "admin/statistics_management/overview";
    }
    @GetMapping("/download-annotations-excel/{datasetId}")
    public void downloadAnnotationsExcel(@PathVariable Long datasetId, HttpServletResponse response) throws IOException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormat.format(new Date());
        
        // Get dataset name for the filename
        Dataset dataset = datasetService.findDatasetById(datasetId);
        String datasetName = dataset != null ? dataset.getName() : "unknown";
        
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=annotations_" + datasetName + "_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        
        List<Annotation> annotationList = annotationService.findAllAnnotationsByDataset(datasetId);
        
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Annotations");
        
        // Create header row
        Row headerRow = sheet.createRow(0);
        Cell headerCell1 = headerRow.createCell(0);
        headerCell1.setCellValue("Label");
        Cell headerCell2 = headerRow.createCell(1);
        headerCell2.setCellValue("Text 1");
        Cell headerCell3 = headerRow.createCell(2);
        headerCell3.setCellValue("Text 2");
        
        // Create data rows
        int rowNum = 1;
        for (Annotation annotation : annotationList) {
            Row row = sheet.createRow(rowNum++);
            
            Cell cell1 = row.createCell(0);
            cell1.setCellValue(annotation.getChosenClass());
            
            Cell cell2 = row.createCell(1);
            cell2.setCellValue(annotation.getCoupleText().getText_1());
            
            Cell cell3 = row.createCell(2);
            cell3.setCellValue(annotation.getCoupleText().getText_2());
        }
        
        // Auto-size columns
        for (int i = 0; i < 3; i++) {
            sheet.autoSizeColumn(i);
        }
        
        // Write to response
        workbook.write(response.getOutputStream());
        workbook.close();
    }

    /**
     * Export annotations for a specific dataset to CSV format
     */
    @GetMapping("/download-annotations-csv/{datasetId}")
    public void downloadAnnotationsCsv(@PathVariable Long datasetId, HttpServletResponse response) throws IOException {
        response.setContentType("text/csv");
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormat.format(new Date());
        
        // Get dataset name for the filename
        Dataset dataset = datasetService.findDatasetById(datasetId);
        String datasetName = dataset != null ? dataset.getName() : "unknown";
        
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=annotations_" + datasetName + "_" + currentDateTime + ".csv";
        response.setHeader(headerKey, headerValue);
        
        List<Annotation> annotationList = annotationService.findAllAnnotationsByDataset(datasetId);
        
        // Create CSV writer
        CSVWriter csvWriter = new CSVWriter(new OutputStreamWriter(response.getOutputStream()));
        
        // Write header
        String[] header = {"Label", "Text 1", "Text 2"};
        csvWriter.writeNext(header);
        
        // Write data rows
        for (Annotation annotation : annotationList) {
            String[] data = {
                annotation.getChosenClass(),
                annotation.getCoupleText().getText_1(),
                annotation.getCoupleText().getText_2()
            };
            csvWriter.writeNext(data);
        }
        
        csvWriter.close();
    }

}
