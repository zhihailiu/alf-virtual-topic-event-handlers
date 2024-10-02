package com.acme.event.config;

import org.alfresco.event.sdk.autoconfigure.AlfrescoEventsAutoConfiguration;
import org.alfresco.event.sdk.autoconfigure.AlfrescoEventsProperties;
import org.alfresco.event.sdk.integration.EventChannels;
import org.alfresco.repo.event.v1.model.DataAttributes;
import org.alfresco.repo.event.v1.model.RepoEvent;
import org.alfresco.repo.event.v1.model.Resource;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.core.GenericTransformer;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.jms.dsl.Jms;

import jakarta.jms.Queue;
import jakarta.jms.Topic;

/**
 * Queue configuration to support competing consumers.
 */
@Configuration
@ConditionalOnProperty(name = "alfresco.events.queue.enabled", havingValue = "true")
public class EventsAutoConfiguration {

	private static final Logger LOGGER = LoggerFactory.getLogger(EventsAutoConfiguration.class);

	/**
	 * Queue name that the clients listen to for Alfresco events. Name must follow
	 * ActiveMQ Virtual Topic/Queue naming convention to match the configuration on
	 * the Alfresco side.
	 * 
	 * For example, Alfresco has the following virtual topic definition <code>
	 * repo.event2.topic.endpoint=amqp:topic:VirtualTopic.BAR
	 * </code>
	 * 
	 * Then the SDK application should have
	 * <code>alfresco.events.queue.enabled=true</code> to enable this configuration,
	 * and <code>
	 * alfresco.events.queue.name=Consumer.FOO.VirtualTopic.BAR
	 * </code> to match the Queue name on ACS side. "FOO" can be any arbitrary
	 * string.
	 */
	@Value("${alfresco.events.queue.name:Consumer.FOO.VirtualTopic.BAR}")
	private String queueName;

	@Autowired
	private AlfrescoEventsProperties alfrescoEventsProperties;

	@Autowired
	private GenericTransformer<String, RepoEvent<DataAttributes<Resource>>> acsEventTransformer;

	/**
	 * See
	 * {@link AlfrescoEventsAutoConfiguration#acsEventsListeningFlow(ActiveMQConnectionFactory)}
	 * in Alfresco SDK 6.2.0.
	 * 
	 * Use Queue instead of Topic as destination. "@Primary" annotation makes this
	 * preference over the Topic implementation in Alfresco SDK during dependency
	 * injection.
	 * 
	 * @param activeMQConnectionFactory
	 * @return
	 */
	@Bean
	@Primary
	public IntegrationFlow acsEventsListeningFlow2(final ActiveMQConnectionFactory activeMQConnectionFactory,
			@Qualifier(EventChannels.ERROR) final DirectChannel acsEventErrorChannel, 
			final Topic acsEventsTopic,
			final GenericTransformer<String, RepoEvent<DataAttributes<Resource>>> acsEventTransformer) {

		LOGGER.info("Queue based events listening flow");

		return IntegrationFlow.from(
				Jms.messageDrivenChannelAdapter(activeMQConnectionFactory)
				// use Queue instead of Topic
				.destination(acsEventsQueue())
				.errorChannel(acsEventErrorChannel))
				.transform(acsEventTransformer)
				.routeToRecipients(route -> route
						.recipient(EventChannels.SPRING_INTEGRATION,
								s -> alfrescoEventsProperties.isEnableSpringIntegration())
						.recipient(EventChannels.HANDLERS, s -> alfrescoEventsProperties.isEnableHandlers()))
				.get();
	}

	@Bean
	public Queue acsEventsQueue() {
		return new ActiveMQQueue(queueName);
	}
}
