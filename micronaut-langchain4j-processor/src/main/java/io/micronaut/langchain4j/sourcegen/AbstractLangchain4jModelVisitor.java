package io.micronaut.langchain4j.sourcegen;

import dev.langchain4j.model.chat.ChatLanguageModel;
import io.micronaut.context.annotation.Bean;
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
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.lang.model.element.Modifier;

public abstract sealed class AbstractLangchain4jModelVisitor<A extends Annotation> implements TypeElementVisitor<A, Object> permits ChatLanguageModelVisitor, ImageModelVisitor {
    public static final String CONFIG_PREFIX = "langchain4j.";

    private ClassDef buildFactory(ClassDef configurationDef, ClassElement builderType, String factoryName, ClassElement languageModel) {
        ClassTypeDef configTypeDef = configurationDef.asTypeDef();
        ClassTypeDef builderTypeDef = ClassTypeDef.of(builderType);
        return ClassDef.builder(factoryName)
            .addModifiers(Modifier.PUBLIC)
            .addAnnotation(Factory.class)
            .addMethod(
                MethodDef.builder("builder")
                    .addModifiers(Modifier.PROTECTED)
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
                MethodDef.builder("model")
                    .addModifiers(Modifier.PROTECTED)
                    .addAnnotation(AnnotationDef.builder(Bean.class)
                        .addMember("typed", new VariableDef.StaticField(TypeDef.of(lang4jType()), "class", TypeDef.of(Class.class)))
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

    protected abstract Class<?> lang4jType();

    private static void writeJavaSource(
        SourceGenerator generator,
        VisitorContext context,
        ClassElement element,
        String packageName,
        String simpleName,
        ClassDef classDef,
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

    private static ClassDef buildConfigurationDef(String prefix, String configurationClassName, ClassElement model, ClassElement builderType, String[] requiredInjects, String[] optionalInjects) {
        FieldDef prefixField = FieldDef.builder("PREFIX")
            .ofType(TypeDef.of(String.class))
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
            .initializer(new ExpressionDef.Constant(TypeDef.of(String.class), prefix)).build();
        String[] allExcludes = ArrayUtils.concat(requiredInjects, optionalInjects);
        ClassDef.ClassDefBuilder classDefBuilder = ClassDef.builder(configurationClassName)
            .addModifiers(Modifier.PUBLIC)
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
            AbstractLangchain4jModelVisitor.addInjectionPoint(builderType, requiredInject, true, classDefBuilder);
        }
        for (String optionalInject : optionalInjects) {
            AbstractLangchain4jModelVisitor.addInjectionPoint(builderType, optionalInject, false, classDefBuilder);
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

    @Override
    public VisitorKind getVisitorKind() {
        return VisitorKind.ISOLATING;
    }

    @Override
    public Set<String> getSupportedAnnotationNames() {
        return Set.of(annotationType().getName());
    }

    protected abstract Class<? extends Annotation> annotationType();

    protected abstract String configurationKey();

    protected abstract String getModelKind();

    @Override
    public void visitClass(ClassElement element, VisitorContext context) {
        SourceGenerator generator = SourceGenerators.findByLanguage(context.getLanguage()).orElse(null);
        if (generator != null) {
            String modelKind = getModelKind();
            String modelSuffix = configurationKey();
            Class<? extends Annotation> providerAnnotation = annotationType();
            ClassElement languageModel = element.stringValue(providerAnnotation)
                .flatMap(context::getClassElement)
                .orElse(null);
            String[] requiredInjects = element.stringValues(providerAnnotation, "requiredInject");
            String[] optionalInjects = element.stringValues(providerAnnotation, "optionalInject");
            String packageName = element.getPackageName();
            if (StringUtils.isNotEmpty(packageName) && languageModel != null) {

                String modelName = NameUtils.decapitalize(languageModel.getSimpleName());
                if (modelName.endsWith(modelKind)) {
                    modelName = modelName.substring(0, modelName.length() - modelKind.length());
                }
                ClassElement builderType = languageModel.getEnclosedElement(
                    ElementQuery.ALL_METHODS.onlyStatic().onlyAccessible().onlyConcrete().named("builder")
                ).map(MethodElement::getGenericReturnType).orElse(null);

                if (builderType != null) {

                    String prefix = CONFIG_PREFIX + NameUtils.hyphenate(modelName, true) + "." + modelSuffix;
                    String configurationClassSimpleName = languageModel.getSimpleName() + "Configuration";
                    String configurationClassName = packageName + "." + configurationClassSimpleName;
                    ClassDef configurationDef = AbstractLangchain4jModelVisitor.buildConfigurationDef(
                        prefix,
                        configurationClassName,
                        languageModel,
                        builderType,
                        requiredInjects,
                        optionalInjects
                    );
                    AbstractLangchain4jModelVisitor.writeJavaSource(generator, context, element, packageName, configurationClassSimpleName, configurationDef, modelKind);

                    String factorySimpleName = languageModel.getSimpleName() + "Factory";
                    String factoryName = packageName + "." + factorySimpleName;
                    ClassDef factoryDef = buildFactory(configurationDef, builderType, factoryName, languageModel);
                    AbstractLangchain4jModelVisitor.writeJavaSource(generator, context, element, packageName, factorySimpleName, factoryDef, modelKind);

                }
            }
        }
    }
}
