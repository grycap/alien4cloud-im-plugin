package es.upv.indigodc.alien4cloud.im.plugin;

import alien4cloud.model.orchestrators.ArtifactSupport;
import alien4cloud.model.orchestrators.locations.LocationSupport;
import alien4cloud.orchestrators.plugin.IOrchestratorPluginFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.google.common.collect.Maps;

import es.upv.indigodc.alien4cloud.im.plugin.location.OpenNebulaLocationConfiguration;
import es.upv.indigodc.alien4cloud.im.plugin.location.OpenStackLocationConfigurator;
import es.upv.indigodc.alien4cloud.im.plugin.service.ArtifactRegistryService;
import es.upv.indigodc.alien4cloud.im.plugin.configuration.CloudConfiguration;
import es.upv.indigodc.alien4cloud.im.plugin.location.LocationConfigurator;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.alien4cloud.tosca.model.definitions.PropertyDefinition;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component("indigodc-im-plugin-factory")
public class IndigoDcIMPluginFactory
    implements IOrchestratorPluginFactory<IndigoDcIMPlugin, CloudConfiguration> {

  public static final String CLOUD_CONFIGURATION_DEFAULTS_FILE =
      "/provider/cloud_conf_default.json";
  public static final String NO_DEFAULT_CONF_FILE =
      "Not set; No default conf file found in the package!";
  public static final int NO_DEFAULT_CONF_FILE_POLL = 5;

  @Autowired
  private BeanFactory beanFactory;

  @Autowired
  private ArtifactRegistryService artifactRegistryService;

  @Override
  public void destroy(IndigoDcIMPlugin arg0) {
    arg0.destroy();
  }

  @Override
  public ArtifactSupport getArtifactSupport() {
    return new ArtifactSupport(artifactRegistryService.getSupportedArtifactTypes());
  }

  @Override
  public Class<CloudConfiguration> getConfigurationType() {
    return CloudConfiguration.class;
  }

  @Override
  public CloudConfiguration getDefaultConfiguration() {
    ObjectMapper mapper = new ObjectMapper();
    InputStream is =
            IndigoDcIMPluginFactory.class.getResourceAsStream(getCloudConfDefaultFile());
    CloudConfiguration conf;
    try {
      conf = mapper.readValue(is, CloudConfiguration.class);
    } catch (IOException er) {
      er.printStackTrace();
      conf = new CloudConfiguration(
          NO_DEFAULT_CONF_FILE, NO_DEFAULT_CONF_FILE, NO_DEFAULT_CONF_FILE_POLL,
          NO_DEFAULT_CONF_FILE, NO_DEFAULT_CONF_FILE);
    }
    return conf;
  }

  protected String getCloudConfDefaultFile() {
    return CLOUD_CONFIGURATION_DEFAULTS_FILE;
  }

  @Override
  public Map<String, PropertyDefinition> getDeploymentPropertyDefinitions() {
    return Maps.newHashMap();
  }

  @Override
  public LocationSupport getLocationSupport() {
    return new LocationSupport(true,
            new String[] {OpenNebulaLocationConfiguration.LOCATION_TYPE, OpenStackLocationConfigurator.LOCATION_TYPE});
  }

  @Override
  public String getType() {
    return IndigoDcIMPlugin.TYPE;
  }

  @Override
  public IndigoDcIMPlugin newInstance() {
    return beanFactory.getBean(IndigoDcIMPlugin.class);
  }
}
