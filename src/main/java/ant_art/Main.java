package ant_art;

import ant_art.config.Configuration;
import ant_art.entities.AntArea;
import ant_art.exceptions.AntArtException;
import ant_art.gui.Renderer;
import ant_art.utils.ImageUtils;
import ant_art.utils.MarkovChain;

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
 * Purpose: Entry point of the application
 **/
public class Main {

    /**
     * Get a random file from the directory
     *
     * @param dir input directory
     * @return random file from the input directory
     * @throws AntArtException if can't retrieve random file from the input directory. No need to handle nulls just
     *                         throwing an exception.
     */
    private static File getRandomFile(String dir) throws AntArtException {
        File dirObj = new File(dir);
        if (!dirObj.isDirectory()) {
            throw new AntArtException(String.format("%s is not a directory", dir));
        }
        File[] files = dirObj.listFiles();
        if (files == null || files.length == 0) {
            throw new AntArtException(String.format("%s is empty", dir));
        }
        return files[new Random().nextInt(files.length)];
    }

    public static void main(String[] args) throws IOException, AntArtException {
        int targetColorCount = Configuration.DEFAULT_TARGET_COLOR_COUNT;
        String palletDir = Configuration.Directories.PALLETS;
        String inputDir = Configuration.Directories.INPUT;

        //Try to extract configuration arguments from command line arguments.
        if (args.length > 2) {
            targetColorCount = Integer.parseInt(args[1].trim());
            palletDir = args[2].trim();
            inputDir = args[3].trim();
        }

        //Select an input image
        File inputFile = getRandomFile(inputDir);
        System.out.println(String.format("Working on file:%s\n", inputFile.getName()));
        BufferedImage image = ImageIO.read(inputFile);
        image = ImageUtils.rescaleToLimit(image, Configuration.MAXIMUM_IMAGE_SIZE, Configuration.MAXIMUM_RESCALING_DEPTH);
        //If not able to rescale the image to a limit.
        if (image == null) {
            System.out.println("Image size can't be processed.");
            System.exit(1);
        }

        //Generate color profile from the image
        Map<Color, Float> colorProfile = ImageUtils.colorProfile(image);
        List<Color> colors = new ArrayList<>(colorProfile.keySet());
        for (Color color : colors) {
            //remove colors which are not withing an configured threshold
            if (colorProfile.get(color) >= Configuration.COLOR_FILTERING_HIGHER_LIMIT ||
                    colorProfile.get(color) < Configuration.COLOR_FILTERING_LOWER_LIMIT) {
                colorProfile.remove(color);
            }
        }
        System.out.println("Color profile size after filtering:" + colorProfile.size());

        //Sort map according to decreasing ratio of colors
        colorProfile = colorProfile.entrySet().stream().sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue())).
                collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
        colorProfile.forEach((color, aFloat) -> System.out.println("Color:" + color + ", Percentage:" + aFloat));
        System.out.println();

        colors = new ArrayList<>(colorProfile.keySet());
        List<Color> targetColors = new ArrayList<>();
        for (Color color : colors) {
            //Similar to background color
            if (ImageUtils.isSimilar(Configuration.Colors.DEFAULT, color)) {
                continue;
            }
            targetColors.add(color);
        }
        System.out.println(String.format("Colors found after background removal:%s\n", targetColors.size()));

        //Checking if any color is found or not.
        if (targetColors.size() == 0) {
            System.out.println(String.format("Not able to extract any color from the input image:%s", inputFile.getName()));
            System.exit(1);
        }

        //If found number of colors are less than target color count
        if (targetColors.size() < targetColorCount) {
            System.out.println(String.format("Number of colors found for processing:%s are less than the target colors:" +
                    "%s! Proceeding with found number of colors", colorProfile.size(), targetColorCount));
            targetColorCount = colorProfile.size();
        }
        targetColors = targetColors.subList(0, targetColorCount);

        System.out.println(String.format("Target colors found:%s\n", targetColors));

        //Select pallets to replace target colors
        MarkovChain[] chains = new MarkovChain[targetColorCount];
        for (int i = 0; i < targetColorCount; i++) {
            //Selecting a pallet
            File pallet = getRandomFile(palletDir);
            System.out.println("Selected pallet:" + pallet.getName());
            InputStream inputStream = new FileInputStream(pallet);

            //Training markov chains
            chains[i] = new MarkovChain(Collections.singletonList(Configuration.Colors.DEFAULT));
            chains[i].train(ImageIO.read(inputStream));
            System.out.println(String.format("Chain created for pallet: %s", pallet.getName()));
        }
        System.out.println();

        //Create ant area
        AntArea antArea = new AntArea(chains, image, targetColors, Configuration.Colors.DEFAULT);
        //Create the GUI
        System.out.println("Starting GUI...");
        Renderer renderer = new Renderer(antArea, "Ant Simulator", Configuration.GUI.FPS, Configuration.GUI.DURATION,
                Configuration.GUI.SAMPLE_INTERVAL, Configuration.Directories.OUTPUT);
        //Run the GUI
        renderer.execute();
    }
}
