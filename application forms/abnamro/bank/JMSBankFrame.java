package abnamro.bank;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.concurrent.TimeoutException;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import com.rabbitmq.client.*;
import mix.IFrame;
import mix.messaging.MessageListener;
import mix.messaging.MessageSender;
import mix.messaging.RequestReply;
import mix.model.bank.BankInterestReply;
import mix.model.bank.BankInterestRequest;

public class JMSBankFrame extends IFrame implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTextField tfReply;
	private JComboBox cbBank;
	private DefaultListModel<RequestReply<BankInterestRequest, BankInterestReply>> listModel = new DefaultListModel<RequestReply<BankInterestRequest, BankInterestReply>>();

    private MessageSender<BankInterestReply> bankInterestReplySender;
    private MessageListener<BankInterestRequest> bankInterestRequestListener;

    private String[] banks = { "ABN", "ING", "RABO" };
    private String activeBank = banks[0];

    protected HashMap<Serializable, String> corrMap = new HashMap<>();

    /**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					JMSBankFrame frame = new JMSBankFrame();
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
	public JMSBankFrame() {
        try {
            startListener();
            bankInterestReplySender = new MessageSender<>("BROKER_QUEUE");
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }

        setTitle(banks[0]);
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
		gbc_scrollPane.gridwidth = 5;
		gbc_scrollPane.insets = new Insets(0, 0, 5, 5);
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 0;
		contentPane.add(scrollPane, gbc_scrollPane);
		
		JList<RequestReply<BankInterestRequest, BankInterestReply>> list = new JList<RequestReply<BankInterestRequest, BankInterestReply>>(listModel);
		scrollPane.setViewportView(list);
		
		JLabel lblNewLabel = new JLabel("type reply");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel.insets = new Insets(0, 0, 0, 5);
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 1;
		contentPane.add(lblNewLabel, gbc_lblNewLabel);
		
		tfReply = new JTextField();
		GridBagConstraints gbc_tfReply = new GridBagConstraints();
		gbc_tfReply.gridwidth = 2;
		gbc_tfReply.insets = new Insets(0, 0, 0, 5);
		gbc_tfReply.fill = GridBagConstraints.HORIZONTAL;
		gbc_tfReply.gridx = 1;
		gbc_tfReply.gridy = 1;
		contentPane.add(tfReply, gbc_tfReply);
		tfReply.setColumns(10);
		
		JButton btnSendReply = new JButton("send reply");
		btnSendReply.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				RequestReply<BankInterestRequest, BankInterestReply> rr = list.getSelectedValue();
				double interest = Double.parseDouble((tfReply.getText()));
				BankInterestReply reply = new BankInterestReply(interest, activeBank);
				if (rr != null && reply != null){
					rr.setReply(reply);
	                list.repaint();

                    try {
                        bankInterestReplySender.send(reply, corrMap.get(rr.getRequest()));
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
			}
		});
		GridBagConstraints gbc_btnSendReply = new GridBagConstraints();
		gbc_btnSendReply.anchor = GridBagConstraints.NORTHWEST;
		gbc_btnSendReply.gridx = 4;
		gbc_btnSendReply.gridy = 1;
		contentPane.add(btnSendReply, gbc_btnSendReply);

		// TODO: implement bank changer
		cbBank = new JComboBox(banks);
		GridBagConstraints gbc_cbBank = new GridBagConstraints();
		gbc_cbBank.gridwidth = 2;
		gbc_cbBank.insets = new Insets(0, 0, 0, 5);
		gbc_cbBank.fill = GridBagConstraints.HORIZONTAL;
		gbc_cbBank.gridx = 1;
		gbc_cbBank.gridy = 2;
		contentPane.add(cbBank, gbc_cbBank);

		cbBank.addActionListener(this);

	}

	@Override
    public void add(Serializable component, String corrId) {
		BankInterestRequest bankRequest = (BankInterestRequest) component;
        corrMap.put(bankRequest, corrId);

        RequestReply<BankInterestRequest, BankInterestReply> requestReply = new RequestReply<>(bankRequest, null);
        listModel.addElement(requestReply);
    }

	@Override
	public void actionPerformed(ActionEvent e) {
		JComboBox cb = (JComboBox)e.getSource();
		activeBank = (String)cb.getSelectedItem();
		setTitle(activeBank);

		try {
            startListener();
        } catch (IOException | TimeoutException e1) {
            e1.printStackTrace();
        }
    }

	private void startListener() throws IOException, TimeoutException {
        if(bankInterestRequestListener != null) bankInterestRequestListener.close();
		bankInterestRequestListener = new MessageListener<>(this,activeBank + "_QUEUE");
		bankInterestRequestListener.listen();
	}
}
