package com.example.graphql.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Simple controller that serves a professional landing page for executive demos.
 * This page tells the story of "from schema description to fully functional GraphQL API"
 * and provides one-click access to GraphiQL with the most impressive queries.
 */
@Controller
public class DemoController {

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("title", "GraphQL Relational Data Demo");
        model.addAttribute("subtitle", "Built rapidly from a schema specification using Spring for GraphQL");
        return "index";
    }
}
