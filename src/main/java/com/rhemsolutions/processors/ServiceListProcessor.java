package com.rhemsolutions.processors;

import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import com.google.web.bindery.requestfactory.shared.Service;

@SupportedAnnotationTypes("com.google.web.bindery.requestfactory.shared.Service")
public class ServiceListProcessor extends AbstractProcessor {

	@Override
	public boolean process(Set<? extends TypeElement> annotations,
			RoundEnvironment roundEnv) {
		
		Filer filer = processingEnv.getFiler();
		
        String className = null;
        String packageName = null;
        Map<String,String> methods = new HashMap<String,String>();
        
		for (Element e : roundEnv.getElementsAnnotatedWith(Service.class)) {
			if (e.getKind() == ElementKind.INTERFACE) {

                TypeElement classElement = (TypeElement) e;
                PackageElement packageElement = (PackageElement) classElement.getEnclosingElement();

                className = classElement.getSimpleName().toString();
                packageName = packageElement.getQualifiedName().toString();
                
                methods.put(className, methodName(className));
            }
		}
		
		if(methods.size() > 0){
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

            vc.put("methods", methods);
            vc.put("packageName", packageName);

            Template vt = ve.getTemplate("servicelist.vm");
            
            JavaFileObject jfo = null;
			try {
				jfo = filer.createSourceFile(packageName+".AppRequestFactory");
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
		return true;
	}
	
	private String methodName(String line)
	{
	  return Character.toLowerCase(line.charAt(0)) + line.substring(1);
	}

}
