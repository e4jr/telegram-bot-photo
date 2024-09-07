package utils;

import functions.ImageOperation;

import java.awt.image.BufferedImage;

public class RGBMaster {
    BufferedImage image;
    private int height;
    private int width;
    private  boolean alphaChannel;
    private int[] pixels;

    public RGBMaster(BufferedImage image){
       this.image =  image;
       width =  image.getWidth();
       height = image.getHeight();
       alphaChannel =  image.getAlphaRaster()!=null;
       pixels = image.getRGB(0, 0, width, height, pixels, 0, width);
    }

    public  BufferedImage getImage(){
        return  image;
    }


 public void changeImage(ImageOperation operation) throws Exception {
        for (int i=0; i< pixels.length; i++){
            float[] pixel = ImageUtils.RGBIntToArray(pixels[i]);
            float[] newPixel = operation.execute(pixel);
            pixels[i] = ImageUtils.arrayToIntRGB(newPixel);
        }
     image.setRGB(0, 0, width, height, pixels, 0, width);
    }



}
