
# Chatbot de negocio (RAG + Tool Calling + Text-to-SQL)

Le puedes preguntar en español normal cosas del negocio (facturas, pagos,
productos, auditoría) y responde con datos reales de la base de datos —
nunca inventados —, corriendo 100% en tu computadora, sin mandar nada a
internet.

Ejemplo:

```
POST /chat
{ "pregunta": "cuanto se ha pagado de la factura 3" }
```

Está construido con tres piezas que se combinan:

| Pieza | En una frase |
|---|---|
| **RAG** (Retrieval-Augmented Generation) | Antes de responder, busca información real (de tu base de datos y de tu código) y se la da al modelo como contexto. |
| **Text-to-SQL** | El modelo convierte tu pregunta en una consulta `SELECT` y la ejecuta contra la base de datos. |
| **Tool Calling** | En vez de un flujo fijo escrito a mano, el modelo decide por sí mismo cuándo usar cada una de las dos piezas de arriba. |

El resto de este documento explica cada una con el detalle de qué
archivo hace qué.

---

## 1. Instalación

### Paso 1 — Instalar Ollama

Ollama es el programa que corre el "cerebro" (modelo de lenguaje) en tu
propia máquina.

Descárgalo de [ollama.com](https://ollama.com) e instálalo. Verifica:

```bash
ollama --version
```

Por defecto expone su API en `http://localhost:11434`.

### Paso 2 — Descargar los modelos que usa el proyecto

El proyecto necesita dos modelos:

```bash
# El que "piensa" y responde
ollama pull llama3.1

# El que convierte texto en vectores para la búsqueda semántica
ollama pull nomic-embed-text
```

Verifica que quedaron:

```bash
ollama list
```

### Paso 3 — Verificar el modelo de chat (opcional)

```bash
ollama run llama3.1
```

Si responde en la terminal, quedó bien conectado.

### Paso 4 — Levantar el proyecto

```bash
mvn spring-boot:run
```

Al arrancar vas a ver en el log que indexa el esquema de la base de datos
y el conocimiento del proyecto (sección 2). Eso pasa **cada vez que
arrancas la app** — no queda nada guardado de una corrida a otra, a
propósito: así siempre respondes con la base de datos y el código tal
como están en ese momento, sin datos viejos colgados.

No se necesita ningún contenedor de Docker para el chatbot. (Si ves
Docker corriendo en tu máquina para este proyecto, es por MinIO — el
almacenamiento de imágenes de productos —, algo aparte que no tiene que
ver con el chatbot.)

---

## 2. RAG — de dónde saca el conocimiento

RAG quiere decir, en corto: "antes de responder, busca información
relevante y dásela al modelo como contexto" — en vez de esperar que el
modelo ya se sepa de memoria los datos de tu negocio (que nunca los va a
saber, porque son tuyos).

Hay **dos** fuentes separadas de conocimiento, cada una indexada por su
cuenta, guardadas en el mismo buscador en memoria:

```mermaid
flowchart LR
    subgraph Esquema["RAG de ESQUEMA"]
        direction TB
        DSE[DatabaseSchemaExtractor] -->|"lee tablas, columnas,<br/>llaves primarias/foráneas"| SDG[SchemaDocumentGenerator]
        SDG --> SI[SchemaIndexer]
    end

    subgraph Proyecto["RAG de PROYECTO"]
        direction TB
        JMS[JavaModelScanner] -->|"lee enums y entidades<br/>de co.santiago"| PDG[ProjectDocumentGenerator]
        DS[DocsScanner] -->|"lee README, esta guía,<br/>el enunciado original"| PDG
        PDG --> PI[ProjectIndexer]
    end

    SI --> VS[("VectorStore en memoria<br/>VectorStoreConfig")]
    PI --> VS

    VS --> SR[SchemaRetriever]
    VS --> PR[ProjectRetriever]
```

- **RAG de esquema**: lee directamente la base de datos (tablas,
  columnas, llaves) y arma un texto por tabla. `SchemaRetriever` además
  expande automáticamente por llave foránea: si preguntas por "pagos",
  también trae `INVOICE` aunque no la hayas nombrado, porque `PAYMENT`
  la referencia.
- **RAG de proyecto**: lee tu propio código — los `enum` (¿qué estados
  puede tener una factura?) y las entidades (¿qué campos tiene un
  `Payment`?) — más la documentación en Markdown del repo. Esto es lo
  que le permite al chatbot entender vocabulario de negocio, no solo
  nombres de tabla.

Las dos cosas se reconstruyen **completas, desde cero, en cada
arranque** de la aplicación — no queda ningún archivo ni base de datos
externa guardando esto entre una corrida y otra.

**Dónde vive en el código:**

| Archivo | Qué hace |
|---|---|
| `VectorStoreConfig.java` | Crea el buscador semántico en memoria — la pieza de la que dependen los cuatro renglones de abajo. |
| `DatabaseSchemaExtractor.java` | Lee la estructura real de la base de datos (tablas, columnas, llaves). |
| `SchemaDocumentGenerator.java` | Convierte esa estructura en texto legible para indexar. |
| `SchemaRelationExpander.java` | Agrega automáticamente las tablas relacionadas por llave foránea. |
| `SchemaIndexer.java` | Indexa el esquema completo en cada arranque. |
| `SchemaRetriever.java` | Busca qué tablas son relevantes para una pregunta. |
| `ColumnMetadata.java` / `TableMetadata.java` / `ForeignKeyMetadata.java` | Estructuras simples que guardan la info de una columna/tabla/llave mientras se procesa. |
| `CodeUnit.java` | Estructura simple que guarda la info de un enum o entidad leída del código. |
| `JavaModelScanner.java` | Lee tus enums y entidades (`.java`) para sacar sus campos/valores posibles. |
| `DocsScanner.java` | Lee la documentación en Markdown del proyecto. |
| `ProjectDocumentGenerator.java` | Convierte enums/entidades/docs en texto indexable, partiendo los documentos largos en pedazos. |
| `ProjectIndexer.java` | Indexa el conocimiento del proyecto en cada arranque. |
| `ProjectRetriever.java` | Busca qué conocimiento de negocio es relevante para una pregunta. |

---

## 3. Text-to-SQL — de la pregunta al `SELECT`

Esta es la parte que convierte tu pregunta en una consulta SQL real y la
ejecuta. No hay una clase separada que "solo genera SQL": el modelo lo
escribe él mismo, guiado por las instrucciones del sistema (ver sección
4), y una herramienta (`executeReadOnlyQuery`) es la que efectivamente
lo corre contra la base de datos.

**Dónde vive en el código:**

| Archivo | Qué hace |
|---|---|
| `ChatServiceImpl.java` | Le da al modelo, en el mensaje de sistema, las reglas de qué SQL puede escribir (solo `SELECT`, usar las tablas del esquema, etc.). |
| `ChatTools.executeReadOnlyQuery` (dentro de `ChatTools.java`) | Recibe el SQL que escribió el modelo y lo manda a ejecutar. Si falla, devuelve el error para que el modelo lo corrija y reintente. |
| `SqlValidator.java` | Antes de ejecutar, revisa que el texto sea de verdad un `SELECT`/`WITH`, sin comentarios ni instrucciones peligrosas. |
| `DatabaseQueryService.java` / `DatabaseQueryServiceImpl.java` | Ejecuta la consulta ya validada contra la base de datos. |
| `AiDatabaseConfig.java` | Define la conexión que se usa para ejecutar ese SQL: de solo lectura, máximo 100 filas, máximo 5 segundos. |
| `AiReadOnlyUserInitializer.java` | Crea, al arrancar, el usuario de base de datos `ai_readonly` con permiso de solo `SELECT`. |

Ver la sección 5 para el detalle de por qué esto es seguro aunque el
modelo se equivoque.

---

## 4. Tool Calling — el agente que decide

En vez de que el código le diga paso a paso al modelo "primero busca
esto, luego ejecuta esto otro", le entregamos al modelo **tres
herramientas** y dejamos que él mismo decida cuáles usar y en qué orden
para responder tu pregunta.

```mermaid
flowchart TD
    U["Usuario hace una pregunta<br/>POST /chat"] --> C[ChatController]
    C --> S["ChatServiceImpl<br/>(¿ya la respondió antes? devuelve al instante)"]
    S --> M["Ollama (llama3.1)<br/>decide qué necesita"]

    M -->|"necesita saber qué tablas/columnas hay"| T1["searchDatabaseSchema<br/>(RAG de esquema)"]
    M -->|"necesita saber qué significa un estado/campo"| T2["searchProjectKnowledge<br/>(RAG de proyecto)"]
    M -->|"ya sabe qué consultar"| T3["executeReadOnlyQuery<br/>(Text-to-SQL)"]

    T1 --> M
    T2 --> M
    T3 -->|"SQL generado"| DB[("Base de datos H2<br/>usuario de SOLO LECTURA")]
    DB -->|"resultado o error"| T3
    T3 -->|"si hubo error, el modelo corrige<br/>el SQL y vuelve a intentar"| M
    M --> R["Respuesta en español,<br/>con datos reales"]
    R --> U
```

Puntos clave de esa imagen:

- El modelo puede usar una herramienta, mirar el resultado, y decidir
  usar otra — o la misma de nuevo con una consulta corregida — antes de
  contestarte. Nosotros no lo forzamos a seguir un orden fijo.
- Si `executeReadOnlyQuery` falla (por ejemplo, el modelo escribió mal
  el nombre de una tabla), el modelo ve el mensaje de error y por su
  cuenta reintenta con una consulta corregida.
- Si le preguntas exactamente lo mismo dos veces, la segunda vez la
  respuesta sale al instante (queda guardada en memoria mientras la app
  esté prendida; se olvida al reiniciar).

**Dónde vive en el código:**

| Archivo | Qué hace |
|---|---|
| `ChatTools.java` | Define las tres herramientas (`searchDatabaseSchema`, `searchProjectKnowledge`, `executeReadOnlyQuery`) que el modelo puede invocar. |
| `ChatServiceImpl.java` | Le pasa esas herramientas al modelo (`.tools(chatTools)`) y deja que decida cómo combinarlas; también cachea la respuesta. |
| `ChatService.java` | Contrato/interfaz de lo anterior. |
| `ChatController.java` | El endpoint `POST /chat` por donde entra tu pregunta. |
| `ChatRequestDTO.java` / `ChatResponseDTO.java` | Forma de la pregunta que entra y la respuesta que sale. |
| `CacheConfig.java` | Habilita que las respuestas repetidas se guarden en memoria. |

---

## 5. Seguridad de las consultas SQL

Como el modelo escribe SQL por su cuenta, hay varias capas para que
nunca pueda hacer daño, aunque se equivoque:

1. **Solo lectura, a nivel de código**: `SqlValidator` revisa el texto
   de la consulta antes de ejecutarla — solo deja pasar `SELECT`/`WITH`,
   bloquea `INSERT`/`UPDATE`/`DELETE`/`DROP`/etc., bloquea comentarios
   SQL y bloquea que se manden varias consultas pegadas.
2. **Solo lectura, a nivel de base de datos**: existe un usuario de base
   de datos aparte (`ai_readonly`) que **solo tiene permiso de
   `SELECT`** sobre las tablas. El chatbot ejecuta todo a través de ese
   usuario, nunca del usuario administrador que usa el resto de la app.
   Así, aunque el paso anterior fallara por algún motivo, la base de
   datos misma rechazaría cualquier intento de borrar o modificar algo.
3. **Límites de la consulta**: máximo 100 filas de resultado y máximo 5
   segundos de espera por consulta, para que nunca se cuelgue ni traiga
   una cantidad absurda de datos.

---

## 6. Configuración (`application.properties`)

```properties
# Dónde está Ollama corriendo
spring.ai.ollama.base-url=http://localhost:11434

# Qué modelo "piensa" las respuestas
spring.ai.ollama.chat.options.model=llama3.1

# Qué modelo convierte texto en vectores para la búsqueda
spring.ai.ollama.embedding.options.model=nomic-embed-text

# Usuario de solo lectura para las consultas SQL generadas por el chatbot
ai.datasource.username=ai_readonly
ai.datasource.password=ai_readonly_2026

# Límites de seguridad de esas consultas
ai.sql.max-rows=100
ai.sql.query-timeout-seconds=5
```

**Sobre el modelo (`spring.ai.ollama.chat.options.model`)**: el proyecto
está pensado para correr en una máquina más potente (por ejemplo, un Mac
reciente), donde un modelo grande responde rápido y con más precisión.
En una PC sin tarjeta gráfica dedicada, un modelo grande puede tardar
1-2 minutos por respuesta. Si necesitas probar rápido en una máquina
más limitada, puedes cambiar esta línea a un modelo más chico (por
ejemplo `llama3.2`) — responde en segundos, a cambio de equivocarse un
poco más seguido en preguntas complejas.

---

## 7. Metodología: cómo se construyó, por fases

El chatbot se armó en fases, cada una construyendo sobre la anterior:

1. **RAG de esquema** — el chatbot aprende a describir tus tablas y a
   expandir automáticamente por llave foránea.
2. **SQL seguro** — validación de que solo se ejecuten `SELECT`, usuario
   de base de datos de solo lectura, límites de filas y de tiempo.
3. **RAG de proyecto** — el chatbot aprende el vocabulario de negocio:
   qué significan los estados, qué campos tiene cada entidad.
4. **Tool calling** — en vez de un flujo fijo escrito a mano, se le dan
   las tres herramientas al modelo y él decide solo cómo combinarlas
   para responder, incluyendo corregirse a sí mismo si algo falla.

Se probó además una fase de "producción" (guardar el conocimiento en una
base de datos externa para no reconstruirlo en cada arranque, medir qué
tan seguido se usa, limitar cuántas preguntas por minuto) pero se
descartó a propósito: la idea de este proyecto es que sea simple y que
cada arranque parta de cero, sin nada persistido de una corrida a otra.

---

## 8. Preguntas frecuentes

**¿Necesito internet para que funcione?** No. Todo corre local: Ollama,
la base de datos y la aplicación.

**¿Se pierde el conocimiento del chatbot si reinicio la app?** Sí, a
propósito — se reconstruye completo en cada arranque desde tu base de
datos y tu código actuales.

**¿Puede el chatbot borrar o modificar datos?** No. Solo puede leer
(ver sección 5).

**¿Por qué a veces tarda tanto?** Porque el modelo corre en tu propia
máquina, sin usar ningún servidor externo — el tiempo de respuesta
depende directamente de qué tan potente sea tu computadora.
