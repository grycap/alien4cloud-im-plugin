package es.upv.alien4cloud.im.service;

import alien4cloud.paas.model.PaaSTopologyDeploymentContext;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Inject;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.alien4cloud.tosca.editor.EditionContextManager;
import org.alien4cloud.tosca.exporter.ArchiveExportService;
import org.alien4cloud.tosca.model.Csar;
import org.alien4cloud.tosca.model.definitions.AbstractPropertyValue;
import org.alien4cloud.tosca.model.definitions.PropertyValue;
import org.alien4cloud.tosca.model.templates.Topology;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.DumperOptions;

/**
 * Manages the creation of the generation of the tosca topology in a format that is accepted by the
 * Orchestrator. It uses the TOSCA document as found in the TOSCA topology text editor.
 *
 * @author asalic
 */
@Service("builder-service")
@Slf4j
public class BuilderService {

  public static final String A4C_DEPLOYMENT_LOCATIONS_IDS_SEPARATOR = ",";
  public static final String METADATA_A4C_DEPLOYMENT_TOPOLOGY_ID = "a4c_deployment_topology_id";
  public static final String METADATA_A4C_DEPLOYMENT_ID = "a4c_deployment_id";
  public static final String METADATA_A4C_DEPLOYMENT_PAAS_ID = "a4c_deployment_paas_id";
  public static final String METADATA_A4C_DEPLOYMENT_ORCHESTRATOR_ID =
          "a4c_deployment_orchestrator_id";
  public static final String METADATA_A4C_DEPLOYMENT_ORCHESTRATOR_DEPLOYMENT_ID =
          "a4c_deployment_orchestrator_deployment_id";
  public static final String METADATA_A4C_DEPLOYMENT_LOCATIONS_ID = "a4c_deployment_location_ids";
  public static final String METADATA_A4C_DEPLOYMENT_VERSION_ID = "a4c_deployment_version_id";

  /** Gets the TOSCA topology in text format from the A4C topology editor. */
  @Inject protected ArchiveExportService exportService;
  /** Initializes the the manager of the TOSCA editor for a certain deployment. */
  @Inject protected EditionContextManager editionContextManager;
  /** Retrieves the configuration for the plugin. */
//  @Autowired
//  @Qualifier("cloud-configuration-manager")
//  protected CloudConfigurationManager cloudConfigurationHolder;
  /** The Orchestrator's accepted TOSCA YAML definition declaration. */
  public static final String TOSCA_DEFINITIONS_VERSION = "tosca_simple_yaml_1_0";
  /**
   * The options used by the TOSCA YAML writer to generate the text representation. of the topology
   * that is sent to the Orchestrator
   */
  protected static final DumperOptions dumperOptions;

  static {
    dumperOptions = new DumperOptions();
    dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
    dumperOptions.setIndent(4);
    dumperOptions.setDefaultScalarStyle(DumperOptions.ScalarStyle.PLAIN);
    // dumperOptions.setLineBreak(DumperOptions.LineBreak.valueOf("\\n"));
    dumperOptions.setLineBreak(DumperOptions.LineBreak.UNIX);
    dumperOptions.setPrettyFlow(true);
    dumperOptions.setCanonical(false);
  }

  /**
   * Describes the payload sent to the Orchestrator containing the TOSCA topology.
   *
   * @author asalic
   */
  @Data
  @NoArgsConstructor
  public static class Deployment {

    /** The textual representation of the TOSCA topology that will be sent to the Orchestrator. */
    protected String template;
    /** The inputs from the TOSCA topology. */
    protected Map<String, Object> parameters;
    /** The callback function used by the Orchestrator. */
    protected String callback;

    /**
     * Generates the text representation of the deployment of a topology as requested by the
     * Orchestrator.
     *
     * @return The payload that will be sent to the Orchestrator
     * @throws JsonProcessingException when parsing the JSON
     */
    public String toOrchestratorString() throws JsonProcessingException {
      ObjectMapper mapper = new ObjectMapper();
      return mapper.writeValueAsString(this).replace("\n", "\\n");
    }
  }

  /**
   * Takes the A4C textual representation of the TOSCA topology and encodes (comments) the TOSCA
   * methods as strings in order to be processed by YAML parser (which doesn't discern between a
   * TOSCA method and a a value).
   *
   * @param functionDb Keep an index of functions
   * @param a4cTopologyYaml The A4C topology that can be seen in the A4C text TOSCA editor
   * @return The A4C topology with the TOSCA methods commented
   */
  public static String encodeToscaMethods(Map<String, String> functionDb, String a4cTopologyYaml) {
    StringBuffer newa4cTopologyYaml = new StringBuffer();
    Pattern pattern =
            Pattern.compile("(:){1}(\\s)*(\\\'){0}(\\s)*(\\{){1}(.?)+(\\}){1}(\\s)*(\\\'){0}");
    Matcher matcher = pattern.matcher(a4cTopologyYaml);
    while (matcher.find()) {
      String group = matcher.group();
      //StringBuilder group = new StringBuilder();
      int posEnd = group.lastIndexOf("}");
      int posStart = group.indexOf("{");
      String uuid = UUID.randomUUID().toString();
      while (a4cTopologyYaml.indexOf(uuid) != -1) {
        uuid = UUID.randomUUID().toString();
      }
      String val = group.substring(0, posStart) + uuid + group.substring(posEnd + 1);
      //String function = group.substring(posStart, posEnd + 1);
      log.info("Found method: " + group + " replace with " + val);
      matcher.appendReplacement(newa4cTopologyYaml, val);
      functionDb.put(val, group);
    }
    matcher.appendTail(newa4cTopologyYaml);
    log.info("Topo with methods changed: " + newa4cTopologyYaml.toString());
    return newa4cTopologyYaml.toString();
  }

  /**
   * Converts the A4C topology to a modified version that the Orchestrator understands.
   *
   * @param a4cTopologyYaml The A4C topology that can be seen in the A4C text TOSCA editor
   * @param importIndigoCustomTypes The path to the repository (file) containing the TOSCA types
   *     used by the Orchestrator
   * @return The textual representation of the topology that will be sent to the Orchestrator
   * @throws JsonProcessingException when parsing the YAML topology
   * @throws IOException when parsing the YAML topology
   */
  public static String getIndigoDcTopologyYaml(
          PaaSTopologyDeploymentContext deploymentContext,
          String a4cTopologyYaml,
          String importIndigoCustomTypes)
          throws JsonProcessingException, IOException {
    ObjectMapper mapper =
            new ObjectMapper(
                    new YAMLFactory()
                            .enable(Feature.MINIMIZE_QUOTES)
                            .disable(Feature.WRITE_DOC_START_MARKER)
                            .disable(Feature.SPLIT_LINES)
                            .disable(Feature.CANONICAL_OUTPUT));
    //a4cTopologyYaml = a4cTopologyYaml.replaceAll("\"", "\'");
    Map<String, String> functionDb = new HashMap<>();
    String a4cTopologyYamlIgnoreMethods = encodeToscaMethods(functionDb, a4cTopologyYaml);
    ObjectNode rootNode = mapper.createObjectNode();
    ObjectNode root = (ObjectNode) mapper.readTree(a4cTopologyYamlIgnoreMethods);
    root.remove("tosca_definitions_version");
    rootNode.put("tosca_definitions_version", TOSCA_DEFINITIONS_VERSION);
    rootNode.setAll(root);
    root = rootNode;
    ((ObjectNode) root.get("topology_template")).remove("workflows");
    ObjectNode metadata = null;
    if (root.has("metadata")) {
      metadata = (ObjectNode) root.get("metadata");
    } else {
      metadata = mapper.createObjectNode();
    }
    root.remove("metadata");
    metadata.put(
            METADATA_A4C_DEPLOYMENT_TOPOLOGY_ID, deploymentContext.getDeploymentTopology().getId());
    metadata.put(METADATA_A4C_DEPLOYMENT_ID, deploymentContext.getDeploymentId());
    metadata.put(METADATA_A4C_DEPLOYMENT_PAAS_ID, deploymentContext.getDeploymentPaaSId());
    metadata.put(
            METADATA_A4C_DEPLOYMENT_ORCHESTRATOR_ID,
            deploymentContext.getDeployment().getOrchestratorId());
    metadata.put(
            METADATA_A4C_DEPLOYMENT_ORCHESTRATOR_DEPLOYMENT_ID,
            deploymentContext.getDeployment().getOrchestratorDeploymentId());
    metadata.put(
            METADATA_A4C_DEPLOYMENT_LOCATIONS_ID,
            String.join(
                    A4C_DEPLOYMENT_LOCATIONS_IDS_SEPARATOR,
                    deploymentContext.getDeployment().getLocationIds()));
    metadata.put(
            METADATA_A4C_DEPLOYMENT_VERSION_ID, deploymentContext.getDeployment().getVersionId());
    root.put("metadata", metadata);

    //    ObjectNode metadata = (ObjectNode) root.get("metadata");
    //    if (metadata == null) {
    //      metadata = mapper.createObjectNode();
    //      root.set("metadata", metadata);
    //    }
    //    metadata.put("a4c_deployment_paas_id", deploymentContext.getDeploymentPaaSId());
    //    metadata.put("a4c_deployment_id", deploymentContext.getDeploymentId());
    // TextNode description = (TextNode) root.get("description");

    //    ObjectMapper mapperJson = new ObjectMapper();
    //    if (description == null) {
    //      root.put("description", mapperJson.writeValueAsString(a4cOrchestratorInfo));
    //    } else {
    //      root.put("description", description.asText() +
    // mapperJson.writeValueAsString(a4cOrchestratorInfo));
    //    }
    root.remove("imports");
    ObjectNode imports = mapper.createObjectNode();
    imports.put("indigo_custom_types", importIndigoCustomTypes);
    root.putArray("imports").add(imports);
    ObjectNode tmp = (ObjectNode) root.get("topology_template").get("node_templates");
    Iterator<JsonNode> it = tmp.elements();
    while (it.hasNext()) {
      ObjectNode nodeTemplate = (ObjectNode) it.next();
      // Eliminate metadata info
      nodeTemplate.remove("metadata");

      JsonNode properties = rmNullProps(nodeTemplate.get("properties"));
      if (properties == null) {
        nodeTemplate.remove("properties");
      }

      // Change requirements (no type and the name of the requirement is the type)
      ArrayNode requirements = ((ArrayNode) nodeTemplate.get("requirements"));
      if (requirements != null) {
        Iterator<JsonNode> itRequirements = requirements.elements();
        while (itRequirements.hasNext()) {
          ObjectNode requirement = (ObjectNode) itRequirements.next();
          Entry<String, JsonNode> firstField = requirement.fields().next();
          if (firstField.getValue().has("type_requirement")) {
            requirement.remove(firstField.getKey());
            requirement.set(
                    firstField.getValue().get("type_requirement").asText(), firstField.getValue());
            ((ObjectNode) firstField.getValue()).remove("type_requirement");
          }
        }
      }
    }
    return toscaMethodsStrToMethod(functionDb, mapper.writer().writeValueAsString(root));
  }

  /**
   * Cleanse the properties of a node_template by removing those nodes with null value. This method
   * modifies the properties that it receives.
   *
   * @param properties The array of properties of a node_template (can include inherited ones). Can
   *     be null, in which case nothing is done, null is returned
   * @return The input parameter after modification, null if input is null
   */
  public static JsonNode rmNullProps(JsonNode properties) {
    if (properties != null) {
      if (properties.isObject()) {
        ObjectNode properties2 = (ObjectNode) properties;
        Iterator<Map.Entry<String, JsonNode>> itProperties = properties2.fields();
        while (itProperties.hasNext()) {
          Map.Entry<String, JsonNode> property = itProperties.next();
          if (property.getValue().isNull()) {
            itProperties.remove();
          }
        }
        return properties2.size() > 0 ? properties2 : null;
      } else {
        return properties;
      }
    } else {
      return null;
    }
  }

  /**
   * Executes the uncomment of the TOSCA methods that where commented using {@link
   * #encodeToscaMethods}.
   *
   * @param functionDb Keep an index of functions
   * @param topologyYaml The modified TOSCA topology that is accepted by the Orchestrator
   * @return The textual representation of the TOSCA topology with uncommented TOSCA methods
   */
  public static String toscaMethodsStrToMethod(Map<String, String> functionDb,
                                               String topologyYaml) {
    //    StringBuffer newTopologyYaml = new StringBuffer();
    //
    //    Pattern pattern =
    //        Pattern.compile(
    //            "(\n){0,1}(\\s)*(-){0,1}(\\s)*(\\\'){1}(\\s)*(\\{){1}(\\s)*"
    //                + "[a-zA-Z_\\-0-9]+(\\s)*(:){1}(\\s)*(.?)+(\\s)*(\\}){1}(\\s)*(\\\'){1}");
    //
    //    Matcher matcher = pattern.matcher(topologyYaml);
    //    while (matcher.find()) {
    //      StringBuilder group = new StringBuilder(matcher.group());
    //      int pos = group.lastIndexOf("\'");
    //      if (pos >= 0) {
    //        group.replace(pos, pos + 1, "");
    //      }
    //      pos = group.indexOf("\'");
    //      if (pos >= 0) {
    //        group.replace(pos, pos + 1, "");
    //      }
    //      matcher.appendReplacement(
    //          newTopologyYaml, group.toString().replaceAll("(\\n){0,1}(\\s)*(-){1}", ""));
    //    }
    //    matcher.appendTail(newTopologyYaml);
    for (Map.Entry<String, String> func: functionDb.entrySet()) {
      topologyYaml = topologyYaml.replaceAll(func.getKey(), func.getValue());
    }

    return topologyYaml;
  }

  /**
   * Creates the payload that will be sent to the Orchestrator.
   *
   * @param deploymentContext The deployment object that represents
   *     the whole deployment process,
   *     including the TOSCA topology represented as A4C Java classes
   * @param importIndigoCustomTypes The path to the repository (file) containing the TOSCA types
   *     used by the Orchestrator
   * @param callbackUrl The url used by the orchestrator
   *     for the callback when a deployment is made
   * @return The textual representation of the topology that will be sent to the Orchestrator
   * @throws IOException when parsing the YAML topology
   */
  public String buildApp(
          PaaSTopologyDeploymentContext deploymentContext,
          String importIndigoCustomTypes,
          String callbackUrl)
          throws IOException {
    Deployment deployment = new Deployment();
    deployment.setParameters(getParameters(deploymentContext));
    deployment.setCallback(callbackUrl);
    editionContextManager.init(
            deploymentContext.getDeploymentTopology().getInitialTopologyId());
    String a4cTopologyYaml =
            exportService.getYaml(getEditionContextManagerCsar(),
                    getEditionContextManagerTopology());
    //    Csar csar = csarService.get(deploymentContext
    //            .getDeploymentTopology().getArchiveName(),
    //            deploymentContext.getDeploymentTopology().getArchiveVersion());
    String yamlIndigoD =
            getIndigoDcTopologyYaml(deploymentContext, a4cTopologyYaml, importIndigoCustomTypes);
    deployment.setTemplate(yamlIndigoD);
    return deployment.toOrchestratorString();
  }

  protected Csar getEditionContextManagerCsar() {
    return EditionContextManager.getCsar();
  }

  protected Topology getEditionContextManagerTopology() {
    return EditionContextManager.getTopology();
  }

  /**
   * Generates the parameters needed by the Orchestrator from the inputs found in the TOSCA topology
   * generated by A4C.
   *
   * @param deploymentContext The deployment object that represents the whole deployment process,
   *     including the TOSCA topology represented as A4C Java classes
   * @return A map having the keys as the input name and the values as the corresponding A4C objects
   *     describing the TOSCA inputs
   */
  public Map<String, Object> getParameters(PaaSTopologyDeploymentContext deploymentContext) {
    final Map<String, Object> params = new HashMap<>();
    Map<String, AbstractPropertyValue> vals =
            deploymentContext.getDeploymentTopology().getAllInputProperties();
    for (Map.Entry<String, AbstractPropertyValue> v : vals.entrySet()) {
      if (v.getValue() instanceof PropertyValue) {
        params.put(v.getKey(), ((PropertyValue<?>) v.getValue()).getValue());
      } else {
        log.warn(String.format("Can't add property %s", v.getKey()));
      }
    }

    return params;
  }
}
