package mix.messaging;

import com.rabbitmq.client.*;
import mix.SerializeUtil;
import mix.model.bank.BankInterestReply;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeoutException;

/**
 * Created by sguldemond on 15/03/2018.
 */
public class MessageSender<T> {
    private String queueName;
    private Channel channel;

    private String replyQueueName;

    public MessageSender(String queueName) throws IOException, TimeoutException {
        this.queueName = queueName;

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();

        channel = connection.createChannel();
        channel.queueDeclare(queueName, false, false, false, null);

        replyQueueName = channel.queueDeclare().getQueue();
    }

    public String send(T requestReply, String corrId) throws IOException {
        if(corrId == null) corrId = UUID.randomUUID().toString();

        AMQP.BasicProperties props = new AMQP.BasicProperties
                .Builder()
                .correlationId(corrId)
                .replyTo(replyQueueName)
                .build();

        channel.basicPublish("", queueName, props, SerializeUtil.serialize(requestReply));

        System.out.println(" [x] Sent '" + requestReply.toString() + "' with corrId '" + corrId + "'");

        return corrId;
    }

    public BankInterestReply call(T requestReply) throws IOException, InterruptedException {
        final String corrId = UUID.randomUUID().toString();

        AMQP.BasicProperties props = new AMQP.BasicProperties
                .Builder()
                .correlationId(corrId)
                .replyTo(replyQueueName)
                .build();

        channel.basicPublish("", queueName, props, SerializeUtil.serialize(requestReply));

        final BlockingQueue<BankInterestReply> response = new ArrayBlockingQueue<BankInterestReply>(1);

        channel.basicConsume(replyQueueName, true, new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                if (properties.getCorrelationId().equals(corrId)) {
                    try {
                        response.offer((BankInterestReply) SerializeUtil.deserialize(body));
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        return response.take();
    }


}
