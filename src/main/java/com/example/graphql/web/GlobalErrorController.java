package com.example.graphql.web;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Professional error handling to completely eliminate Spring's default Whitelabel error page.
 * In a high-stakes demo, even error states must look intentional and high-quality.
 */
@Controller
public class GlobalErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request) {
        // For the purposes of this demo, any error path shows a clean, helpful message.
        // In a real system this would render a proper branded error experience.
        // Returning a view name would require a template; for maximum robustness we
        // can return a simple indication. Since we have a static-first strategy,
        // the most common "error" (early 404) is already solved by static/index.html.
        return "forward:/error.html"; // Will fall back to a basic response if not present
    }
}