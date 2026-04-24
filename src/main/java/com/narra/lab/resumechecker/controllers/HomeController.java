package com.narra.lab.resumechecker.controllers;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.narra.lab.resumechecker.dto.CandidateAnalysisDTO;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeController {

    @GetMapping("/")
    public String getMethodName() {
        return "home";
    }

    @PostMapping("/check")
    public String handleFileUpload(
            @RequestParam("resume") MultipartFile file,
            @RequestParam("jobDescription") String jobDescription,
            RedirectAttributes redirectAttributes) {

        try {
            // 1. Convert PDF to Base64 String
            String base64Resume = Base64.getEncoder().encodeToString(file.getBytes());

            // 2. Prepare JSON body
            Map<String, String> jsonMap = new HashMap<>();
            jsonMap.put("resumeBase64", base64Resume);
            jsonMap.put("jobDescription", jobDescription);
            jsonMap.put("fileName", file.getOriginalFilename());

            // 3. Send as standard JSON
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(jsonMap, headers);

            RestTemplate restTemplate = new RestTemplate();

            // change URL base on the workflow in n8n, but make sure it has the same
            // resposnse structure
            String n8nUrl = "https://stanstanley.app.n8n.cloud/webhook-test/checkResume";

            ResponseEntity<String> response = restTemplate.postForEntity(n8nUrl, requestEntity, String.class);

            String rawJson = response.getBody();

            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());

            CandidateAnalysisDTO dto;

            if (rawJson != null && rawJson.startsWith("\"{")) {
                // double-encoded JSON
                String cleanedJson = mapper.readValue(rawJson, String.class);
                dto = mapper.readValue(cleanedJson, CandidateAnalysisDTO.class);
            } else {
                dto = mapper.readValue(rawJson, CandidateAnalysisDTO.class);
            }

            redirectAttributes.addFlashAttribute("result", dto);

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage",
                    "This application used a free-tier model with limited usage. The LLM Model reached the current limit. Please try again later.");
            return "redirect:/";
        }

        return "redirect:/result";
    }

}
