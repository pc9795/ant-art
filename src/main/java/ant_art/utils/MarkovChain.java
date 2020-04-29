package ant_art.utils;

import javafx.util.Pair;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

/**
 * Created By: Prashant Chaubey
 * Created On: 18-04-2020 16:03
 * Purpose: Implementation of simple first order Markov chain which can learn colors from an input file.
 **/
public class MarkovChain {
    private Map<Color, Map<Color, Integer>> colorCounts = new HashMap<>();
    //This will be used in weighted selection of colors.
    private Map<Color, Pair<Integer, NavigableMap<Integer, Color>>> selectorMap = new HashMap<>();
    private Random random = new Random();
    private List<Color> excludedColors;

    public MarkovChain(List<Color> excludedColors) {
        this.excludedColors = excludedColors;
    }

    /**
     * Train the chain on the image
     * It is expected that this image is RGB else the behavior is unexpected. There is currently no functionality to
     * check the same.
     *
     * @param image input image
     */
    public void train(BufferedImage image) {
        //Before training we erase previous data.
        colorCounts = new HashMap<>();
        selectorMap = new HashMap<>();

        int width = image.getWidth();
        int height = image.getHeight();

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                Color color = new Color(image.getRGB(x, y));
                if (isExcluded(color)) {
                    continue;
                }
                if (!colorCounts.containsKey(color)) {
                    colorCounts.put(color, new HashMap<>());
                }
                Map<Color, Integer> neighbors = colorCounts.get(color);
                //Adding neighbors
                for (int i = x - 1; i <= x + 1; i++) {
                    for (int j = y - 1; j <= y + 1; j++) {
                        if (i < 0 || i >= width || j < 0 || j >= height) {
                            continue;
                        }
                        Color neighborColor = new Color(image.getRGB(i, j));
                        if (isExcluded(neighborColor)) {
                            continue;
                        }
                        neighbors.put(neighborColor, neighbors.getOrDefault(neighborColor, 0) + 1);
                    }
                }
            }
        }
    }

    /**
     * Check whether a color is excluded
     *
     * @param color input color
     * @return true if excluded
     */
    private boolean isExcluded(Color color) {
        for (Color excludedColor : excludedColors) {
            if (ImageUtils.isSimilar(excludedColor, color)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Return a random color from the chain. Remember to train first before calling this.
     * todo check what will happen if called before training
     *
     * @return color
     */
    public Color getRandomColor() {
        List<Color> colors = new ArrayList<>(colorCounts.keySet());
        return colors.get(random.nextInt(colors.size()));
    }

    /**
     * Get a neighboring color from the chain for an input color
     * todo check what will happen if called before training
     *
     * @param color input color
     * @return neighboring color from the chain
     */
    public Color getRandomNeighboringColor(Color color) {
        Pair<Integer, NavigableMap<Integer, Color>> selector = getSelector(color);
        if (selector == null) {
            return null;
        }
        return selector.getValue().ceilingEntry(random.nextInt(selector.getKey() + 1)).getValue();
    }

    /**
     * Create a navigable map for weighted selection for neighbors
     *
     * @param color input color whose selector is to be made
     * @return pair of total weight, navigable map containing neighbors and their weights
     */
    private Pair<Integer, NavigableMap<Integer, Color>> getSelector(Color color) {
        // Return cached data.
        if (selectorMap.containsKey(color)) {
            return selectorMap.get(color);
        }
        //Check color is present
        Map<Color, Integer> colors = colorCounts.get(color);
        if (colors == null || colors.isEmpty()) {
            return null;
        }
        //Create a navigable map for weighted selection for neighbors
        NavigableMap<Integer, Color> selector = new TreeMap<>();
        int totalWeight = 0;
        for (Color neighborColor : colors.keySet()) {
            totalWeight += colors.get(neighborColor);
            selector.put(totalWeight, neighborColor);
        }
        //Cache for future use.
        selectorMap.put(color, new Pair<>(totalWeight, selector));
        return selectorMap.get(color);
    }

}
