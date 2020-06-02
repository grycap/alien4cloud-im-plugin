package es.upv.alien4cloud.im.configuration;

import lombok.Getter;

import java.util.Map;

public abstract class CloudConfigurationBase {


    @Getter
    protected String type;
    @Getter
    protected String id;


    public abstract String getAuthHeader(Map<String, String> vars);
}
