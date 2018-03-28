package mix.messaging;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import mix.SerializeUtil;

import java.io.IOException;
import java.io.Serializable;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

/**
 * Created by sguldemond on 28/03/2018.
 */
public class MessageSenderFanout<T extends Serializable> {
    private String exchangeName;
    private Connection connection;
    private Channel channel;

    public MessageSenderFanout(String exchangeName) throws IOException, TimeoutException {
        this.exchangeName = exchangeName;

        ConnectionFactory factory = new ConnectionFactory();
        connection = factory.newConnection();
        channel = connection.createChannel();

        channel.exchangeDeclare(this.exchangeName, "fanout");
    }

    public String send(T message, String corrId) throws IOException {
        if(corrId == null) corrId = UUID.randomUUID().toString();

        AMQP.BasicProperties props = new AMQP.BasicProperties
                .Builder()
                .correlationId(corrId)
                .build();

        channel.basicPublish(this.exchangeName, "", props, SerializeUtil.serialize(message));

        System.out.println(" [x] Sent '" + message.toString() + "' on exchange name '" + exchangeName + "'");

        return corrId;
    }

    public void close() throws IOException, TimeoutException {
        channel.close();
        connection.close();
        System.out.println(" [*] Stopped sender to '" + exchangeName + "'!");

    }
}
