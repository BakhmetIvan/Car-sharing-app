package mate.capsharingapp.service.impl;

import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import mate.capsharingapp.model.Role;
import mate.capsharingapp.model.User;
import mate.capsharingapp.repository.UserRepository;
import mate.capsharingapp.security.AuthenticationService;
import mate.capsharingapp.service.NotificationService;
import mate.capsharingapp.service.TelegramCommand;
import mate.capsharingapp.service.TelegramCommandHandlerStrategy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Service
@Setter
@Getter
@RequiredArgsConstructor
public class TelegramNotificationServiceImpl extends TelegramLongPollingBot
        implements NotificationService {
    private final UserRepository userRepository;
    private final AuthenticationService authenticationService;
    private final TelegramCommandHandlerStrategy telegramCommandHandler;
    @Value("${telegram.bot.username}")
    private String botUsername;
    @Value("${telegram.bot.token}")
    private String botToken;

    @Override
    public void onUpdateReceived(Update update) {
        String messageText = update.getMessage().getText();
        Long chatId = update.getMessage().getChatId();
        TelegramCommand handledCommand = telegramCommandHandler.handleCommand(messageText);
        String response = handledCommand.execute(messageText, chatId);
        sendNotification(chatId, response);
    }

    @Override
    public void sendNotificationToAdmins(String message) {
        List<User> admins = userRepository.findAllByRolesName(Role.RoleName.ROLE_MANAGER);
        for (User user : admins) {
            if (user.getTgChatId() != null) {
                sendNotification(user.getTgChatId(), message);
            }
        }
    }

    public void sendNotification(Long chatId, String message) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(message);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
