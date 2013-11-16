/*
 * Brandenburg University of Technology Cottbus - Senftenberg (BTU)
 * Department of Computer Science, Information and Media Technology
 * Computer Networks and Communication Systems Group
 *
 * Practical course "Introduction to Computer Networks"
 * Task 1: Implementation of a Simple SMTP Client
 *
 * @author: Vitaliy Sobur <soburvit@tu-cottbus.de>
 * @author: Michael Vogel <mv@informatik.tu-cottbus.de>
 * @version: 2013-10-10
 *
 * A simple GUI of a primitive SMTP Client.
 * Other necessary files: SpringUtilities.java.
 */

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

public class SMTPClient extends JPanel implements ActionListener {

	public final static String subject = "Cicero: \"De finibus bonorum et malorum\", 1.10.32";
	public final static String messageString = "Sed ut perspiciatis, unde omnis iste natus error sit voluptatem\n"
			+ "accusantium doloremque laudantium, totam rem aperiam eaque ipsa,\n"
			+ "quae ab illo inventore veritatis et quasi architecto beatae vitae dicta sunt,\n"
			+ "explicabo. nemo enim ipsam voluptatem, quia voluptas sit, aspernatur\n"
			+ "aut odit aut fugit, sed quia consequuntur magni dolores eos, qui ratione\n"
			+ "voluptatem sequi nesciunt, neque porro quisquam est, qui dolorem\n"
			+ "ipsum, quia dolor sit, amet, consectetur, adipisci velit, sed quia non\n"
			+ "numquam eius modi tempora incidunt, ut labore et dolore magnam\n"
			+ "aliquam quaerat voluptatem. ut enim ad minima veniam, quis nostrum\n"
			+ "exercitationem ullam corporis suscipit laboriosam, nisi ut aliquid ex ea\n"
			+ "commodi consequatur? quis autem vel eum iure reprehenderit, qui in ea\n"
			+ "voluptate velit esse, quam nihil molestiae consequatur, vel illum, qui\n"
			+ "dolorem eum fugiat, quo voluptas nulla pariatur?";

	private final String[] labels = { "From: ", "To: ", "Cc: ", "Bcc: ",
			"Username: ", "Password: ", "Subject: ", "Message: " };

	protected enum InputField {
		FROM, TO, CC, BCC, USERNAME, PASSWORD, SUBJECT, MESSAGE
	}

	protected JComponent[] inputComponents;
	protected JCheckBox verboseBox;

	/**
	 * Constructor creates all GUI components and adds them to the content pane.
	 */
	public SMTPClient() {
		super(new SpringLayout()); // Set Layout Manager.

		inputComponents = new JComponent[labels.length];
		JComponent comp;

		// Add input fields to pane.
		for (InputField f : InputField.values()) {
			JLabel label = new JLabel(labels[f.ordinal()], JLabel.TRAILING);
			add(label);

			// Create the different input fields.
			switch (f) {
			case PASSWORD:
				comp = new JPasswordField(10);
				break;
			case MESSAGE:
				comp = new JTextArea(messageString, 13, 40);
				break;
			case SUBJECT:
				comp = new JTextField(subject, 10);
				break;
			default:
				comp = new JTextField(10);
			}

			inputComponents[f.ordinal()] = comp; // Store input field reference.
			label.setLabelFor(comp); // Connect "springs" of spring layout.
			add(comp);
		}

		add(new JLabel(" ")); // add some vertical space.
		add(new JLabel(" "));
		verboseBox = new JCheckBox("verbose");
		add(verboseBox);
		JButton sendButton = new JButton("Send");
		sendButton.addActionListener(this);
		sendButton.setActionCommand("send");
		sendButton.setToolTipText("Click this button to send a mail.");
		add(sendButton);

		// Create panel layout using Spring Layout utilities.
		SpringUtilities.makeCompactGrid(this, // pane
				labels.length + 2, 2, // rows, cols
				6, 6, // initX, initY
				6, 6); // xPad, yPad
	}

	/**
	 * Create the GUI and show it. For thread safety, this method should be
	 * invoked from the event-dispatching thread.
	 */
	private static void createAndShowGUI() {
		// Create and set up the window.
		JFrame frame = new JFrame("Praktikum GRN, V1: SMTP-Client");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Create and set up the content pane.
		SMTPClient contentPane = new SMTPClient();
		contentPane.setOpaque(true); // content panes must be opaque
		frame.setContentPane(contentPane);

		// Display the window.
		frame.pack();
		frame.setVisible(true);
	}

	/**
	 * Action handler is called when the user clicks the button or presses Enter
	 * in a text field.
	 */
	public void actionPerformed(ActionEvent e) {
		String from, to, cc, bcc, username, password, subject, message;
		boolean verbose;

		if ("send".equals(e.getActionCommand())) {
			from = ((JTextComponent) inputComponents[InputField.FROM.ordinal()])
					.getText();
			to = ((JTextComponent) inputComponents[InputField.TO.ordinal()])
					.getText();
			cc = ((JTextComponent) inputComponents[InputField.CC.ordinal()])
					.getText();
			bcc = ((JTextComponent) inputComponents[InputField.BCC.ordinal()])
					.getText();
			username = ((JTextComponent) inputComponents[InputField.USERNAME
					.ordinal()]).getText();
			password = ((JTextComponent) inputComponents[InputField.PASSWORD
					.ordinal()]).getText();
			subject = ((JTextComponent) inputComponents[InputField.SUBJECT
					.ordinal()]).getText();
			message = ((JTextComponent) inputComponents[InputField.MESSAGE
					.ordinal()]).getText();
			verbose = verboseBox.isSelected();

			// --- test data
/*			from = "soburvit@tu-cottbus.de";
			to = "soburvit@tu-cottbus.de";
			
			bcc = "shumemar@tu-cottbus.de";
			username = "soburvit";
			password = "ma17slo";*/
			// ---

			sendMail(from, to, cc, bcc, username, password, subject, message, verbose);
		}
	}

	public void showMessage(String code, String message) {
		new ModalDialog(new JFrame(), code, message);
	}

	public Map<String, String> possibleResponses() {
		Map<String, String> responses = new HashMap<>();
		
		responses.put("421", "Service not available. Please try later.");
		responses.put("450", "The mailbox of recipient is unavailable. Please check the entered data or try again later.");
		responses.put("451", "local error in processing. Please try later.");
		responses.put("452", "Requested action not taken: insufficient system storage.");
		responses.put("500", "Syntax error, command unrecognised.");
		responses.put("501", "Syntax error in the input data. Please check all fields.");
		responses.put("502", "Command not implemented.");
		responses.put("503", "Bad sequence of commands.");
		responses.put("504", "Command parameter not implemented.");
		responses.put("521", "The Host does not accept mail. Make sure, that the entered data are correct.");
		responses.put("530", "Access denied.");
		responses.put("535", "Bad username or password.");
		responses.put("550", "The mailbox of recipient is unavailable. Please check the entered data or try again later.");
		responses.put("552", "Requested mail action aborted: exceeded storage allocation. Please try again later");
		responses.put("553", "Requested action not taken: mailbox name not allowed. Please check the input data.");
		responses.put("554", "Transaction failed. Please check the entered data or try again later.");

		return responses;
	}

	public String getResponesCode(String input) {
		return input.substring(0, 3);
	}

	public String getResponesMessage(Map<String, String> responses,
			String input) {
		String message = possibleResponses().get(getResponesCode(input));

		if (message != null) {
			return message;
		} else {
			return "Unknown Error!";
		}
	}

	public void log(String string, boolean verbose, boolean isServer) {
		if (verbose) {
			if (isServer == true) {
				String[] arr;	
				arr = string.split("\\|");
				for (int i = 0; i < arr.length; i ++) {
					if (i == 0) {
						System.out.println("S: " + arr[i]);
					} else {
						System.out.println("   " + arr[i]);
					}
				}
			} else {
				System.out.println("C: " + string);
			}
		}
	}
	
	public void sendMail(String from, String to, String cc, String bcc,
			String username, String password, String subject, String message, Boolean verbose) {

		String SMTPServer = "romeo.informatik.tu-cottbus.de";
		int SMTPPort = 25;

		try {
			@SuppressWarnings("resource")
			Socket echoSocket = new Socket(SMTPServer, SMTPPort);

			// Create a buffered reader for line-oriented reading from the
			// socket
			BufferedReader in = new BufferedReader(new InputStreamReader(
					echoSocket.getInputStream()));

			// Create a print writer for line-oriented writing to the socket
			PrintWriter out = new PrintWriter(echoSocket.getOutputStream(),
					true);

			// read first response from the mail server
			String reply = in.readLine();

			log(reply, verbose, true);
			
			if (getResponesCode(reply).equals("220")) {
				out.println("EHLO " + SMTPServer);
				log("EHLO " + SMTPServer, verbose, false);
				
		        // ______________ temporary FIX (code needs to be changed)
		        reply = "";
		        for (int i = 0; i < 10; i++) {
		          reply += "|" + in.readLine();
		        }
		        reply = reply.substring(1);
		        // ______________
				
				log(reply, verbose, true);
				
				if (getResponesCode(reply).equals("250")) {
					out.println("AUTH LOGIN");
					log("AUTH LOGIN", verbose, false);
					
					reply = in.readLine();
					log(reply, verbose, true);
					
					if(getResponesCode(reply).equals("334")) {
						out.println(Base64Coder.encode(username.getBytes()));
						log(Base64Coder.encode(username.getBytes()).toString(), verbose, false);
						
						reply = in.readLine();
						log(reply, verbose, true);
						
						if (getResponesCode(reply).equals("334")) {
							out.println(Base64Coder.encode(password.getBytes()));
							log(Base64Coder.encode(password.getBytes()).toString(), verbose, false);
							
							reply = in.readLine();
							log(reply, verbose, true);
							
							if (getResponesCode(reply).equals("235")) {
								out.println("MAIL FROM: <" + from + ">");
								log("MAIL FROM: <" + from + ">", verbose, false);
								
								reply = in.readLine();
								log(reply, verbose, true);
								
								if (getResponesCode(reply).equals("250")) {
									out.println("RCPT TO: <" + to + ">");
									log("RCPT TO: <" + to + ">", verbose, false);
									
									reply = in.readLine();
									log(reply, verbose, true);

									if (getResponesCode(reply).equals("250")) {
										if (cc != null && cc.equals("") == false) {
											String[] ccArr = cc.split(",");
											for (int i = 0; i < ccArr.length; i ++) {
												if (getResponesCode(reply).equals("250")) {
													out.println("RCPT TO: <" + ccArr[i].trim() + ">");
													log("RCPT TO: <" + ccArr[i].trim() + ">", verbose, false);
													
													reply = in.readLine();
													log(reply, verbose, true);
												} else {
													showMessage(getResponesCode(reply),
															getResponesMessage(possibleResponses(), reply));
													break;
												}
											}
					                    }
										
										if (bcc != null && bcc.equals("") == false) {
											String[] bccArr = bcc.split(",");
											
											for (int i = 0; i < bccArr.length; i ++) {
												if (getResponesCode(reply).equals("250")) {
													out.println("RCPT TO: <" + bccArr[i].trim() + ">");
													log("RCPT TO: <" + bccArr[i].trim() + ">", verbose, false);
													
													reply = in.readLine();
													log(reply, verbose, true);
												} else {
													showMessage(getResponesCode(reply),
															getResponesMessage(possibleResponses(), reply));
													break;
												}
											}
					                    }
										
										if (getResponesCode(reply).equals("250")) {
											out.println("DATA");
											log("DATA", verbose, false);
											
											reply = in.readLine();
											log(reply, verbose, true);
											
											if (getResponesCode(reply).equals("354")) {
							                      out.println("From: " + from);
							                      out.println("To: " + to);
							                      out.println("CC: " + cc);
							                      out.println("Subject: " + subject);
							                      out.println("Message: " + message);
							                      out.println(".");
							                      
							                      String letter = "From: " + from + "\n" +
							                    		  			"   To: " + to + "\n";
							                      if (cc != null && cc.equals("") == false) {
							                    	  letter += " CC: " + cc + "\n";
							                      }
							                      letter += "Subject: "+ subject + "\n" +
							                    		  "Message: " + message + "\n";
							                      
							                      log(letter, verbose, false);
												
												reply = in.readLine();
												log(reply, verbose, true);
												
												if (getResponesCode(reply).equals("250")) {
													out.println("QUIT");
													log("QUIT", verbose, false);
													
													reply = in.readLine();
													log(reply, verbose, true);
													
													if (getResponesCode(reply).equals("221")) {
														showMessage(getResponesCode(reply), "Your message was successfully sent!");
													} else {
														showMessage(getResponesCode(reply),
																getResponesMessage(possibleResponses(), reply));
													}
												} else {
													showMessage(getResponesCode(reply),
															getResponesMessage(possibleResponses(), reply));
												}
											} else {
												showMessage(getResponesCode(reply),
														getResponesMessage(possibleResponses(), reply));
											}
										} else {
											showMessage(getResponesCode(reply),
													getResponesMessage(possibleResponses(), reply));
										}
									} else {
										showMessage(getResponesCode(reply),
												getResponesMessage(possibleResponses(), reply));
									}
								} else {
									showMessage(getResponesCode(reply),
											getResponesMessage(possibleResponses(), reply));
								}
								
							} else {
								showMessage(getResponesCode(reply),
										getResponesMessage(possibleResponses(), reply));								
							}
						} else {
							showMessage(getResponesCode(reply),
									getResponesMessage(possibleResponses(), reply));
						}
					} else {
						showMessage(getResponesCode(reply),
								getResponesMessage(possibleResponses(), reply));
					}
				} else {
					showMessage(getResponesCode(reply),
							getResponesMessage(possibleResponses(), reply));
				}
			} else {
				showMessage(getResponesCode(reply),
						getResponesMessage(possibleResponses(), reply));
			}

		} catch (IOException e) {
			System.out.println(e.toString());
		} finally {
		}
	}

	public static void main(String[] args) throws UnknownHostException,
			IOException {
		// Schedule a job for the event-dispatching thread:
		// creating and showing this application's GUI.
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});
	}
}