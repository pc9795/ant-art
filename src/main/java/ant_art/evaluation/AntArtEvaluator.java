package ant_art.evaluation;

import ant_art.config.Configuration;
import ant_art.utils.ImageUtils;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Created By: Prashant Chaubey
 * Created On: 01-05-2020 03:18
 * Purpose: Evaluation algorithms for the project
 **/
public class AntArtEvaluator {

    /**
     * Check the number of pixels covered in the image
     *
     * @param image input image
     * @return the coverage ration of the image
     */
    public static float getCoverage(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        int count = 0;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                Color pixelColor = new Color(image.getRGB(x, y));
                //Skipping if background
                if (ImageUtils.isSimilar(Configuration.Colors.DEFAULT, pixelColor)) {
                    continue;
                }
                count++;
            }
        }
        return (float) count / (width * height);
    }

    /**
     * Get mosaic score for an image.
     *
     * @param image input image
     * @return mosaic score of the image.
     */
    public static float getMosaicScore(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        int count = 0;
        int mosaics = 0;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                Color pixelColor = new Color(image.getRGB(x, y));
                //Skipping if background
                if (ImageUtils.isSimilar(Configuration.Colors.DEFAULT, pixelColor)) {
                    continue;
                }
                int differentNeighbors = 0;
                for (int i = x - Configuration.MOSAIC_NEIGHBORHOOD; i <= x + Configuration.MOSAIC_NEIGHBORHOOD; i++) {
                    for (int j = y - Configuration.MOSAIC_NEIGHBORHOOD; j <= y + Configuration.MOSAIC_NEIGHBORHOOD; j++) {
                        if (i < 0 || j < 0 || i >= width || j >= height) {
                            continue;
                        }
                        Color neighborColor = new Color(image.getRGB(i, j));
                        if (!ImageUtils.isSimilar(pixelColor, neighborColor)) {
                            differentNeighbors++;
                        }
                    }
                }
                mosaics += differentNeighbors > Configuration.MOSAIC_THRESHOLD ? 1 : 0;
                count++;
            }
        }
        return (float) mosaics / count;
    }
}
