package demo.jms.assistant.rag;

public class Chunk {
    enum Type { SECTION, PARAGRAPH, CODE, TABLE, LIST, MIXED }

    private final String text;
    private final Type type;
    private final String sectionPath;

    public Chunk(String text, Type type, String sectionPath) {
        this.text = text;
        this.type = type;
        this.sectionPath = sectionPath;
    }

    public String text() { return text; }
    public Type type() { return type; }
    public String sectionPath() { return sectionPath; }
}
