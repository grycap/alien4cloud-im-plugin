package es.upv.alien4cloud.im;


import es.upv.alien4cloud.im.service.model.StatusNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class UtilTest {
	
	@Test
	public void checkAllSupportedA4CDeploymentStatuses() throws StatusNotFoundException {
		for (IndigoDcDeploymentStatus val:IndigoDcDeploymentStatus.values()) {
			Util.indigoDcStatusToDeploymentStatus(val.name());
			Util.indigoDcStatusToInstanceStatus(val.name());
		}
	}
	
	
	@Test
	public void checkAllSupportedA4CDeploymentStatusesStatusNotFound() throws StatusNotFoundException {
			Assertions.assertThrows(StatusNotFoundException.class, () -> {Util.indigoDcStatusToDeploymentStatus("bad value");});
			Assertions.assertThrows(StatusNotFoundException.class, () -> {Util.indigoDcStatusToInstanceStatus("bad value");});
	}
	

}
