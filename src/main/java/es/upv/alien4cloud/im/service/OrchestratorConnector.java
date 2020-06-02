package es.upv.alien4cloud.im.service;

import alien4cloud.component.repository.IFileRepository;
import alien4cloud.utils.TreeNode;
import es.upv.alien4cloud.im.service.model.OrchestratorIamException;
import es.upv.alien4cloud.im.service.model.OrchestratorResponse;
import es.upv.alien4cloud.im.AuthorizationFileNotFoundException;
import es.upv.alien4cloud.im.configuration.CloudConfiguration;
import es.upv.alien4cloud.im.configuration.CloudConfigurationBase;
import lombok.extern.slf4j.Slf4j;
import org.alien4cloud.tosca.catalog.index.CsarService;
import org.alien4cloud.tosca.catalog.repository.ICsarRepositry;
import org.alien4cloud.tosca.editor.EditionContextManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.social.connect.ConnectionRepository;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Manage and do the calls to the REST API exposed by the IndigoDC Orchestrator; Connect to the
 * authorization system to obtain a token for the calls to the IndigoDC Orchestrator.
 *
 * @author asalic
 */
@Slf4j
@Service("orchestrator-connector")
public class OrchestratorConnector {

  /** Web service path for deployments operations; It is appended to the orchestrator endpoint. */
  public static final String WS_PATH_DEPLOYMENTS = "/infrastructures";

  public static final String IM_AUTORIZATION = "id = im; type = InfrastructureManager; username = %s; password = %s";

  private static final Logger LOGGER = Logger.getLogger(OrchestratorConnector.class.getName());

  @Autowired
  private CsarService csarService;

  @Autowired
  private ConnectionRepository connRepository;

  @Inject
  private ICsarRepositry csarRepositry;

  @Inject
  private IFileRepository artifactRepository;
  /**
   * Store the token information received after a successful call to the user authorization service
   * from DEEP.
   *
   * @author asalic
   */
//  @Data
//  public static class AccessToken {
//
//    /** The life of the token. */
//    private int life;
//    /** The actual access token. */
//    private String accessToken;
//
//    public AccessToken(int life, String accessToken) {
//      this.life = life;
//      this.accessToken = accessToken;
//    }
//
//    @Override
//    public String toString() {
//      return "AccessToken [life=" + life + ", accessToken=" + accessToken + "]";
//    }
//  }

  /**
   * Get the token used to access the Orchestrator.
   *
   * @param cloudConfiguration The configuration used for the plugin instance
   * @return the information received from the authorization service
   * @throws IOException when cannot read from the stream sent by the server or cannot send the
   *         data.
   * @throws NoSuchFieldException when cannot parse the JSOn response.
   * @throws OrchestratorIamException when response code from the orchestrator is not between 200
   *         and 299.
   */
//  public AccessToken obtainAuthTokens(CloudConfiguration cloudConfiguration, String userName,
//      String userPassword) throws IOException, NoSuchFieldException, OrchestratorIamException {
//
//    StringBuilder sbuf = new StringBuilder();
//    sbuf.append("grant_type=password&");
//    sbuf.append("client_id=").append(cloudConfiguration.getClientId()).append("&");
//    sbuf.append("client_secret=").append(cloudConfiguration.getClientSecret()).append("&");
//    sbuf.append("username=").append(userName).append("&");
//    sbuf.append("password=").append(userPassword).append("&");
//    sbuf.append("scope=").append(cloudConfiguration.getClientScopes().replaceAll(" ", "%20"));
//    URL requestUrl = new URL(cloudConfiguration.getTokenEndpoint());
//
//    AccessToken at = null;
//    SSLContext sslContext = getSslContext(cloudConfiguration);
//    OrchestratorResponse response =
//        restCall(requestUrl, sbuf.toString(), null, true, sslContext, HttpMethod.GET);
//    ObjectMapper mapper = new ObjectMapper();
//    Map<String, Object> map =
//        mapper.readValue(response.getResponse().toString(),
//            new TypeReference<Map<String, String>>() {});
//    at = new AccessToken(Integer.parseInt((String) map.get("expires_in")),
//        (String) map.get("access_token"));
//
//    return at;
//  }

  protected Map<String, String> generateHeaders(CloudConfiguration cloudConf) throws AuthorizationFileNotFoundException, IOException {
    Map<String, String> headers = new HashMap<>();
    headers.put("Accept", "application/json");
    headers.put("Content-Type", "application/json");
    //Csar csar = csarService.get(deploymentTopology.getArchiveName(), deploymentTopology.getArchiveVersion());
    //csarRepositry.getExpandedCSAR(deploymentTopology.getArchiveName(), deploymentTopology.getArchiveVersion());
    TreeNode root = EditionContextManager.get().getArchiveContentTree().getChildren().first();
    Optional<TreeNode> file =root.getChildren().stream().filter(el -> el.getName().equalsIgnoreCase("AUTHORIZATION")).findFirst();
    if (file.isPresent()) {
      InputStream is = artifactRepository.getFile(file.get().getArtifactId());
      BufferedInputStream bis = new BufferedInputStream(is);
      ByteArrayOutputStream buf = new ByteArrayOutputStream();
      int result = bis.read();
      while(result != -1) {
        buf.write((byte) result);
        result = bis.read();
      }
// StandardCharsets.UTF_8.name() > JDK 7
      String header = buf.toString("UTF-8");
      header = header.replaceAll("\\n", "\\\\n");
      headers.put("AUTHORIZATION", header);
    } else
      throw new AuthorizationFileNotFoundException("AUTHORIZATION");


    return headers;
  }

  /**
   * Obtain the list of deployments created by the the user with the client id stored in the cloud
   * configuration for the plugin.
   *
   * @param cloudConfiguration The configuration used for the plugin instance
   * @return The response from the orchestrator
   * @throws IOException when cannot read from the stream sent by the server or cannot send the
   *         data.
   * @throws NoSuchFieldException when cannot parse the JSOn response.
   * @throws OrchestratorIamException when response code from the orchestrator is not between 200
   *         and 299.
   */
  public OrchestratorResponse callGetDeployments(CloudConfiguration cloudConfiguration)
          throws IOException, NoSuchFieldException, OrchestratorIamException, AuthorizationFileNotFoundException {
    StringBuilder sbuf = new StringBuilder(cloudConfiguration.getImEndpoint());
    sbuf.append(WS_PATH_DEPLOYMENTS);

    URL requestUrl = new URL(sbuf.toString());

    SSLContext sslContext = getSslContext(cloudConfiguration);
    return restCall(requestUrl, null, generateHeaders(cloudConfiguration),
            isUrlSecured(cloudConfiguration.getImEndpoint()), sslContext, HttpMethod.GET);
  }

  /**
   * Deploy an alien 4 cloud topology It is already adapted to the normative TOSCA supported by the
   * Orchestrator.
   *
   * @param cloudConfiguration The configuration used for the plugin instance
   * @param yamlTopology The actual topology accepted by the orchestrator. It is a string formated
   *        and packed for the orchestrator e.g. new lines are replaced with their representation of
   *        '\n'.
   * @return The orchestrator REST response to this call
   * @throws IOException when cannot read from the stream sent by the server or cannot send the
   *         data.
   * @throws NoSuchFieldException when cannot parse the JSOn response.
   * @throws OrchestratorIamException when response code from the orchestrator is not between 200
   *         and 299.
   */
  public OrchestratorResponse callDeploy(CloudConfiguration cloudConfiguration,
                                         String yamlTopology)
          throws IOException, NoSuchFieldException, OrchestratorIamException, AuthorizationFileNotFoundException {
    log.info("call Deploy");

    StringBuilder sbuf = new StringBuilder(cloudConfiguration.getImEndpoint());
    sbuf.append(WS_PATH_DEPLOYMENTS);

    URL requestUrl = new URL(sbuf.toString());

    SSLContext sslContext = getSslContext(cloudConfiguration);
    LOGGER.info("Post Data: " + yamlTopology);
    return restCall(requestUrl, yamlTopology, generateHeaders(cloudConfiguration),
            isUrlSecured(cloudConfiguration.getImEndpoint()), sslContext, HttpMethod.POST);
  }

  public OrchestratorResponse callGetTemplate(CloudConfiguration cloudConfiguration,          ,
                                              String deploymentUUID)
          throws IOException, NoSuchFieldException, OrchestratorIamException, AuthorizationFileNotFoundException {
    log.info("call Deploy");

    StringBuilder sbuf = new StringBuilder(cloudConfiguration.getImEndpoint());
    sbuf.append(WS_PATH_DEPLOYMENTS);

    URL requestUrl = new URL(sbuf.toString());

    SSLContext sslContext = getSslContext(cloudConfiguration);
    LOGGER.info("Post Data: " + yamlTopology);
    return restCall(requestUrl, yamlTopology, generateHeaders(cloudConfiguration),
            isUrlSecured(cloudConfiguration.getImEndpoint()), sslContext, HttpMethod.POST);
  }

  /**
   * Get the status of a deployment with a given deployment ID.
   *
   * @param cloudConfiguration The configuration used for the plugin instance
   * @param deploymentId The id of the deployment we need the information for
   * @return The orchestrator REST response to this call
   * @throws IOException when cannot read from the stream sent by the server or cannot send the
   *         data.
   * @throws NoSuchFieldException when cannot parse the JSOn response.
   * @throws OrchestratorIamException when response code from the orchestrator is not between 200
   *         and 299.
   */
  public OrchestratorResponse callDeploymentStatus(CloudConfiguration cloudConfiguration,
                                                   String deploymentId)
          throws IOException, NoSuchFieldException, OrchestratorIamException, AuthorizationFileNotFoundException {
    log.info("call deployment status for UUID " + deploymentId);

    StringBuilder sbuf = new StringBuilder(cloudConfiguration.getImEndpoint());
    sbuf.append(WS_PATH_DEPLOYMENTS).append("/").append(deploymentId);


    URL requestUrl = new URL(sbuf.toString());

    SSLContext sslContext = getSslContext(cloudConfiguration);
    return restCall(requestUrl, null, generateHeaders(cloudConfiguration),
            isUrlSecured(cloudConfiguration.getImEndpoint()), sslContext, HttpMethod.GET);
  }

  /**
   * Invoke the undeploy REST API for a given deployment ID.
   *
   * @param cloudConfiguration The configuration used for the plugin instance
   * @param deploymentId The id of the deployment we need to undeploy
   * @return The orchestrator REST response to this call
   * @throws IOException when cannot read from the stream sent by the server or cannot send the
   *         data.
   * @throws NoSuchFieldException when cannot parse the JSOn response.
   * @throws OrchestratorIamException when response code from the orchestrator is not between 200
   *         and 299.
   */
  public OrchestratorResponse callUndeploy(CloudConfiguration cloudConfiguration,
                                           String deploymentId)
          throws IOException, NoSuchFieldException, OrchestratorIamException, AuthorizationFileNotFoundException {

    log.info("call undeploy");

    StringBuilder sbuf = new StringBuilder(cloudConfiguration.getImEndpoint());
    sbuf.append(WS_PATH_DEPLOYMENTS).append("/").append(deploymentId);

    URL requestUrl = new URL(sbuf.toString());

    SSLContext sslContext = getSslContext(cloudConfiguration);
    return restCall(requestUrl, null, generateHeaders(cloudConfiguration),
            isUrlSecured(cloudConfiguration.getImEndpoint()), sslContext, HttpMethod.DELETE);
  }

  /**
   * General method that actually performs the calls of the DEEP Orchestrator and authorization
   * endpoints It can connect to both HTTPS and HTTP servers.
   *
   * @param requestUrl The endpoint that handles the call.
   * @param postData Data to be sent to the endpoint. It can be null
   * @param headers The headers of the call made on the endpoint
   * @param isSecured HTTPS or HTTP protocols
   * @param sslContext The certificate handler
   * @param requestType one of the following values: OrchestratorConnector.{POST, GET, DELETE, PUT}
   * @return The orchestrator REST response to this call
   * @throws IOException when cannot read from the stream sent by the server or cannot send the
   *         data.
   * @throws NoSuchFieldException when cannot parse the JSOn response.
   * @throws OrchestratorIamException when response code from the orchestrator is not between 200
   *         and 299.
   */
  public OrchestratorResponse restCall(URL requestUrl, String postData, Map<String, String> headers,
                                       boolean isSecured, SSLContext sslContext, HttpMethod requestType)
          throws IOException, NoSuchFieldException, OrchestratorIamException {

    URLConnection connection = null;
    if (isSecured) {
      connection = (HttpsURLConnection) requestUrl.openConnection();
      ((HttpsURLConnection) connection).setSSLSocketFactory(sslContext.getSocketFactory());
    } else {
      connection = (HttpURLConnection) requestUrl.openConnection();
    }
    connection.setUseCaches(false);
    if (headers != null) {
      for (Map.Entry<String, String> entry : headers.entrySet()) {
        connection.setRequestProperty(entry.getKey(), entry.getValue());
      }
    }
    if (postData != null) {

      if (isSecured) {
        ((HttpsURLConnection) connection).setRequestMethod(requestType.name());
      } else {
        ((HttpURLConnection) connection).setRequestMethod(requestType.name());
      }
      connection.setDoOutput(true);
      byte[] pd = postData.getBytes(StandardCharsets.UTF_8);
      int postDataLength = pd.length;
      connection.setRequestProperty("Content-Length", Integer.toString(postDataLength));

      // //Send request
      DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
      wr.write(pd);
      wr.close();
    } else {
      if (isSecured) {
        ((HttpsURLConnection) connection).setRequestMethod(requestType.name());
      } else {
        ((HttpURLConnection) connection).setRequestMethod(requestType.name());
      }
    }

    int responseCode;
    String title;
    if (isSecured) {
      responseCode = ((HttpsURLConnection) connection).getResponseCode();
      title = ((HttpsURLConnection) connection).getResponseMessage();
    } else {
      responseCode = ((HttpURLConnection) connection).getResponseCode();
      title = ((HttpURLConnection) connection).getResponseMessage();
    }

    LOGGER.info("Response code: " + responseCode);
    LOGGER.info("Response title: " + title);
    // Get Response
    InputStream is;
    if (200 <= responseCode && responseCode <= 299) {
      is = connection.getInputStream();
      StringBuilder response = getResponse(is);
      LOGGER.info("Response content: " + response);
      return new OrchestratorResponse(responseCode, response);
    } else if (400 <= responseCode && responseCode <= 599) {
      if (isSecured) {
        is = ((HttpsURLConnection) connection).getErrorStream();
      } else {
        is = ((HttpURLConnection) connection).getErrorStream();
      }
      StringBuilder response = getResponse(is);
      LOGGER.info("Response content: " + response);
      throw new OrchestratorIamException(responseCode, title, response.toString());
    } else {
      throw new OrchestratorIamException(responseCode, title, "{}");
    }
  }

  /**
   * Obtain the response from the DEEP endpoint once a successful HTTP/HTTPS connection is
   * established.
   *
   * @param is The stream that contains the response from the endpoint
   * @return The full response from the DEEP endpoint
   * @throws IOException when cannot read from the stream sent by the server
   */
  protected StringBuilder getResponse(InputStream is) throws IOException {
    StringBuilder response = new StringBuilder();
    BufferedReader rd = new BufferedReader(new InputStreamReader(is));
    String line;
    while ((line = rd.readLine()) != null) {
      response.append(line);
      response.append('\r');
    }
    rd.close();
    return response;
  }

  /**
   * Builds a SSL context using a configuration. It gets the certificates from that configuration.
   *
   * @param cloudConfiguration the configuration used by the plugin
   * @return the SSL context
   */
  private SSLContext getSslContext(CloudConfiguration cloudConfiguration) {

    SslContextBuilder sslContextBuilder = new SslContextBuilder();
    //sslContextBuilder.addCertificate(cloudConfiguration.getIamHostCert());
    sslContextBuilder.addCertificate(cloudConfiguration.getImEndpoint());
    //sslContextBuilder.addCertificate(cloudConfiguration.getTokenEndpointCert());
    return sslContextBuilder.build();
  }

  /**
   * Check if the URL is secured.
   *
   * @param url The full url, including https:// or http://
   * @return true if if secure, false otherwise
   */
  private boolean isUrlSecured(String url) {
    return url.toLowerCase().startsWith("https:");
  }
}
//public class OrchestratorConnector {
//
//  /** Web service path for deployments operations; It is appended to the orchestrator endpoint. */
//  public static final String WS_PATH_DEPLOYMENTS = "/deployments";
//
//  private static final Logger LOGGER = Logger.getLogger(OrchestratorConnector.class.getName());
//
//  @Autowired
//  protected ConnectionRepository repository;
//
//  private DeepOrchestrator getClient() {
//    Connection<DeepOrchestrator> connection = repository.findPrimaryConnection(DeepOrchestrator.class);
//    if (connection.hasExpired())
//      connection.refresh();
//    DeepOrchestrator deepOrchestrator = connection != null ? connection.getApi() : null;
//    return deepOrchestrator;
//  }
//
//  /**
//   * Obtain the list of deployments created by the the user with the client id stored in the cloud
//   * configuration for the plugin.
//   *
//   * @return The response from the orchestrator
//   * @throws IOException when cannot read from the stream sent by the server or cannot send the
//   *         data.
//   * @throws NoSuchFieldException when cannot parse the JSOn response.
//   * @throws OrchestratorIamException when response code from the orchestrator is not between 200
//   *         and 299.
//   */
//  public OrchestratorResponse callGetDeployments(String orchestrarorUrl)
//      throws IOException, NoSuchFieldException, OrchestratorIamException {
//    return buildResponse(() -> getClient().callGetDeployments(orchestrarorUrl));
//  }
//
//  /**
//   * Deploy an alien 4 cloud topology It is already adapted to the normative TOSCA supported by the
//   * Orchestrator.
//   *
//   * @param yamlTopology The actual topology accepted by the orchestrator. It is a string formated
//   *        and packed for the orchestrator e.g. new lines are replaced with their representation of
//   *        '\n'.
//   * @return The orchestrator REST response to this call
//   * @throws IOException when cannot read from the stream sent by the server or cannot send the
//   *         data.
//   * @throws NoSuchFieldException when cannot parse the JSOn response.
//   * @throws OrchestratorIamException when response code from the orchestrator is not between 200
//   *         and 299.
//   */
//  public OrchestratorResponse callDeploy(String orchestratorUrl,
//      String yamlTopology)
//      throws IOException, NoSuchFieldException, OrchestratorIamException {
//    log.info("call Deploy");
//    log.info("Topology to be sent to the orchestrator: \n" + yamlTopology);
//    return buildResponse(() ->
//            getClient().callDeploy(orchestratorUrl, yamlTopology));
//  }
//
//  /**
//   * Get the status of a deployment with a given deployment ID.
//   *
//   * @param deploymentId The id of the deployment given by the orchestrator we need the information for
//   * @return The orchestrator REST response to this call
//   * @throws IOException when cannot read from the stream sent by the server or cannot send the
//   *         data.
//   * @throws NoSuchFieldException when cannot parse the JSOn response.
//   * @throws OrchestratorIamException when response code from the orchestrator is not between 200
//   *         and 299.
//   */
//  public OrchestratorResponse callDeploymentStatus(String orchestrarorUrl, String deploymentId)
//      throws IOException, NoSuchFieldException, OrchestratorIamException {
//    log.info("call deployment status for UUID " + deploymentId);
//    return buildResponse(() -> getClient().callDeploymentStatus(orchestrarorUrl, deploymentId));
//  }
//
//  /**
//   *
//   * @param deploymentId The id of the deployment given by the orchestrator we need the information for
//   * @return The orchestrator REST response to this call
//   * @throws IOException when cannot read from the stream sent by the server or cannot send the
//   *    *         data.
//   * @throws NoSuchFieldException when cannot parse the JSOn response.
//   * @throws OrchestratorIamException when response code from the orchestrator is not between 200
//   *    *         and 299.
//   */
//  public OrchestratorResponse callGetTemplate(String orchestrarorUrl, String deploymentId)
//          throws IOException, NoSuchFieldException, OrchestratorIamException {
//    log.info("call get template for UUID " + deploymentId);
//    return buildResponse(() -> getClient().callGetTemplate(orchestrarorUrl, deploymentId));
//  }
//
//
//  /**
//   * Invoke the undeploy REST API for a given deployment ID.
//   *
//   * @param deploymentId The id of the deployment we need to undeploy
//   * @return The orchestrator REST response to this call
//   * @throws IOException when cannot read from the stream sent by the server or cannot send the
//   *         data.
//   * @throws NoSuchFieldException when cannot parse the JSOn response.
//   * @throws OrchestratorIamException when response code from the orchestrator is not between 200
//   *         and 299.
//   */
//  public OrchestratorResponse callUndeploy(String orchestrarorUrl,
//      String deploymentId)
//      throws IOException, NoSuchFieldException, OrchestratorIamException {
//    log.info("call undeploy");
//    return buildResponse(() -> getClient().callUndeploy(orchestrarorUrl, deploymentId));
//  }
//
//  private OrchestratorResponse buildResponse(Supplier<ResponseEntity<String>> func) throws OrchestratorIamException, IOException {
//    try {
//      ResponseEntity<String> response = func.get();
//      int responseCode = response.getStatusCode().value();
//      if (300 <= responseCode && responseCode <= 599) {
//        throw new OrchestratorIamException(responseCode, response.getBody(), response.getBody());
//      }
//      return new OrchestratorResponse(response);
//    } catch (HttpStatusCodeException e) {
//      throw new OrchestratorIamException(e.getStatusCode().value(), e.getResponseBodyAsString(), e.getResponseBodyAsString());
//    }
//  }
//}
