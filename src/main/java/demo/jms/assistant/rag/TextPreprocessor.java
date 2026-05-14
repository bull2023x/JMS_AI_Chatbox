package demo.jms.assistant.rag;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class TextPreprocessor {

    public List<Chunk> extractChunks(File textFile) {
        try {
            String content = Files.readString(textFile.toPath(), StandardCharsets.UTF_8);
            return splitIntoChunks(content, textFile.getName());
        } catch (IOException e) {
            throw new RuntimeException("Failed to read text file: " + textFile.getAbsolutePath(), e);
        }
    }

    private List<Chunk> splitIntoChunks(String content, String fileName) {
        List<Chunk> chunks = new ArrayList<>();

        String sectionPath = fileName;
        StringBuilder buffer = new StringBuilder();

        for (String line : content.split("\\R")) {
            String trimmed = line.trim();

            if (trimmed.isEmpty()) {
                flushParagraph(chunks, buffer, sectionPath);
                continue;
            }

            if (trimmed.startsWith("# ")) {
                flushParagraph(chunks, buffer, sectionPath);
                sectionPath = fileName + " > " + trimmed.substring(2).trim();
                chunks.add(new Chunk(trimmed, Chunk.Type.SECTION, sectionPath));
                continue;
            }

            buffer.append(trimmed).append("\n");
        }

        flushParagraph(chunks, buffer, sectionPath);
        return chunks;
    }

    private void flushParagraph(List<Chunk> chunks, StringBuilder buffer, String sectionPath) {
        String text = buffer.toString().trim();

        if (!text.isEmpty()) {
            chunks.add(new Chunk(text, Chunk.Type.PARAGRAPH, sectionPath));
            buffer.setLength(0);
        }
    }
}
