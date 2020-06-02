package es.upv.alien4cloud.im;

import es.upv.alien4cloud.im.modifier.IaasModifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = { "es.upv.indigodc.alien4cloud.im.plugin" })
public class PluginFactoryConfiguration {

    @Bean(name = "indigodc-im-plugin-factory")
    public IndigoDcIMPluginFactory indigoDcIMPluginFactory() {
        return new IndigoDcIMPluginFactory();
    }

    @Bean(name = "iaas-modifier")
    public IaasModifier iaasPropertiesModifierBean() {return new IaasModifier();}
}
