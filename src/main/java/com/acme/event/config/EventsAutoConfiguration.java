package com.acme.event.config;

import javax.jms.Queue;

import org.alfresco.event.sdk.autoconfigure.AlfrescoEventsAutoConfiguration;
import org.alfresco.event.sdk.autoconfigure.AlfrescoEventsProperties;
import org.alfresco.event.sdk.integration.EventChannels;
import org.alfresco.event.sdk.model.v1.model.DataAttributes;
import org.alfresco.event.sdk.model.v1.model.RepoEvent;
import org.alfresco.event.sdk.model.v1.model.Resource;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.jms.dsl.Jms;
import org.springframework.integration.transformer.GenericTransformer;

@Configuration
public class EventsAutoConfiguration {

	private static final Logger LOGGER = LoggerFactory.getLogger(EventsAutoConfiguration.class);

	/**
	 * Queue name that the clients listen to for Alfresco events. Name must follow
	 * ActiveMQ Virtual Topic/Queue naming convention to match the configuration on
	 * the Alfresco side.
	 * 
	 * For example, Alfresco has the following virtual topic definition <code>
	 * repo.event2.topic.endpoint=amqp:topic:VirtualTopic.events2
	 * </code>
	 * 
	 * Then the SDK application should have <code>
	 * alfresco.events.queueName=Consumer.FOO.VirtualTopic.events2
	 * </code> where "FOO" can be any arbitrary string.
	 */
	@Value("${alfresco.events.queueName:Consumer.FOO.VirtualTopic.events2}")
	private String queueName;

	@Autowired
	private AlfrescoEventsProperties alfrescoEventsProperties;

	@Autowired
	private DirectChannel acsEventErrorChannel;

	@Autowired
	private GenericTransformer<String, RepoEvent<DataAttributes<Resource>>> acsEventTransformer;

	/**
	 * See
	 * {@link AlfrescoEventsAutoConfiguration#acsEventsListeningFlow(ActiveMQConnectionFactory)}
	 * in Alfresco SDK.
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
	public IntegrationFlow acsEventsListeningFlow2(final ActiveMQConnectionFactory activeMQConnectionFactory) {
		return IntegrationFlows
				.from(Jms
						.messageDrivenChannelAdapter(activeMQConnectionFactory).destination(
								acsEventsQueue())
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
