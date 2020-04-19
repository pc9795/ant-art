package ant_art;

import javafx.util.Pair;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

/**
 * Created By: Prashant Chaubey
 * Created On: 18-04-2020 16:03
 * Purpose: TODO:
 **/
public class MarkovChain {
    private int bucketSize = 1;
    private int order = 1;
    private Map<Color, Map<Color, Integer>> colorCounts = new HashMap<>();
    //This will be used in weighted selection of colors.
    private Map<Color, Pair<Integer, NavigableMap<Integer, Color>>> selectorMap = new HashMap<>();
    private Random random = new Random();

    public MarkovChain(int bucketSize) {
        this.bucketSize = bucketSize;
    }

    //It is expected that this image is RGB else the behavior is unexpected. There is currently no functionality to
    //check the same.
    public void train(BufferedImage image) {
        //Before training we erase previous data.
        colorCounts = new HashMap<>();
        selectorMap = new HashMap<>();

        int width = image.getWidth();
        int height = image.getHeight();

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                Color color = new Color(image.getRGB(x, y));
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
                        neighbors.put(neighborColor, neighbors.getOrDefault(neighborColor, 0) + 1);
                    }
                }
            }
        }
    }

    public Color getRandomColor() {
        List<Color> colors = new ArrayList<>(colorCounts.keySet());
        return colors.get(random.nextInt(colors.size()));
    }

    public Color getRandomNeighboringColor(Color color) {
        Pair<Integer, NavigableMap<Integer, Color>> selector = getSelector(color);
        if (selector == null) {
            return null;
        }
        return selector.getValue().ceilingEntry(random.nextInt(selector.getKey() + 1)).getValue();
    }

    private Pair<Integer, NavigableMap<Integer, Color>> getSelector(Color color) {
        if (selectorMap.containsKey(color)) {
            return selectorMap.get(color);
        }
        Map<Color, Integer> colors = colorCounts.get(color);
        if (color == null || colors.isEmpty()) {
            return null;
        }
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
