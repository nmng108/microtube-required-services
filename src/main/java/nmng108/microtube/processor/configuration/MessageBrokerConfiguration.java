package nmng108.microtube.processor.configuration;


import jakarta.jms.ConnectionFactory;
import jakarta.jms.Queue;
import jakarta.jms.Session;
import lombok.extern.slf4j.Slf4j;
import nmng108.microtube.processor.exception.VideoProcessingException;
import nmng108.microtube.processor.repository.VideoRepository;
import nmng108.microtube.processor.util.constant.Constants;
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.apache.activemq.artemis.jms.client.ActiveMQQueue;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.jms.DefaultJmsListenerContainerFactoryConfigurer;
import org.springframework.boot.autoconfigure.jms.artemis.ArtemisConfigurationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;
import org.springframework.util.ErrorHandler;

@Configuration
@Slf4j
public class MessageBrokerConfiguration {
    @Bean
    public DefaultJmsListenerContainerFactory clientAckJmsListenerContainerFactory(ConnectionFactory connectionFactory, DefaultJmsListenerContainerFactoryConfigurer configurer) {
        DefaultJmsListenerContainerFactory containerFactory = new DefaultJmsListenerContainerFactory();

        configurer.configure(containerFactory, connectionFactory);
        containerFactory.setSessionAcknowledgeMode(Session.CLIENT_ACKNOWLEDGE);

        return containerFactory;
    }

    @Bean
    public DefaultJmsListenerContainerFactory videoProcessingJmsListenerContainerFactory(
            ConnectionFactory connectionFactory,
            DefaultJmsListenerContainerFactoryConfigurer configurer,
            @Qualifier("videoProcessingErrorHandler") ErrorHandler errorHandler
    ) {
        DefaultJmsListenerContainerFactory containerFactory = new DefaultJmsListenerContainerFactory();

        configurer.configure(containerFactory, connectionFactory);
        containerFactory.setSessionAcknowledgeMode(Session.CLIENT_ACKNOWLEDGE);
        containerFactory.setErrorHandler(errorHandler);

        return containerFactory;
    }

//    @Bean
//    public ArtemisConfigurationCustomizer artemisConfigurationCustomizer() {
//        return (configuration) -> {
//            var a = configuration.getAddressSettings();
//            a.get().setMaxDeliveryAttempts(1);
//        };
//    }

//    @Bean // Serialize message content to json using TextMessage
//    public MessageConverter jacksonJmsMessageConverter() {
//        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
//
//        converter.setTargetType(MessageType.TEXT);
//        converter.setTypeIdPropertyName("_type");
//
//        return converter;
//    }

    @Bean
    public ErrorHandler videoProcessingErrorHandler(VideoRepository videoRepository) {
        return (throwable) -> { // instanceof ListenerExecutionFailedException
            throwable.printStackTrace();
            if (throwable.getCause() instanceof VideoProcessingException e) {
                log.info("Handling VideoProcessingException: {}", e.getMessage());
//                // TODO: schedule to delete the video record along with original file
//                videoRepository.deleteById(e.getVideoId());
                return;
            }

            log.error("{}: {}", throwable.getClass().getName(), throwable.getMessage());
//            throw new RuntimeException("thrown by ErrorHandler", throwable);
        };
    }

    @Bean
    public Queue videoProcessingRequestQueue() {
        return new ActiveMQQueue(Constants.MessageBrokerQueueNames.VIDEO_PROCESSING_REQUEST_QUEUE);
    }
}
