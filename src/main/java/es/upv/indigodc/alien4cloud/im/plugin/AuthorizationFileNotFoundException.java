package es.upv.indigodc.alien4cloud.im.plugin;

public class AuthorizationFileNotFoundException extends Exception {

    public AuthorizationFileNotFoundException(String autorizationFileName) {
        super("An authorization file with name " + autorizationFileName + " was not found. Please upload/create one in the TOSCA editor in the root folder along with the topology YAML file.");
    }
}
