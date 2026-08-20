package co.santiago.controllers;

import co.santiago.ai.schema.SchemaDocumentGenerator;
import co.santiago.ai.schema.SchemaRetriever;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/schema")
public class SchemaController {

    private final SchemaDocumentGenerator schemaDocumentGenerator;
    private final SchemaRetriever schemaRetriever;

    public SchemaController(
            SchemaDocumentGenerator schemaDocumentGenerator,
            SchemaRetriever schemaRetriever
    ) {
        this.schemaDocumentGenerator = schemaDocumentGenerator;
        this.schemaRetriever = schemaRetriever;
    }

    @GetMapping("/documents")
    public ResponseEntity<List<String>> getDocuments() {

        return ResponseEntity.ok(
                schemaDocumentGenerator.generateDocuments()
        );
    }

    @GetMapping("/search")
    public ResponseEntity<List<String>> searchSchema(
            @RequestParam String query
    ) {

        return ResponseEntity.ok(
                schemaRetriever.searchRelevantSchema(query)
        );
    }
}