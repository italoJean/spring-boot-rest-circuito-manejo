package com.spring.boot.carro.circuito_manejo.configuration.app;

//@Configuration
public class RabbitMQConfig {
/*
    public static final String DLX_EXCHANGE = "dlx.reserva.exchange";
    public static final String PROCESSING_QUEUE = "cola.procesamiento.reserva";
    public static final String DL_ROUTING_KEY = "reserva.event";

    @Bean
    public CustomExchange delayedExchange() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-delayed-type", "direct");
        // Importante: Si no tiene el plugin 'x-delayed-message' instalado en RabbitMQ, debe usar el patrón DLX/TTL manual.
        return new CustomExchange(DLX_EXCHANGE, "x-delayed-message", true, false, args);
    }

    @Bean
    public Queue procesamientoQueue() {
        return new Queue(PROCESSING_QUEUE, true);
    }

    @Bean
    public Binding processingBinding(Queue procesamientoQueue, CustomExchange delayedExchange) {
        // Todos los eventos retrasados serán enrutados a esta única cola
        return BindingBuilder.bind(procesamientoQueue).to(delayedExchange)
                .with(DL_ROUTING_KEY).noargs();
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }*/
}
