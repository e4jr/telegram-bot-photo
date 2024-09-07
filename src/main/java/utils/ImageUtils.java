package utils;

import commands.AppBotCommand;
import commands.BotCommonCommands;
import functions.FilterOperations;
import functions.ImageOperation;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;

public class ImageUtils {

 static public BufferedImage getImage(String path) throws IOException {
        final File file =  new File(path);
       return ImageIO.read(file);
    }

  static public void saveImage (BufferedImage image, String path) throws IOException {
        ImageIO.write(image,"png", new File(path));
    }

    static  float[] RGBIntToArray(int pixel){
        Color color = new Color(pixel);
        return color.getRGBColorComponents(null);
    }


    static int arrayToIntRGB(float[] pixel) throws Exception {
        Color color =  null;
        if (pixel.length == 3){
            color = new Color(pixel[0], pixel[1], pixel[2]);
        }else if (pixel.length == 4){
            color = new Color(pixel[0], pixel[1], pixel[2], pixel[3]);
        }

        if (color!=null) {
            return color.getRGB();
        }
        throw new Exception("invalide color");
    }

   public static ImageOperation getOperation(String operationName){
        FilterOperations filterOperations = new FilterOperations();
        Method[] methods = BotCommonCommands.class.getDeclaredMethods();
        for (Method method : methods){
            if (method.isAnnotationPresent(AppBotCommand.class)){
                AppBotCommand command = method.getAnnotation(AppBotCommand.class);
                if (command.name().equals(operationName)){
                    return (f)-> (float[]) method.invoke(filterOperations, f);

                    }
                }
            }
            return null;
        }


}
