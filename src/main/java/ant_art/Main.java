package ant_art;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created By: Prashant Chaubey
 * Created On: 17-04-2020 15:07
 * Purpose: TODO:
 **/
public class Main {
    public static void main(String[] args) throws IOException {
        MarkovChain chain = new MarkovChain(1);
        System.out.println("Chain created...");
        InputStream inputStream = new FileInputStream(new File("sample.jpg"));
        chain.train(ImageIO.read(inputStream));
        //AntArea antArea = new AntArea(512, 512, chain);
        BufferedImage image = ImageIO.read(new File("frames/human.jpg"));
        AntArea antArea = new AntArea(512, 512, chain, image);
        Renderer renderer = new Renderer(antArea, "Ant Simulator", 600, 600, 100, 30, 5);
        renderer.execute();
    }
}
