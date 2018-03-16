package mix.messaging;

import com.rabbitmq.client.*;
import mix.IFrame;
import mix.SerializeUtil;

import java.io.IOException;
import java.io.Serializable;
import java.util.concurrent.TimeoutException;

/**
 * Created by sguldemond on 15/03/2018.
 */
public class MessageListener<T extends Serializable> {
    private String queueName;
    private Channel channel;

    private IFrame frame;

    public MessageListener(IFrame frame, String queueName) throws IOException, TimeoutException {
        this.queueName = queueName;

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();

        channel = connection.createChannel();
        channel.queueDeclare(queueName, false, false, false, null);

        this.frame = frame;
    }

    public void listen() throws IOException {
        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
                    throws IOException {
                try {
                    T requestReply = (T) SerializeUtil.deserialize(body);
                    System.out.println(" [x] Received '" + requestReply.toString() + "'");

                    frame.add(requestReply, properties.getCorrelationId());
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        };
        channel.basicConsume(queueName, true, consumer);
    }

}
