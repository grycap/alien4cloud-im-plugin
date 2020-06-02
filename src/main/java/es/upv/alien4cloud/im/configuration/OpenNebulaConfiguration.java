package es.upv.alien4cloud.im.configuration;

import alien4cloud.ui.form.annotation.FormLabel;
import alien4cloud.ui.form.annotation.FormProperties;
import alien4cloud.ui.form.annotation.FormPropertyDefinition;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@FormProperties({  "host" })
public class OpenNebulaConfiguration extends CloudConfigurationBase{

//    @FormLabel("username")
//    @FormPropertyDefinition(description = "User to authenticate to OpenNebula with.", type = "string", isRequired = true)
//    private String username;
//
//    @FormLabel("password secret key")
//    @FormPropertyDefinition(description = "The pssword.", type = "string", isRequired = true)
//    private String password;

    public OpenNebulaConfiguration() {

    }

    @FormLabel("host")
    @FormPropertyDefinition(description = "URL of the OpenNebula host.", type = "string", isRequired = true)
    private String host;


    @Override
    public String getAuthHeader(Map<String, String> vars) {
        return new StringBuilder().append("id = ").append(id).append("; ")
                .append("type = ").append(type).append("; ")
                .append("username = ").append(vars.get("username")).append("; ")
                .append("password = ").append(vars.get("password")).toString();
    }

}
