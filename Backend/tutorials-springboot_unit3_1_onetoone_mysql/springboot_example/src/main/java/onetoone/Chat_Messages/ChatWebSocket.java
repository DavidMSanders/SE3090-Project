package onetoone.Chat_Messages;

import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import jakarta.websocket.server.PathParam;


import onetoone.Users.UserRepository;
import onetoone.Users.User;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 *@author Chris Lopez
 *Endpoint: ws://server:8080/chat/{username}
 */
@Component
@ServerEndpoint(value = "/chat/{username}")
public class ChatWebSocket{
    private static MessageRepository messageStore;
    private static UserRepository accountStore;
    private static Map<String, Session> liveConnections = new ConcurrentHashMap<>();
    private static Map<Session, String> sessionToUsername = new ConcurrentHashMap<>();
    private static Map<Session, Integer> sessionToUserId = new ConcurrentHashMap<>();
    private final Logger log = LoggerFactory.getLogger(ChatWebSocket.class);

    @Autowired
    public void setMessageRepository(MessageRepository repository) {
        ChatWebSocket.messageStore = repository;
    }


    @Autowired
    public void setUserRepository(UserRepository repository){
        ChatWebSocket.accountStore = repository;
    }


    @OnOpen
    public void handleOpen(Session clientSession, @PathParam("username") String username) throws IOException {
        log.info("Connection attempt from username: {}", username);

        Optional<User> accountQuery = accountStore.findByAccountName(username);
        if(accountQuery.isEmpty()){
        log.warn("User not found: {}", username);
        clientSession.close(new CloseReason(CloseReason.CloseCodes.CANNOT_ACCEPT, "User not found"));
        return;
        }

        User account = accountQuery.get();
        liveConnections.put(username, clientSession);
        sessionToUsername.put(clientSession, username);
        sessionToUserId.put(clientSession, account.getUserId());
        log.info("User connected: {} (ID: {}, Role: {})", username, account.getUserId(), account.getRole());

        transmitSystemAlert(clientSession, "Connected to Personal Trainer Chat");
        transmitSystemAlert(clientSession, "Your role: " + account.getRole());
        loadMessageHistory(clientSession, account.getUserId());

        String arrivalAlert = username + " joined the chat";
        notifyStaffMembers(arrivalAlert, account.getUserId());
    }


    @OnMessage
    public void handleMessage(Session clientSession, String incomingText) throws IOException{
        String authorName = sessionToUsername.get(clientSession);
        Integer authorId = sessionToUserId.get(clientSession);

        if(authorName == null || authorId == null){
            log.warn("Message from unknown session");
            return;
        }

        log.info("Message from {}: {}", authorName, incomingText);


        if(incomingText.startsWith("@")) {
            processDirectMessage(clientSession, authorId, authorName, incomingText);
        } else {
            processStaffBroadcast(clientSession, authorId, authorName, incomingText);
        }
        }

        @OnClose
    public void handleClose(Session clientSession) throws IOException {
            String username = sessionToUsername.get(clientSession);
            Integer userId = sessionToUserId.get(clientSession);

            if (username != null) {
                liveConnections.remove(username);
                sessionToUsername.remove(clientSession);
                sessionToUserId.remove(clientSession);
                log.info("User disconnected: {} (ID: {})", username, userId);

                String departureAlert = username + " left the chat";
                notifyStaffMembers(departureAlert, userId);
            }
        }
        @OnError
    public void handleError(Session clientSession, Throwable error) {
            log.error("WebSocket error", error);
        }
        private void processDirectMessage(Session originatorSession, Integer originatorId, String originatorName, String fullText){
        try {
            String[] components = fullText.substring(1).split(" ", 2);
            if (components.length < 2) {
                transmitSystemAlert(originatorSession, "Invalid format. Use: @username message");
                return;
            }

            String destinationName = components[0];
            String messagePayload = components[1];

            Optional<User> destinationQuery = accountStore.findByAccountName(destinationName);
            if (destinationQuery.isEmpty()) {
                transmitSystemAlert(originatorSession, "User not found: " + destinationName);
                return;
            }

            User destinationAccount = destinationQuery.get();
            Integer destinationId = destinationAccount.getUserId();
            Message messageRecord = new Message(originatorId, destinationId, messagePayload);
            messageStore.save(messageRecord);
            String displayText = composeTimestampedMessage(originatorName, messagePayload, messageRecord.getCreatedTimestamp());

            Session destinationSession = liveConnections.get(destinationName);
            if (destinationSession != null && destinationSession.isOpen()) {
                transmitToSession(destinationSession, displayText);
            }

            transmitToSession(originatorSession, "[To " + destinationName + "] " + displayText);
            alertStaffOfPrivateChat(originatorId, destinationId, originatorName, destinationName, displayText);
            log.info("Private message: {} -> {}", originatorName, destinationName);
        } catch (Exception error) {
            log.error("Error in private message", error);
            transmitSystemAlert(originatorSession, "Error sending message");
        }
        }

        private void processStaffBroadcast(Session originatorSession, Integer originatorId, String originatorName, String messagePayload) {
            String broadcastText = "[BROADCAST] " + composeTimestampedMessage(originatorName, messagePayload, LocalDateTime.now());
            notifyStaffMembers(broadcastText, originatorId);
            transmitSystemAlert(originatorSession, "Broadcast sent to all staff");
        }

        private void transmitToSession(Session destination, String messageText){
        if(destination != null && destination.isOpen()){
            try {
                destination.getBasicRemote().sendText(messageText);
            } catch (IOException error) {
                log.error("Error sending to session", error);
                }
            }
        }

        private void transmitSystemAlert(Session destination, String alertText) {
            transmitToSession(destination, "[SYSTEM] " + alertText);
        }

        private void notifyStaffMembers(String notification, Integer excludedId){
        sessionToUserId.forEach((connection, accountId) -> {
            if (accountId.equals(excludedId)) {
                return;
            }

            Optional<User> accountQuery = accountStore.findById(accountId);
            if (accountQuery.isPresent()) {
                User account = accountQuery.get();
                String accountRole = account.getRole();

                if (accountRole.equals("Trainer") || accountRole.equals("Manager") || accountRole.equals("Admin")) {
                    transmitToSession(connection, notification);
                }
            }

        });

        }

        private void alertStaffOfPrivateChat(Integer originatorId, Integer destinationId, String originatorName, String destinationName,
                                              String messageDisplay){
        sessionToUserId.forEach((connection, accountId) -> {
            if(accountId.equals(originatorId) || accountId.equals(destinationId)){
                return;
            }
            Optional<User> accountQuery = accountStore.findById(accountId);
            if (accountQuery.isPresent()){
                User account = accountQuery.get();
                String accountRole = account.getRole();

                if(accountRole.equals("Trainer") || accountRole.equals("Manager") || accountRole.equals("Admin")){
                    String monitorNotice = String.format("[MONITOR] %s -> %s: %s", originatorName, destinationName, messageDisplay);
                    transmitToSession(connection, monitorNotice);
                }
            }
        });
        }

        private void loadMessageHistory(Session destination, Integer accountId){
        try {
            List<Message> historicalMessages = messageStore.retrieveAllMessagesForUser(accountId);

            if (historicalMessages.isEmpty()) {
                transmitSystemAlert(destination, "No message history");
                return;
            }

            transmitSystemAlert(destination, "=== Recent Messages (last 50) ===");

            int maxDisplay = Math.min(historicalMessages.size(), 50);
            for (int position = maxDisplay - 1; position >= 0; position--) {
                Message record = historicalMessages.get(position);

                Optional<User> authorQuery = accountStore.findById(record.getOriginatorId());
                String authorName = authorQuery.map(User::getAccountName).orElse("Unknown");

                String displayText = composeTimestampedMessage(authorName, record.getTextBody(), record.getCreatedTimestamp());
                transmitToSession(destination, displayText);
            }

            transmitSystemAlert(destination, "=== End of History ===");
        } catch (Exception error) {
            log.error("Error sending history", error);
        }
        }

        private String composeTimestampedMessage(String authorName, String bodyText, LocalDateTime timestamp){
        DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm:ss");
        return String.format("[%s] %s: %s", timestamp.format(timeFormat), authorName, bodyText);





    }



}
/*
@Controller      // this is needed for this to be an endpoint to springboot

@ServerEndpoint(value = "/chat/{username}")  // this is Websocket url
public class ChatSocket {

  // cannot autowire static directly (instead we do it by the below
  // method
	private static MessageRepository msgRepo; 

	/*
   * Grabs the MessageRepository singleton from the Spring Application
   * Context.  This works because of the @Controller annotation on this
   * class and because the variable is declared as static.
   * There are other ways to set this. However, this approach is
   * easiest.

	@Autowired
	public void setMessageRepository(MessageRepository repo) {
		msgRepo = repo;  // we are setting the static variable
	}

	// Store all socket session and their corresponding username.
	private static Map<Session, String> sessionUsernameMap = new Hashtable<>();
	private static Map<String, Session> usernameSessionMap = new Hashtable<>();

	private final Logger logger = LoggerFactory.getLogger(ChatSocket.class);

	@OnOpen
	public void onOpen(Session session, @PathParam("username") String username) 
      throws IOException {

		logger.info("Entered into Open");

    // store connecting user information
		sessionUsernameMap.put(session, username);
		usernameSessionMap.put(username, session);

		//Send chat history to the newly connected user
		sendMessageToPArticularUser(username, getChatHistory());
		
    // broadcast that new user joined
		String message = "User:" + username + " has Joined the Chat";
		broadcast(message);
	}


	@OnMessage
	public void onMessage(Session session, String message) throws IOException {

		// Handle new messages
		logger.info("Entered into Message: Got Message:" + message);
		String username = sessionUsernameMap.get(session);

    // Direct message to a user using the format "@username <message>"
		if (message.startsWith("@")) {
			String destUsername = message.split(" ")[0].substring(1); 

      // send the message to the sender and receiver
			sendMessageToPArticularUser(destUsername, "[DM] " + username + ": " + message);
			sendMessageToPArticularUser(username, "[DM] " + username + ": " + message);

		} 
    else { // broadcast
			broadcast(username + ": " + message);
		}

		// Saving chat history to repository
		msgRepo.save(new Message(username, message));
	}


	@OnClose
	public void onClose(Session session) throws IOException {
		logger.info("Entered into Close");

    // remove the user connection information
		String username = sessionUsernameMap.get(session);
		sessionUsernameMap.remove(session);
		usernameSessionMap.remove(username);

    // broadcase that the user disconnected
		String message = username + " disconnected";
		broadcast(message);
	}


	@OnError
	public void onError(Session session, Throwable throwable) {
		// Do error handling here
		logger.info("Entered into Error");
		throwable.printStackTrace();
	}


	private void sendMessageToPArticularUser(String username, String message) {
		try {
			usernameSessionMap.get(username).getBasicRemote().sendText(message);
		} 
    catch (IOException e) {
			logger.info("Exception: " + e.getMessage().toString());
			e.printStackTrace();
		}
	}


	private void broadcast(String message) {
		sessionUsernameMap.forEach((session, username) -> {
			try {
				session.getBasicRemote().sendText(message);
			} 
      catch (IOException e) {
				logger.info("Exception: " + e.getMessage().toString());
				e.printStackTrace();
			}

		});

	}
	

  // Gets the Chat history from the repository
	private String getChatHistory() {
		List<Message> messages = msgRepo.findAll();

		// convert the list to a string
		StringBuilder sb = new StringBuilder();
		if (messages != null && messages.size() != 0) {
			for (Message message : messages) {
				sb.append(message.getUserName() + ": " + message.getContent() + "\n");
			}
		}
		return sb.toString();
	} */

 // end of Class
