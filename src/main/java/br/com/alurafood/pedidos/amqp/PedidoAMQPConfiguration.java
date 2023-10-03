package br.com.alurafood.pedidos.amqp;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PedidoAMQPConfiguration {

    //  Especifica qual conversor usaremos para o envio das mensagens
    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    //  Muda o conversor padrão para o que especificamos no método acima
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, Jackson2JsonMessageConverter messageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);
        return rabbitTemplate;
    }

    @Bean
    public Queue filaDetalhesPedidos() {
        return QueueBuilder
                .nonDurable("pagamentos.detalhes-pedido")
                .build();
    }

//  Criada exchange para referenciar o nome dela. Criamos ela aqui para fazermos o bind
    @Bean
    public FanoutExchange fanoutExchange() {
        return ExchangeBuilder
                .fanoutExchange("pagamentos.ex")
                .build();
    }

//  Bind da fila criada no filaDetalhesPedidos() com a exchange
    @Bean
    public Binding bindPagamentoPedido(FanoutExchange fanoutExchange) {
        return BindingBuilder
                .bind(filaDetalhesPedidos())
                .to(fanoutExchange);
    }

    @Bean
    public RabbitAdmin criaRabbitAdmin(ConnectionFactory conn) {
        return new RabbitAdmin(conn);
    }

    @Bean
    public ApplicationListener<ApplicationReadyEvent> inicializaAdmin(RabbitAdmin rabbitAdmin) {
        return event -> rabbitAdmin.initialize();
    }
}
