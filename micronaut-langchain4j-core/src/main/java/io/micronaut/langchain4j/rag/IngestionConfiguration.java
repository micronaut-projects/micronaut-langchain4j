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

import io.micronaut.context.annotation.EachProperty;
import io.micronaut.context.annotation.Parameter;

import java.util.ArrayList;
import java.util.List;

/**
 * Configures the ingestion of documents into an embedding store: the documents are loaded from the sources, split
 * into segments, embedded with the embedding model and stored. An
 * {@link dev.langchain4j.store.embedding.EmbeddingStoreIngestor} bean named after the configuration is created to
 * ingest other documents.
 *
 * @since 2.4.0
 */
@EachProperty(IngestionConfiguration.PREFIX)
public final class IngestionConfiguration extends EmbeddingStoreUsingConfiguration {

    /**
     * The configuration prefix.
     */
    public static final String PREFIX = "langchain4j.rag.ingestion";

    /**
     * The default maximum size of a segment, in characters.
     */
    public static final int DEFAULT_MAX_SEGMENT_SIZE = 1000;

    /**
     * The default maximum overlap of two segments, in characters.
     */
    public static final int DEFAULT_MAX_OVERLAP_SIZE = 100;

    /**
     * The default glob of the documents.
     */
    public static final String DEFAULT_GLOB = "**";

    private List<String> sources = new ArrayList<>();
    private String glob = DEFAULT_GLOB;
    private boolean recursive = true;
    private int maxSegmentSize = DEFAULT_MAX_SEGMENT_SIZE;
    private int maxOverlapSize = DEFAULT_MAX_OVERLAP_SIZE;
    private boolean ingestOnStartup = true;

    /**
     * @param name The name of the ingestion
     */
    public IngestionConfiguration(@Parameter String name) {
        super(name);
    }

    /**
     * @return The locations of the documents
     */
    public List<String> getSources() {
        return sources;
    }

    /**
     * @param sources The locations of the documents: directories of the file system, or of the classpath with the
     *                {@code classpath:} prefix
     */
    public void setSources(List<String> sources) {
        this.sources = sources;
    }

    /**
     * @return The glob the documents match
     */
    public String getGlob() {
        return glob;
    }

    /**
     * @param glob The glob the documents match, for example {@code *.md}. Defaults to {@value #DEFAULT_GLOB}.
     */
    public void setGlob(String glob) {
        this.glob = glob;
    }

    /**
     * @return Whether the documents of the sub-directories of the file system sources are loaded
     */
    public boolean isRecursive() {
        return recursive;
    }

    /**
     * @param recursive Whether the documents of the sub-directories of the file system sources are loaded. Defaults to
     *                  {@code true}.
     */
    public void setRecursive(boolean recursive) {
        this.recursive = recursive;
    }

    /**
     * @return The maximum size of a segment, in characters
     */
    public int getMaxSegmentSize() {
        return maxSegmentSize;
    }

    /**
     * @param maxSegmentSize The maximum size of a segment, in characters. Defaults to
     *                       {@value #DEFAULT_MAX_SEGMENT_SIZE}.
     */
    public void setMaxSegmentSize(int maxSegmentSize) {
        this.maxSegmentSize = maxSegmentSize;
    }

    /**
     * @return The maximum overlap of two consecutive segments, in characters
     */
    public int getMaxOverlapSize() {
        return maxOverlapSize;
    }

    /**
     * @param maxOverlapSize The maximum overlap of two consecutive segments, in characters. Defaults to
     *                       {@value #DEFAULT_MAX_OVERLAP_SIZE}.
     */
    public void setMaxOverlapSize(int maxOverlapSize) {
        this.maxOverlapSize = maxOverlapSize;
    }

    /**
     * @return Whether the documents of the sources are ingested when the application starts
     */
    public boolean isIngestOnStartup() {
        return ingestOnStartup;
    }

    /**
     * @param ingestOnStartup Whether the documents of the sources are ingested when the application starts. Defaults
     *                        to {@code true}.
     */
    public void setIngestOnStartup(boolean ingestOnStartup) {
        this.ingestOnStartup = ingestOnStartup;
    }
}
