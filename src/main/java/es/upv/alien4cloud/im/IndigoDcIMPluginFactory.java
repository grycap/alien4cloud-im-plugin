package es.upv.alien4cloud.im;

import alien4cloud.model.orchestrators.ArtifactSupport;
import alien4cloud.model.orchestrators.locations.LocationSupport;
import alien4cloud.orchestrators.plugin.IOrchestratorPluginFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.google.common.collect.Maps;

import es.upv.alien4cloud.im.service.ArtifactRegistryService;
import es.upv.alien4cloud.im.location.OpenNebulaLocationConfigurator;
import es.upv.alien4cloud.im.location.OpenStackLocationConfigurator;
import es.upv.alien4cloud.im.configuration.CloudConfiguration;

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
            new String[] {OpenNebulaLocationConfigurator.LOCATION_TYPE, OpenStackLocationConfigurator.LOCATION_TYPE});
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
