package loanbroker.loanbroker;

import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.concurrent.TimeoutException;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

import com.rabbitmq.client.RpcClient;
import mix.IFrame;
import mix.messaging.MessageListener;
import mix.messaging.MessageSender;
import mix.model.bank.BankInterestReply;
import mix.model.bank.BankInterestRequest;
import mix.model.loan.LoanReply;
import mix.model.loan.LoanRequest;

public class LoanBrokerFrame extends IFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private DefaultListModel<JListLine> listModel = new DefaultListModel<JListLine>();
	private JList<JListLine> list;

	private HashMap<String, Serializable> corrMap = new HashMap<>();

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					LoanBrokerFrame frame = new LoanBrokerFrame();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public LoanBrokerFrame() {
        try {
            MessageListener<LoanRequest> loanRequestListener = new MessageListener<>(this, "LOAN_QUEUE");
            loanRequestListener.listen();

            MessageListener<BankInterestReply> bankInterestReplyListener = new MessageListener<>(this, "BROKER_QUEUE");
            bankInterestReplyListener.listen();
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }

        setTitle("Loan Broker");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		GridBagLayout gbl_contentPane = new GridBagLayout();
		gbl_contentPane.columnWidths = new int[]{46, 31, 86, 30, 89, 0};
		gbl_contentPane.rowHeights = new int[]{233, 23, 0};
		gbl_contentPane.columnWeights = new double[]{1.0, 0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_contentPane.rowWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		contentPane.setLayout(gbl_contentPane);
		
		JScrollPane scrollPane = new JScrollPane();
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.gridwidth = 7;
		gbc_scrollPane.insets = new Insets(0, 0, 5, 5);
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 0;
		contentPane.add(scrollPane, gbc_scrollPane);
		
		list = new JList<JListLine>(listModel);
		scrollPane.setViewportView(list);
	}

    private JListLine getRequestReply(LoanRequest request){
	     for (int i = 0; i < listModel.getSize(); i++){
	    	 JListLine rr =listModel.get(i);
	    	 if (rr.getLoanRequest() == request){
	    		 return rr;
	    	 }
	     }
	     return null;
	}

	@Override
	public void add(Serializable component, String corrId){
	    if(component.getClass() == LoanRequest.class) {
            corrMap.put(corrId, component);

            add((LoanRequest) component);

            try {
                BankInterestRequest bankInterestRequest = new BankInterestRequest((LoanRequest) component);

                String queueName;

                if(bankInterestRequest.getAmount() > 100) {
                	queueName = "ABN_QUEUE";
				} else {
                	queueName = "ING_QUEUE";
				}

				// TODO: send multiple messages

                MessageSender<BankInterestRequest> bankInterestRequestSender = new MessageSender<>(queueName);
                bankInterestRequestSender.send(bankInterestRequest, corrId, null);

            } catch (IOException | TimeoutException e) {
                e.printStackTrace();
            }
        } else if (component.getClass() == BankInterestReply.class) {
	        BankInterestReply bankReply = (BankInterestReply) component;
            add((LoanRequest) corrMap.get(corrId), bankReply);

            try {
                LoanReply loanReply = new LoanReply(bankReply.getInterest(), null);

                MessageSender<LoanReply> loanReplySender = new MessageSender<>("CLIENT_QUEUE");
                loanReplySender.send(loanReply, corrId, null);
            } catch (IOException | TimeoutException e) {
                e.printStackTrace();
            }
        }
    }

    public void add(LoanRequest loanRequest){
        listModel.addElement(new JListLine(loanRequest));
    }

    public void add(LoanRequest loanRequest,BankInterestRequest bankRequest){
		JListLine rr = getRequestReply(loanRequest);
		if (rr!= null && bankRequest != null) {
			rr.setBankRequest(bankRequest);
            list.repaint();
		}
	}
	
	public void add(LoanRequest loanRequest, BankInterestReply bankReply){
		JListLine rr = getRequestReply(loanRequest);
		if (rr!= null && bankReply != null){
			rr.setBankReply(bankReply);
            list.repaint();
		}
	}
}
