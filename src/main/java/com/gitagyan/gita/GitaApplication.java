package com.gitagyan.gita;

import com.gitagyan.gita.components.PDFTikaDocumentReader;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootApplication
public class GitaApplication {
	@Autowired
	VectorStore vectorStore;
	@Autowired
	PDFTikaDocumentReader PDFTikaDocumentReader;
	public static void main(String[] args) {
		SpringApplication.run(GitaApplication.class, args);
	}

	@Bean
	public CommandLineRunner commandLineRunner(JdbcTemplate jdbcTemplate){
		return (args -> {

			if(jdbcTemplate.queryForObject("""
					select count(*)
					from gitagyan
					""",Integer.class)==0) {
				vectorStore.add(PDFTikaDocumentReader.loadText());
			}
		});
	}

}
