package com.example.graphql.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.concurrent.TimeUnit;

/**
 * Config to serve the custom GraphiQL UI at /graphiql from the static resource.
 * This makes /graphiql always available early (as soon as static content is served by Tomcat),
 * without depending on the DispatcherServlet or DemoController forward being ready.
 * The custom graphiql.html is self-contained and handles preloaded queries via ?query= param.
 * This eliminates the "Page not found" when opening GraphiQL from the landing page early.
 *
 * Cache policy:
 * - HTML, JS, CSS and the landing page must never be cached (expires=0 / no-store).
 *   This ensures every navigation / click on the demo landing page gets the absolute latest
 *   version from the server (critical while iterating on the impressive UI, preloaded queries,
 *   mutation buttons, etc.).
 * - Only images (and similar binary assets) are allowed to be cached long-term.
 */
@Configuration
public class GraphQlUiConfig implements WebMvcConfigurer {

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    // 1. Serve the special /graphiql endpoint (maps to the self-contained HTML).
    //    Must never be cached so that preloaded queries and the demo always get the latest.
    registry.addResourceHandler("/graphiql")
            .addResourceLocations("classpath:/static/graphiql.html")
            .setCacheControl(CacheControl.noStore().mustRevalidate());

    // 2. No-cache / no-store for the demo's critical content:
    //    - index.html (the main landing page with the mutation sections)
    //    - Any .html, .js, .css that drive the "Run in GraphiQL" buttons and PRELOADED_QUERIES
    //    - The root path ("/") which serves index.html
    //
    //    This ensures every click / navigation gets the absolute latest version from the server.
    //    Using noStore() + mustRevalidate() is the modern equivalent of Expires: 0.
    //
    //    Note: We use the safe pattern "/**" because Spring 6+ PathPatternParser
    //    does not allow patterns like "/**/*.png" (the error you saw).
    //    Only images should be cached long-term. Because this demo currently serves
    //    images via CDN (Font Awesome, Tailwind), we have no local image files.
    //
    //    If you later add local images, put them under /static/img/** (or /assets/img/**)
    //    and register a more specific handler *before* the no-cache one:
    //
    //      registry.addResourceHandler("/img/**")
    //              .addResourceLocations("classpath:/static/img/")
    //              .setCacheControl(CacheControl.maxAge(365, TimeUnit.DAYS).cachePublic());
    //
    //    Then the no-cache "/**" catch-all below will only affect non-image content.
    registry.addResourceHandler("/**")
            .addResourceLocations("classpath:/static/")
            .setCacheControl(CacheControl.noStore().mustRevalidate());
  }
}
