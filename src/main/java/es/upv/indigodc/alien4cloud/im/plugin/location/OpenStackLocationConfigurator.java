package es.upv.indigodc.alien4cloud.im.plugin.location;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Getter
@Setter
@Component
@Scope("prototype")
public class OpenStackLocationConfigurator extends LocationConfiguratorBase {


    public static final String LOCATION_TYPE = "OpenStack";
    public static final String LOCATION_ID = "ost";

    @PostConstruct
    public void init() {
        this.id = LOCATION_ID;
        this.type = LOCATION_TYPE;
    }


}
