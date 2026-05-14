package demo.jms.assistant.rag;

import java.util.*;

public class ChunkGrouper {
    private final int maxChars;

    public ChunkGrouper(int maxChars) {
        this.maxChars = maxChars;
    }

    public List<Chunk> groupChunks(List<Chunk> input) {
        var grouped = new ArrayList<Chunk>();

        var builder = new StringBuilder();
        String currentSection = null;
        var type = Chunk.Type.MIXED;

        for (var chunk : input) {
            if (currentSection == null) currentSection = chunk.sectionPath();

            // If switching section or chunk is too big to add
            if (!chunk.sectionPath().equals(currentSection) || builder.length() + chunk.text().length() > maxChars) {
                if (!builder.isEmpty()) {
                    grouped.add(new Chunk(builder.toString().trim(), type, currentSection));
                    builder.setLength(0);
                }
                currentSection = chunk.sectionPath();
            }

            builder.append(chunk.text()).append("\n\n");
        }

        if (!builder.isEmpty()) {
            grouped.add(new Chunk(builder.toString().trim(), type, currentSection));
        }

        return grouped;
    }
}
