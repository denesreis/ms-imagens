package com.scasistemas.msbluedot;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * Inicializador do Servlet para deploy como WAR em container externo (Tomcat
 * 10+).
 *
 * <p>
 * Necessário quando o projeto é empacotado como WAR e implantado em um servidor
 * de aplicação externo, como Apache Tomcat 10 ou Jakarta EE.
 * </p>
 */
public class ServletInitializer extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(MsBluedotApplication.class);
    }

}
