package com.github.monzou.grinder.annotation.processor;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic.Kind;
import javax.tools.JavaFileObject;

import com.github.monzou.grinder.BeanProperty;
import com.github.monzou.grinder.BeanPropertyAccessor;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.base.Strings;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

/**
 * Grinder
 */
@SupportedAnnotationTypes(Constants.ANNOTATION)
@SupportedOptions({ Options.PACKAGE, Options.PACKAGE_SUFFIX, Options.CLASS_SUFFIX })
public class Grinder extends AbstractProcessor {

    /** {@inheritDoc} */
    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (roundEnv.processingOver()) {
            return true;
        }
        for (TypeElement annotation : annotations) {
            for (TypeElement element : ElementFilter.typesIn(roundEnv.getElementsAnnotatedWith(annotation))) {
                if (hasAnnotation(element)) {
                    generateMetaClass(element, roundEnv);
                }
            }
        }
        return true;
    }

    private boolean hasAnnotation(TypeElement element) {
        List<? extends AnnotationMirror> mirrors = element.getAnnotationMirrors();
        if (mirrors == null || mirrors.isEmpty()) {
            return false;
        }
        for (AnnotationMirror mirror : mirrors) {
            if (mirror.getAnnotationType().toString().equals(Constants.ANNOTATION)) {
                return true;
            }
        }
        return false;
    }

    private interface PropertyMetaData {

        static Predicate<PropertyMetaData> PROPERTY = new Predicate<Grinder.PropertyMetaData>() {
            @Override
            public boolean apply(PropertyMetaData input) {
                return input.canWrite();
            }
        };

        String getWrappedType();

        String getType();

        String getName();

        boolean canWrite();

    }

    private class VariableElementMetaData implements PropertyMetaData {

        final VariableElement element;

        VariableElementMetaData(VariableElement element) {
            this.element = element;
        }

        /** {@inheritDoc} */
        @Override
        public String getWrappedType() {
            return wrappedTypeOf(element.asType());
        }

        /** {@inheritDoc} */
        @Override
        public String getType() {
            return typeOf(element.asType());
        }

        /** {@inheritDoc} */
        @Override
        public String getName() {
            return element.getSimpleName().toString();
        }

        /** {@inheritDoc} */
        @Override
        public boolean canWrite() {
            return !element.getModifiers().contains(Modifier.FINAL);
        }

    }

    private class ExecutableElementMetaData implements PropertyMetaData {

        final ExecutableElement element;

        ExecutableElementMetaData(ExecutableElement element) {
            this.element = element;
        }

        /** {@inheritDoc} */
        @Override
        public String getWrappedType() {
            return wrappedTypeOf(getTypeMirror());
        }

        /** {@inheritDoc} */
        @Override
        public String getType() {
            return typeOf(getTypeMirror());
        }

        private TypeMirror getTypeMirror() {
            String methodName = getMethodName();
            if (methodName.startsWith("get") || methodName.startsWith("is")) {
                List<? extends VariableElement> params = element.getParameters();
                if (params.isEmpty()) {
                    return element.getReturnType();
                }
            } else if (methodName.startsWith("set")) {
                List<? extends VariableElement> params = element.getParameters();
                if (params.size() == 1) {
                    VariableElement param = params.iterator().next();
                    return param.asType();
                }
            }
            throw new IllegalStateException();
        }

        /** {@inheritDoc} */
        @Override
        public String getName() {
            String methodName = getMethodName();
            return uncapitalize(methodName.replaceAll("^(set|get|is)", ""));
        }

        /** {@inheritDoc} */
        @Override
        public boolean canWrite() {
            return getMethodName().startsWith("set");
        }

        String getMethodName() {
            return element.getSimpleName().toString();
        }

    }

    private Map<String, PropertyMetaData> collectMetaData(TypeElement element) {

        Map<String, PropertyMetaData> repository = Maps.newLinkedHashMap();

        // collect properties
        for (VariableElement e : ElementFilter.fieldsIn(element.getEnclosedElements())) {
            if (e.getModifiers().contains(Modifier.STATIC)) {
                continue;
            }
            VariableElementMetaData metaData = new VariableElementMetaData(e);
            repository.put(metaData.getName(), metaData);
        }

        // collect getter
        for (ExecutableElement e : ElementFilter.methodsIn(element.getEnclosedElements())) {
            if (e.getModifiers().contains(Modifier.STATIC) || !e.getModifiers().contains(Modifier.PUBLIC)) {
                continue;
            }
            ExecutableElementMetaData metaData = new ExecutableElementMetaData(e);
            String methodName = metaData.getMethodName();
            if (methodName.startsWith("get") || methodName.startsWith("is")) {
                String propertyName = metaData.getName();
                if (repository.containsKey(propertyName)) {
                    continue;
                }
                repository.put(propertyName, metaData);
            }
        }

        // collect setter
        for (ExecutableElement e : ElementFilter.methodsIn(element.getEnclosedElements())) {
            if (e.getModifiers().contains(Modifier.STATIC) || !e.getModifiers().contains(Modifier.PUBLIC)) {
                continue;
            }
            ExecutableElementMetaData metaData = new ExecutableElementMetaData(e);
            String methodName = metaData.getMethodName();
            if (methodName.startsWith("set")) {
                String propertyName = metaData.getName();
                if (repository.containsKey(propertyName)) {
                    repository.put(propertyName, metaData); // replace if exists
                }
            }
        }

        return repository;

    }

    private String toMetaClassName(String baseName) {
        String prefix = optionOf(Options.CLASS_PREFIX, null);
        String suffix = optionOf(Options.CLASS_SUFFIX, null);
        if (prefix == null && suffix == null) {
            suffix = "Meta";
        }
        String className = baseName;
        if (prefix != null) {
            className = String.format("%s%s", prefix, className);
        }
        if (suffix != null) {
            className = String.format("%s%s", className, suffix);
        }
        return className;
    }

    private void generateMetaClass(TypeElement element, RoundEnvironment roundEnv) {

        String beanPackageName = packageNameOf(element);
        String beanClassName = simpleClassNameOf(element);
        String pkg = optionOf(Options.PACKAGE, beanPackageName);
        String suffix = optionOf(Options.PACKAGE_SUFFIX, null);
        if (suffix != null) {
            pkg = pkg == null || pkg.trim().length() == 0 ? suffix : String.format("%s.%s", pkg, suffix);
        }
        String className = toMetaClassName(beanClassName);
        String fqn = String.format("%s.%s", pkg, className);

        debug("Generate meta class by grinder: bean=%s,  meta=%s", classNameOf(element), fqn);
        try {
            Map<String, PropertyMetaData> repository = collectMetaData(element);
            JavaFileObject file = processingEnv.getFiler().createSourceFile(fqn, element);
            try (SourceWriter w = new SourceWriter(new BufferedWriter(file.openWriter()))) {
                w.println("package %s;", pkg);
                w.println();
                w.println("//CHECKSTYLE:OFF");
                w.println("import %s;", List.class.getName());
                w.println("import %s;", Map.class.getName());
                w.println();
                w.println("import %s;", BeanProperty.class.getName());
                w.println("import %s;", BeanPropertyAccessor.class.getName());
                w.println("import %s;", classNameOf(element));
                w.println("import %s;", ImmutableList.class.getName());
                w.println("import %s;", ImmutableMap.class.getName());
                w.println("import %s;", ImmutableMap.Builder.class.getName().replaceAll("\\$", "."));
                w.println();
                w.println("/**");
                w.println(" * %s", className);
                w.println(" */");
                w.println("public class %s {", className);
                w.println();
                new PropertyWriter(beanClassName).write(w, repository);
                new AccessorsWriter(beanClassName).write(w, repository);
                w.println("}");
                w.flush();
            } catch (Exception e) { // SUPPRESS CHECKSTYLE
                throw new IOException(e);
            }
        } catch (IOException e) {
            error(e.toString());
        }

    }

    private class PropertyWriter {

        private final String beanName;

        PropertyWriter(String beanName) {
            this.beanName = beanName;
        }

        void write(SourceWriter w, Map<String, PropertyMetaData> repository) {

            for (PropertyMetaData metaData : repository.values()) {

                String propertyType = metaData.getWrappedType();
                String propertyName = metaData.getName();
                String className = metaData.canWrite() ? BeanProperty.class.getSimpleName() : BeanPropertyAccessor.class.getSimpleName();
                String typedClassName = String.format("%s<%s, %s>", className, beanName, propertyType);

                w.println("    /** %s */", propertyName);
                w.println("    public static final %s %s = new %s() {", typedClassName, propertyName, typedClassName);
                w.println();
                w.println("        /** {@inheritDoc} */");
                w.println("        @Override");
                w.println("        public java.lang.String getName() {");
                w.println("            return \"%s\";", propertyName);
                w.println("        }");
                w.println();
                w.println("        /** {@inheritDoc} */");
                w.println("        @Override");
                w.println("        public %s apply(%s bean) {", propertyType, beanName);
                w.println("            return bean.%s();", toGetterName(metaData.getType(), propertyName));
                w.println("        }");
                w.println();
                if (metaData.canWrite()) {
                    w.println("        /** {@inheritDoc} */");
                    w.println("        @Override");
                    w.println("        public %s apply(%s bean, %s %s) {", beanName, beanName, propertyType, propertyName);
                    w.println("            bean.%s(%s);", toSetterName(propertyName), propertyName);
                    w.println("            return bean;");
                    w.println("        }");
                    w.println();
                }
                w.println("    };");
                w.println();

            }

        }

    }

    private static class AccessorsWriter {

        private final String beanName;

        AccessorsWriter(String beanName) {
            this.beanName = beanName;
        }

        void write(SourceWriter w, Map<String, PropertyMetaData> repository) {
            w.println("    private static final Map<String, BeanProperty<? super %s, ?>> properties;", beanName);
            w.println();
            w.println("    private static final Map<String, BeanPropertyAccessor<? super %s, ?>> propertyAccessors;", beanName);
            w.println();
            w.println("    static {");
            w.println();
            w.println("        Builder<String, BeanProperty<? super %s, ?>> propertiesBuilder = ImmutableMap.builder();", beanName);
            for (PropertyMetaData property : Collections2.filter(repository.values(), PropertyMetaData.PROPERTY)) {
                w.println("        propertiesBuilder.put(\"%s\", %s);", property.getName(), property.getName());
            }
            w.println("        properties = propertiesBuilder.build();");
            w.println();
            w.println("        Builder<String, BeanPropertyAccessor<? super %s, ?>> propertyAccessorsBuilder = ImmutableMap.builder();", beanName);
            for (PropertyMetaData property : Collections2.filter(repository.values(), Predicates.not(PropertyMetaData.PROPERTY))) {
                w.println("        propertyAccessorsBuilder.put(\"%s\", %s);", property.getName(), property.getName());
            }
            w.println("        propertyAccessors = propertyAccessorsBuilder.build();");
            w.println();
            w.println("    }");
            w.println();
            w.println("    /**");
            w.println("     * Get all properties");
            w.println("     * ");
            w.println("     * @return properties");
            w.println("     */");
            w.println("    public static List<BeanProperty<? super %s, ?>> getProperties() {", beanName);
            w.println("        return ImmutableList.copyOf(properties.values());");
            w.println("    }");
            w.println();
            w.println("    /**");
            w.println("     * Get all property accessors");
            w.println("     * ");
            w.println("     * @return property accessors");
            w.println("     */");
            w.println("    public static List<BeanPropertyAccessor<? super %s, ?>> getPropertyAccessors() {", beanName);
            w.println("        ImmutableList.Builder<BeanPropertyAccessor<? super %s, ?>> builder = ImmutableList.builder();", beanName);
            w.println("        builder.addAll(properties.values());");
            w.println("        builder.addAll(propertyAccessors.values());");
            w.println("        return builder.build();");
            w.println("    }");
            w.println();
            w.println("    /**");
            w.println("     * Get the bean property by name");
            w.println("     * ");
            w.println("     * @param propertyName property name");
            w.println("     * @return {@link BeanProperty}");
            w.println("     */");
            w.println("    public static BeanProperty<? super %s, ?> getProperty(String propertyName) {", beanName);
            w.println("        return properties.get(propertyName);");
            w.println("    }");
            w.println();
            w.println("    /**");
            w.println("     * Get the bean property accessor by name");
            w.println("     * ");
            w.println("     * @param propertyName property name");
            w.println("     * @return {@link BeanPropertyAccessor}");
            w.println("     */");
            w.println("    public static BeanPropertyAccessor<? super %s, ?> getPropertyAccessor(String propertyName) {", beanName);
            w.println("        BeanPropertyAccessor<? super %s,?> property = getProperty(propertyName);", beanName);
            w.println("        if (property == null) {");
            w.println("            property = propertyAccessors.get(propertyName);");
            w.println("        }");
            w.println("        return property;");
            w.println("    }");
            w.println();
        }
    }

    private void debug(String format, Object... args) {
        log(Kind.NOTE, String.format(format, args));
    }

    private void error(String format, Object... args) {
        log(Kind.ERROR, String.format(format, args));
    }

    private void log(Kind kind, String message) {
        processingEnv.getMessager().printMessage(kind, message);
    }

    private String optionOf(String key, String defaultValue) {
        final String value = processingEnv.getOptions().get(key);
        return value == null ? defaultValue : value;
    }

    private String toGetterName(String propertyType, String propertyName) {
        String template = "boolean".equals(propertyType) ? "is%s" : "get%s";
        return String.format(template, capitalize(propertyName));
    }

    private String toSetterName(String propertyName) {
        return String.format("set%s", capitalize(propertyName));
    }

    private String packageNameOf(TypeElement element) {
        return processingEnv.getElementUtils().getPackageOf(element).getQualifiedName().toString();
    }

    private String classNameOf(TypeElement element) {
        return element.getQualifiedName().toString();
    }

    private String simpleClassNameOf(TypeElement element) {
        return element.getSimpleName().toString();
    }

    private String typeOf(TypeMirror mirror) {
        return mirror.toString();
    }

    private String wrappedTypeOf(TypeMirror mirror) {
        if (mirror.getKind().isPrimitive()) {
            return processingEnv.getTypeUtils().boxedClass((PrimitiveType) mirror).getQualifiedName().toString();
        }
        return typeOf(mirror);
    }

    private String capitalize(String s) {
        if (Strings.isNullOrEmpty(s)) {
            return s;
        }
        return new StringBuilder().append(Character.toTitleCase(s.charAt(0))).append(s.substring(1)).toString();
    }

    private String uncapitalize(String s) {
        if (Strings.isNullOrEmpty(s)) {
            return s;
        }
        return new StringBuilder().append(Character.toLowerCase(s.charAt(0))).append(s.substring(1)).toString();
    }

    private static class SourceWriter implements AutoCloseable {

        private final PrintWriter writer;

        SourceWriter(Writer writer) {
            this.writer = new PrintWriter(writer);
        }

        void println(String format, Object... args) {
            writer.println(String.format(format, args));
        }

        void println() {
            writer.println();
        }

        void flush() {
            writer.flush();
        }

        /** {@inheritDoc} */
        @Override
        public void close() throws Exception { // SUPPRESS CHECKSTYLE
            writer.close();
        }

    }

}