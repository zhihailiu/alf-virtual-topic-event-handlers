package com.acme.event.handler;

import java.io.IOException;

import org.alfresco.testcontainers.AlfrescoContainer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.activemq.ActiveMQContainer;

import com.acme.event.rest.AlfrescoClient;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

/**
 * Integration test for {@link ContentTypeNodeCreatedHandler} using Spring Boot
 * and Testcontainers.
 * <p>
 * This class verifies the behavior of the handler when a node of cm:content
 * type is created in Alfresco repository. It uses Testcontainers to start an
 * Alfresco instance and an ActiveMQ broker for testing the event handling.
 */
@SpringBootTest
public class ContentTypeNodeCreatedHandlerTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(ContentTypeNodeCreatedHandlerTest.class);

	public static final String ACS_VERSION = "23.4.1";
	public static final int ACS_PORT = 8080;
	public static final int ACTIVEMQ_PORT = 61616;

	// Containers are static to ensure they are shared across tests and only started
	// once
	private static AlfrescoContainer<?> alfrescoContainer;
	private static ActiveMQContainer activemqContainer;

	// In-memory log appender to capture logs for assertion
	private ListAppender<ILoggingEvent> listAppender;

	@Autowired
	private AlfrescoClient restClient;

	@Autowired
	private ContentTypeNodeCreatedHandler handler;

	/**
	 * Registers dynamic properties such as the ActiveMQ broker URL.
	 *
	 * @param registry the dynamic property registry
	 */
	@DynamicPropertySource
	static void dynamicProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.activemq.brokerUrl",
				() -> "tcp://" + activemqContainer.getHost() + ":" + activemqContainer.getMappedPort(ACTIVEMQ_PORT));
	}

	/**
	 * Initializes resources before all tests, including starting containers.
	 */
	@BeforeAll
	static void setUp() {
		alfrescoContainer = new AlfrescoContainer<>(ACS_VERSION).withMessagingEnabled();
		alfrescoContainer.start();
		activemqContainer = alfrescoContainer.getActivemqContainer();
	}

	/**
	 * Stops the containers after all tests have run to clean up resources.
	 */
	@AfterAll
	static void tearDownAll() {
		alfrescoContainer.stop();
	}

	/**
	 * Sets up the environment before each test, configuring the REST client and
	 * initializing log capturing.
	 */
	@BeforeEach
	void setUpEach() {
		restClient.setAlfrescoUrl("http", alfrescoContainer.getHost(), alfrescoContainer.getMappedPort(ACS_PORT),
				"alfresco");

		// Configure in-memory log appender to capture logs for verification
		LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
		listAppender = new ListAppender<>();
		listAppender.setContext(loggerContext);
		listAppender.start();

		Logger logger = LoggerFactory.getLogger(ContentTypeNodeCreatedHandler.class);
		((ch.qos.logback.classic.Logger) logger).addAppender(listAppender);
	}

	/**
	 * Cleans up the log appender after each test to prevent memory leaks.
	 */
	@AfterEach
	void tearDown() {
		Logger logger = LoggerFactory.getLogger(ContentTypeNodeCreatedHandler.class);
		((ch.qos.logback.classic.Logger) logger).detachAppender(listAppender);
	}

	/**
	 * Test the handler's behavior when a cm:content node is created in Alfresco.
	 * <p>
	 * It ensures the handler logs the correct message when processing a cm:content
	 * node creation event.
	 *
	 * @throws IOException          if there is an issue creating the file in
	 *                              Alfresco
	 * @throws InterruptedException if the thread is interrupted while waiting for
	 *                              log processing
	 */
	@Test
	void testHandleEventForCreated() throws IOException, InterruptedException {
		try {
			String filename = "test.txt";
			String nodeName = restClient.createFile(filename);
			Assertions.assertTrue(nodeName.contains(filename), "Node name should contain 'test.txt'");
		} catch (IOException ioException) {
			LOGGER.error("Alfresco container logs:\n{}", alfrescoContainer.getLogs());
			throw ioException;
		}

		// Allow some time for the log message to be processed by the handler
		Thread.sleep(1000);

		// Assert that the correct log message was captured
		Assertions.assertFalse(listAppender.list.isEmpty(), "Log message should not be empty");
		ILoggingEvent logEvent = listAppender.list.get(0);
		Assertions.assertEquals("A new node named test.txt of type cm:content has been created!",
				logEvent.getFormattedMessage(), "Log message should indicate the creation of 'test.txt");

	}

	/**
	 * Test the handler's behavior when a non-HTML file (TXT) is created in
	 * Alfresco.
	 * <p>
	 * It ensures the handler does not log a message when processing a non-HTML file
	 * creation event.
	 *
	 * @throws IOException          if there is an issue creating the file in
	 *                              Alfresco
	 * @throws InterruptedException if the thread is interrupted while waiting for
	 *                              log processing
	 */
	@Test
	void testHandleEventForCreated_ignore_folder() throws IOException, InterruptedException {
		try {
			String name = "test_folder";
			String nodeName = restClient.createFolder(name);
			Assertions.assertTrue(nodeName.contains(name), "Node name should contain 'test_folder'");
		} catch (IOException ioException) {
			LOGGER.error("Alfresco container logs:\n{}", alfrescoContainer.getLogs());
			throw ioException;
		}

		// Allow some time for the log message to be processed
		Thread.sleep(1000);

		// Assert that no log message was captured since it's not an HTML file
		Assertions.assertTrue(listAppender.list.isEmpty(), "Log message should be empty for non cm:content nodes");
	}

}
