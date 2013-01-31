package com.rhemsolutions.processors;

import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.util.Properties;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.persistence.Entity;
import javax.tools.JavaFileObject;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

@SupportedAnnotationTypes("javax.persistence.Entity")
public class ResultBeanListProcessor extends AbstractProcessor {

	@Override
	public boolean process(Set<? extends TypeElement> annotations,
			RoundEnvironment roundEnv) {
		Filer filer = processingEnv.getFiler();
		
		String fqClassName = null;
        String className = null;
        String packageName = null;
        String packageResultBean = null;
        
		for (Element e : roundEnv.getElementsAnnotatedWith(Entity.class)) {
			if (e.getKind() == ElementKind.CLASS) {

                TypeElement classElement = (TypeElement) e;
                PackageElement packageElement = (PackageElement) classElement.getEnclosingElement();

                fqClassName = classElement.getQualifiedName().toString();
                className = classElement.getSimpleName().toString();
                packageName = packageElement.getQualifiedName().toString();
                
                packageResultBean = packageName.replaceFirst("domain", "resultbean");
                
                if (fqClassName != null) {

                    Properties props = new Properties();
                    URL url = this.getClass().getClassLoader().getResource("velocity.properties");
                    try {
        				props.load(url.openStream());
        			} catch (IOException e1) {
        				e1.printStackTrace();
        			}

                    VelocityEngine ve = new VelocityEngine(props);
                    ve.init();

                    VelocityContext vc = new VelocityContext();

                    vc.put("className", className);
                    vc.put("packageResultBean", packageResultBean);
                    vc.put("fqClassName", fqClassName);

                    Template vt = ve.getTemplate("resultbeanlist.vm");
                    
                    JavaFileObject jfo = null;
        			try {
        				jfo = filer.createSourceFile(fqClassName.replaceFirst("domain", "resultbean") + "ListLoadResultBean");
        			} catch (IOException e1) {
        				e1.printStackTrace();
        			}

                    Writer writer = null;
        			try {
        				writer = jfo.openWriter();
        			} catch (IOException e1) {
        				e1.printStackTrace();
        			}

                    vt.merge(vc, writer);

                    try {
        				writer.close();
        			} catch (IOException e1) {
        				e1.printStackTrace();
        			}
                    
                }

            }
		}
		return false;
	}
	
	@Override
	public SourceVersion getSupportedSourceVersion() {
		return SourceVersion.latest();
	}

}
