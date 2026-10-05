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
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Configures a {@link dev.langchain4j.rag.DefaultRetrievalAugmentor} that retrieves content from one or more content
 * retrievers and, with a scoring model, re-ranks it. The retrieval augmentor bean is named after the configuration,
 * so that the {@link io.micronaut.langchain4j.annotation.AiService} with that name uses it.
 *
 * @since 2.4.0
 */
@EachProperty(RetrievalAugmentorConfiguration.PREFIX)
public final class RetrievalAugmentorConfiguration {

    /**
     * The configuration prefix.
     */
    public static final String PREFIX = "langchain4j.rag.retrieval-augmentors";

    private final String name;
    private List<String> contentRetrievers = new ArrayList<>();
    private @Nullable String scoringModel;
    private boolean reRank;
    private @Nullable Double minScore;
    private @Nullable Integer maxResults;

    /**
     * @param name The name of the retrieval augmentor
     */
    public RetrievalAugmentorConfiguration(@Parameter String name) {
        this.name = name;
    }

    /**
     * @return The name of the retrieval augmentor
     */
    public String getName() {
        return name;
    }

    /**
     * @return The names of the content retriever beans
     */
    public List<String> getContentRetrievers() {
        return contentRetrievers;
    }

    /**
     * @param contentRetrievers The names of the content retriever beans the content is retrieved from. Defaults to the
     *                          content retriever named after the retrieval augmentor, otherwise the default content
     *                          retriever.
     */
    public void setContentRetrievers(List<String> contentRetrievers) {
        this.contentRetrievers = contentRetrievers;
    }

    /**
     * @return The name of the scoring model bean that re-ranks the content
     */
    public @Nullable String getScoringModel() {
        return scoringModel;
    }

    /**
     * @param scoringModel The name of the scoring model bean that re-ranks the content. Setting it enables re-ranking.
     */
    public void setScoringModel(@Nullable String scoringModel) {
        this.scoringModel = scoringModel;
    }

    /**
     * @return Whether the content is re-ranked
     */
    public boolean isReRank() {
        return reRank || scoringModel != null;
    }

    /**
     * @param reRank Whether the content is re-ranked with the default scoring model. Defaults to {@code false}, unless
     *               a scoring model is named.
     */
    public void setReRank(boolean reRank) {
        this.reRank = reRank;
    }

    /**
     * @return The minimum score of the re-ranked content
     */
    public @Nullable Double getMinScore() {
        return minScore;
    }

    /**
     * @param minScore The minimum score, given by the scoring model, of the content passed to the model
     */
    public void setMinScore(@Nullable Double minScore) {
        this.minScore = minScore;
    }

    /**
     * @return The maximum number of re-ranked contents passed to the model
     */
    public @Nullable Integer getMaxResults() {
        return maxResults;
    }

    /**
     * @param maxResults The maximum number of re-ranked contents passed to the model. No maximum by default.
     */
    public void setMaxResults(@Nullable Integer maxResults) {
        this.maxResults = maxResults;
    }
}
