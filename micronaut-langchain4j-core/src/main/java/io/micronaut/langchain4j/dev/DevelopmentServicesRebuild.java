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
package io.micronaut.langchain4j.dev;

import io.micronaut.context.WatchableBeanContext;
import io.micronaut.core.annotation.Internal;

/**
 * What else to build again when the development runtime rebuilds the AI services, for the modules that build their own
 * services from the same tools, models and retrievers, on top of this one.
 *
 * @author graemerocher
 * @since 2.4.0
 */
@Internal
public interface DevelopmentServicesRebuild {

    /**
     * Called after the tool registry and the AI service interceptor were recreated.
     *
     * @param context The context, in which to recreate what the module built
     */
    void rebuild(WatchableBeanContext context);
}
