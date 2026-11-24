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
package io.micronaut.langchain4j.agentic;

import io.micronaut.context.BeanContext;
import io.micronaut.context.WatchableBeanContext;
import io.micronaut.context.annotation.Context;
import io.micronaut.context.env.DevelopmentActive;
import io.micronaut.context.reload.ClassChangeEvent;
import io.micronaut.context.reload.ReloadStrategy;
import io.micronaut.context.watch.BeanDefinitionChange;
import io.micronaut.context.watch.BeanDefinitionWatcher;
import io.micronaut.context.watch.ClassChangeWatcher;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.order.Ordered;
import io.micronaut.langchain4j.agentic.annotation.AgenticService;
import io.micronaut.langchain4j.dev.DevelopmentServicesRebuild;

/**
 * Builds the agentic services again in development mode, without a restart. An agentic service is built once, on its
 * first call, from the tools, models and retrievers of its generation, and kept by the {@link AgentRegistry}, so
 * recreating the registry builds it again on its next call, with the interceptor and the services on top of it.
 *
 * <p>This follows the rebuilds of the AI services, for a changed tool or file of an in-memory embedding store, an
 * {@link AgenticService} definition added, removed or replaced, and a reload that retires a class loader.</p>
 *
 * @author graemerocher
 * @since 2.4.0
 */
@Internal
@Context
@DevelopmentActive
final class DevelopmentAgenticServicesReloader implements DevelopmentServicesRebuild {

    private volatile boolean anyAgents;

    /**
     * @param beanContext The context, watched when it can be
     */
    DevelopmentAgenticServicesReloader(BeanContext beanContext) {
        if (beanContext instanceof WatchableBeanContext watchable) {
            watchable.definitions().stereotype(AgenticService.class).watch(new AgentsWatcher(watchable));
            watchable.classChanges().watch(new ClassWatcher(watchable));
        }
    }

    @Override
    public void rebuild(WatchableBeanContext context) {
        context.recreate(AgentRegistry.class, null);
    }

    /**
     * Follows the agentic service definitions. The first batch only says whether there are any.
     */
    private final class AgentsWatcher implements BeanDefinitionWatcher<Object>, Ordered {
        private final WatchableBeanContext context;

        private AgentsWatcher(WatchableBeanContext context) {
            this.context = context;
        }

        @Override
        public void onChange(BeanDefinitionChange<Object> change) {
            anyAgents = !change.current().isEmpty();
            if (!change.initial() && (!change.added().isEmpty() || !change.removed().isEmpty())) {
                rebuild(context);
            }
        }

        @Override
        public int getOrder() {
            return Ordered.LOWEST_PRECEDENCE;
        }
    }

    /**
     * Follows a class change applied in place that retires a class loader: what was built holds its classes.
     */
    private final class ClassWatcher implements ClassChangeWatcher, Ordered {
        private final WatchableBeanContext context;

        private ClassWatcher(WatchableBeanContext context) {
            this.context = context;
        }

        @Override
        public void onChange(ClassChangeEvent change) {
            if (anyAgents && change.strategy() != ReloadStrategy.RESTART && !change.retiredLoaders().isEmpty()) {
                rebuild(context);
            }
        }

        @Override
        public int getOrder() {
            return Ordered.LOWEST_PRECEDENCE;
        }
    }
}
