package es.upv.indigodc.alien4cloud.im.plugin.location;

import alien4cloud.ui.form.annotation.FormLabel;
import alien4cloud.ui.form.annotation.FormProperties;
import alien4cloud.ui.form.annotation.FormPropertyDefinition;
import es.upv.indigodc.alien4cloud.im.plugin.configuration.OpenNebulaConfiguration;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Getter
@Setter
@Component
@Scope("prototype")
public class OpenNebulaLocationConfiguration extends LocationConfiguratorBase {

    public static final String LOCATION_TYPE = "OpenNebula";
    public static final String LOCATION_ID = "one";

    @PostConstruct
    public void init() {
        this.id = LOCATION_ID;
        this.type = LOCATION_TYPE;
    }

}
