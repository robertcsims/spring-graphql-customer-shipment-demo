package com.example.graphql.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
<<<<<<< HEAD
 * Note: The primary impressive first screen is now served statically from
 * /static/index.html for immediate availability (even during early startup phases).
 * This eliminates 404/Whitelabel issues and provides a world-class executive demo experience.
 *
 * The Thymeleaf version has been superseded by the static professional landing page.
=======
 * Simple controller that serves a professional landing page for executive demos.
 * This page tells the story of "from schema description to fully functional GraphQL API"
 * and provides one-click access to GraphiQL with the most impressive queries.
>>>>>>> origin/main
 */
@Controller
public class DemoController {

<<<<<<< HEAD
    /**
     * Serve our reliable CDN-based GraphiQL at the standard /graphiql path.
     * This forwards to the static HTML we provide, which is configured to talk to our /graphql endpoint.
     * Much more reliable than the built-in one in this demo setup (avoids auto-config/handler registration timing).
     */
    @GetMapping("/graphiql")
    public String graphiql() {
        return "forward:/graphiql.html";
=======
    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("title", "GraphQL Relational Data Demo");
        model.addAttribute("subtitle", "Built rapidly from a schema specification using Spring for GraphQL");
        return "index";
>>>>>>> origin/main
    }
}
