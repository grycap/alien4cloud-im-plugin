package es.upv.indigodc.alien4cloud.im.plugin.configuration;

import lombok.Getter;

import java.util.Map;

public abstract class CloudConfigurationBase {


    @Getter
    protected String type;
    @Getter
    protected String id;


    public abstract String getAuthHeader(Map<String, String> vars);
}
