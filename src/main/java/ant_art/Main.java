package ant_art;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created By: Prashant Chaubey
 * Created On: 17-04-2020 15:07
 * Purpose: TODO:
 **/
public class Main {
    public static void main(String[] args) throws IOException {
        MarkovChain chain = new MarkovChain(1);
        InputStream inputStream = new FileInputStream(new File("sample.jpg"));
        chain.train(ImageIO.read(inputStream));
        System.out.println("Chain created...");
        //AntArea antArea = new AntArea(512, 512, chain);
        BufferedImage image = ImageIO.read(new File("frames/1.jpg"));
        int maxImageSize = 520;
        if (image.getHeight() > maxImageSize || image.getWidth() > maxImageSize) {
            System.out.println("Image dimensions(" + image.getWidth() + " X " + image.getHeight() + ") are more than maximum allowed size");
            return;
        }
        Map<Color, Float> colorProfile = ImageUtils.colorProfile(image);
        List<Color> colors = new ArrayList<>(colorProfile.keySet());
        for (Color color : colors) {
            //remove values more than 50% and less than 5%;
            if (colorProfile.get(color) >= 0.5 || colorProfile.get(color) < 0.05) {
                colorProfile.remove(color);
            }
        }
        Color backgroundColor = Color.black;
        System.out.println("Color profile size:" + colorProfile.size());
        colorProfile = colorProfile.entrySet().stream().sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue())).
                collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
        colorProfile.forEach((color, aFloat) -> System.out.println("Color:" + color + ", Percentage:" + aFloat));
        colors = new ArrayList<>(colorProfile.keySet());
        Color targetColor = null;
        for (Color color : colors) {
            if (ImageUtils.isSimilar(backgroundColor, color)) {
                continue;
            }
            targetColor = color;
            break;
        }
        if (targetColor == null) {
            System.out.println("Not able to extract target color");
            return;
        }
        System.out.println("Target color:" + targetColor);
        AntArea antArea = new AntArea(chain, image, targetColor, backgroundColor);
        Renderer renderer = new Renderer(antArea, "Ant Simulator", 60, 30, 5);
        renderer.execute();
    }
}
