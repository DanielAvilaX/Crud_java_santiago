
# Integración de IA con Spring AI y Ollama

El objetivo es implementar un chatbot dentro de la aplicación que permita realizar preguntas sobre el negocio utilizando un modelo de lenguaje ejecutado localmente con Ollama.

La arquitectura inicial será:

Spring Boot → Spring AI → Ollama → Llama 3.2

Posteriormente se integrarán los datos de la aplicación (facturas, pagos, productos y auditoría) y RAG para consultar información documental.

---

## Paso 1 - Instalar y configurar Ollama

Ollama permite ejecutar modelos de lenguaje localmente.

### Instalar Ollama

Una vez instalado Ollama, verificar la instalación:

```bash
ollama --version
````

### Descargar Llama 3.2

```bash
ollama pull llama3.2
```

Verificar que el modelo esté disponible:

```bash
ollama list
```

Para probar el modelo directamente:

```bash
ollama run llama3.2
```

Esto permite comprobar que Llama 3.2 funciona correctamente antes de conectarlo con Spring Boot.

Por defecto, Ollama expone su API local en:

```text
http://localhost:11434
```

---

## Paso 2 - Integrar Spring AI con el proyecto

El proyecto utiliza:

* Spring Boot 3.5.11
* Java 25
* Maven
* Spring AI
* Ollama
* Llama 3.2

### Agregar Spring AI BOM

Agregar el siguiente bloque en `pom.xml`, fuera de `<dependencies>`:

```xml
<dependencyManagement>
    <dependencies>

        <dependency>
            <groupId>org.springframework.ai</groupId>
            <artifactId>spring-ai-bom</artifactId>
            <version>1.1.4</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>

    </dependencies>
</dependencyManagement>
```

El BOM se encarga de administrar las versiones de las dependencias de Spring AI.

### Agregar Ollama Starter

Dentro de `<dependencies>` agregar:

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-model-ollama</artifactId>
</dependency>
```

No es necesario indicar la versión directamente porque está siendo administrada por `spring-ai-bom`.

### Instalar Maven en macOS

El proyecto actualmente no utiliza Maven Wrapper (`mvnw`), por lo que Maven debe estar instalado localmente.

Con Homebrew:

```bash
brew install maven
```

Verificar:

```bash
mvn -version
```

### Problema de codificación de application.properties

Durante la compilación se encontró que:

```text
src/main/resources/application.properties
```

estaba utilizando:

```text
ISO-8859-1
```

mientras Maven esperaba UTF-8.

Esto puede producir errores como:

```text
MalformedInputException: Input length = 1
```

Verificar la codificación:

```bash
file -I src/main/resources/application.properties
```

Si aparece:

```text
charset=iso-8859-1
```

convertir el archivo a UTF-8:

```bash
iconv -f ISO-8859-1 -t UTF-8 \
src/main/resources/application.properties \
> /tmp/application.properties

mv /tmp/application.properties \
src/main/resources/application.properties
```

Verificar nuevamente:

```bash
file -I src/main/resources/application.properties
```

El resultado esperado es:

```text
charset=utf-8
```

Finalmente ejecutar:

```bash
mvn clean install -U
```

El resultado esperado es:

```text
BUILD SUCCESS
```


