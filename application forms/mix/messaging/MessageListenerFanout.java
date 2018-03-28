package mix.messaging;

import com.rabbitmq.client.*;
import mix.IFrame;
import mix.SerializeUtil;

import java.io.IOException;
import java.io.Serializable;
import java.util.concurrent.TimeoutException;

/**
 * Created by sguldemond on 28/03/2018.
 */
public class MessageListenerFanout<T extends Serializable> {
    private String exchangeName;
    private IFrame frame;
    private String queueName;

    private Connection connection;
    private Channel channel;

    public MessageListenerFanout(String exchangeName, IFrame frame) throws IOException, TimeoutException {
        this.exchangeName = exchangeName;
        this.frame = frame;

        ConnectionFactory factory = new ConnectionFactory();
        connection = factory.newConnection();
        channel = connection.createChannel();

        channel.exchangeDeclare(this.exchangeName, "fanout");
        queueName = channel.queueDeclare().getQueue();
        channel.queueBind(queueName, this.exchangeName, "");

    }

    public void listen() throws IOException {
        System.out.println(" [*] Waiting for messages on '" + exchangeName + "'...");

        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                try {
                    T message = (T)SerializeUtil.deserialize(body);

                    System.out.println(" [x] Received '" + message.toString() + "' on exchange name '" + exchangeName + "'");

                    frame.add(message, properties.getCorrelationId());
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        };
        channel.basicConsume(queueName, true, consumer);
    }

    public void close() throws IOException, TimeoutException {
        channel.close();
        connection.close();
        System.out.println(" [*] Stopped listening to '" + exchangeName + "'!");
    }
}
