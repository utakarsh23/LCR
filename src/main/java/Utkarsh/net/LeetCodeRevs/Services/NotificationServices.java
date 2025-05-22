package Utkarsh.net.LeetCodeRevs.Services;

import Utkarsh.net.LeetCodeRevs.Entity.User;
import Utkarsh.net.LeetCodeRevs.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class NotificationServices {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DailyQuestionAssignmentServices dailyQuestionAssignmentServices;

    public void sendDailyAssignmentNotification(String email, String questionTitle, String questionTopicTitle) {
        System.out.println(email);
        String msg = "Your daily questions are: " + questionTitle + "\n&\n" + questionTopicTitle;
        messagingTemplate.convertAndSend("/topic/" + email, msg); //sends the msg to all clients subscribed to the topic /topic/{email},
        // enabling personalized real-time messaging per user.
    }

    public void sendReminderNotification(String email) {
        System.out.println(email);
        String msg = "You haven't solved today's question!";
        messagingTemplate.convertAndSend("/topic/" + email, msg); //sends the msg to all clients subscribed to the topic /topic/{email},
        // enabling personalized real-time messaging per user.
    }

    @Scheduled(cron = "00 00 20 * * ?")
    @Async // run this method asynchronously (in background thread)
    public void remindUsersToSolveDailyQuestion() { // add feature to not send messages to the user who already solved today's question
        System.out.println("Reminder to solving question given");
        for (User user : userRepository.findAll()) { // loop through every user in the DB
            if(user.getDailyAssignedQuestionLink() != null)   // if user already has a daily assigned question link, just send reminder notification
                sendReminderNotification(user.getEmail());
            else {
                // if no daily question assigned yet, assign one and then notify
                try {
                    dailyQuestionAssignmentServices.assignDailyQuestion(user);
                    sendReminderNotification(user.getEmail());
                } catch (Exception e) {
                    System.err.println("Error while assigning questions and Sending Notification:" + e);  // catch and log any error so rest of users still get reminders
                }
            }

        }
    }

    @Scheduled(cron = "00 00 05 * * ?")
    @Async
    public void questionAssignmentNotification() { // add feature to not to sned messages to the user who already solved today's question
        System.out.println("Reminder to assignment of question given");
        for (User user : userRepository.findAll()) { //ditto comments as above
            if(user.getDailyAssignedTopicQuestionLink() != null || user.getDailyAssignedQuestionLink() != null) {
                sendDailyAssignmentNotification(
                        user.getEmail(),
                        user.getDailyAssignedQuestionLink(),
                        user.getDailyAssignedTopicQuestionLink()
                    );
            } else {
                try {
                    dailyQuestionAssignmentServices.assignTopicBasedQuestion(user);
                    sendDailyAssignmentNotification(
                            user.getEmail(),
                            user.getDailyAssignedQuestionLink(),
                            user.getDailyAssignedTopicQuestionLink()
                    );
                } catch (Exception e) {
                    System.err.println("Error while assigning Topic questions and sending Notification:" + e);
                }
            }
        }
    }
}
