package es.upv.indigodc.alien4cloud.im.plugin.configuration;

import alien4cloud.ui.form.annotation.FormProperties;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

/**
 * Container class that has all the fields that allow the user to configure an orchestrator
 * instance. These properties are exposed to the user, some of them with default values
 *
 * @author asalic
 */
@FormProperties({
//"clientId", "clientSecret", "tokenEndpoint", "tokenEndpointCert", "clientScopes", "iamHost", "iamHostCert",
    "orchestratorEndpoint", "orchestratorEndpointCert", 
    "orchestratorPollInterval", "importIndigoCustomTypes",
        "callbackUrl", "locationConfigurations"
})
public class CloudConfiguration extends CloudConfigurationBase {

//  @NotNull
//  private String clientId;
//  @NotNull
//  private String clientSecret;
//  @NotNull
//  private String tokenEndpoint;
//  @NotNull
//  private String tokenEndpointCert;
//  @NotNull
//  private String iamHost;
//  @NotNull
//  private String iamHostCert;
//  @NotNull
//  private String clientScopes;

  public CloudConfiguration() {
    type = "InfrastructureManager";
    id = "im";
  }
  
  @NotNull
  @Getter
  @Setter
  protected String imEndpoint;
  @NotNull
  @Getter
  @Setter
  protected String imEndpointCert;
  @Getter
  @Setter
  protected int imPollInterval;
  @NotNull
  @Getter
  @Setter
  protected String importIndigoCustomTypes;
  @NotNull
  @Getter
  @Setter
  protected String callbackUrl;

  @NotNull
  private LocationConfigurations locationConfigurations;

  @Override
  public String getAuthHeader(Map<String, String> vars) {
    return new StringBuilder().append("id = ").append(id).append("; ")
            .append("type = ").append(type).append("; ")
            .append("username = ").append(vars.get("username")).append("; ")
            .append("password = ").append(vars.get("password")).toString();
  }

}
