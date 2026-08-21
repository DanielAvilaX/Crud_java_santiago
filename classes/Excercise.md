# Task:

Design and implement a CRUD application using Java or Kotlin that manages invoices and their corresponding line items. The application should:

- **Create**: Allow users to generate new invoices and add multiple line items to each Invoice. Each line item should include details like item description and price.
- **Read**: Enable users to view and search existing invoices, along with their associated line items.
- **Update**: Add line item(s) to an existing invoice.
- **Delete**: operation is not needed.

The application should also allow users to pay for invoices. This feature should include the following functionalities:
- **Paying the Invoice**: Invoice can be marked as paid with relevant information pertaining to the payment persisted.

Consider using appropriate data structures and algorithms to represent invoices and line items, and choose a suitable storage mechanism to persist the data. Implement an API to interact with the application and perform aforementioned operations.

## Additional Notes

- Try to tackle this task as if you would be creating a real production-grade application.
- Feel free to use any Java or Kotlin framework or library of your choice.


softdelete y harddelete, hacer softdelete (HECHO)

formatear precios (HECHO)

auditoria a todas las transacciones en todas las entidades (HECHO)

CREAR UN ENUM DE ESTADOS DE AUDITORIA (HECHO)

409 (HECHO)

getbyid (HECHO)

modulos de pago (HECHO)


ollama con contexto por base de datos RAG
base de datos vectorial

FASE 1 — Schema RAG
────────────────────
✅ DatabaseSchemaExtractor
✅ SchemaDocumentGenerator
✅ Embeddings
✅ VectorStore
✅ SchemaIndexer
✅ SchemaRetriever
✅ Integración ChatService
✅ Expansión automática PK/FK (SchemaRelationExpander)


FASE 2 — Robustecer Text-to-SQL
────────────────────────────────
✅ SQL Validator serio (SqlValidator, usado desde ChatTools.executeReadOnlyQuery)
✅ Retry automático (ahora lo hace el propio agente en Fase 4, ver nota abajo)
✅ Corrección automática SQL (idem, el agente reintenta con SQL corregido al ver el error)
✅ Límites de resultados (aiJdbcTemplate.setMaxRows)
✅ Timeout (aiJdbcTemplate.setQueryTimeout)
✅ Usuario BD read-only (ai_readonly, AiReadOnlyUserInitializer + AiDatabaseConfig)

Nota: SqlExecutionService/SqlExecutionServiceImpl (el loop de retry+corrección
manual de Fase 2) se eliminó al implementar Fase 4: quedó reemplazado por la
orquestación automática del propio agente, que ve el error de
executeReadOnlyQuery y decide por sí mismo si corrige y reintenta.


FASE 3 — Project RAG
────────────────────
✅ Indexar Java (JavaModelScanner: enums + entidades de co.santiago.models)
✅ Indexar README/docs (DocsScanner: README.md, INSTALACIONCHATBOT.md, Excercise.md,
   con chunking por párrafos para no embeder archivos largos como un solo bloque)
✅ Indexar enums/reglas (ProjectDocumentGenerator, valores de enum + campos de entidades)
✅ ProjectRetriever (integrado en ChatServiceImpl junto al SchemaRetriever)


FASE 4 — Agent / Tool Calling
──────────────────────────────
✅ searchDatabaseSchema (ChatTools, @Tool)
✅ searchProjectKnowledge (ChatTools, @Tool)
✅ executeReadOnlyQuery (ChatTools, @Tool, valida con SqlValidator + usuario read-only)
✅ Orquestación automática (ChatServiceImpl.ask usa chatClient.prompt().tools(chatTools),
   el modelo decide qué herramientas llamar y en qué orden; reemplaza el pipeline fijo
   manual de antes)

Nota real de calidad: con el modelo local llama3.2 (3B) el agente a veces "alucina"
la respuesta final en vez de reintentar bien tras un error de SQL. Se probó
llama3.1 (8B, ya descargado) y sigue mejor las instrucciones, pero en esta
máquina (CPU, sin GPU) tarda varios minutos por respuesta — demasiado lento
para uso interactivo. Se dejó llama3.2 por velocidad; cambiar el modelo en
application.properties si hay GPU disponible o no importa la latencia.


FASE 5 — Producción
───────────────────
✅ PGVector/Qdrant (perfil "prod" = Qdrant vía Docker, persistente; perfil por
   defecto = SimpleVectorStore en memoria, cero configuración)
✅ Reindexación incremental (IncrementalIndexer: id determinístico por hash del
   contenido, salta el embedding si no cambió; verificado en vivo: 2do arranque
   con Qdrant bajó de 21.5s a 7.4s al saltarse los 24 documentos sin cambios)
✅ Observabilidad (Actuator + Micrometer: /actuator/health, /actuator/metrics,
   contadores chat.requests / chat.tools.calls, timer chat.duration)
✅ Seguridad (usuario BD read-only ya cubierto en Fase 2; + rate limit por IP en
   /chat vía ChatRateLimiter, + límite de longitud en la pregunta)
✅ Cache (@Cacheable en ChatServiceImpl.ask, verificado: 28.8s sin caché vs
   0.05s con caché en la misma pregunta)
✅ Evaluaciones del RAG (RagEvaluationTest: preguntas de referencia con la
   tabla/enum esperado; encontró y motivó arreglos reales: chunking de docs
   largos, fix de CRLF en el split, topK más alto para el corpus de proyecto,
   y mejor alineación léxica en el texto generado de los enums)


FASE 6 — Si realmente crece mucho    ← ESTAMOS AQUÍ
─────────────────────────────────
⬜ Dynamic Tool Discovery
⬜ Multi-database
⬜ MCP