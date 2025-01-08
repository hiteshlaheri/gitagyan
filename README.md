# Bhagavad Gita Q&A Application

This application provides a REST API to answer questions from the Bhagavad Gita using natural language processing and vector similarity search. The application is built with Spring Boot, integrates a Hugging Face ONNX embedding model for sentence similarity, stores vectors in a PostgreSQL database with pgvector, and uses GROQ AI for chatbot responses.

---

## Features
1. **Read PDF File**: Parse and extract text from a Bhagavad Gita PDF file.
2. **Text Tokenization**: Split extracted text into smaller segments using a tokenizer.
3. **Sentence Similarity with Embedding**: Use Hugging Face ONNX embedding models to generate vector embeddings.
4. **Vector Storage**: Store embeddings in a PostgreSQL database with pgvector for similarity search.
5. **Similarity Search**: Retrieve relevant sections based on user queries.
6. **Chatbot Integration**: Use GROQ AI chat client for generating answers to questions based on retrieved information.
7. **REST APIs**: Expose endpoints to interact with the application.

---

## Prerequisites
- Java 11.5 or later
- Spring Boot framework
- PostgreSQL with pgvector extension
- GROQ AI API key
- Hugging Face ONNX embedding model and tokenizer

---

## Application Configuration

Add the following to `application.properties`:

```properties
# GROQ AI Configuration
spring.ai.openai.base-url=https://api.groq.com/openai
spring.ai.openai.chat.options.model=llama3-70b-8192
spring.ai.openai.chat.options.temperature=0.7

# Database Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.hibernate.show-sql=true
spring.datasource.url=jdbc:postgresql://localhost:5432/postgres
spring.datasource.username=tad
spring.datasource.password=tad
```

Set the GROQ AI key as an environment variable:
```bash
export GROQ_AI_KEY=your-groq-api-key
```

---

## Implementation

### 1. **Reading PDF File**
The `PDFTikaDocumentReader` component reads and tokenizes the Bhagavad Gita PDF:

```java
@Component
public class PDFTikaDocumentReader {

    private final Resource resource;

    PDFTikaDocumentReader(@Value("classpath:/gitaenglish.pdf") Resource resource) {
        this.resource = resource;
    }

    public List<Document> loadText() {
        TikaDocumentReader tikaDocumentReader = new TikaDocumentReader(this.resource);
        TokenTextSplitter splitter = new TokenTextSplitter(1000, 400, 10, 5000, true);
        var documentList = tikaDocumentReader.get();
        return splitter.apply(documentList);
    }
}
```

### 2. **Embedding Model Configuration**
The embedding model is configured to use Hugging Face ONNX:

```java
@Configuration
public class EmbeddingTransformer {
    @Bean
    public EmbeddingModel embeddingModel1() {
        TransformersEmbeddingModel embeddingModel = new TransformersEmbeddingModel();
        embeddingModel.setTokenizerResource("classpath:tokenizer.json");
        embeddingModel.setModelResource("classpath:model.onnx");
        embeddingModel.setResourceCacheDirectory("/tmp/onnx-zoo");
        embeddingModel.setTokenizerOptions(Map.of("padding", "true"));
        embeddingModel.setModelOutputName("token_embeddings");
        return embeddingModel;
    }

    @Value("${GROQ_AI_KEY}")
    private String GROQ_AI_KEY;

    @Bean
    PgVectorStore pgVectorStore(JdbcTemplate jdbcTemplate) {
        return new PgVectorStore("gitagyan", jdbcTemplate, embeddingModel1(), 384, COSINE_DISTANCE, false, PgVectorStore.PgIndexType.HNSW, true);
    }

    @Bean
    ChatClient chatClient() {
        var openai = new OpenAiApi("https://api.groq.com/openai", GROQ_AI_KEY);
        var openAiChatOptions = OpenAiChatOptions.builder()
            .withModel("llama3-70b-8192")
            .withTemperature(0.4)
            .withMaxTokens(200)
            .build();
        var openAiChatModel = new OpenAiChatModel(openai, openAiChatOptions);
        return ChatClient.create(openAiChatModel);
    }
}
```

### 3. **REST API Controller**
The `GitaGyanController` exposes an endpoint to answer Bhagavad Gita questions:

```java
@CrossOrigin
@RestController
public class GitaGyanController {
    @Autowired
    ChatClient chatClient;
    @Autowired
    PgVectorStore pgVectorStore;
    @Value("classpath:SystemPrompt.st")
    String systemPrompt;

    @GetMapping("/gitagyan")
    public Map<String, String> gitagyan(@RequestParam String question) {
        ChatResponse chatResponse = this.chatClient.prompt()
            .system(systemPrompt)
            .user(question)
            .advisors(new QuestionAnswerAdvisor(pgVectorStore, SearchRequest.query(question).withTopK(4)))
            .call()
            .chatResponse();
        return Map.of("response", chatResponse.getResult().getOutput().getContent());
    }
}
```

---

## Steps to Run the Application
1. Clone the repository.
2. Ensure PostgreSQL is installed and pgvector extension is enabled.
3. Update `application.properties` with your database and GROQ AI credentials.
4. Place the Bhagavad Gita PDF (`gitaenglish.pdf`) in the `resources` directory.
5. Run the Spring Boot application:
   ```bash
   ./gradlew bootRun
   ```
6. Access the REST API at `http://localhost:8080/gitagyan?question=YourQuestion`.

---

## Example Usage

**Request:**
```http
GET /gitagyan?question=who is author of bhagavat gita? HTTP/1.1
Host: localhost:8080
```

**Response:**
```json
{
    "response": "The author of the Bhagavat Gita is Lord Sri Krishna, who is considered the Supreme Personality of Godhead."
}
```

---

## Dependencies
- **Spring Boot** for application development
- **Tika** for PDF text extraction
- **pgvector** for vector similarity search
- **Hugging Face ONNX** for embedding model
- **GROQ AI** for chat-based question answering

---


