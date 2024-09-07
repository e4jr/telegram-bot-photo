package commands;

public class BotCommonCommands {

    @AppBotCommand(name = "/hello", description = "when request hello", showInHelp = true)
    String hello(){
        return "Hello, user";
    }


    @AppBotCommand(name = "/bye", description = "when request bye", showInHelp = true)
    String bye(){
        return "Good bye, user";
    }

    @AppBotCommand(name = "/help", description = "when request help", showInKeyboard = true)
    String help(){
        return "Bot commands \n greyScale - grey filter \n onlyRed - red filter \n onlyGreen - green filter \n onlyBlue - blue filter \n sepia - bot make sepia on image \n How use \n just write needed filter on chat ";
    }

}
