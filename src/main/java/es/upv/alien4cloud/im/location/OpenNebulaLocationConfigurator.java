package es.upv.alien4cloud.im.location;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Getter
@Setter
@Component
@Scope("prototype")
public class OpenNebulaLocationConfigurator extends LocationConfigurator {

    public static final String LOCATION_TYPE = "OpenNebula";
    public static final String LOCATION_ID = "one";

    @PostConstruct
    public void init() {
        this.id = LOCATION_ID;
    }

    @Override
    public String getLocationType() {
        return LOCATION_TYPE;
    }
}
