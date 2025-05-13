package com.annotations.demo.controller;

import com.annotations.demo.entity.Annotateur;
import com.annotations.demo.entity.SpamDetectionResults;
import com.annotations.demo.repository.AnnotateurRepository;
import com.annotations.demo.repository.SpamDetectionResultsRepository;
import com.annotations.demo.service.PythonService;
import com.annotations.demo.service.SpamDetectorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class SpamDetectionController {
    private static final Logger logger = LoggerFactory.getLogger(SpamDetectionController.class);
    
    @Autowired
    private SpamDetectorService spamDetectorService;

    @Autowired
    private AnnotateurRepository annotateurRepository;

    @Autowired
    private SpamDetectionResultsRepository spamResultsRepository;
    
    @Autowired
    private PythonService pythonService;
    
    @Value("${spam.detection.threshold:0.3}")
    private double defaultThreshold;
    
    @GetMapping("/spam/configure")
    public String showConfigurationPage(Model model) {
        model.addAttribute("threshold", defaultThreshold);
        model.addAttribute("annotateurs", annotateurRepository.findAll());
        model.addAttribute("pythonServiceStatus", pythonService.isServiceHealthy());
        return "admin/spam_detection/configure";
    }

    @PostMapping("/spam/detect")
    public String runSpamDetection(
            @RequestParam(required = false, defaultValue = "0") double threshold,
            @RequestParam(required = false, name = "annotateur_ids") List<Long> annotateurIds,
            RedirectAttributes redirectAttributes) {
        
        try {
            logger.info("Starting spam detection with threshold: {}", threshold);
            logger.info("Annotateur IDs provided in request: {}", annotateurIds != null ? annotateurIds : "none");
            
            boolean serviceHealthy = pythonService.isServiceHealthy();
            logger.info("Python service health check result: {}", serviceHealthy ? "HEALTHY" : "UNHEALTHY");
            
            if (!serviceHealthy) {
                logger.warn("Python service is not running, attempting to start it");
                boolean started = pythonService.startPythonService();
                logger.info("Python service start attempt result: {}", started ? "SUCCESS" : "FAILURE");
                
                if (!started) {
                    logger.error("Failed to start Python service, aborting spam detection");
                    redirectAttributes.addFlashAttribute("error", 
                            "Failed to start Python service. Please check the logs and try again.");
                    return "redirect:/admin/advanced-stats";
                }
            }
            
            List<Annotateur> annotateurs;
            if (annotateurIds != null && !annotateurIds.isEmpty()) {
                annotateurs = annotateurRepository.findAllById(annotateurIds);
                logger.info("Selected {} annotateurs for evaluation", annotateurs.size());
            } else {
                annotateurs = annotateurRepository.findAll();
                logger.info("Evaluating all {} annotateurs", annotateurs.size());
            }
            
            spamDetectorService.runDetectionProcess(threshold, annotateurs);
            
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            redirectAttributes.addFlashAttribute("timestamp", timestamp);
            redirectAttributes.addFlashAttribute("success", 
                    String.format("Spam detection completed successfully. Evaluated %d annotators.", annotateurs.size()));
            
            return "redirect:/admin/advanced-stats";
            
        } catch (Exception e) {
            logger.error("Error running spam detection: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", 
                    "An error occurred during spam detection: " + e.getMessage());
            return "redirect:/admin/spam/configure";
        }
    }

    @GetMapping("/spam/results")
    public String showResults(RedirectAttributes redirectAttributes) {
        List<SpamDetectionResults> results = spamResultsRepository.findAll();
        redirectAttributes.addFlashAttribute("results", results);
        
        long flaggedCount = results.stream().filter(SpamDetectionResults::isFlagged).count();
        redirectAttributes.addFlashAttribute("flaggedCount", flaggedCount);
        
        return "redirect:/admin/advanced-stats";
    }
    
    @GetMapping("/spam/service-status")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> checkServiceStatus() {
        Map<String, Object> response = new HashMap<>();
        boolean isHealthy = pythonService.isServiceHealthy();
        
        response.put("healthy", isHealthy);
        response.put("timestamp", LocalDateTime.now().toString());
        
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    
    @PostMapping("/spam/start-service")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> startPythonService() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean started = pythonService.startPythonService();
            response.put("success", started);
            response.put("message", started ? 
                    "Python service started successfully" : 
                    "Failed to start Python service");
            
            return new ResponseEntity<>(response, 
                    started ? HttpStatus.OK : HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            logger.error("Error starting Python service: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
