package mix.messaging;

import com.rabbitmq.client.*;
import mix.SerializeUtil;
import java.io.IOException;
import java.io.Serializable;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

/**
 * Created by sguldemond on 15/03/2018.
 */
public class MessageSender<T extends Serializable> {
    private String queueName;
    private Connection connection;
    private Channel channel;

    public MessageSender(String queueName) throws IOException, TimeoutException {
        this.queueName = queueName;

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        connection = factory.newConnection();

        channel = connection.createChannel();
        channel.queueDeclare(this.queueName, false, false, false, null);
    }

    public String send(T message, String corrId) throws IOException {
        if(corrId == null) corrId = UUID.randomUUID().toString();

        AMQP.BasicProperties props = new AMQP.BasicProperties
                .Builder()
                .correlationId(corrId)
                .build();

        channel.basicPublish("", this.queueName, props, SerializeUtil.serialize(message));

        System.out.println(" [x] Sent '" + message.toString() + "' on queueName '" + queueName + "'");

        return corrId;
    }

    public void close() throws IOException, TimeoutException {
        channel.close();
        connection.close();
        System.out.println(" [*] Stopped sender to '" + queueName + "'!");

    }
}
