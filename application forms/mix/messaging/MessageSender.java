package mix.messaging;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import mix.SerializeUtil;
import mix.model.bank.BankInterestRequest;

import java.io.IOException;
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

    public void send(T requestReply) throws IOException {
        channel.basicPublish("", queueName, null, SerializeUtil.serialize(requestReply));
        System.out.println(" [x] Sent '" + requestReply.toString() + "'");
    }

}
