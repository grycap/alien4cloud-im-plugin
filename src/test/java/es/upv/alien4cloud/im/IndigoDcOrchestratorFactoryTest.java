package es.upv.alien4cloud.im;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.BeanFactory;

import es.upv.alien4cloud.im.configuration.CloudConfiguration;
import es.upv.alien4cloud.im.location.LocationConfigurator;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class IndigoDcOrchestratorFactoryTest {
	
	protected static class IndigoDcOrchestratorFactoryBadPath  extends IndigoDcIMPluginFactory{
		
		@Override
		  protected String getCloudConfDefaultFile() {
			  return "fake";
		  }
	}
	
	@Test
	public void badPathToDefaultConf() throws NoSuchFieldException, SecurityException, Exception {
		IndigoDcIMPluginFactory fact = new IndigoDcIMPluginFactory();
		CloudConfiguration cc = fact.getDefaultConfiguration();
		Assertions.assertEquals(cc.getImportIndigoCustomTypes(), IndigoDcIMPluginFactory.NO_DEFAULT_CONF_FILE);
		Assertions.assertEquals(cc.getOrchestratorEndpoint(), IndigoDcIMPluginFactory.NO_DEFAULT_CONF_FILE);
		Assertions.assertEquals(cc.getOrchestratorEndpointCert(), IndigoDcIMPluginFactory.NO_DEFAULT_CONF_FILE);
		Assertions.assertEquals(cc.getOrchestratorEndpointCert(), IndigoDcIMPluginFactory.NO_DEFAULT_CONF_FILE);
		Assertions.assertEquals(cc.getOrchestratorPollInterval(), IndigoDcIMPluginFactory.NO_DEFAULT_CONF_FILE_POLL);
	}
	
	@Test
	public void destroy_call_orchestrator_destroy() {
		IndigoDcIMPluginFactory fact = new IndigoDcIMPluginFactory();
		IndigoDcIMPlugin orc = Mockito.mock(IndigoDcIMPlugin.class);
		final List<Integer> destExec = new ArrayList<>();
		Mockito.doAnswer((i) -> { destExec.add(0);return null;}).when(orc).destroy();
		fact.destroy(orc);
		Assertions.assertTrue(!destExec.isEmpty());
	}
	
	@Test
	public void getDeploymentPropertyDefinitions_empty() {
		IndigoDcIMPluginFactory fact = new IndigoDcIMPluginFactory();
		Assertions.assertTrue(fact.getDeploymentPropertyDefinitions().isEmpty());
	}
	
	@Test
	public void getLocationSupport_unique() {
		IndigoDcIMPluginFactory fact = new IndigoDcIMPluginFactory();
		Assertions.assertEquals(fact.getLocationSupport().getTypes().length, 1);
		Assertions.assertFalse(fact.getLocationSupport().isMultipleLocations());		
	}
	
	@Test
	public void getLocationSupport_is_LocationConfigurator_LOCATION_TYPE() {
		IndigoDcIMPluginFactory fact = new IndigoDcIMPluginFactory();
		Assertions.assertEquals(fact.getLocationSupport().getTypes()[0], LocationConfigurator.LOCATION_TYPE);
		
	}
	
	@Test
	public void getType_is_IndigoDcOrchestrator_TYPE() {
		IndigoDcIMPluginFactory fact = new IndigoDcIMPluginFactory();
		Assertions.assertEquals(fact.getType(), IndigoDcIMPlugin.TYPE);
		
	}
	
	@Test
	public void newInstance_creates_new_IndigoDcOrchestrator_with_default_opts() {
		IndigoDcIMPluginFactory fact = new IndigoDcIMPluginFactory();
		BeanFactory bf = Mockito.mock(BeanFactory.class);
		Mockito.when(bf.getBean(IndigoDcIMPlugin.class)).thenReturn(new IndigoDcIMPlugin());
		TestUtil.setPrivateField(fact, "beanFactory", bf);
		Assertions.assertEquals(fact.newInstance().getClass(), IndigoDcIMPlugin.class);
	}

}
