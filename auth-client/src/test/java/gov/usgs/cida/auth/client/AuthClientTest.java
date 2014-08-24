package gov.usgs.cida.auth.client;

import gov.usgs.cida.auth.model.AuthToken;
import gov.usgs.cida.auth.service.ServicePaths;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import org.apache.commons.io.IOUtils;
import static org.hamcrest.CoreMatchers.*;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockserver.integration.ClientAndServer;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import org.mockserver.model.Header;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import org.mockserver.socket.PortFactory;

/**
 *
 * @author isuftin
 */
public class AuthClientTest {

	private static ClientAndServer mockServer;
	private static int serverPort;
	private static String authUrl;
	private static String getAuthTokenValidResponse;
	private static final String appName = "/cida-auth-app/";
	private static final String host = "http://localhost:";
	private static final String tokenId = "fda34827-f5d7-44d7-b46f-db6603accb7c";

	public AuthClientTest() {
	}

	@BeforeClass
	public static void setUpClass() throws URISyntaxException, IOException {

		serverPort = PortFactory.findFreePort();
		mockServer = startClientAndServer(serverPort);
		authUrl = host + serverPort + appName;

		URL getAuthTokenValidResponseUrl = AuthClientTest.class.getResource("/examples/GetAuthTokenValidResponse.json");
		getAuthTokenValidResponse = IOUtils.toString(new FileInputStream(new File(getAuthTokenValidResponseUrl.toURI())));
	}

	@AfterClass
	public static void tearDownClass() {
		mockServer.stop();
	}

	@Before
	public void setUp() {
		mockServer.reset();
		mockServer.
				when(request().withPath(appName + ServicePaths.AUTHENTICATION + "/" + ServicePaths.AD + "/" + ServicePaths.TOKEN)).
				respond(response().
						withHeaders(new Header("Content-Type", "application/json")).
						withBody(getAuthTokenValidResponse));

		mockServer.
				when(request().withPath(appName + ServicePaths.TOKEN + "/" + tokenId)).
				respond(response().
						withHeaders(new Header("Content-Type", "application/json")).
						withBody(getAuthTokenValidResponse));
	}

	@After
	public void tearDown() {
		mockServer.dumpToLog();

	}

	@Test
	public void testGetNewToken() throws URISyntaxException {
		System.out.println("getNewToken");
		String username = "testuser";
		String password = "testpassword";
		AuthClient instance = new AuthClient(host + serverPort + appName);
		AuthToken result = instance.getNewToken(username, password);
		assertNotNull(result);
		assertThat(result.getTokenId(), is(equalTo("fda34827-f5d7-44d7-b46f-db6603accb7c")));
	}

	@Test
	public void testGetToken() throws URISyntaxException {
		System.out.println("getToken");
		AuthClient instance = new AuthClient(host + serverPort + appName);
		AuthToken result = instance.getToken(tokenId);
		assertNotNull(result);
	}

}
