/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pitiupi.screenCast.control;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

/**
 *
 * @author flavio
 */
public class ImageTransformation {

    public static byte[] toByteArray(BufferedImage image) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", baos);
        byte[] data = baos.toByteArray();
        return data;
    }

    public static byte[] compress(BufferedImage image, float scale) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
        ImageWriter writer = writers.next();
        ImageWriteParam param = writer.getDefaultWriteParam();
        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        param.setCompressionQuality(scale);
        ImageOutputStream ios = ImageIO.createImageOutputStream(baos);
        writer.setOutput(ios);
        writer.write(null, new IIOImage(image, null, null), param);
        byte[] data = baos.toByteArray();
        writer.dispose();
        return data;
    }

    public static BufferedImage resize(BufferedImage img, int maxWidth, int maxHeight) throws IOException {
        int scaledWidth = maxWidth;
        int scaledHeight = (int) (img.getHeight() * ((double) scaledWidth / img.getWidth()));

        if (scaledHeight > maxHeight) {
            scaledHeight = maxHeight;
            scaledWidth = (int) (img.getWidth() * ((double) scaledHeight / img.getHeight()));

            if (scaledWidth > maxWidth) {
                scaledWidth = maxWidth;
                scaledHeight = maxHeight;
            }
        }

        Image resized = img.getScaledInstance(scaledWidth, scaledHeight, Image.SCALE_SMOOTH);
        BufferedImage buffered = new BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = buffered.createGraphics();
        graphics.drawImage(resized, 0, 0, null);
        graphics.dispose();
        return buffered;
    }
}
