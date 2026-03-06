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
import io.micronaut.context.annotation.ConfigurationInject;
import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.Context;
import io.micronaut.context.annotation.EachBean;
import io.micronaut.context.annotation.EachProperty;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Parameter;
import io.micronaut.context.annotation.Primary;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.Internal;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
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
import io.micronaut.sourcegen.model.TypeDef;
import io.micronaut.sourcegen.model.VariableDef;
import jakarta.inject.Inject;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.Modifier;

@Internal
public class Langchain4jConfigVisitor implements TypeElementVisitor<Lang4jConfig, Object> {
    public static final String CONFIG_PREFIX = "langchain4j.";
    private static final Map<String, String> MODEL_NAME_MAPPINGS = Map.of(
        "ChatLanguageModel", "ChatModel",
        "StreamingChatLanguageModel", "StreamingChatModel"
    );
    private static final String CTOR_NAME = "<init>";
    private final Set<String> writtenSourceClasses = new HashSet<>();

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
                MethodElement modelNameMethod = modelConfig.builderType.getEnclosedElement(
                    ElementQuery.ALL_METHODS.named("modelName").onlyAccessible().onlyInstance()).orElse(null);
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
                        commonConfig,
                        modelNameMethod,
                        modelConfig.defaultModelName
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
                        modelConfig.builderMethod,
                        requiredInjects,
                        optionalInjects,
                        commonConfig,
                        modelNameMethod,
                        modelConfig.defaultModelName,
                        modelConfig.configRequired
                    );

                    writeJavaSource(generator, context, element, packageName, defaultConfigSimpleName, defaultConfigDef, modelConfig.modelKind);

                    String factorySimpleName = modelConfig.languageModel().getSimpleName() + "Factory";

                    String factoryName = packageName + "." + factorySimpleName;
                    if (context.getClassElement(factoryName).isEmpty()) {
                        ClassDef factoryDef = buildFactory(
                            namedConfigDef,
                            defaultConfigDef,
                            modelConfig.builderType,
                            factoryName,
                            modelConfig.languageModel(),
                            modelConfig.languageModelKind(),
                            modelConfig.exposed()
                        );
                        writeJavaSource(generator, context, element, packageName, factorySimpleName, factoryDef, modelConfig.modelKind);
                    }


                }
            }
        }
    }

    private RecordDef buildCommonConfig(ClassElement element, VisitorContext context, List<PropertyConfig> commonProperties, List<AnnotationValue<Model>> providers, String packageName, SourceGenerator generator) {
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
                    ClassElement propertyType = methodElement.getParameters()[0].getGenericType();
                    if (!property.required && !(propertyType.isPrimitive() && !propertyType.isArray())) {
                        builder.addAnnotation(Nullable.class);
                    }
                    recordDefBuilder.addProperty(
                        builder.ofType(TypeDef.of(propertyType))
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
        ClassElement exposedKind = provider.stringValue("exposed")
            .flatMap(context::getClassElement)
            .orElse(null);
        String defaultModelName = provider.stringValue("defaultModelName").orElse(null);
        boolean configRequired = provider.booleanValue("configRequired").orElse(false);

        if (languageModelKind == null) {
            throw new ProcessingException(element, "@ModelProvider kind must be on the compilation classpath");
        }
        if (languageModel == null) {
            throw new ProcessingException(element, "@ModelProvider kind must be on the compilation classpath");
        }
        return new ModelConfig(languageModel, languageModelKind, defaultModelName, exposedKind, configRequired);
    }

    private ClassDef buildFactory(
        ClassDef namedConfigDef,
        ClassDef defaultConfigDef,
        ClassElement builderType,
        String factoryName,
        ClassElement languageModel,
        ClassElement languageModelKind,
        @Nullable ClassElement exposed) {
        ClassTypeDef namedConfigDefTypeDef = namedConfigDef.asTypeDef();
        ClassTypeDef builderTypeDef = ClassTypeDef.of(builderType);
        MethodDef.MethodBodyBuilder methodBody = (aThis, methodParameters) -> {
            VariableDef.MethodParameter methodParameter = methodParameters.get(0);
            return methodParameter.invoke(
                MethodDef.builder("getBuilder").returns(TypeDef.of(builderType)).build()
            ).returning();
        };
        return ClassDef.builder(factoryName)
            .addModifiers(Modifier.PUBLIC)
            .addAnnotation(Factory.class)
            .addMethod(
                MethodDef.builder("namedBuilder")
                    .addModifiers(Modifier.PROTECTED)
                    .addAnnotation(AnnotationDef.builder(EachBean.class)
                        .addMember("value", new VariableDef.StaticField(ClassTypeDef.of(namedConfigDefTypeDef.getCanonicalName()), "class", TypeDef.of(Class.class)))
                        .build())
                    .addParameter("config", namedConfigDefTypeDef)
                    .returns(TypeDef.of(builderType))
                    .build(methodBody)
            )
            .addMethod(
                MethodDef.builder("primaryBuilder")
                    .addModifiers(Modifier.PROTECTED)
                    .addAnnotation(Bean.class)
                    .addAnnotation(Primary.class)
                    .addAnnotation(AnnotationDef.builder(Requires.class)
                        .addMember("beans", new VariableDef.StaticField(ClassTypeDef.of(defaultConfigDef.asTypeDef().getCanonicalName()), "class", TypeDef.of(Class.class)))
                        .build())
                    .addParameter("config", defaultConfigDef.asTypeDef())
                    .returns(TypeDef.of(builderType))
                    .build(methodBody)
            ).addMethod(
                MethodDef.builder("model")
                    .addModifiers(Modifier.PROTECTED)
                    .addAnnotation(Context.class)
                    .addAnnotation(AnnotationDef.builder(Bean.class)
                        .addMember("typed", new VariableDef.StaticField(ClassTypeDef.of(languageModelKind.getRawClassElement().getName()), "class", TypeDef.of(Class.class)))
                        .build())
                    .addAnnotation(AnnotationDef.builder(EachBean.class)
                        .addMember("value", new VariableDef.StaticField(ClassTypeDef.of(builderTypeDef.getCanonicalName()), "class", TypeDef.of(Class.class)))
                        .build())
                    .addParameter("builder", builderTypeDef)
                    .returns(exposed != null ? ClassTypeDef.of(exposed) : ClassTypeDef.of(languageModel))
                    .build((aThis, parameters) -> {
                        VariableDef.MethodParameter builder = parameters.get(0);
                        return builder.invoke(MethodDef.builder("build").returns(ClassTypeDef.of(languageModel)).build())
                            .returning();
                    })
            ).build();
    }

    private void writeJavaSource(
        SourceGenerator generator,
        VisitorContext context,
        ClassElement element,
        String packageName,
        String simpleName,
        ObjectDef classDef,
        String modelKind) {
        String generatedClassName = StringUtils.isNotEmpty(packageName) ? packageName + "." + simpleName : simpleName;
        if (writtenSourceClasses.contains(generatedClassName)) {
            return;
        }
        context.visitGeneratedSourceFile(packageName, simpleName, element)
            .ifPresent(sourceFile -> {
                try {
                    sourceFile.write(
                        writer -> generator.write(classDef, writer)
                    );
                    writtenSourceClasses.add(generatedClassName);
                } catch (IOException e) {
                    throw new ProcessingException(element, "Error generating " + modelKind + "Configuration: " + e.getMessage());
                }
            });
    }

    private static ClassDef buildNamedConfigurationDef(String prefix, String configurationClassName, ClassElement model, ClassElement builderType, String[] requiredInjects, String[] optionalInjects, RecordDef commonConfig, MethodElement modelNameMethod, String defaultModelName) {
        FieldDef prefixField = FieldDef.builder("PREFIX")
            .ofType(TypeDef.of(String.class))
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
            .initializer(new ExpressionDef.Constant(TypeDef.of(String.class), prefix)).build();
        String[] allExcludes = ArrayUtils.concat(requiredInjects, optionalInjects);
        FieldDef builderField = FieldDef.builder("builder")
            .addAnnotation(AnnotationDef.builder(ConfigurationBuilder.class)
                .addMember("prefixes", "")
                .addMember("excludes", Arrays.asList(allExcludes))
                .build())
            .initializer(
                ClassTypeDef.of(model.getName()).invokeStatic(
                    "builder",
                    List.of(),
                    TypeDef.of(builderType)
                ))
            .ofType(TypeDef.of(builderType))
            .build();
        ClassDef.ClassDefBuilder classDefBuilder = ClassDef.builder(configurationClassName)
            .addModifiers(Modifier.PUBLIC)
            .addAnnotation(AnnotationDef.builder(EachProperty.class)
                .addMember("value", prefix)
                .build()
            )
            .addAnnotation(Context.class)
            .addField(prefixField)
            .addField(
                builderField
            )
            .addMethod(MethodDef.builder("getBuilder")
                .returns(TypeDef.of(builderType))
                .build((aThis, parameters) ->
                    aThis.field("builder", TypeDef.of(builderType)).returning()
                ));

        addCommonConstructor(commonConfig, classDefBuilder, modelNameMethod, defaultModelName, false, builderField);

        for (String requiredInject : requiredInjects) {
            addInjectionPoint(builderType, requiredInject, true, classDefBuilder, builderField);
        }
        for (String optionalInject : optionalInjects) {
            addInjectionPoint(builderType, optionalInject, false, classDefBuilder, builderField);
        }

        return classDefBuilder
            .build();
    }

    private static void addCommonConstructor(
        RecordDef commonConfig,
        ClassDef.ClassDefBuilder classDefBuilder,
        MethodElement modelNameMethod,
        String defaultModelName,
        boolean isDefaultConfiguration,
        FieldDef builderField) {
        MethodDef.MethodDefBuilder constructorBuilder = MethodDef.builder(CTOR_NAME).addAnnotation(ConfigurationInject.class).addModifiers(Modifier.PUBLIC);
        if (commonConfig != null) {
            classDefBuilder.addAnnotation(AnnotationDef.builder(Requires.class)
                .addMember("beans", new VariableDef.StaticField(commonConfig.asTypeDef(), "class", TypeDef.of(Class.class)))
                .build());

            if (modelNameMethod != null && defaultModelName != null) {
                String modelNameMethodName = modelNameMethod.getName();
                constructorBuilder.addParameter(
                    ParameterDef.builder(modelNameMethodName, TypeDef.of(String.class))
                        .addAnnotation(AnnotationDef.builder(Bindable.class).addMember(
                            "defaultValue", defaultModelName
                        ).build())
                        .build()
                );
                constructorBuilder.addStatement(
                    addModelNameStatement(modelNameMethodName, builderField)
                );
            }
            constructorBuilder.addParameter(ParameterDef.builder("config", commonConfig.asTypeDef()).build());
            List<PropertyDef> properties = commonConfig.getProperties();
            for (PropertyDef property : properties) {
                if (property.getName().equals("enabled")) {
                    continue;
                }
                constructorBuilder.addStatement(
                    (aThis, parameters) -> aThis.field(builderField)
                            .invoke(
                                property.getName(),
                                TypeDef.of(void.class),
                                parameters
                                    .get(parameters.size() - 1).invoke(
                                        property.getName(),
                                        property.getType()
                                    )
                            )
                );
            }
            classDefBuilder.addMethod(
                constructorBuilder
                    .build()
            );
        } else if (modelNameMethod != null && !isDefaultConfiguration) {
            String modelNameMethodName = modelNameMethod.getName();
            classDefBuilder.addMethod(constructorBuilder.addParameter(
                ParameterDef.builder(modelNameMethodName, TypeDef.of(String.class))
                    .addAnnotation(Parameter.class)
                    .build()
            ).build(addModelNameStatement(modelNameMethodName, builderField)));
        }
    }

    private static MethodDef.MethodBodyBuilder addModelNameStatement(String modelNameMethodName, FieldDef builderField) {
        return (aThis, methodParameters) -> {
            VariableDef.MethodParameter modelNameParameter = methodParameters.get(0);
            return aThis.field(builderField)
                .invoke(
                    modelNameMethodName,
                    TypeDef.of(void.class),
                    modelNameParameter
                );
        };
    }

    private static ClassDef buildDefaultConfigurationDef(
        String prefix,
        String configurationClassName,
        ClassElement model,
        ClassElement builderType,
        MethodElement builderMethod,
        String[] requiredInjects,
        String[] optionalInjects,
        RecordDef commonConfig,
        MethodElement modelNameMethod,
        String defaultModelName, boolean configRequired) {
        FieldDef prefixField = FieldDef.builder("PREFIX")
            .ofType(TypeDef.of(String.class))
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
            .initializer(ExpressionDef.constant(prefix)).build();
        String[] allExcludes = ArrayUtils.concat(requiredInjects, optionalInjects);
        FieldDef builderField = FieldDef.builder("builder")
            .addAnnotation(AnnotationDef.builder(ConfigurationBuilder.class)
                .addMember("prefixes", "")
                .addMember("excludes", Arrays.asList(allExcludes))
                .build())
            .initializer(ClassTypeDef.of(model.getName()).invokeStatic(builderMethod))
            .ofType(TypeDef.of(builderType))
            .build();
        ClassDef.ClassDefBuilder classDefBuilder = ClassDef.builder(configurationClassName)
            .addModifiers(Modifier.PUBLIC)
            .addAnnotation(AnnotationDef.builder(ConfigurationProperties.class)
                .addMember("value", prefix)
                .build()
            )
            .addAnnotation(Context.class)
            .addField(prefixField)
            .addField(
                builderField
            )
            .addMethod(MethodDef.builder("getBuilder")
                .returns(TypeDef.of(builderType))
                .build((aThis, methodParameters) -> aThis.field(builderField).returning()));

        if (configRequired) {
            classDefBuilder.addAnnotation(AnnotationDef.builder(Requires.class).addMember("property", prefix).build());
        }

        for (String requiredInject : requiredInjects) {
            addInjectionPoint(builderType, requiredInject, true, classDefBuilder, builderField);
        }
        for (String optionalInject : optionalInjects) {
            addInjectionPoint(builderType, optionalInject, false, classDefBuilder, builderField);
        }
        if (commonConfig == null) {
            classDefBuilder.addAnnotation(AnnotationDef.builder(Requires.class)
                .addMember("property", prefix)
                .build()
            );
        }
        addCommonConstructor(
            commonConfig,
            classDefBuilder,
            modelNameMethod,
            defaultModelName,
            true,
            builderField
        );
        return classDefBuilder
            .build();
    }

    private static void addInjectionPoint(ClassElement builderType,
                                          String requiredInject,
                                          boolean isRequired,
                                          ClassDef.ClassDefBuilder classDefBuilder,
                                          FieldDef builderField) {
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
            classDefBuilder.addMethod(
                MethodDef.builder(methodName)
                    .addModifiers(Modifier.PROTECTED)
                    .returns(void.class)
                    .addParameter(parameterDefBuilder.build())
                    .addAnnotation(Inject.class)
                    .build((aThis, methodParameters) -> aThis.field(builderField).invoke(
                        methodElement,
                        methodParameters
                    ))
            );
        }
    }

    private record ModelConfig(
        @NonNull ClassElement languageModel,
        @NonNull ClassElement languageModelKind,
        @NonNull ClassElement builderType,
        @NonNull MethodElement builderMethod,
        @Nullable ClassElement exposed,
        String modelKind,
        String modelSuffix,
        String modelName,
        String defaultModelName,
        boolean configRequired) {
        public ModelConfig(ClassElement languageModel, ClassElement languageModelKind, String defaultModelName, ClassElement exposed, boolean configRequired) {
            this(
                languageModel,
                languageModelKind,
                resolveBuilder(languageModel),
                resolveBuilderMethod(languageModel),
                exposed,
                resolveModelKind(languageModelKind),
                resolveModelSuffix(languageModelKind),
                resolveModelName(languageModel, languageModelKind),
                defaultModelName,
                configRequired
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

        @SuppressWarnings("java:S2637")
        private static ClassElement resolveBuilder(ClassElement languageModel) {
            MethodElement methodElement = resolveBuilderMethod(languageModel);
            if (methodElement == null) {
                throw new ProcessingException(null, "Model includes no builder() method: " + languageModel.getName());
            }
            return methodElement.getGenericReturnType();
        }

        private static MethodElement resolveBuilderMethod(ClassElement languageModel) {
            return languageModel.getEnclosedElement(
                ElementQuery.ALL_METHODS.onlyStatic().onlyAccessible().onlyConcrete().named("builder")
            ).orElse(null);
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
