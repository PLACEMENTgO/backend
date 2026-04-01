package com.placementgo.backend.referral.service;

import org.springframework.stereotype.Service;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class JobParserService {

    public JobDetails parseJobDescription(String jobDescription) {
        String company = extractCompany(jobDescription);
        String role = extractRole(jobDescription);
        return new JobDetails(company, role);
    }
    
    private String extractCompany(String text) {
        String[] companyKeywords = {
            "(?i)(?:at|@|company:|for)\\s+([A-Z][A-Za-z0-9\\s&.]+?)(?=\\s+is|\\s+seeks|\\s+-|\\n|\\.|,|$)",
            "(?i)^([A-Z][A-Za-z0-9\\s&.]+?)(?=\\s+is hiring|\\s+seeks|\\s+is looking)",
        };
        
        for (String pattern : companyKeywords) {
            Pattern p = Pattern.compile(pattern);
            Matcher m = p.matcher(text);
            if (m.find()) {
                return m.group(1).trim();
            }
        }
        
        String[] knownCompanies = {"Google", "Amazon", "Microsoft", "Meta", "Apple", 
                                   "Netflix", "Tesla", "Adobe", "Salesforce", "Oracle",
                                   "Uber", "Airbnb", "Stripe", "Spotify", "LinkedIn"};
        for (String company : knownCompanies) {
            if (text.contains(company)) {
                return company;
            }
        }
        
        return "Company";
    }
    
    private String extractRole(String text) {
        String[] roleKeywords = {
            "(?i)(?:position:|role:|title:)\\s+(.+?)(?=\\n|\\.|,|$)",
            "(?i)(?:hiring|seeking|looking for)\\s+(?:a|an)?\\s+(.+?)(?=\\s+at|\\s+to|\\n|\\.|,|$)",
            "(?i)^(.+?)(?=\\s+at\\s+[A-Z])",
        };
        
        for (String pattern : roleKeywords) {
            Pattern p = Pattern.compile(pattern);
            Matcher m = p.matcher(text);
            if (m.find()) {
                String role = m.group(1).trim();
                role = role.replaceAll("(?i)^(a|an|the)\\s+", "");
                return role;
            }
        }
        
        String[] commonRoles = {
            "Software Engineer", "Backend Engineer", "Frontend Engineer", 
            "Full Stack Engineer", "DevOps Engineer", "Data Engineer", 
            "ML Engineer", "Product Manager", "Designer", "Analyst"
        };
        
        for (String role : commonRoles) {
            if (text.toLowerCase().contains(role.toLowerCase())) {
                return role;
            }
        }
        
        return "Software Engineer";
    }
    
    public static class JobDetails {
        private final String company;
        private final String role;
        
        public JobDetails(String company, String role) {
            this.company = company;
            this.role = role;
        }
        
        public String getCompany() { 
            return company; 
        }
        
        public String getRoleTitle() { 
            return role; 
        }
    }
}