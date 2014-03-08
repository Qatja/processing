package se.goransson.qatja;

/*
 * Copyright (C) 2012 Andreas Goransson, David Cuartielles
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ConcurrentHashMap;

import processing.core.PApplet;
import se.goransson.qatja.messages.MQTTConnack;
import se.goransson.qatja.messages.MQTTConnect;
import se.goransson.qatja.messages.MQTTDisconnect;
import se.goransson.qatja.messages.MQTTMessage;
import se.goransson.qatja.messages.MQTTPingreq;
import se.goransson.qatja.messages.MQTTPuback;
import se.goransson.qatja.messages.MQTTPubcomp;
import se.goransson.qatja.messages.MQTTPublish;
import se.goransson.qatja.messages.MQTTPubrec;
import se.goransson.qatja.messages.MQTTPubrel;
import se.goransson.qatja.messages.MQTTSuback;
import se.goransson.qatja.messages.MQTTSubscribe;
import se.goransson.qatja.messages.MQTTUnsuback;
import se.goransson.qatja.messages.MQTTUnsubscribe;

/**
 * Main library class
 * 
 * @author andreas
 * 
 */
public class Qatja implements MQTTConstants, MQTTConnectionConstants {

	/** Print debug messages */
	public boolean DEBUG = false;

	/** Reference to the parent sketch */
	private PApplet mPApplet;

	/** Library version */
	public final static String VERSION = "##library.prettyVersion##";

	private Connection mConnection;

	/** */
	private String host = null;

	private int port = -1;

	private String clientIdentifier = null;

	private String username = null;

	private String password = null;

	private boolean cleanSession = true;

	private MonitoringThread mMonitoringThread;
	private KeepaliveThread mKeepaliveThread;

	private int state = STATE_NONE;

	// private HashMap<String, Method> subscriptions;

	private Method callback = null;

	private ConcurrentHashMap<Integer, MQTTMessage> sentPackages = new ConcurrentHashMap<Integer, MQTTMessage>();
	private ConcurrentHashMap<Integer, MQTTMessage> receivedPackages = new ConcurrentHashMap<Integer, MQTTMessage>();

	// Ping Related variables
	/**
	 * Keep alive defines the interval (in seconds) at which the client should
	 * send pings to the broker to avoid disconnects.
	 * 
	 * This is set at the same time as the {@link #CONNECT} message is created.
	 */
	private int keepalive = 10;

	/**
	 * Defines at what time the last action was taken by the client (this is
	 * used to determine if a ping should be sent or not)
	 */
	private long last_action = 0;

	/**
	 * Defines the number of seconds that the client will wait for a ping
	 * response before disconnecting.
	 */
	private long ping_grace = 5;

	/** Storage for the last sent ping request message. */
	private long last_ping_request = 0;

	/** Defines if a ping request has been sent. */
	private boolean ping_sent = false;

	/**
	 * a Constructor, usually called in the setup() method in your sketch to
	 * initialize and start the library.
	 * 
	 * @example Hello
	 * @param theParent
	 */
	public Qatja(PApplet theParent) {
		mPApplet = theParent;
		welcome();

		// subscriptions = new HashMap<String, Method>();

		// Add the raw method subscription (gets all subscriptions)
		try {
			callback = mPApplet.getClass().getMethod("mqttCallback",
					MQTTPublish.class);
		} catch (Exception e) {
			PApplet.println("you need the void mqttCallback(MQTTPublish msg){} method!");
			if (DEBUG)
				e.printStackTrace();
			return;
		}
	}

	/**
	 * Print library message to console
	 */
	private void welcome() {
		System.out
				.println("##library.name## ##library.prettyVersion## by ##author##");
	}

	/**
	 * return the version of the library.
	 * 
	 * @return String
	 */
	public static String version() {
		return VERSION;
	}

	/**
	 * Set the keep alive time out for the client in seconds; this defines at
	 * what interval the client should send pings to the broker so that it
	 * doesn't disconnect.
	 * 
	 * Default is set at 10 seconds.
	 * 
	 * @param seconds
	 */
	public void setKeepalive(int seconds) {
		keepalive = seconds;
	}

	/**
	 * @return the clientIdentifier
	 */
	public String getClientIdentifier() {
		return clientIdentifier;
	}

	/**
	 * @param clientIdentifier
	 *            the clientIdentifier to set
	 */
	public void setClientIdentifier(String clientIdentifier) {
		this.clientIdentifier = clientIdentifier;
	}

	/**
	 * @return the host
	 */
	public String getHost() {
		return host;
	}

	/**
	 * @param host
	 *            the host to set
	 */
	public void setHost(String host) {
		this.host = host;
	}

	/**
	 * @return the port
	 */
	public int getPort() {
		return port;
	}

	/**
	 * @param port
	 *            the port to set
	 */
	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * @param username
	 *            the username to set
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @param password
	 *            the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * @return the keepalive
	 */
	public long getKeepalive() {
		return keepalive;
	}

	/**
	 * @return the cleanSession
	 */
	public boolean getCleanSession() {
		return cleanSession;
	}

	/**
	 * @param cleanSession
	 *            set clean session
	 */
	public void setCleanSession(boolean cleanSession) {
		this.cleanSession = cleanSession;
	}

	/**
	 * Connect to a MQTT server. This also sends the required connect message.
	 */
	public void connect() {
		InetAddress addr = null;
		try {
			addr = InetAddress.getByName(host);
		} catch (UnknownHostException e) {
			if (DEBUG)
				PApplet.println("Ohno! Something went wrong... Unknown host error, I didn't understand the host name.");
			if (DEBUG)
				e.printStackTrace();
			return;
		}

		try {
			mConnection = new TCPConnection(addr, port);
		} catch (IOException e) {
			if (DEBUG)
				PApplet.println("Ohno! Something went wrong... IO Error, failed to establish a connection to host.");
			if (DEBUG)
				e.printStackTrace();
			return;
		}

		mMonitoringThread = new MonitoringThread(mConnection);
		Thread thread1 = new Thread(null, mMonitoringThread, "MonitoringThread");
		thread1.start();

		mKeepaliveThread = new KeepaliveThread();
		Thread thread2 = new Thread(null, mKeepaliveThread, "KeepaliveThread");
		thread2.start();

		sendConnect();
	}

	/**
	 * Used to send the connect message, shouldn't be used outside the MQTT
	 * class.
	 * 
	 * @param id
	 */
	private void sendConnect() {
		if (state == STATE_NONE) {
			MQTTConnect connect;
			connect = new MQTTConnect(clientIdentifier, username, password,
					true);
			connect.setCleanSession(getCleanSession());

			keepalive = connect.getKeepAlive();
			sendMessage(connect, false);
		} else {
			if (DEBUG)
				PApplet.println("Ohno! Something went wrong... MQTT Error, dude... one connection at a time!");
		}
	}

	/**
	 * Send the disconnect message.
	 */
	public void disconnect() {
		// TODO Maybe a better way of doing this?
		if (state == STATE_CONNECTED) {
			MQTTDisconnect msg = new MQTTDisconnect();
			sendMessage(msg);

			try {
				mConnection.close();
			} catch (IOException e) {
				if (DEBUG)
					PApplet.println("Ohno! Something went wrong... IO Error, failed to close the connection.");
				if (DEBUG)
					e.printStackTrace();
			}

			mMonitoringThread.stop();
			mKeepaliveThread.stop();

			state = STATE_NONE;
		} else {
			if (DEBUG)
				PApplet.println("Ohno! Something went wrong... MQTT Error, you gots to be connected dude!");
		}
	}

	/**
	 * Publish a message to specific topic. Quality of service is
	 * {@link #AT_MOST_ONCE}, no retain
	 * 
	 * @param topic
	 *            the topic
	 * @param message
	 *            the message
	 */
	public void publish(String topic, String message) {
		publish(topic, message.getBytes());
	}

	/**
	 * Publish a message to specific topic. Quality of service is
	 * {@link #AT_MOST_ONCE}, no retain
	 * 
	 * @param topic
	 *            the topic
	 * @param buffer
	 *            the message
	 */
	public void publish(String topic, byte[] buffer) {
		publish(topic, buffer, false);
	}

	/**
	 * Publish a message to specific topic. Quality of service is
	 * {@link #AT_MOST_ONCE}
	 * 
	 * @param topic
	 *            the topic
	 * @param message
	 *            the message
	 * @param retain
	 *            set retain message
	 */
	public void publish(String topic, String message, boolean retain) {
		publish(topic, message.getBytes(), retain);
	}

	/**
	 * Publish a byte[] to specific topic. Quality of service is
	 * {@link #AT_MOST_ONCE}
	 * 
	 * @param topic
	 *            the topic
	 * @param buffer
	 *            the buffer
	 * @param retain
	 *            set retain message
	 */
	public void publish(String topic, byte[] buffer, boolean retain) {
		MQTTPublish msg = new MQTTPublish(topic, buffer);
		msg.setRetain(retain);
		sendMessage(msg);
	}

	/**
	 * Publish a message to a specific topic. Quality of service is
	 * {@link #AT_LEAST_ONCE}, no retain
	 * 
	 * @param topic
	 *            the topic
	 * @param message
	 *            the message
	 */
	public void publishAtLeastOnce(String topic, String message) {
		publishAtLeastOnce(topic, message.getBytes());
	}

	/**
	 * Publish a message to a specific topic. Quality of service is
	 * {@link #AT_LEAST_ONCE}, to retain
	 * 
	 * @param topic
	 *            the topic
	 * @param buffer
	 *            the message
	 */
	public void publishAtLeastOnce(String topic, byte[] buffer) {
		publishAtLeastOnce(topic, buffer, false);
	}

	/**
	 * Publish a message to a specific topic. Quality of service is
	 * {@link #AT_LEAST_ONCE}
	 * 
	 * @param topic
	 *            the topic
	 * @param message
	 *            the message
	 * @param retain
	 *            set retain message
	 */
	public void publishAtLeastOnce(String topic, String message, boolean retain) {
		publishAtLeastOnce(topic, message.getBytes(), retain);
	}

	/**
	 * Publish a message to a specific topic. Quality of service is
	 * {@link #AT_LEAST_ONCE}
	 * 
	 * @param topic
	 *            the topic
	 * @param buffer
	 *            the message
	 * @param retain
	 *            set retain message
	 */
	public void publishAtLeastOnce(String topic, byte[] buffer, boolean retain) {
		MQTTPublish msg = new MQTTPublish(topic, buffer, AT_LEAST_ONCE);
		msg.setRetain(retain);
		addSentPackage(msg);
		sendMessage(msg);
	}

	/**
	 * Publish a message to a specific topic. Quality of service is
	 * {@link #EXACTLY_ONCE}, no retain
	 * 
	 * @param topic
	 *            the topic
	 * @param message
	 *            the message
	 */
	public void publishExactlyOnce(String topic, String message) {
		publishExactlyOnce(topic, message.getBytes());
	}

	/**
	 * Publish a message to a specific topic. Quality of service is
	 * {@link #EXACTLY_ONCE}, no retain
	 * 
	 * @param topic
	 *            the topic
	 * @param buffer
	 *            the message
	 */
	public void publishExactlyOnce(String topic, byte[] buffer) {
		publishExactlyOnce(topic, buffer, false);
	}

	/**
	 * Publish a message to a specific topic. Quality of service is
	 * {@link #EXACTLY_ONCE}
	 * 
	 * @param topic
	 *            the topic
	 * @param message
	 *            the message
	 * @param retain
	 *            set retain
	 */
	public void publishExactlyOnce(String topic, String message, boolean retain) {
		publishExactlyOnce(topic, message.getBytes(), retain);
	}

	/**
	 * Publish a message to a specific topic. Quality of service is
	 * {@link #EXACTLY_ONCE}
	 * 
	 * @param topic
	 *            the topic
	 * @param buffer
	 *            the message
	 * @param retain
	 *            set retain
	 */
	public void publishExactlyOnce(String topic, byte[] buffer, boolean retain) {
		MQTTPublish msg = new MQTTPublish(topic, buffer, EXACTLY_ONCE);
		msg.setRetain(retain);
		addSentPackage(msg);
		sendMessage(msg);
	}

	/**
	 * Subscribe to topic
	 * 
	 * @param topic
	 *            the topic
	 */
	public void subscribe(String topic) {
		subscribe(topic, AT_MOST_ONCE);
	}

	public void subscribe(String topic, byte QoS) {
		String[] topicFilters = { topic };
		byte[] QoSs = { QoS };
		MQTTSubscribe msg = new MQTTSubscribe(topicFilters, QoSs);
		addSentPackage(msg);
		sendMessage(msg);
	}

	/**
	 * Subscribe to multiple topics with {@link #AT_MOST_ONCE}
	 * 
	 * @param topics
	 */
	public void subscribe(String... topics) {
		byte[] QoSs = new byte[topics.length];
		for (int i = 0; i < topics.length; i++)
			QoSs[i] = AT_MOST_ONCE;
		MQTTSubscribe msg = new MQTTSubscribe(topics, QoSs);
		addSentPackage(msg);
		sendMessage(msg);
	}

	/**
	 * Unsubscribe to a topic
	 * 
	 * @param topic
	 */
	public void unsubscribe(String topic) {
		String[] topicFilters = { topic };
		unsubscribe(topicFilters);
	}

	/**
	 * Unsubscribe to multiple topics
	 * 
	 * @param topics
	 */
	public void unsubscribe(String... topics) {
		MQTTUnsubscribe msg = new MQTTUnsubscribe(topics);
		addSentPackage(msg);
		sendMessage(msg);
	}

	/**
	 * Make sure to resend packages that haven't been successfully sent. TODO
	 */
	private void resendPackages() {
		for (MQTTMessage msg : sentPackages.values()) {
			if (msg instanceof MQTTPublish) {
				((MQTTPublish) msg).setDup();
			}
			sendMessage(msg);
		}

		for (MQTTMessage msg : receivedPackages.values()) {
			if (msg instanceof MQTTPublish) {
				((MQTTPublish) msg).setDup();
			}
			sendMessage(msg);
		}
	}

	private synchronized void handleSubscriptions(MQTTMessage msg) {
		if (msg instanceof MQTTSuback) {
			MQTTSuback suback = (MQTTSuback) msg;

			MQTTSubscribe subscribe = (MQTTSubscribe) sentPackages.get(suback
					.getPackageIdentifier());
			String[] topicFilters = subscribe.getTopicFilters();
			byte[] qoss = suback.getPayload();
			for (int i = 0; i < qoss.length; i++) {
				switch (qoss[i]) {
				case SUBSCRIBE_SUCCESS_AT_MOST_ONCE:
				case SUBSCRIBE_SUCCESS_AT_LEAST_ONCE:
				case SUBSCRIBE_SUCCESS_EXACTLY_ONCE:
					PApplet.println("Success subscribing to " + topicFilters[i]);
					break;
				case SUBSCRIBE_FAILURE:
					PApplet.println("Failed subscribing to " + topicFilters[i]);
					break;
				}
			}
		} else if (msg instanceof MQTTUnsuback) {
			MQTTUnsuback unsuback = (MQTTUnsuback) msg;

			MQTTUnsubscribe unsubscribe = (MQTTUnsubscribe) sentPackages
					.get(unsuback.getPackageIdentifier());
			String[] topicFilters = unsubscribe.getTopicFilters();
			for (int i = 0; i < topicFilters.length; i++) {
				PApplet.println("Success unsubscribing to " + topicFilters[i]);
			}
		}
	}

	// /**
	// *
	// * @param topic
	// * @return
	// */
	// private boolean registerSubscription(String topic, String method) {
	// Method m = null;
	//
	// try {
	// m = mPApplet.getClass().getMethod(method, MQTTMessage.class);
	// } catch (Exception e) {
	// if (DEBUG)
	// PApplet.println("Ohno! Something went wrong... MQTT Error, you forgot to add the subscription method! 1 "
	// + topic);
	// if (DEBUG)
	// e.printStackTrace();
	// return false;
	// }
	//
	// // Add the callback
	// if (m != null) {
	// subscriptions.put(topic, m);
	// } else {
	// if (DEBUG)
	// PApplet.println("Ohno! Something went wrong... MQTT Error, you forgot to add the subscription method! 2 "
	// + topic);
	// return false;
	// }
	// return true;
	// }

	private void addSentPackage(MQTTMessage msg) {
		sentPackages.put(msg.getPackageIdentifier(), msg);
	}

	private void removeSentPackage(MQTTMessage msg) {
		sentPackages.remove(msg.getPackageIdentifier());
	}

	private void addReceivedPackage(MQTTMessage msg) {
		receivedPackages.put(msg.getPackageIdentifier(), msg);
	}

	private void removeReceivedPackage(MQTTMessage msg) {
		receivedPackages.remove(msg.getPackageIdentifier());
	}

	/**
	 * Send an MQTT message to server
	 * 
	 * @param msg
	 *            the message
	 */
	private synchronized void sendMessage(MQTTMessage msg) {
		sendMessage(msg, true);
	}

	// TODO Clean this up...
	/**
	 * Send an MQTT message to server
	 * 
	 * @param msg
	 *            the message
	 * @param checkConnection
	 *            check if connected, false for CONNECT message
	 */
	private synchronized void sendMessage(MQTTMessage msg,
			boolean checkConnection) {
		if (checkConnection) {
			if (state == STATE_CONNECTED) {
				if (DEBUG)
					PApplet.println("Sending "
							+ MQTTHelper.decodePackageName(msg));
				try {
					mConnection.getOutputStream().write(msg.get());
					last_action = System.currentTimeMillis();
				} catch (IOException e) {
					if (DEBUG)
						PApplet.println("Ohno! Something went wrong... IO Error, failed to send MQTT message.");
					if (DEBUG)
						e.printStackTrace();
				} catch (MQTTException e) {
					e.printStackTrace();
				}
			} else {
				if (DEBUG)
					PApplet.println("Ohno! Something went wrong... MQTT Error, you gots to be connected dude!");
			}
		} else {
			try {
				mConnection.getOutputStream().write(msg.get());
				last_action = System.currentTimeMillis();
			} catch (IOException e) {
				if (DEBUG)
					PApplet.println("Ohno! Something went wrong... IO Error, failed to send MQTT message.");
				if (DEBUG)
					e.printStackTrace();
			} catch (MQTTException e) {
				e.printStackTrace();
			}
		}
	}

	private class KeepaliveThread implements Runnable {

		private volatile boolean finished = false;

		@Override
		public void run() {
			while (!finished) {

				if (ping_sent
						&& System.currentTimeMillis() - last_ping_request > (ping_grace * 1000)) {
					if (DEBUG)
						PApplet.println("Ohno! Something went wrong... MQTT Error, didn't get a ping response - maybe the connection died.");
					disconnect();
					ping_sent = false;
				}

				if (System.currentTimeMillis() - last_action > (keepalive * 1000)) {
					if (state == STATE_CONNECTED) {
						synchronized (mConnection) {
							MQTTPingreq msg = new MQTTPingreq();

							try {
								mConnection.getOutputStream().write(msg.get());
								ping_sent = true;
								last_action = System.currentTimeMillis();
							} catch (IOException e) {
								if (DEBUG)
									PApplet.println("Ohno! Something went wrong... IO Error, failed to send PING message.");
								if (DEBUG)
									e.printStackTrace();
							} catch (MQTTException e) {
								e.printStackTrace();
							}
						}
					}
				}

				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					if (DEBUG)
						e.printStackTrace();
				}
			}
		}

		public void stop() {
			finished = true;
		}
	}

	private class MonitoringThread implements Runnable {

		Connection mConnection;

		private volatile boolean finished;

		public MonitoringThread(Connection connection) {
			mConnection = connection;
		}

		public void stop() {
			finished = true;
		}

		public void run() {
			int len = 0;
			byte[] buffer = new byte[16384];

			while (!finished || len >= 0) {
				try {
					len = mConnection.getInputStream().read(buffer);
				} catch (IOException e) {
					if (DEBUG)
						PApplet.println("Ohno! Something went wrong... IO Error, failed to read messages.");
					if (DEBUG)
						e.printStackTrace();
					break;
				}

				if (len > 0) {
					byte type = MQTTHelper.decode(buffer);

					if (DEBUG)
						PApplet.println(MQTTHelper.decodePackageName(type));

					switch (type) {
					case CONNECT:
						state = STATE_CONNECTING;
						break;
					case CONNACK:
						MQTTConnack msg = new MQTTConnack(buffer, len);
						switch (msg.getReturnCode()) {
						case CONNECTION_ACCEPTED:
							if (DEBUG)
								PApplet.println("Connected");
							state = STATE_CONNECTED;
							break;
						case CONNECTION_REFUSED_VERSION:
							if (DEBUG)
								PApplet.println("Failed to connect, unaccebtable protocol version");
							state = STATE_CONNECTION_FAILED;
							break;
						case CONNECTION_REFUSED_IDENTIFIER:
							if (DEBUG)
								PApplet.println("Failed to connect, identifier rejected");
							state = STATE_CONNECTION_FAILED;
							break;
						case CONNECTION_REFUSED_SERVER:
							if (DEBUG)
								PApplet.println("Failed to connect, server unavailable");
							state = STATE_CONNECTION_FAILED;
							break;
						case CONNECTION_REFUSED_USER:
							if (DEBUG)
								PApplet.println("Failed to connect, bad username or password");
							state = STATE_CONNECTION_FAILED;
							break;
						case CONNECTION_REFUSED_AUTH:
							if (DEBUG)
								PApplet.println("Failed to connect, not authorized");
							state = STATE_CONNECTION_FAILED;
							break;
						}
						break;
					case PUBLISH:
						MQTTPublish publish = new MQTTPublish(buffer, len);
						switch (publish.getQoS()) {
						case AT_MOST_ONCE:
							// Do nothing
							break;
						case AT_LEAST_ONCE:
							// Send PUBACK
							MQTTPuback puback_ = new MQTTPuback(
									publish.getPackageIdentifier());
							if (DEBUG)
								PApplet.println("Got "
										+ MQTTHelper.decodePackageName(publish)
										+ " with QoS AT LEAST ONCE, sending "
										+ MQTTHelper.decodePackageName(puback_));
							sendMessage(puback_);

							break;
						case EXACTLY_ONCE:
							// Send PUBREC and store message
							MQTTPubrec pubrec_ = new MQTTPubrec(
									publish.getPackageIdentifier());
							if (DEBUG)
								PApplet.println("Got "
										+ MQTTHelper.decodePackageName(publish)
										+ " with QoS EXACTLY ONCE, sending "
										+ MQTTHelper.decodePackageName(pubrec_));
							addReceivedPackage(pubrec_);
							sendMessage(pubrec_);

							break;
						}

						// Method eventMethod = subscriptions
						// .get(msg.variableHeader.get("topic_name"));
						//
						// if (eventMethod != null) {
						// try {
						// eventMethod.invoke(mPApplet, msg);
						// } catch (IllegalAccessException e) {
						// if (DEBUG)
						// e.printStackTrace();
						// } catch (IllegalArgumentException e) {
						// if (DEBUG)
						// e.printStackTrace();
						// } catch (InvocationTargetException e) {
						// if (DEBUG)
						// e.printStackTrace();
						// }
						// }

						// Always send to "the callback" output
						try {
							callback.invoke(mPApplet, publish);
						} catch (IllegalAccessException e) {
							if (DEBUG)
								e.printStackTrace();
						} catch (IllegalArgumentException e) {
							if (DEBUG)
								e.printStackTrace();
						} catch (InvocationTargetException e) {
							if (DEBUG)
								e.printStackTrace();
						}

						break;
					case PUBACK:
						MQTTPuback puback = new MQTTPuback(buffer, len);
						removeSentPackage(puback);

						break;
					case PUBREC:
						MQTTPubrec pubrec = new MQTTPubrec(buffer, len);
						int packageIdentifier = pubrec.getPackageIdentifier();
						removeSentPackage(pubrec);

						MQTTPubrel pubrel_ = new MQTTPubrel(packageIdentifier);
						addSentPackage(pubrel_);
						sendMessage(pubrel_);

						break;
					case PUBREL:
						MQTTPubrel pubrel = new MQTTPubrel(buffer, len);
						removeReceivedPackage(pubrel);

						MQTTPubcomp pubcomp_ = new MQTTPubcomp(
								pubrel.getPackageIdentifier());
						sendMessage(pubcomp_);

						break;
					case PUBCOMP:
						MQTTPubcomp pubcomp = new MQTTPubcomp(buffer, len);
						removeReceivedPackage(pubcomp);

						break;
					case SUBSCRIBE:
						// Client doesn't receive this message

						break;
					case SUBACK:
						MQTTSuback suback = new MQTTSuback(buffer, len);
						handleSubscriptions(suback);

						break;
					case UNSUBSCRIBE:
						// Client doesn't receive this message

						break;
					case UNSUBACK:
						MQTTUnsuback unsuback = new MQTTUnsuback(buffer, len);
						handleSubscriptions(unsuback);
						removeSentPackage(unsuback);

						break;
					case PINGREQ:
						// Client doesn't receive this message
						last_ping_request = System.currentTimeMillis();

						break;
					case PINGRESP:
						ping_sent = false;

						break;
					}
				}
			}
		}
	}
}
