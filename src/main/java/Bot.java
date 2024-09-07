import commands.AppBotCommand;
import commands.BotCommonCommands;
import functions.FilterOperations;
import functions.ImageOperation;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import utils.ImageUtils;
import utils.PhotoMessageUtils;
import utils.RGBMaster;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Bot extends TelegramLongPollingBot {

  HashMap<String, Message> messages = new HashMap<>();


  private SendMessage runPhotoMessage(Message message){
      List<File> files = getFilesByMessage(message);
      if (files.isEmpty()){
          return  null;
      }
      String chatId = message.getChatId().toString();
      messages.put(message.getChatId().toString(), message);
      ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
      ArrayList<KeyboardRow> allKeyboardRows = new ArrayList<>(getKeyboardRows(FilterOperations.class));
      replyKeyboardMarkup.setKeyboard(allKeyboardRows);
      replyKeyboardMarkup.setOneTimeKeyboard(true);
      SendMessage sendMessage =  new SendMessage();
      sendMessage.setReplyMarkup(replyKeyboardMarkup);
      sendMessage.setChatId(chatId);
      sendMessage.setText("Select filter");
      return sendMessage;
  }


    @Override
    public void onUpdateReceived(Update update) {
        Message message = update.getMessage();
        try {
            SendMessage responseTextMessage = runCommonCommand(message);
            if (responseTextMessage != null){
                execute(responseTextMessage);
                return;
            }

            responseTextMessage = runPhotoMessage(message);
            if (responseTextMessage != null){
                execute(responseTextMessage);
                return;
            }


            Object responseMediaMessage = runPhotoFilter(message);
            if (responseMediaMessage != null){
                if (responseMediaMessage instanceof SendMediaGroup) {
                    execute((SendMediaGroup) responseMediaMessage);
                } else if (responseMediaMessage instanceof SendMessage) {
                    execute((SendMessage) responseMediaMessage);
                }
                return;
            }

        } catch (InvocationTargetException | IllegalAccessException | TelegramApiException e) {
            throw new RuntimeException(e);
        }




//       String response = message.getFrom().getId().toString();
//        System.out.println(message.getText());
//        SendMessage sendMessage = new SendMessage();
//        sendMessage.setChatId(message.getChatId().toString());
//        sendMessage.setText("Your message: " + response);
//            execute(sendMessage);
    }

    private SendMessage runCommonCommand(Message message) throws InvocationTargetException, IllegalAccessException {
        String text = message.getText();
        BotCommonCommands commands = new BotCommonCommands();
        Method[] methods = BotCommonCommands.class.getDeclaredMethods();
        for (Method method : methods){
            if (method.isAnnotationPresent(AppBotCommand.class)){
              AppBotCommand command = method.getAnnotation(AppBotCommand.class);
              if (command.name().equals(text)){
                  method.setAccessible(true);
                  String responseText = (String) method.invoke(commands);
                 if (responseText != null){
                     SendMessage sendMessage =  new SendMessage();
                     sendMessage.setChatId(message.getChatId().toString());
                     sendMessage.setText(responseText);
                     return sendMessage;
                 }
              }
            }
        }
        return null;
    }




    private Object runPhotoFilter(Message message) {
        final String text = message.getText();
        ImageOperation operation = ImageUtils.getOperation(text);
        if (operation == null) return null;
        String chatId = message.getChatId().toString();
        Message photoMessage = messages.get(chatId);
        if (photoMessage != null) {
            List<File> files = getFilesByMessage(photoMessage);
            try {
                List<String> paths = PhotoMessageUtils.savePhotos(files, getBotToken());
                return preparePhotoMessage(paths, operation, chatId);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }else {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(chatId);
            sendMessage.setText("Enter photo!");
            return sendMessage;
        }
    }


    @Override
    public String getBotToken() {
        return "7382057432:AAHpH3OcfoYDRhIBtgZ16J_ga02-w-w4YBA";
    }

    @Override
    public String getBotUsername() {
        return "Java132983219_bot";
    }

    private List<org.telegram.telegrambots.meta.api.objects.File> getFilesByMessage(Message message){
       List<PhotoSize>  photoSizes =  message.getPhoto();
       if (photoSizes == null) return  new ArrayList<>();
        ArrayList<File> files = new ArrayList<>();
       for (PhotoSize photoSize : photoSizes){
           final String fileId = photoSize.getFileId();
           try {
               files.add(sendApiMethod(new GetFile(fileId)));
           } catch (TelegramApiException e) {
               throw new RuntimeException(e);
           }
       }
    return  files;
    }

    private SendMediaGroup preparePhotoMessage(List<String> localPaths, ImageOperation operation, String chatId) throws Exception {
        SendMediaGroup mediaGroup =  new SendMediaGroup();
        ArrayList<InputMedia> medias =  new ArrayList<>();
        for (String path : localPaths){
            InputMedia inputMedia = new InputMediaPhoto();
                PhotoMessageUtils.processingImage(path, operation);
                inputMedia.setMedia(new java.io.File(path), "path");
                medias.add(inputMedia);

        }

        mediaGroup.setMedias(medias);
        mediaGroup.setChatId(chatId);

        return mediaGroup;
    }


    private ReplyKeyboardMarkup getKeyboard(java.lang.Class someCla){
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        ArrayList<KeyboardRow> allKeyboardRows =  new ArrayList<>();
        allKeyboardRows.addAll(getKeyboardRows(BotCommonCommands.class));
        allKeyboardRows.addAll(getKeyboardRows(FilterOperations.class));


        replyKeyboardMarkup.setKeyboard(allKeyboardRows);
        replyKeyboardMarkup.setOneTimeKeyboard(true);
        return  replyKeyboardMarkup;

    }

    private ArrayList<KeyboardRow> getKeyboardRows(java.lang.Class someClass){
        Method[] methods =  someClass.getDeclaredMethods();
        ArrayList<AppBotCommand> commands = new ArrayList<>();
        for (Method method : methods){
            if (method.isAnnotationPresent(AppBotCommand.class)){
                commands.add(method.getAnnotation(AppBotCommand.class));
            }
        }


        ArrayList<KeyboardRow> keyboardRows =  new ArrayList<>();
        int columnCount = 3;
        int rowsCount =commands.size()/columnCount + (commands.size() % columnCount == 0 ? 0 : 1);
        for (int rowIndex = 0; rowIndex < rowsCount; rowIndex++) {
            KeyboardRow row =  new KeyboardRow();
            for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
                int index = rowIndex*columnCount+columnIndex;
                if (index>= commands.size()) continue;
                AppBotCommand command = commands.get(rowIndex*columnCount+columnIndex);
                KeyboardButton keyboardButton = new KeyboardButton( command.name());
                row.add(keyboardButton);
            }
            keyboardRows.add(row);
        }

    return  keyboardRows;
    }

}
