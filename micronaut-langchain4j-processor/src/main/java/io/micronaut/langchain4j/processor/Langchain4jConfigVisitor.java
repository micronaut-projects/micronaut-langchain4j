/*
 * Copyright 2017-2024 original authors
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
package io.micronaut.langchain4j.processor;

import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.ConfigurationBuilder;
import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.Context;
import io.micronaut.context.annotation.EachBean;
import io.micronaut.context.annotation.EachProperty;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Parameter;
import io.micronaut.context.annotation.Primary;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.bind.annotation.Bindable;
import io.micronaut.core.naming.NameUtils;
import io.micronaut.core.util.ArrayUtils;
import io.micronaut.core.util.StringUtils;
import io.micronaut.inject.ast.ClassElement;
import io.micronaut.inject.ast.ElementQuery;
import io.micronaut.inject.ast.MethodElement;
import io.micronaut.inject.processing.ProcessingException;
import io.micronaut.inject.visitor.TypeElementVisitor;
import io.micronaut.inject.visitor.VisitorContext;
import io.micronaut.langchain4j.annotation.Lang4jConfig;
import io.micronaut.langchain4j.annotation.Lang4jConfig.Model;
import io.micronaut.sourcegen.generator.SourceGenerator;
import io.micronaut.sourcegen.generator.SourceGenerators;
import io.micronaut.sourcegen.model.AnnotationDef;
import io.micronaut.sourcegen.model.ClassDef;
import io.micronaut.sourcegen.model.ClassTypeDef;
import io.micronaut.sourcegen.model.ExpressionDef;
import io.micronaut.sourcegen.model.FieldDef;
import io.micronaut.sourcegen.model.MethodDef;
import io.micronaut.sourcegen.model.ObjectDef;
import io.micronaut.sourcegen.model.ParameterDef;
import io.micronaut.sourcegen.model.PropertyDef;
import io.micronaut.sourcegen.model.RecordDef;
import io.micronaut.sourcegen.model.StatementDef;
import io.micronaut.sourcegen.model.TypeDef;
import io.micronaut.sourcegen.model.VariableDef;
import jakarta.inject.Inject;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.lang.model.element.Modifier;

public class Langchain4jConfigVisitor implements TypeElementVisitor<Lang4jConfig, Object> {
    public static final String CONFIG_PREFIX = "langchain4j.";
    private static final Map<String, String> MODEL_NAME_MAPPINGS = Map.of(
        "ChatLanguageModel", "ChatModel",
        "StreamingChatLanguageModel", "StreamingChatModel"
    );

    @Override
    public VisitorKind getVisitorKind() {
        return VisitorKind.ISOLATING;
    }

    @Override
    public Set<String> getSupportedAnnotationNames() {
        return Set.of(Lang4jConfig.class.getName());
    }

    @Override
    public void visitClass(ClassElement element, VisitorContext context) {
        SourceGenerator generator = SourceGenerators.findByLanguage(context.getLanguage()).orElse(null);
        AnnotationValue<Lang4jConfig> lang4jConfig = element.findAnnotation(Lang4jConfig.class).orElse(null);
        String packageName = element.getPackageName();
        if (generator != null && lang4jConfig != null) {
            List<PropertyConfig> properties = lang4jConfig.getAnnotations("properties", Lang4jConfig.Property.class)
                .stream().map(PropertyConfig::new).toList();
            List<PropertyConfig> commonProperties = properties
                .stream().filter(p -> p.common && !p.injected)
                .toList();


            List<AnnotationValue<Model>> providers = lang4jConfig.getAnnotations("models", Model.class);
            RecordDef commonConfig = buildCommonConfig(element, context, commonProperties, providers, packageName, generator);

            for (AnnotationValue<Model> provider : providers) {
                ModelConfig modelConfig = getModelConfig(element, context, provider);
                String[] requiredInjects = properties.stream().filter(p -> p.required && p.injected)
                    .map(p -> p.name).toArray(String[]::new);
                String[] optionalInjects = properties.stream().filter(p -> !p.required && p.injected)
                    .map(p -> p.name).toArray(String[]::new);

                if (StringUtils.isNotEmpty(packageName)) {
                        String namedPrefix = modelConfig.getNamedPrefix();
                        String namedConfigSimpleName = "Named" + modelConfig.languageModel().getSimpleName() + "Configuration";
                        String namedConfigQualifiedName = packageName + "." + namedConfigSimpleName;
                        ClassDef namedConfigDef = buildNamedConfigurationDef(
                            namedPrefix,
                            namedConfigQualifiedName,
                            modelConfig.languageModel(),
                            modelConfig.builderType,
                            requiredInjects,
                            optionalInjects,
                            commonConfig
                        );
                        writeJavaSource(generator, context, element, packageName, namedConfigSimpleName, namedConfigDef, modelConfig.modelKind);

                        String defaultPrefix = modelConfig.getDefaultPrefix();
                        String defaultConfigSimpleName = "Default" + modelConfig.languageModel().getSimpleName() + "Configuration";
                        String defaultConfigQualifiedName = packageName + "." + defaultConfigSimpleName;
                        ClassDef defaultConfigDef = buildDefaultConfigurationDef(
                            defaultPrefix,
                            defaultConfigQualifiedName,
                            modelConfig.languageModel(),
                            modelConfig.builderType,
                            requiredInjects,
                            optionalInjects,
                            commonConfig
                        );

                        writeJavaSource(generator, context, element, packageName, defaultConfigSimpleName, defaultConfigDef, modelConfig.modelKind);

                        String factorySimpleName = modelConfig.languageModel().getSimpleName() + "Factory";

                        String factoryName = packageName + "." + factorySimpleName;
                        ClassDef factoryDef = buildFactory(
                            namedConfigDef,
                            defaultConfigDef,
                            modelConfig.builderType,
                            factoryName,
                            modelConfig.languageModel(),
                            modelConfig.languageModelKind()
                        );

                        writeJavaSource(generator, context, element, packageName, factorySimpleName, factoryDef, modelConfig.modelKind);

                }
            }
        }
    }

    private static RecordDef buildCommonConfig(ClassElement element, VisitorContext context, List<PropertyConfig> commonProperties, List<AnnotationValue<Model>> providers, String packageName, SourceGenerator generator) {
        RecordDef commonConfig = null;
        if (!commonProperties.isEmpty() && !providers.isEmpty()) {
            ModelConfig firstConfig = getModelConfig(element, context, providers.iterator().next());
            String commonConfigSimpleName = "Common" + firstConfig.languageModel().getSimpleName() + "Configuration";
            String prefix = firstConfig.getCommonPrefix();
            RecordDef.RecordDefBuilder recordDefBuilder = RecordDef.builder(packageName + "." + commonConfigSimpleName)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Context.class)
                .addAnnotation(AnnotationDef.builder(Requires.class)
                    .addMember("property", prefix)
                    .build())
                .addAnnotation(AnnotationDef.builder(Requires.class)
                    .addMember("property", prefix + ".enabled")
                    .addMember("value", StringUtils.TRUE)
                    .addMember("defaultValue", StringUtils.TRUE)
                    .build())
                .addAnnotation(AnnotationDef.builder(ConfigurationProperties.class)
                    .addMember("value", prefix)
                    .build());
            ClassElement builderType = firstConfig.builderType();
            recordDefBuilder.addProperty(PropertyDef.builder("enabled").ofType(boolean.class)
                    .addAnnotation(AnnotationDef.builder(Bindable.class)
                        .addMember("defaultValue", StringUtils.TRUE).build())
                .build());

            for (PropertyConfig property : commonProperties) {
                builderType.getEnclosedElement(
                        ElementQuery.ALL_METHODS
                                .onlyInstance()
                                .onlyDeclared()
                                .onlyAccessible()
                                .named(n -> n.equals(property.name))
                                .filter(m -> !m.getGenericReturnType().isVoid() && m.hasParameters())
                ).ifPresent(methodElement -> {
                    PropertyDef.PropertyDefBuilder builder = PropertyDef.builder(property.name);
                    if (property.defaultValue != null) {
                        builder.addAnnotation(
                            AnnotationDef.builder(Bindable.class)
                            .addMember("defaultValue", property.defaultValue).build()
                        );
                    }
                    if (!property.required) {
                        builder.addAnnotation(Nullable.class);
                    }
                    recordDefBuilder.addProperty(
                            builder.ofType(TypeDef.of(methodElement.getParameters()[0].getGenericType()))
                                   .build()
                    );
                });
            }
            commonConfig = recordDefBuilder.build();
            writeJavaSource(
                generator,
                context,
                element,
                packageName,
                commonConfigSimpleName,
                commonConfig,
                firstConfig.modelKind
            );

        }
        return commonConfig;
    }

    private static ModelConfig getModelConfig(ClassElement element, VisitorContext context, AnnotationValue<Model> provider) {
        ClassElement languageModel = provider.stringValue("impl")
            .flatMap(context::getClassElement)
            .orElse(null);
        ClassElement languageModelKind = provider.stringValue("kind")
            .flatMap(context::getClassElement)
            .orElse(null);

        if (languageModelKind == null) {
            throw new ProcessingException(element, "@ModelProvider kind must be on the compilation classpath");
        }
        if (languageModel == null) {
            throw new ProcessingException(element, "@ModelProvider kind must be on the compilation classpath");
        }
        return new ModelConfig(languageModel, languageModelKind);
    }

    private ClassDef buildFactory(
        ClassDef namedConfigDef,
        ClassDef defaultConfigDef,
        ClassElement builderType,
        String factoryName,
        ClassElement languageModel,
        ClassElement languageModelKind) {
        ClassTypeDef namedConfigDefTypeDef = namedConfigDef.asTypeDef();
        ClassTypeDef builderTypeDef = ClassTypeDef.of(builderType);
        List<StatementDef> getBuilderStatement = List.of(
            new StatementDef.Return(new VariableDef.CallInstanceMethod(
                new VariableDef.MethodParameter("config", namedConfigDefTypeDef),
                MethodDef.builder("getBuilder").returns(TypeDef.of(builderType)).build()
            ))
        );
        return ClassDef.builder(factoryName)
            .addModifiers(Modifier.PUBLIC)
            .addAnnotation(Factory.class)
            .addMethod(
                MethodDef.builder("namedBuilder")
                    .addModifiers(Modifier.PROTECTED)
                    .addAnnotation(AnnotationDef.builder(EachBean.class)
                        .addMember("value", new VariableDef.StaticField(namedConfigDefTypeDef, "class", TypeDef.of(Class.class)))
                        .build())
                    .addParameter("config", namedConfigDefTypeDef)
                    .addStatements(getBuilderStatement)
                    .returns(TypeDef.of(builderType))
                    .build()
            )
            .addMethod(
                MethodDef.builder("primaryBuilder")
                    .addModifiers(Modifier.PROTECTED)
                    .addAnnotation(Bean.class)
                    .addAnnotation(Primary.class)
                    .addAnnotation(AnnotationDef.builder(Requires.class)
                        .addMember("beans", new VariableDef.StaticField(defaultConfigDef.asTypeDef(), "class", TypeDef.of(Class.class)))
                        .build())
                    .addParameter("config", defaultConfigDef.asTypeDef())
                    .addStatements(getBuilderStatement)
                    .returns(TypeDef.of(builderType))
                    .build()
            ).addMethod(
                MethodDef.builder("model")
                    .addModifiers(Modifier.PROTECTED)
                    .addAnnotation(Context.class)
                    .addAnnotation(AnnotationDef.builder(Bean.class)
                        .addMember("typed", new VariableDef.StaticField(ClassTypeDef.of(languageModelKind.getRawClassElement().getName()), "class", TypeDef.of(Class.class)))
                        .build())
                    .addAnnotation(AnnotationDef.builder(EachBean.class)
                        .addMember("value", new VariableDef.StaticField(builderTypeDef, "class", TypeDef.of(Class.class)))
                        .build())
                    .addParameter("builder", builderTypeDef)
                    .addStatements(List.of(
                        new StatementDef.Return(new VariableDef.CallInstanceMethod(
                            new VariableDef.MethodParameter("builder", builderTypeDef),
                            MethodDef.builder("build").returns(ClassTypeDef.of(languageModel)).build()
                        ))
                    ))
                    .returns(ClassTypeDef.of(languageModel))
                    .build()
            ).build();
    }

    private static void writeJavaSource(
        SourceGenerator generator,
        VisitorContext context,
        ClassElement element,
        String packageName,
        String simpleName,
        ObjectDef classDef,
        String modelKind) {
        context.visitGeneratedSourceFile(packageName, simpleName, element)
            .ifPresent(sourceFile -> {
                try {
                    sourceFile.write(
                        writer -> generator.write(classDef, writer)
                    );
                } catch (IOException e) {
                    throw new ProcessingException(element, "Error generating " + modelKind + "Configuration: " + e.getMessage());
                }
            });
    }

    private static ClassDef buildNamedConfigurationDef(String prefix, String configurationClassName, ClassElement model, ClassElement builderType, String[] requiredInjects, String[] optionalInjects, RecordDef commonConfig) {
        FieldDef prefixField = FieldDef.builder("PREFIX")
            .ofType(TypeDef.of(String.class))
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
            .initializer(new ExpressionDef.Constant(TypeDef.of(String.class), prefix)).build();
        String[] allExcludes = ArrayUtils.concat(requiredInjects, optionalInjects);
        ClassDef.ClassDefBuilder classDefBuilder = ClassDef.builder(configurationClassName)
            .addModifiers(Modifier.PUBLIC)
            .addAnnotation(AnnotationDef.builder(EachProperty.class)
                .addMember("value", prefix)
                .build()
            )
            .addAnnotation(Context.class)
            .addField(prefixField)
            .addField(
                FieldDef.builder("builder")
                    .addAnnotation(AnnotationDef.builder(ConfigurationBuilder.class)
                        .addMember("prefixes", "")
                        .addMember("excludes", Arrays.asList(allExcludes))
                        .build())
                    .initializer(ExpressionDef.invokeStatic(
                        ClassTypeDef.of(model.getName()),
                        "builder",
                        List.of(),
                        TypeDef.of(builderType)
                    ))
                    .ofType(TypeDef.of(builderType))
                    .build()
            )
            .addMethod(MethodDef.builder("getBuilder")
                .returns(TypeDef.of(builderType))
                .addStatements(List.of(
                    new StatementDef.Return(new VariableDef.Field(new VariableDef.This(ClassTypeDef.of(configurationClassName)), "builder", TypeDef.of(builderType)))
                ))
                .build());

        addCommonConstructor(builderType, commonConfig, classDefBuilder);

        for (String requiredInject : requiredInjects) {
            addInjectionPoint(builderType, requiredInject, true, classDefBuilder);
        }
        for (String optionalInject : optionalInjects) {
            addInjectionPoint(builderType, optionalInject, false, classDefBuilder);
        }

        Optional<MethodElement> modelNameMethod = builderType.getEnclosedElement(
            ElementQuery.ALL_METHODS.named("modelName").onlyAccessible().onlyInstance());
        if (modelNameMethod.isPresent()) {
            classDefBuilder.addMethod(MethodDef.builder("<init>").addParameter(
                ParameterDef.builder("modelName", TypeDef.of(String.class))
                    .addAnnotation(Parameter.class)
                    .build()
            ).addStatements(
                List.of(
                    ExpressionDef.invoke(
                        new VariableDef.Local("builder", TypeDef.of(builderType)),
                        "modelName",
                        List.of(new VariableDef.MethodParameter("modelName", TypeDef.of(String.class))),
                        TypeDef.of(void.class)
                    )
                )
            ).build());
        }
        return classDefBuilder
            .build();
    }

    private static void addCommonConstructor(ClassElement builderType, RecordDef commonConfig, ClassDef.ClassDefBuilder classDefBuilder) {
        if (commonConfig != null) {
            classDefBuilder.addAnnotation(AnnotationDef.builder(Requires.class)
                .addMember("beans", new VariableDef.StaticField(commonConfig.asTypeDef(), "class", TypeDef.of(Class.class)))
                .build());
            MethodDef.MethodDefBuilder constructorBuilder = MethodDef.builder("<init>")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(ParameterDef.builder("config", commonConfig.asTypeDef()).build());
            List<PropertyDef> properties = commonConfig.getProperties();
            for (PropertyDef property : properties) {
                if (property.getName().equals("enabled")) {
                    continue;
                }
                constructorBuilder.addStatement(
                    ExpressionDef.invoke(
                        new VariableDef.Local("builder", TypeDef.of(builderType)),
                        property.getName(),
                        List.of(ExpressionDef.invoke(
                            new VariableDef.MethodParameter("config", commonConfig.asTypeDef()),
                            property.getName(),
                            List.of(),
                            property.getType()
                        )),
                        TypeDef.of(void.class)
                    )
                );
            }
            classDefBuilder.addMethod(
                constructorBuilder
                    .build()
            );
        }
    }

    private static ClassDef buildDefaultConfigurationDef(String prefix, String configurationClassName, ClassElement model, ClassElement builderType, String[] requiredInjects, String[] optionalInjects, RecordDef commonConfig) {
        FieldDef prefixField = FieldDef.builder("PREFIX")
            .ofType(TypeDef.of(String.class))
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
            .initializer(new ExpressionDef.Constant(TypeDef.of(String.class), prefix)).build();
        String[] allExcludes = ArrayUtils.concat(requiredInjects, optionalInjects);
        ClassDef.ClassDefBuilder classDefBuilder = ClassDef.builder(configurationClassName)
            .addModifiers(Modifier.PUBLIC)
            .addAnnotation(AnnotationDef.builder(ConfigurationProperties.class)
                .addMember("value", prefix)
                .build()
            )
            .addAnnotation(Context.class)
            .addField(prefixField)
            .addField(
                FieldDef.builder("builder")
                    .addAnnotation(AnnotationDef.builder(ConfigurationBuilder.class)
                        .addMember("prefixes", "")
                        .addMember("excludes", Arrays.asList(allExcludes))
                        .build())
                    .initializer(ExpressionDef.invokeStatic(
                        ClassTypeDef.of(model.getName()),
                        "builder",
                        List.of(),
                        TypeDef.of(builderType)
                    ))
                    .ofType(TypeDef.of(builderType))
                    .build()
            )
            .addMethod(MethodDef.builder("getBuilder")
                .returns(TypeDef.of(builderType))
                .addStatements(List.of(
                    new StatementDef.Return(new VariableDef.Field(new VariableDef.This(ClassTypeDef.of(configurationClassName)), "builder", TypeDef.of(builderType)))
                ))
                .build());

        for (String requiredInject : requiredInjects) {
            addInjectionPoint(builderType, requiredInject, true, classDefBuilder);
        }
        for (String optionalInject : optionalInjects) {
            addInjectionPoint(builderType, optionalInject, false, classDefBuilder);
        }
        if (commonConfig == null) {
            classDefBuilder.addAnnotation(AnnotationDef.builder(Requires.class)
                .addMember("property", prefix)
                .build()
            );
        }
        addCommonConstructor(
            builderType,
            commonConfig,
            classDefBuilder
        );
        return classDefBuilder
            .build();
    }


    private static void addInjectionPoint(ClassElement builderType, String requiredInject, boolean isRequired, ClassDef.ClassDefBuilder classDefBuilder) {
        MethodElement methodElement = builderType.getEnclosedElement(
                ElementQuery.ALL_METHODS.named(requiredInject))
            .orElse(null);
        if (methodElement != null && methodElement.hasParameters()) {
            TypeDef typeToInject = TypeDef.of(methodElement.getParameters()[0].getGenericType());
            String methodName = methodElement.getName();
            ParameterDef.ParameterDefBuilder parameterDefBuilder = ParameterDef.builder(methodName, typeToInject);
            if (!isRequired) {
                parameterDefBuilder.addAnnotation(Nullable.class);
            }
            VariableDef.MethodParameter methodParameter = new VariableDef.MethodParameter(
                methodName,
                typeToInject
            );
            classDefBuilder.addMethod(
                MethodDef.builder(methodName)
                    .addModifiers(Modifier.PROTECTED)
                    .returns(void.class)
                    .addParameter(parameterDefBuilder.build())
                    .addAnnotation(Inject.class)
                    .addStatements(List.of(
                        ExpressionDef.invoke(
                            new VariableDef.Local("builder", TypeDef.of(builderType)),
                            methodName,
                            List.of(methodParameter),
                            TypeDef.of(void.class)
                        )
                    ))
                    .build()
            );
        }
    }

    private record ModelConfig(
        @NonNull ClassElement languageModel,
        @NonNull ClassElement languageModelKind,
        @NonNull ClassElement builderType,
        String modelKind,
        String modelSuffix,
        String modelName) {
        public ModelConfig(ClassElement languageModel, ClassElement languageModelKind) {
            this(
                languageModel,
                languageModelKind,
                resolveBuilder(languageModel),
                resolveModelKind(languageModelKind),
                resolveModelSuffix(languageModelKind),
                resolveModelName(languageModel, languageModelKind)
            );
        }

        private static String resolveModelSuffix(ClassElement languageModelKind) {
            return NameUtils.hyphenate(resolveModelKind(languageModelKind), true);
        }

        private static String resolveModelKind(ClassElement languageModelKind) {
            return MODEL_NAME_MAPPINGS.getOrDefault(languageModelKind.getSimpleName(), languageModelKind.getSimpleName());
        }

        private static String resolveModelName(ClassElement languageModel, ClassElement languageModelKind) {
            String modelKind = resolveModelKind(languageModelKind);
            String modelName = NameUtils.decapitalize(languageModel.getSimpleName());
            if (modelName.endsWith(modelKind)) {
                modelName = modelName.substring(0, modelName.length() - modelKind.length());
            }
            return modelName;
        }

        private static ClassElement resolveBuilder(ClassElement languageModel) {
            ClassElement builderType = languageModel.getEnclosedElement(
                ElementQuery.ALL_METHODS.onlyStatic().onlyAccessible().onlyConcrete().named("builder")
            ).map(MethodElement::getGenericReturnType).orElse(null);
            if (builderType == null) {
                throw new ProcessingException(null, "Model includes no builder() method: " + languageModel.getName());
            }
            return builderType;
        }

        public String getNamedPrefix() {
            return CONFIG_PREFIX + NameUtils.hyphenate(modelName, true) + '.' + modelSuffix + "s";
        }

        public String getDefaultPrefix() {
            return CONFIG_PREFIX + NameUtils.hyphenate(modelName, true) + "." + modelSuffix;
        }

        public String getCommonPrefix() {
            return CONFIG_PREFIX + NameUtils.hyphenate(modelName, true);
        }
    }

    @SuppressWarnings("ClassExplicitlyAnnotation")
    record PropertyConfig(
        String name,
        String defaultValue,
        boolean required,
        boolean injected,
        boolean common
    ) implements Lang4jConfig.Property {
        public PropertyConfig(AnnotationValue<Lang4jConfig.Property> annotation) {
            this(
                annotation.getRequiredValue("name", String.class),
                annotation.stringValue("defaultValue").orElse(null),
                annotation.booleanValue("required").orElse(false),
                annotation.booleanValue("injected").orElse(false),
                annotation.booleanValue("common").orElse(false)
            );
        }

        @Override
        public Class<? extends Annotation> annotationType() {
            return Lang4jConfig.Property.class;
        }
    }
}
