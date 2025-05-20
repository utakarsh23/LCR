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

    public void sendDailyAssignmentNotification(String email, String questionTitle, String questionTopicTitle) {
        System.out.println(email);
        String msg = "Your daily question is: " + questionTitle + " & " + questionTopicTitle;
        messagingTemplate.convertAndSend("/topic/" + email, msg);
    }

    public void sendReminderNotification(String email) {
        System.out.println(email);
        String msg = "You haven't solved today's question!";
        messagingTemplate.convertAndSend("/topic/" + email, msg);
    }

    @Scheduled(cron = "00 010,07,08,09,10 01 * * ?")
    @Async
    public void remindUsersToSolveDailyQuestion() { // add feature to not send messages to the user who already solved today's question
        System.out.println("Reminder to solving question given");
        for (User user : userRepository.findAll()) {
            if(user.getDailyAssignedQuestionLink() != null)
                sendReminderNotification(user.getEmail());
        }
    }

    @Scheduled(cron = "11 010,07,08,09,10 01 * * ?")
    @Async
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
