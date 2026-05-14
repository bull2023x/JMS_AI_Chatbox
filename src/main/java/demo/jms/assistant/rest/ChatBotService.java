package demo.jms.assistant.rest;

import java.util.Collections;

import io.helidon.service.registry.Service;
import io.helidon.webserver.http.HttpRules;
import io.helidon.webserver.http.HttpService;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;

import jakarta.json.Json;
import jakarta.json.JsonBuilderFactory;
import jakarta.json.JsonObject;
import demo.jms.assistant.ai.ChatAiService;
import demo.jms.assistant.ai.SummaryAiService;

@Service.Singleton
public class ChatBotService implements HttpService {

    private static final JsonBuilderFactory JSON = Json.createBuilderFactory(Collections.emptyMap());

    private final ChatAiService chatAiService;
    private final SummaryAiService summaryAiService;

    @Service.Inject
    public ChatBotService(ChatAiService chatAiService,
                          SummaryAiService summaryAiService) {
        this.chatAiService = chatAiService;
        this.summaryAiService = summaryAiService;
    }

    @Override
    public void routing(HttpRules httpRules) {
        httpRules.post("/", this::chatWithAssistant);
    }

    private void chatWithAssistant(ServerRequest req, ServerResponse res) {
        var json = req.content().as(JsonObject.class);
        var message = json.getString("message");
        var summary = json.getString("summary");

        var answer = chatAiService.chat(message, summary);
        var updatedSummary = summaryAiService.chat(summary, message, answer);

        var returnObject = JSON.createObjectBuilder()
                .add("message", answer)
                .add("summary", updatedSummary)
                .build();
        res.send(returnObject);
    }
}
