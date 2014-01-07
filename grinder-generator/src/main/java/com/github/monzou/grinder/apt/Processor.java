package com.github.monzou.grinder.apt;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic.Kind;
import javax.tools.JavaFileObject;

/**
 * Processor
 */
@SupportedAnnotationTypes(Constants.ANNOTATION)
@SupportedOptions({ Constants.Options.PACKAGE, Constants.Options.PACKAGE_SUFFIX, Constants.Options.CLASS_SUFFIX })
public class Processor extends AbstractProcessor {

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

    private void generateMetaClass(TypeElement element, RoundEnvironment roundEnv) {

        String beanPackageName = packageNameOf(element);
        String beanClassName = simpleClassNameOf(element);
        String pkg = optionOf(Constants.Options.PACKAGE, null);
        if (pkg == null) {
            pkg = beanPackageName;
            String suffix = optionOf(Constants.Options.PACKAGE_SUFFIX, "meta");
            pkg = pkg == null || pkg.trim().length() == 0 ? suffix : String.format("%s.%s", pkg, suffix);
        }
        String className = String.format("%s%s", beanClassName, optionOf(Constants.Options.CLASS_SUFFIX, "Meta"));
        String fqn = String.format("%s.%s", pkg, className);

        debug("Generate meta class by grinder-generator: bean=%s,  meta=%s", classNameOf(element), fqn);
        try {
            JavaFileObject file = processingEnv.getFiler().createSourceFile(fqn, element);
            try (SourceWriter w = new SourceWriter(new BufferedWriter(file.openWriter()))) {
                w.println("package %s;", pkg);
                w.println();
                w.println("//CHECKSTYLE:OFF");
                w.println("import com.github.monzou.grinder.BeanProperty;");
                w.println("import %s;", classNameOf(element));
                w.println();
                w.println("/**");
                w.println(" * %s", className);
                w.println(" */");
                w.println("public class %s {", className);
                w.println();
                for (VariableElement e : ElementFilter.fieldsIn(element.getEnclosedElements())) {
                    if (e.getModifiers().contains(Modifier.STATIC)) {
                        continue;
                    }
                    String propertyType = typeOf(e.asType());
                    String propertyName = propertyNameOf(e);
                    w.println("    /** %s */", propertyName);
                    w.println("    public static final BeanProperty<%s, %s> %s = new BeanProperty<%s, %s>() {", beanClassName,
                            propertyType, propertyName, beanClassName, propertyType);
                    w.println();
                    w.println("        /** {@inheritDoc} */");
                    w.println("        @Override");
                    w.println("        public java.lang.String getName() {");
                    w.println("            return \"%s\";", propertyName);
                    w.println("        }");
                    w.println();
                    w.println("        /** {@inheritDoc} */");
                    w.println("        @Override");
                    w.println("        public %s apply(%s bean) {", propertyType, beanClassName);
                    w.println("            return bean.%s();", getterNameOf(e, e.asType()));
                    w.println("        }");
                    w.println();
                    w.println("        /** {@inheritDoc} */");
                    w.println("        @Override");
                    w.println("        public %s apply(%s bean, %s %s) {", beanClassName, beanClassName, propertyType, propertyName);
                    w.println("            bean.%s(%s);", setterNameOf(e, e.asType()), propertyName);
                    w.println("            return bean;");
                    w.println("        }");
                    w.println();
                    w.println("    };");
                    w.println();
                }
                w.println("}");
                w.flush();
            } catch (Exception e) { // SUPPRESS CHECKSTYLE
                throw new IOException(e);
            }
        } catch (IOException e) {
            error(e.toString());
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
        if (mirror.getKind().isPrimitive()) {
            return processingEnv.getTypeUtils().boxedClass((PrimitiveType) mirror).getQualifiedName().toString();
        }
        return mirror.toString();
    }

    private String propertyNameOf(VariableElement element) {
        return element.getSimpleName().toString();
    }

    private String getterNameOf(VariableElement element, TypeMirror mirror) {
        String propertyName = propertyNameOf(element);
        String capitalizedName = capitalize(propertyName);
        if (mirror.getKind().isPrimitive() && "boolean".equals(mirror.toString())) {
            return String.format("is%s", capitalizedName);
        } else {
            return String.format("get%s", capitalizedName);
        }
    }

    private String setterNameOf(VariableElement element, TypeMirror mirror) {
        String propertyName = propertyNameOf(element);
        String capitalizedName = capitalize(propertyName);
        return String.format("set%s", capitalizedName);
    }

    private String capitalize(String s) {
        return new StringBuilder().append(Character.toTitleCase(s.charAt(0))).append(s.substring(1)).toString();
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