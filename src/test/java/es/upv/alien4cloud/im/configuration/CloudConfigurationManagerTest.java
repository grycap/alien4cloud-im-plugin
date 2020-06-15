package es.upv.alien4cloud.im.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

import es.upv.alien4cloud.im.IndigoDcIMPluginFactory;
import org.junit.jupiter.api.Test;

public class CloudConfigurationManagerTest {
	
	@Test
	public void multipleOrchestratorsSameConf() {
		IndigoDcIMPluginFactory fact = new IndigoDcIMPluginFactory();
		CloudConfiguration cc = fact.getDefaultConfiguration();
		CloudConfigurationManager ccm = new CloudConfigurationManager();
		ccm.addCloudConfiguration("1", cc);
		ccm.addCloudConfiguration("2", cc);
		CloudConfiguration cc3 = fact.getDefaultConfiguration();
		ccm.addCloudConfiguration("3", cc3);
		assertEquals(ccm.getCloudConfiguration("2"), cc);
		ccm.addCloudConfiguration("2", cc3);
		assertNotSame(ccm.getCloudConfiguration("2"), cc);
		assertEquals(ccm.getCloudConfiguration("2"), cc3);
	}

}
