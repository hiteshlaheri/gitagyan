package com.gitagyan.gita.configurations;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.transformers.TransformersEmbeddingModel;
import org.springframework.ai.vectorstore.PgVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

import static org.springframework.ai.vectorstore.PgVectorStore.PgDistanceType.COSINE_DISTANCE;

@Configuration
public class EmbeddingTransformer {
    @Bean
    public EmbeddingModel embeddingModel1() {
        TransformersEmbeddingModel embeddingModel = new TransformersEmbeddingModel();
        embeddingModel.setTokenizerResource("classpath:tokenizer.json");
        embeddingModel.setModelResource("classpath:model.onnx");
        embeddingModel.setResourceCacheDirectory("/tmp/onnx-zoo");
        embeddingModel.setTokenizerOptions(Map.of("padding","true"));
        embeddingModel.setModelOutputName("token_embeddings");
        return embeddingModel;
    }
    @Value("${GROQ_AI_KEY}")
    private String GROQ_AI_KEY;
    @Bean
    PgVectorStore pgVectorStore(JdbcTemplate jdbcTemplate){
        return new PgVectorStore("gitagyan",jdbcTemplate,embeddingModel1(),384,COSINE_DISTANCE,false, PgVectorStore.PgIndexType.HNSW,true);
    }

    @Bean
    ChatClient chatClient(){
        var openai = new OpenAiApi("https://api.groq.com/openai",GROQ_AI_KEY);
        var openAiChatOptions = OpenAiChatOptions.builder()
                .withModel("llama3-70b-8192")
                .withTemperature(0.4)
                .withMaxTokens(200)
                .build();
        var openaichatmodel = new OpenAiChatModel(openai,openAiChatOptions);
        return ChatClient.create(openaichatmodel);
    }
}
