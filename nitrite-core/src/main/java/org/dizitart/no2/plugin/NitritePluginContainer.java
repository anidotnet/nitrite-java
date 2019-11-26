package org.dizitart.no2.plugin;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Anindya Chatterjee
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PACKAGE)
public @interface NitritePluginContainer {
    Class<? extends NitritePlugin>[] plugins() default {};
}
