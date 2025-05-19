package Utkarsh.net.LeetCodeRevs.Services;

import Utkarsh.net.LeetCodeRevs.Entity.User;
import Utkarsh.net.LeetCodeRevs.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class NotificationServices {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private UserRepository userRepository;

    public void sendDailyAssignmentNotification(String email, String questionTitle, String questionTopicTitle) {
        String msg = "Your daily question is: " + questionTitle + " & " + questionTopicTitle;
        messagingTemplate.convertAndSend("/topic/" + email, msg);
    }

    public void sendReminderNotification(String email) {
        String msg = "You haven't solved today's question!";
        messagingTemplate.convertAndSend("/topic/" + email, msg);
    }

    @Scheduled(cron = "0 0 20 * * ?")
    public void remindUsersToSolveDailyQuestion() { // add feature to not send messages to the user who already solved today's question
        System.out.println("Reminder to solving question given");
        for (User user : userRepository.findAll()) {
            if(user.getDailyAssignedQuestionLink() != null)
                sendReminderNotification(user.getEmail());
        }
    }

    @Scheduled(cron = "0 0 8 * * ?")
    public void questionAssignmentNotification() { // add feature to not to sned messages to the user who already solved today's question
        System.out.println("Reminder to assignment of question given");
        for (User user : userRepository.findAll()) {
            if(user.getDailyAssignedTopicQuestionLink() != null || user.getDailyAssignedQuestionLink() != null) {
                sendDailyAssignmentNotification(
                        user.getEmail(),
                        user.getDailyAssignedQuestionLink(),
                        user.getDailyAssignedTopicQuestionLink()
                    );
            }
        }
    }
}
