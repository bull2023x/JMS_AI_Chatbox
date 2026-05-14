package demo.jms.assistant;

import io.helidon.common.config.Config;
import io.helidon.http.Header;
import io.helidon.http.HeaderNames;
import io.helidon.http.HeaderValues;
import io.helidon.http.Status;
import io.helidon.logging.common.LogConfig;
import io.helidon.service.registry.Services;
import io.helidon.webserver.WebServerConfig;
import io.helidon.webserver.http.HttpRouting;
import io.helidon.webserver.staticcontent.StaticContentFeature;

import demo.jms.assistant.rag.DocsIngestor;
import demo.jms.assistant.rest.ChatBotService;

public class ApplicationMain {

    private static final Header UI_REDIRECT = HeaderValues.createCached(HeaderNames.LOCATION, "/ui");

    public static void main(String[] args) {
        // Make sure logging is enabled as the first thing
        LogConfig.configureRuntime();

        var config = Services.get(Config.class);

        // Initialize embedding store
        Services.get(DocsIngestor.class)
                .ingest();

        // Static content setup
        var staticContentFeature = StaticContentFeature.builder()
                .addClasspath(cl -> cl.location("WEB")
                        .context("/ui")
                        .welcome("index.html"))
                .build();

        // Initialize and start web server
        WebServerConfig.builder()
                .addFeature(staticContentFeature)
                .config(config.get("server"))
                .routing(ApplicationMain::routing)
                .build()
                .start();
    }

    /**
     * Updates HTTP Routing.
     */
    static void routing(HttpRouting.Builder routing) {
        routing.any("/", (req, res) -> {
                    // showing the capability to run on any path, and redirecting from root
                    res.status(Status.MOVED_PERMANENTLY_301);
                    res.headers().set(UI_REDIRECT);
                    res.send();
                })
                .register("/chat", Services.get(ChatBotService.class));
    }
}
