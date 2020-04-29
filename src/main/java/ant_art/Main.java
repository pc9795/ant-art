package ant_art;

import ant_art.entities.AntArea;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created By: Prashant Chaubey
 * Created On: 17-04-2020 15:07
 * Purpose: TODO:
 **/
public class Main {
    public static void main(String[] args) throws IOException {
        Color backgroundColor = Color.black;
        //Selecting a pallete
        File palletesDir = new File("palletes");
        File palletes[] = palletesDir.listFiles();
        File selectedPallete = palletes[new Random().nextInt(palletes.length)];
        System.out.println("Selected pallete:" + selectedPallete.getName());
        InputStream inputStream = new FileInputStream(selectedPallete);
        //Training markov chain
        MarkovChain chain = new MarkovChain(1, Collections.singletonList(backgroundColor));
        chain.train(ImageIO.read(inputStream));
        System.out.println("Chain created...");
        //Selecting a target color.
        BufferedImage image = ImageIO.read(new File("frames/2.jpg"));
        int maxImageSize = 520;
        image = ImageUtils.rescaleToLimit(image, maxImageSize, 3);
        if (image == null) {
            System.out.println("Image size can't be processed.");
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
