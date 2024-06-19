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
package io.micronaut.langchain4j.sourcegen;

import io.micronaut.context.annotation.ConfigurationBuilder;
import io.micronaut.context.annotation.Context;
import io.micronaut.context.annotation.EachBean;
import io.micronaut.context.annotation.EachProperty;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Parameter;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.naming.NameUtils;
import io.micronaut.core.util.ArrayUtils;
import io.micronaut.core.util.StringUtils;
import io.micronaut.inject.ast.ClassElement;
import io.micronaut.inject.ast.ElementQuery;
import io.micronaut.inject.ast.MethodElement;
import io.micronaut.inject.processing.ProcessingException;
import io.micronaut.inject.visitor.TypeElementVisitor;
import io.micronaut.inject.visitor.VisitorContext;
import io.micronaut.langchain4j.annotation.ChatLanguageModelProvider;
import io.micronaut.sourcegen.generator.SourceGenerator;
import io.micronaut.sourcegen.generator.SourceGenerators;
import io.micronaut.sourcegen.model.AnnotationDef;
import io.micronaut.sourcegen.model.ClassDef;
import io.micronaut.sourcegen.model.ClassTypeDef;
import io.micronaut.sourcegen.model.ExpressionDef;
import io.micronaut.sourcegen.model.FieldDef;
import io.micronaut.sourcegen.model.MethodDef;
import io.micronaut.sourcegen.model.ParameterDef;
import io.micronaut.sourcegen.model.StatementDef;
import io.micronaut.sourcegen.model.TypeDef;
import io.micronaut.sourcegen.model.VariableDef;
import jakarta.inject.Inject;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.lang.model.element.Modifier;

public class ChatLanguageModelVisitor implements TypeElementVisitor<ChatLanguageModelProvider, Object> {
    @Override
    public VisitorKind getVisitorKind() {
        return VisitorKind.ISOLATING;
    }

    @Override
    public Set<String> getSupportedAnnotationNames() {
        return Set.of(ChatLanguageModelProvider.class.getName());
    }

    @Override
    public void visitClass(ClassElement element, VisitorContext context) {
        SourceGenerator generator = SourceGenerators.findByLanguage(context.getLanguage()).orElse(null);
        if (generator != null) {
            ClassElement chatLanguageModel = element.stringValue(ChatLanguageModelProvider.class)
                .flatMap(context::getClassElement)
                .orElse(null);
            String[] requiredInjects = element.stringValues(ChatLanguageModelProvider.class, "requiredInject");
            String[] optionalInjects = element.stringValues(ChatLanguageModelProvider.class, "optionalInject");
            String packageName = element.getPackageName();
            if (StringUtils.isNotEmpty(packageName) && chatLanguageModel != null) {
                String chatLanguageModelName = NameUtils.decapitalize(chatLanguageModel.getSimpleName());
                if (chatLanguageModelName.endsWith("ChatModel")) {
                    chatLanguageModelName = chatLanguageModelName.substring(0, chatLanguageModelName.length() - "ChatModel".length());
                }
                ClassElement builderType = chatLanguageModel.getEnclosedElement(
                    ElementQuery.ALL_METHODS.onlyStatic().onlyAccessible().onlyConcrete().named("builder")
                ).map(MethodElement::getGenericReturnType).orElse(null);

                if (builderType != null) {

                    String prefix = "langchain4j." + NameUtils.hyphenate(chatLanguageModelName, true) + ".chat-models";
                    String configurationClassSimpleName = chatLanguageModel.getSimpleName() + "Configuration";
                    String configurationClassName = packageName + "." + configurationClassSimpleName;
                    ClassDef configurationDef = buildConfigurationDef(
                        prefix,
                        configurationClassName,
                        chatLanguageModel,
                        builderType,
                        requiredInjects,
                        optionalInjects
                    );
                    context.visitGeneratedSourceFile(packageName, configurationClassSimpleName, element)
                        .ifPresent(sourceFile -> {
                            try {
                                sourceFile.write(
                                    writer -> generator.write(configurationDef, writer)
                                );
                            } catch (IOException e) {
                                throw new ProcessingException(element, "Error generating ChatModelConfiguration: " + e.getMessage());
                            }
                        });

                    String factorySimpleName = chatLanguageModel.getSimpleName() + "Factory";
                    String factoryName = packageName + "." + factorySimpleName;
                    ClassTypeDef configTypeDef = configurationDef.asTypeDef();
                    ClassTypeDef builderTypeDef = ClassTypeDef.of(builderType);
                    ClassDef factoryDef = ClassDef.builder(factoryName)
                        .addAnnotation(Factory.class)
                        .addMethod(
                            MethodDef.builder("builder")
                                .addAnnotation(AnnotationDef.builder(EachBean.class)
                                    .addMember("value", new VariableDef.StaticField(configTypeDef, "class", TypeDef.of(Class.class)))
                                    .build())
                                .addParameter("config", configTypeDef)
                                .addStatements(List.of(
                                    new StatementDef.Return(new VariableDef.CallInstanceMethod(
                                        new VariableDef.MethodParameter("config", configTypeDef),
                                        MethodDef.builder("getBuilder").returns(TypeDef.of(builderType)).build()
                                    ))
                                ))
                                .returns(TypeDef.of(builderType))
                                .build()
                        ).addMethod(
                            MethodDef.builder("chatModel")
                                .addAnnotation(AnnotationDef.builder(EachBean.class)
                                    .addMember("value", new VariableDef.StaticField(builderTypeDef, "class", TypeDef.of(Class.class)))
                                    .build())
                                .addParameter("builder", builderTypeDef)
                                .addStatements(List.of(
                                    new StatementDef.Return(new VariableDef.CallInstanceMethod(
                                        new VariableDef.MethodParameter("builder", builderTypeDef),
                                        MethodDef.builder("build").returns(ClassTypeDef.of(chatLanguageModel)).build()
                                    ))
                                ))
                                .returns(ClassTypeDef.of(chatLanguageModel))
                                .build()
                        ).build();
                    context.visitGeneratedSourceFile(packageName, factorySimpleName, element)
                        .ifPresent(sourceFile -> {
                            try {
                                sourceFile.write(
                                    writer -> generator.write(factoryDef, writer)
                                );
                            } catch (IOException e) {
                                throw new ProcessingException(element, "Error generating ChatModelConfiguration: " + e.getMessage());
                            }
                        });

                }
            }
        }
    }

    private static ClassDef buildConfigurationDef(String prefix, String configurationClassName, ClassElement chatLanguageModel, ClassElement builderType, String[] requiredInjects, String[] optionalInjects) {
        FieldDef prefixField = FieldDef.builder("PREFIX")
            .ofType(TypeDef.of(String.class))
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
            .initializer(new ExpressionDef.Constant(TypeDef.of(String.class), prefix)).build();
        String[] allExcludes = ArrayUtils.concat(requiredInjects, optionalInjects);
        ClassDef.ClassDefBuilder classDefBuilder = ClassDef.builder(configurationClassName)
            .addAnnotation(AnnotationDef.builder(EachProperty.class)
                .addMember("value", prefix)
                .addMember("primary", "default")
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
                        ClassTypeDef.of(chatLanguageModel.getName()),
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
}
