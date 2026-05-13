package net.dmitrykornilov.helidon.assistant.rag;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import io.helidon.common.config.Config;
import io.helidon.service.registry.Service;

import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;

@Service.Singleton
public class DocsIngestor {
    private static final Logger LOGGER = Logger.getLogger(DocsIngestor.class.getName());

    private final Config config;
    private final TextPreprocessor preprocessor = new TextPreprocessor();
    private final EmbeddingStore<TextSegment> embeddingStore;
    private final EmbeddingModel embeddingModel;

    @Service.Inject
    DocsIngestor(Config config,
                 EmbeddingStore<TextSegment> embeddingStore,
                 EmbeddingModel embeddingModel) {
        this.config = config;
        this.embeddingStore = embeddingStore;
        this.embeddingModel = embeddingModel;
    }

    public void ingest() {
        // Get files to process
        var appConfig = config.get("app");
        var root = appConfig.get("root").asString().orElseThrow();
        var inclusions = appConfig.get("inclusions").asList(String.class).orElse(Collections.emptyList());
        var exclusions = appConfig.get("exclusions").asList(String.class).orElse(Collections.emptyList());
        var files = FileLister.listFiles(root, exclusions, inclusions);

        // Process files
        var grouper = new ChunkGrouper(1000);
        for (Path path : files) {
            var chunks = preprocessor.extractChunks(path.toFile());
            var groupedChunks = grouper.groupChunks(chunks);

            // Convert to LangChain4J TextSegments with metadata
            List<TextSegment> segments = new ArrayList<>();
            for (int i = 0; i < groupedChunks.size(); i++) {
                var chunk = groupedChunks.get(i);
                var metadata = new Metadata()
                        .put("source", path.toFile().getAbsolutePath())
                        .put("chunk", String.valueOf(i + 1))
                        .put("type", chunk.type().name())
                        .put("section", chunk.sectionPath());

                segments.add(TextSegment.from(chunk.text(), metadata));
            }

            // Embed the segments
            var embeddings = embeddingModel.embedAll(segments);

            // Print segments and metadata
            for (int i = 0; i < segments.size(); i++) {
                TextSegment segment = segments.get(i);
                System.out.println("Chunk " + (i + 1) + ":\n" + segment.text() + "\n");
                System.out.println("Metadata: " + segment.metadata());
                System.out.println("Embedding vector size: " + embeddings.content().get(i).vector().length);
                System.out.println("---");
            }

            embeddingStore.addAll(embeddings.content(), segments);
        }
    }
}
