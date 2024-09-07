package utils;

import functions.FilterOperations;
import functions.ImageOperation;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import static utils.ImageUtils.saveImage;

public class PhotoMessageUtils {
    public static List<String> savePhotos(List<File> files, String botToken) throws IOException {
        Random random = new Random();
        ArrayList<String> paths = new ArrayList<>();
        for (File file : files) {
                final String imageURL = "https://api.telegram.org/file/bot" + botToken + "/" + file.getFilePath();
                final String localFileName = "images/" + new Date().getTime() +random.nextLong() + ".jpg";
                saveImage(imageURL, localFileName);
                paths.add(localFileName);
        }
        return  paths;
    }


    public static void  saveImage(String url, String fileName) throws IOException {
        URL urlModel = new URL(url);
        InputStream inputStream = urlModel.openStream();
        OutputStream outputStream = new FileOutputStream(fileName);
        byte[] b =new  byte[2048];
        int length;
        while ((length = inputStream.read(b)) != -1){
            outputStream.write(b, 0, length);
        }
        inputStream.close();
        outputStream.close();
    }

    public static void processingImage(String fileName, ImageOperation operation) throws Exception {
        final BufferedImage image = ImageUtils.getImage(fileName);
        final RGBMaster rgbMaster = new RGBMaster(image);
        rgbMaster.changeImage(operation);
        ImageUtils.saveImage(rgbMaster.getImage(),fileName);

    }
}
