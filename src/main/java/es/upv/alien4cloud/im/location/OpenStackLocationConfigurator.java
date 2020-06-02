package es.upv.alien4cloud.im.location;

import alien4cloud.orchestrators.plugin.ILocationConfiguratorPlugin;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Getter
@Setter
@Component
@Scope("prototype")
public class OpenStackLocationConfigurator extends LocationConfigurator {


    public static final String LOCATION_TYPE = "OpenStack";
    public static final String LOCATION_ID = "ost";

    @PostConstruct
    public void init() {
        this.id = LOCATION_ID;
    }


    @Override
    public String getLocationType() {
        return LOCATION_TYPE;
    }
}
