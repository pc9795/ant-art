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
        private float maxHomePheremone = 51;
        private float maxFoodPheremone = 51;
        private float homePheremone;
        private float foodPheremone;
        private final AntArea parent;
        private final Pair<Integer, Integer> location;
        private boolean antPresent;
        private int size;
        private int food = 5;

        public Cell(AntArea parent, Pair<Integer, Integer> location, int size) {
            this.parent = parent;
            this.type = CellType.DEFAULT;
            this.location = location;
            this.size = size;
            repaint(cellTypeColorMap.get(this.type));
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
            if (ant.hasFood()) {
                foodPheremone = Math.min(maxFoodPheremone, foodPheremone + 1);
            } else {
                homePheremone = Math.min(maxHomePheremone, homePheremone + 1);
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

        public void leave(Ant ant) {
            //We are not checking invalid operation here because there may be the case that an ant spawned in this cell
            //and couldn't move because an ant was already here. And we are calling leave before moving.
            if (type == CellType.DEFAULT) {
                repaint(new Color((int) foodPheremone * 5, (int) homePheremone * 5, 0));
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
    private int maxAnts = 50;
    private int currAnts = 0;
    private Color antColor = Color.blue;
    private int nestSize = 4;
    private int countNests = 3;
    private List<Pair<Integer, Integer>> nestLocations = new ArrayList<>();
    private int foodSize = 4;
    private int countFood = 20;
    private List<Pair<Integer, Integer>> foodLocations = new ArrayList<>();
    private int cellSize = 4;
    @SuppressWarnings("unchecked")
    private Pair<Integer, Integer>[] directions = new Pair[]{AntDirections.SOUTH_WEST, AntDirections.SOUTH,
            AntDirections.SOUTH_EAST, AntDirections.NORTH_EAST, AntDirections.NORTH_WEST, AntDirections.NORTH,
            AntDirections.EAST, AntDirections.WEST};
    private final Random random = new Random();
    private static Map<CellType, Color> cellTypeColorMap = new HashMap<>();

    static {
        cellTypeColorMap.put(CellType.DEFAULT, Color.black);
        cellTypeColorMap.put(CellType.NEST, Color.green);
        cellTypeColorMap.put(CellType.FOOD, Color.red);
    }

    public AntArea(int width, int height) {
        this.mapImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        this.width = width;
        this.height = height;
        this.map = new Cell[width][height];
        for (int i = 0; i < this.width / cellSize; i++) {
            for (int j = 0; j < this.height / cellSize; j++) {
                this.map[i][j] = new Cell(this, new Pair<>(i, j), cellSize);
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

    public void spawnAnt(Pair<Integer, Integer> directionVector, Pair<Integer, Integer> location) {
        ants.add(new Ant(this, directionVector, location, antColor));
        currAnts++;
    }

    public void update() {
        if (currAnts <= maxAnts) {
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
