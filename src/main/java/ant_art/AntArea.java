package ant_art;

import javafx.util.Pair;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

/**
 * Created By: Prashant Chaubey
 * Created On: 17-04-2020 15:28
 * Purpose: TODO:
 **/
public class AntArea {
    public enum CellType {
        NEST, DEFAULT, FOOD
    }

    public class Cell {
        private CellType type;
        private float maxHomePheremone = 50;
        private float maxFoodPheremone = 50;
        private float homePheremone;
        private float foodPheremone;
        private final AntArea parent;
        private final Pair<Integer, Integer> location;
        private boolean antPresent;
        private int size;
        private int food;
        private Color color;

        public Cell(AntArea parent, Pair<Integer, Integer> location, int size, boolean typeIdentification) {
            this.parent = parent;
            this.type = CellType.DEFAULT;
            this.location = location;
            this.size = size;
            this.food = defaultFoood;
            if (typeIdentification) {
                identifyType();
            }
        }

        private void identifyType() {
            if (isType(CellType.FOOD)) {
                type = CellType.FOOD;
                repaint(cellTypeColorMap.get(type));
            }
        }

        private boolean isType(CellType type) {
            int imageX = location.getKey() * size;
            int imageY = location.getValue() * size;
            Color color = cellTypeColorMap.get(type);
            int count = 0;
            for (int i = imageX; i < imageX + size; i++) {
                for (int j = imageY; j < imageY + size; j++) {
                    Color pixelColor = new Color(parent.mapImage.getRGB(i, j));
                    count += pixelColor.equals(color) ? 1 : 0;
                }
            }
            return (float) count / (size * size) > 0.5;
        }

        private void setType(CellType type) {
            this.type = type;
            repaint(cellTypeColorMap.get(type));
        }

        public Pair<Integer, Integer> getLocation() {
            return location;
        }

        public CellType getType() {
            return type;
        }

        public float getHomePheremone() {
            return homePheremone;
        }

        public float getFoodPheremone() {
            return foodPheremone;
        }

        public int getFood() {
            return food;
        }

        public void pickUpFood() {
            if (type != CellType.FOOD) {
                throw new RuntimeException("Invalid operation: Not a food source");
            }
            food--;
            //All food is gone.
            if (food == 0) {
                type = CellType.DEFAULT;
                repaint(cellTypeColorMap.get(type));
            }
        }

        public void depositFood() {
            if (type != CellType.NEST) {
                throw new RuntimeException("Invalid operation: Not a Ant nest");
            }
            food++;
        }

        public void move(Ant ant) {
            if (antPresent) {
                throw new RuntimeException("Invalid operation: Ant already present");
            }
            if (ant.collectedFood()) {
                foodPheremone = Math.min(maxFoodPheremone, foodPheremone + pheremoneGain);
            } else {
                homePheremone = Math.min(maxHomePheremone, homePheremone + pheremoneGain);
            }
            repaint(ant.getColor());
            antPresent = true;
        }

        public void repaint(Color color) {
            int imageX = location.getKey() * size;
            int imageY = location.getValue() * size;

            for (int i = imageX; i < imageX + size; i++) {
                for (int j = imageY; j < imageY + size; j++) {
                    parent.getMapImage().setRGB(i, j, color.getRGB());
                }
            }
        }

        public boolean canMove() {
            return !antPresent;
        }

        private void repaintAccordingToPheremoneIntensity() {
            if (type != CellType.DEFAULT || color == null) {
                return;
            }
            float intensity = (foodPheremone + homePheremone) / (maxFoodPheremone + maxHomePheremone) * 20;
            intensity = 1;
            intensity = Math.min(intensity, 1f);
            int red = (int) (color.getRed() * intensity);
            int green = (int) (color.getGreen() * intensity);
            int blue = (int) (color.getBlue() * intensity);
            repaint(new Color(red, green, blue));
        }

        public void leave(Ant ant) {
            //We are not checking invalid operation here because there may be the case that an ant spawned in this cell
            //and couldn't move because an ant was already here. And we are calling leave before moving.
            if (type == CellType.DEFAULT) {
                color = getRandomColor();
                repaintAccordingToPheremoneIntensity();
            } else {
                repaint(cellTypeColorMap.get(type));
            }
            antPresent = false;
        }
    }

    private final BufferedImage mapImage;
    private final Cell[][] map;
    private final int width;
    private final int height;
    private final List<Ant> ants;
    private int currAnts = 0;
    private int nestSize = 4;
    private int foodSize = 1;
    private List<Pair<Integer, Integer>> nestLocations = new ArrayList<>();
    private int maxAnts = 5;
    private int countFood = 20;
    private int countNests = 3;
    private int defaultFoood = 1;
    private float decayRate = 0.01f;
    private float pheremoneThreshold = 0.01f;
    private float pheremoneGain = 1;
    private int antFoodCapacity = 100;
    private List<Pair<Integer, Integer>> foodLocations = new ArrayList<>();
    private int cellSize = 4;
    private Color antColor = Color.blue;
    @SuppressWarnings("unchecked")
    private Pair<Integer, Integer>[] directions = new Pair[]{AntDirections.SOUTH_WEST, AntDirections.SOUTH,
            AntDirections.SOUTH_EAST, AntDirections.NORTH_EAST, AntDirections.NORTH_WEST, AntDirections.NORTH,
            AntDirections.EAST, AntDirections.WEST};
    private final Random random = new Random();
    private static Map<CellType, Color> cellTypeColorMap = new HashMap<>();
    private MarkovChain mkvChain;
    private Color color;

    static {
        cellTypeColorMap.put(CellType.DEFAULT, Color.black);
        cellTypeColorMap.put(CellType.NEST, Color.green);
        cellTypeColorMap.put(CellType.FOOD, Color.red);
    }

    public AntArea(int width, int height, MarkovChain mkvChain, BufferedImage frame) {
        this.mapImage = frame;
        this.width = width;
        this.height = height;
        this.map = new Cell[width][height];
        for (int i = 0; i < this.width / cellSize; i++) {
            for (int j = 0; j < this.height / cellSize; j++) {
                this.map[i][j] = new Cell(this, new Pair<>(i, j), cellSize, true);
            }
        }
        ants = new ArrayList<>();
        for (int i = 0; i < countNests; i++) {
            int x = random.nextInt(width / cellSize);
            int y = random.nextInt(height / cellSize);
            for (int l = x; l < x + nestSize && l < width / cellSize; l++) {
                for (int k = y; k < y + nestSize && k < height / cellSize; k++) {
                    map[l][k].setType(CellType.NEST);
                    nestLocations.add(new Pair<>(l, k));
                }
            }
        }
        this.mkvChain = mkvChain;
    }

    public AntArea(int width, int height, MarkovChain mkvChain) {
        this.mapImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        this.width = width;
        this.height = height;
        this.map = new Cell[width][height];
        for (int i = 0; i < this.width / cellSize; i++) {
            for (int j = 0; j < this.height / cellSize; j++) {
                this.map[i][j] = new Cell(this, new Pair<>(i, j), cellSize, false);
            }
        }
        ants = new ArrayList<>();
        for (int i = 0; i < countNests; i++) {
            int x = random.nextInt(width / cellSize);
            int y = random.nextInt(height / cellSize);
            for (int l = x; l < x + nestSize && l < width / cellSize; l++) {
                for (int k = y; k < y + nestSize && k < height / cellSize; k++) {
                    map[l][k].setType(CellType.NEST);
                    nestLocations.add(new Pair<>(l, k));
                }
            }
        }

        for (int i = 0; i < countFood; i++) {
            int x = random.nextInt(width / cellSize);
            int y = random.nextInt(height / cellSize);
            for (int l = x; l < x + foodSize && l < width / cellSize; l++) {
                for (int k = y; k < y + foodSize && k < height / cellSize; k++) {
                    map[k][l].setType(CellType.FOOD);
                    foodLocations.add(new Pair<>(k, l));
                }
            }
        }
        this.mkvChain = mkvChain;
    }

    public BufferedImage getMapImage() {
        return mapImage;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getAreaWidth() {
        return width / cellSize;
    }

    public int getAreaHeight() {
        return height / cellSize;
    }

    public Cell[][] getMap() {
        return map;
    }

    public Color getRandomColor() {
        if (color == null) {
            color = mkvChain.getRandomColor();
            return color;
        }
        color = mkvChain.getRandomNeighboringColor(color);
        return color;
    }

    public void spawnAnt(Pair<Integer, Integer> directionVector, Pair<Integer, Integer> location) {
        ants.add(new Ant(this, directionVector, location, antColor, antFoodCapacity));
        currAnts++;
    }

    public void update() {
        for (int i = 0; i < getAreaWidth(); i++) {
            for (int j = 0; j < getAreaHeight(); j++) {
                Cell cell = map[i][j];
                if (cell.getType() != CellType.DEFAULT) {
                    continue;
                }
                cell.foodPheremone = cell.foodPheremone * (1 - decayRate);
                cell.homePheremone = cell.foodPheremone * (1 - decayRate);
                if (cell.foodPheremone + cell.homePheremone < pheremoneThreshold) {
                    cell.repaint(cellTypeColorMap.get(CellType.DEFAULT));
                    continue;
                }
                cell.repaintAccordingToPheremoneIntensity();
            }
        }
        if (currAnts < maxAnts) {
            int randIndex = random.nextInt(nestLocations.size());
            int x = nestLocations.get(randIndex).getKey();
            int y = nestLocations.get(randIndex).getValue();
            if (map[x][y].canMove()) {
                spawnAnt(directions[random.nextInt(directions.length)], new Pair<>(x, y));
            }
        }
        for (Ant ant : ants) {
            ant.update();
        }
    }
}
