/*
 * Copyright 2017-2026 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.langchain4j.rag;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.ClassPathDocumentLoader;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.IngestionResult;
import io.micronaut.context.BeanContext;
import io.micronaut.context.event.StartupEvent;
import io.micronaut.core.annotation.Internal;
import io.micronaut.inject.qualifiers.Qualifiers;
import io.micronaut.runtime.event.annotation.EventListener;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Ingests the documents of the ingestion configurations that ingest on startup.
 */
@Singleton
@Internal
final class DocumentIngestion {

    private static final Logger LOG = LoggerFactory.getLogger(DocumentIngestion.class);
    private static final String CLASSPATH_PREFIX = "classpath:";

    private final BeanContext beanContext;
    private final Collection<IngestionConfiguration> configurations;

    DocumentIngestion(BeanContext beanContext, Collection<IngestionConfiguration> configurations) {
        this.beanContext = beanContext;
        this.configurations = configurations;
    }

    @EventListener
    void onStartup(StartupEvent event) {
        for (IngestionConfiguration configuration : configurations) {
            if (configuration.isIngestOnStartup() && !configuration.getSources().isEmpty()) {
                ingest(configuration);
            }
        }
    }

    private void ingest(IngestionConfiguration configuration) {
        List<Document> documents = load(configuration);
        EmbeddingStoreIngestor ingestor = beanContext.getBean(EmbeddingStoreIngestor.class, Qualifiers.byName(configuration.getName()));
        IngestionResult result = ingestor.ingest(documents);
        if (LOG.isInfoEnabled()) {
            LOG.info("Ingested {} documents of {} into the embedding store ({} input tokens)",
                documents.size(), configuration.getName(),
                result.tokenUsage() != null ? result.tokenUsage().inputTokenCount() : "unknown");
        }
    }

    static List<Document> load(IngestionConfiguration configuration) {
        PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + configuration.getGlob());
        List<Document> documents = new ArrayList<>();
        for (String source : configuration.getSources()) {
            if (source.startsWith(CLASSPATH_PREFIX)) {
                documents.addAll(ClassPathDocumentLoader.loadDocuments(source.substring(CLASSPATH_PREFIX.length()), matcher));
            } else if (configuration.isRecursive()) {
                documents.addAll(FileSystemDocumentLoader.loadDocumentsRecursively(Path.of(source), matcher));
            } else {
                documents.addAll(FileSystemDocumentLoader.loadDocuments(Path.of(source), matcher));
            }
        }
        return documents;
    }
}
