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

    public MessageSender(String queueName) throws IOException, TimeoutException {
        this.queueName = queueName;

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();

        channel = connection.createChannel();
        channel.queueDeclare(queueName, false, false, false, null);
    }

    public String send(T requestReply, String corrId, String queueName) throws IOException {
        if(corrId == null) corrId = UUID.randomUUID().toString();
        if(queueName != null) this.queueName = queueName;

        // this is a test for switching branches

        AMQP.BasicProperties props = new AMQP.BasicProperties
                .Builder()
                .correlationId(corrId)
                .build();

        channel.basicPublish("", this.queueName, props, SerializeUtil.serialize(requestReply));

        System.out.println(" [x] Sent '" + requestReply.toString() + "' with corrId '" + corrId + "'");

        return corrId;
    }
}
