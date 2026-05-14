package demo.jms.assistant.rag;

import org.asciidoctor.*;
import org.asciidoctor.ast.*;

import java.io.File;
import java.util.*;
import java.util.List;

public class AsciiDocPreprocessor {

    private final Asciidoctor asciidoctor;

    public AsciiDocPreprocessor() {
        this.asciidoctor = Asciidoctor.Factory.create();
    }

    public List<Chunk> extractChunks(File adocFile) {
        var options = Options.builder()
                .baseDir(adocFile.getParentFile()) // enables include:: to resolve
                .safe(SafeMode.UNSAFE)             // allows full access (use cautiously)
                .build();

        var doc = asciidoctor.loadFile(adocFile, options);
        var chunks = new ArrayList<Chunk>();
        walk(doc, chunks, new ArrayDeque<>());
        return chunks;
    }

    private void walk(StructuralNode node, List<Chunk> chunks, Deque<String> sectionStack) {
        if (node instanceof Section) {
            sectionStack.push(((Section) node).getTitle());
        }

        var context = node.getContext();
        var content = node.getContent();
        var sectionPath = joinSection(sectionStack);

        switch (context) {
            case "paragraph":
                chunks.add(new Chunk(strip(content.toString()), Chunk.Type.PARAGRAPH, sectionPath));
                break;
            case "listing":
                chunks.add(new Chunk("// code:\n" + content, Chunk.Type.CODE, sectionPath));
                break;
            case "table":
                chunks.add(new Chunk(tableToText((Table) node), Chunk.Type.TABLE, sectionPath));
                break;
            case "ulist":
            case "olist":
                chunks.add(new Chunk(listToText(node), Chunk.Type.LIST, sectionPath));
                break;
        }

        for (var child : node.getBlocks()) {
            walk(child, chunks, sectionStack);
        }

        if (node instanceof Section) {
            sectionStack.pop();
        }
    }

    private String joinSection(Deque<String> sections) {
        var list = new ArrayList<String>(sections);
        Collections.reverse(list);
        return list.isEmpty() ? "" : String.join(" > ", list);
    }

    private String strip(String text) {
        return text.replaceAll("\\*|_+|\\[.+?\\]|`+", "").trim();
    }

    private String tableToText(Table table) {
        var sb = new StringBuilder();
        for (var row : table.getBody()) {
            var cols = new ArrayList<String>();
            for (var cell : row.getCells()) {
                cols.add(cell.getText());
            }
            sb.append(String.join(" | ", cols)).append("\n");
        }
        return sb.toString().trim();
    }

    private String listToText(StructuralNode listNode) {
        var sb = new StringBuilder();

        var list = (org.asciidoctor.ast.List) listNode;
        for (var item: list.getItems()) {
            var listItem = (ListItem) item;
            sb.append("- ").append(strip(listItem.getText())).append("\n");
        }
        return sb.toString().trim();
    }
}
