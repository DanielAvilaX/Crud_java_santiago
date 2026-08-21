package co.santiago.ai.context;

public record EnrichedContext(
        String schemaContext,
        String projectContext
) {

    public String fullContext() {

        return """
                =========================
                DATABASE SCHEMA
                =========================

                %s

                =========================
                PROJECT KNOWLEDGE
                =========================

                %s
                """.formatted(
                schemaContext,
                projectContext
        );
    }
}