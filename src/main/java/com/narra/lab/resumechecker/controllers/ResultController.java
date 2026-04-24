package com.narra.lab.resumechecker.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.narra.lab.resumechecker.dto.CandidateAnalysisDTO;

@Controller
public class ResultController {

    @GetMapping("/result")
    public String resultPage(
            @ModelAttribute("result") CandidateAnalysisDTO resumeDTO,
            RedirectAttributes redirectAttributes) {

        // Guard: DTO missing or invalid
        if (resumeDTO == null || resumeDTO.getMatchScore() <= 0) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "You must analyze your resume to view the results.");
            return "redirect:/";
        }

        System.out.println("RECEIVED DTO: " + resumeDTO);

        return "result";
    }
}