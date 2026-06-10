package com.example.graphql.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Note: The primary impressive first screen is now served statically from
 * /static/index.html for immediate availability (even during early startup phases).
 * This eliminates 404/Whitelabel issues and provides a world-class executive demo experience.
 *
 * The Thymeleaf version has been superseded by the static professional landing page.
 */
@Controller
public class DemoController {

    /**
     * Serve our reliable CDN-based GraphiQL at the standard /graphiql path.
     * This forwards to the static HTML we provide, which is configured to talk to our /graphql endpoint.
     * Much more reliable than the built-in one in this demo setup (avoids auto-config/handler registration timing).
     */
    @GetMapping("/graphiql")
    public String graphiql() {
        return "forward:/graphiql.html";
    }
}
