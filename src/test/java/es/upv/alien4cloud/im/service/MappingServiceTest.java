package es.upv.alien4cloud.im.service;


import alien4cloud.paas.model.PaaSTopologyDeploymentContext;
import es.upv.alien4cloud.im.service.model.DeploymentInfo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import alien4cloud.paas.model.DeploymentStatus;

import java.util.HashMap;

public class MappingServiceTest {
	
	@Test
	/**
	 * Add deployment and then retrieve it
	 */
	public void addDeploymentRetrieveIt() {
		MappingService ms = new MappingService();
		ms.init(new HashMap<String, PaaSTopologyDeploymentContext>());
		ms.registerDeployment(new DeploymentInfo("alienDeploymentPaasId", "orchestratorUuidDeployment",
		    "alienDeploymentId",
		    "orchestratorId", DeploymentStatus.DEPLOYED, null, null));
		DeploymentInfo odm = ms.getByA4CDeploymentPaasId("alienDeploymentPaasId");
		Assertions.assertEquals(odm.getOrchestratorDeploymentId(), "orchestratorUuidDeployment");
		DeploymentInfo adm = ms.getByOrchestratorDeploymentId("orchestratorUuidDeployment");
		Assertions.assertEquals(adm.getA4cDeploymentPaasId(), "alienDeploymentPaasId");
		Assertions.assertEquals(adm.getOrchestratorId(), "orchestratorId");
	}
}
