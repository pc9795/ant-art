package ant_art.entities;

import ant_art.AntDirections;
import ant_art.Configuration;
import ant_art.ImageUtils;
import ant_art.MarkovChain;
import ant_art.exceptions.AntArtException;
import javafx.util.Pair;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

/**
 * Created By: Prashant Chaubey
 * Created On: 17-04-2020 15:28
 * Purpose: Area where ant moves
 **/
public class AntArea {
    //Type of food cell
    public enum CellType {
        NEST, DEFAULT, FOOD
    }

    /**
     * A cell in the area
     */
    class Cell {
        private CellType type;
        //current level of home pheromone in this cell
        private float homePheromone;
        //current level of food pheromone in this cell
        private float foodPheromone;
        //location which in the area which this cell represents
        private final Pair<Integer, Integer> location;
        //is an ant present on this cell
        private boolean antPresent;
        //size of the cell
        private int size;
        //food present on the cell
        private int food;
        //current color of this cell
        private Color color;
        //is this cell a site or not
        //todo can look for converting a new type instead of using a boolean flag
        private boolean decay = true;

        Cell(Pair<Integer, Integer> location, int size, boolean typeIdentification) {
            this.type = CellType.DEFAULT;
            this.location = location;
            this.size = size;
            this.food = Configuration.DEFAULT_FOOD_IN_CELL;
            //Try to identify the cell type from the area contents on the location
            if (typeIdentification) {
                identifyType();
            }
        }

        Pair<Integer, Integer> getLocation() {
            return location;
        }

        CellType getType() {
            return type;
        }

        float getHomePheromone() {
            return homePheromone;
        }

        float getFoodPheromone() {
            return foodPheromone;
        }

        int getFood() {
            return food;
        }

        /**
         * Try to identify the part of the area which this cell represents and update it accordingly
         */
        private void identifyType() {
            if (isType(CellType.FOOD)) {
                type = CellType.FOOD;
                repaint(cellTypeColorMap.get(type));
            }
        }

        /**
         * Check whether cell is a particular type
         *
         * @param type type of the cell to check
         * @return whether the contents of the cell represents the given type
         */
        @SuppressWarnings("SameParameterValue")
        private boolean isType(CellType type) {
            //Get the coordinates in the area
            int imageX = location.getKey() * size;
            int imageY = location.getValue() * size;
            //Get the color
            //todo update this if want to work on multiple colors
            Color color = cellTypeColorMap.get(type);

            //Check pixel by pixel that the color of the cell is similar to the type
            int count = 0;
            for (int i = imageX; i < imageX + size; i++) {
                for (int j = imageY; j < imageY + size; j++) {
                    Color pixelColor = new Color(mapImage.getRGB(i, j));
                    count += ImageUtils.isSimilar(color, pixelColor) ? 1 : 0;
                }
            }
            return (float) count / (size * size) > Configuration.TYPE_IDENTIFICATION_THRESHOLD;
        }

        /**
         * Set this cell to given type
         *
         * @param type type of the cell
         */
        private void setType(CellType type) {
            this.type = type;
            repaint(cellTypeColorMap.get(type));
        }

        /**
         * Let the food to be picked up from the cell.
         * todo return the food picked
         */
        void pickUpFood() throws AntArtException {
            if (type != CellType.FOOD) {
                throw new AntArtException("Invalid operation: Not a food source");
            }

            food -= Configuration.FOOD_PICKUP_QUANTITY;
            //All food is gone.
            if (food == 0) {
                type = CellType.DEFAULT;
                repaint(cellTypeColorMap.get(type));
                //After all food is gone make it as a site.
                //THIS IS THE MAIN PART WHICH LET THE ART STAY IN THE FRAME
                decay = false;
            }
        }

        /**
         * Deposit the food in cell
         * todo take food value as input
         */
        void depositFood() throws AntArtException {
            if (type != CellType.NEST) {
                throw new AntArtException("Invalid operation: Not a Ant nest");
            }
            food += Configuration.FOOD_PICKUP_QUANTITY;
        }

        /**
         * Move the input ant to this cell
         *
         * @param ant input ant
         */
        void move(Ant ant) throws AntArtException {
            //ant is already present. Before moving the caller should check ant is already present on this cell or not.
            if (antPresent) {
                throw new AntArtException("Invalid operation: Ant already present");
            }
            if (ant.collectedFood()) {
                //if the ant has food with it update the food pheromone
                foodPheromone = Math.min(Configuration.MAX_FOOD_PHEROMONE, foodPheromone + Configuration.PHEROMONE_GAIN);
            } else {
                //if ant is looking for food then update the home pheromone
                homePheromone = Math.min(Configuration.MAX_HOME_PHEROMONE, homePheromone + Configuration.PHEROMONE_GAIN);
            }
            //update the cell
            repaint(ant.getColor());
            antPresent = true;
        }

        /**
         * Update the color of the paint
         *
         * @param color input color
         */
        void repaint(Color color) {
            //Get the coordinates in the area
            int imageX = location.getKey() * size;
            int imageY = location.getValue() * size;

            //Paint the cell pixel by pixel in the area
            for (int i = imageX; i < imageX + size; i++) {
                for (int j = imageY; j < imageY + size; j++) {
                    mapImage.setRGB(i, j, color.getRGB());
                }
            }
        }

        /**
         * @return true if ant can move on this cell
         */
        boolean isAntPresent() {
            return antPresent;
        }

        /**
         * Repaint according to pheromone intensity
         */
        private void repaintAccordingToPheromoneIntensity() {
            //The cell obtains a color from the ant when it moves on it. We dont' change the color of a food or nest cell.
            //Checking that whether this cell is a default cell or not and whether it has got a color from ant or not.
            if (type != CellType.DEFAULT || color == null) {
                return;
            }
            //Right now the intensity is decided on food pheromone levels only
            //todo check whether to include home pheromone or not
            float intensity = (foodPheromone / Configuration.MAX_FOOD_PHEROMONE) * Configuration.INTENSITY_AMPLIFIER;
            //Clip to a value of 1.
            intensity = Math.min(intensity, 1f);
            int red = (int) (color.getRed() * intensity);
            int green = (int) (color.getGreen() * intensity);
            int blue = (int) (color.getBlue() * intensity);
            repaint(new Color(red, green, blue));
        }

        /**
         * Let an ant leave this cell
         */
        void leave() throws AntArtException {
            if (!antPresent) {
                throw new AntArtException("Can't leave as no ant is currently present here");
            }
            switch (type) {
                case DEFAULT:
                    //Get a random color when an ant leave this cell.
                    //todo check whether this method be on area or on ant.
                    color = getRandomColor();
                    repaintAccordingToPheromoneIntensity();
                    break;
                case NEST:
                case FOOD:
                    repaint(cellTypeColorMap.get(type));
                    break;
            }
            antPresent = false;
        }
    }

    //The image of this area
    private final BufferedImage mapImage;
    //Cells in the area
    private final Cell[][] map;
    private final int width;
    private final int height;
    private final List<Ant> ants = new ArrayList<>();
    private int currAnts = 0;
    private List<Pair<Integer, Integer>> nestLocations = new ArrayList<>();
    //todo decide for its use
    @SuppressWarnings({"FieldCanBeLocal", "MismatchedQueryAndUpdateOfCollection"})
    private List<Pair<Integer, Integer>> foodLocations = new ArrayList<>();
    private Color antColor = Color.blue;
    @SuppressWarnings("unchecked")
    private Pair<Integer, Integer>[] directions = new Pair[]{AntDirections.SOUTH_WEST, AntDirections.SOUTH,
            AntDirections.SOUTH_EAST, AntDirections.NORTH_EAST, AntDirections.NORTH_WEST, AntDirections.NORTH,
            AntDirections.EAST, AntDirections.WEST};
    private final Random random = new Random();
    //todo have to remove this to handle multiple colors
    private static Map<CellType, Color> cellTypeColorMap = new HashMap<>();
    //Markov chain object which gives input colors
    private MarkovChain mkvChain;
    //Keep track of the color so that can get neigboring color from the markov chain.
    private Color color;

    //todo have to remove this to handle multiple colors
    static {
        cellTypeColorMap.put(CellType.DEFAULT, Color.black);
        cellTypeColorMap.put(CellType.NEST, Color.green);
        cellTypeColorMap.put(CellType.FOOD, Color.red);
    }

    public AntArea(MarkovChain mkvChain, BufferedImage frame, Color target, Color background) {
        //todo have to remove this to handle multiple colors
        cellTypeColorMap.put(CellType.FOOD, target);
        //todo have to remove this to handle multiple colors
        //Set a background
        cellTypeColorMap.put(CellType.DEFAULT, background);

        //Round up to the multiple of the cell size
        int cellSize = Configuration.CELL_SIZE;
        this.width = (frame.getWidth() / cellSize) * cellSize;
        this.height = (frame.getHeight() / cellSize) * cellSize;

        this.mapImage = frame;
        this.mkvChain = mkvChain;
        this.map = new Cell[width / cellSize][height / cellSize];

        //Create cells for the area
        int foodCellsCount = 0;
        for (int i = 0; i < this.width / cellSize; i++) {
            for (int j = 0; j < this.height / cellSize; j++) {
                this.map[i][j] = new Cell(new Pair<>(i, j), cellSize, true);
                //Record if the cell is a food cell.
                if (this.map[i][j].getType() == CellType.FOOD) {
                    foodCellsCount++;
                }
            }
        }
        System.out.println("Food Cells:" + foodCellsCount + " out of " + (map[0].length * map.length));

        //Spawn nest cells
        for (int i = 0; i < Configuration.NEST_CELLS_TO_SPAWN; i++) {
            int x = random.nextInt(width / cellSize);
            int y = random.nextInt(height / cellSize);
            for (int l = x; l < x + Configuration.NEST_AREA_SIZE && l < width / cellSize; l++) {
                for (int k = y; k < y + Configuration.NEST_AREA_SIZE && k < height / cellSize; k++) {
                    map[l][k].setType(CellType.NEST);
                    nestLocations.add(new Pair<>(l, k));
                }
            }
        }
    }

    //This constructor is used when working on empty canvas as it will spawn some food areas.
    @SuppressWarnings("unused")
    public AntArea(int width, int height, MarkovChain mkvChain) {
        this.mapImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        this.width = width;
        this.height = height;
        this.mkvChain = mkvChain;

        //Create cells for the area
        int cellSize = Configuration.CELL_SIZE;
        this.map = new Cell[width / cellSize][height / cellSize];
        for (int i = 0; i < this.width / cellSize; i++) {
            for (int j = 0; j < this.height / cellSize; j++) {
                this.map[i][j] = new Cell(new Pair<>(i, j), cellSize, false);
            }
        }

        //Spawn nests
        for (int i = 0; i < Configuration.NEST_CELLS_TO_SPAWN; i++) {
            int x = random.nextInt(width / cellSize);
            int y = random.nextInt(height / cellSize);
            for (int l = x; l < x + Configuration.NEST_AREA_SIZE && l < width / cellSize; l++) {
                for (int k = y; k < y + Configuration.NEST_AREA_SIZE && k < height / cellSize; k++) {
                    map[l][k].setType(CellType.NEST);
                    nestLocations.add(new Pair<>(l, k));
                }
            }
        }

        //Spawn food cells
        for (int i = 0; i < Configuration.FOOD_CELLS_TO_SPAWN; i++) {
            int x = random.nextInt(width / cellSize);
            int y = random.nextInt(height / cellSize);
            for (int l = x; l < x + Configuration.FOOD_AREA_SIZE && l < width / cellSize; l++) {
                for (int k = y; k < y + Configuration.FOOD_AREA_SIZE && k < height / cellSize; k++) {
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

    int getAreaWidth() {
        return width / Configuration.CELL_SIZE;
    }

    int getAreaHeight() {
        return height / Configuration.CELL_SIZE;
    }

    Cell[][] getMap() {
        return map;
    }

    /**
     * Generate a color from the markov chain
     *
     * @return color generated from markov chain
     */
    private Color getRandomColor() {
        if (color == null) {
            color = mkvChain.getRandomColor();
            return color;
        }
        color = mkvChain.getRandomNeighboringColor(color);
        return color;
    }

    /**
     * Spawn an ant
     *
     * @param directionVector direction of the ant
     * @param location        location of the ant
     * @throws AntArtException if not able to create ant on the given position
     */
    private void spawnAnt(Pair<Integer, Integer> directionVector, Pair<Integer, Integer> location) throws AntArtException {
        ants.add(new Ant(this, directionVector, location, antColor, Configuration.ANT_FOOD_CAPACITY));
        currAnts++;
    }

    /**
     * Update the area
     */
    private void updateArea() {
        for (int i = 0; i < getAreaWidth(); i++) {
            for (int j = 0; j < getAreaHeight(); j++) {
                Cell cell = map[i][j];
                //Skip food or nest cell
                if (cell.getType() != CellType.DEFAULT) {
                    continue;
                }
                //If it is a site then skip it
                if (!cell.decay) {
                    continue;
                }
                //Decay the pheromone levels
                cell.foodPheromone = cell.foodPheromone * (1 - Configuration.PHEROMONE_DECAY_RATE);
                cell.homePheromone = cell.homePheromone * (1 - Configuration.PHEROMONE_DECAY_RATE);
                //Remove the color if pheromone level drops below a level
                if ((cell.foodPheromone + cell.homePheromone) < Configuration.MINIMUM_PHEROMONE_THRESHOLD) {
                    cell.repaint(cellTypeColorMap.get(CellType.DEFAULT));
                    continue;
                }
                //Let the cell update according to the new intensity
                cell.repaintAccordingToPheromoneIntensity();
            }
        }
    }

    /**
     * Spawn ants
     */
    private void spawnAnts() {
        if (currAnts >= Configuration.MAX_ANTS) {
            return;
        }
        int randIndex = random.nextInt(nestLocations.size());
        int x = nestLocations.get(randIndex).getKey();
        int y = nestLocations.get(randIndex).getValue();

        if (map[x][y].isAntPresent()) {
            return;

        }
        try {
            spawnAnt(directions[random.nextInt(directions.length)], new Pair<>(x, y));

        } catch (AntArtException e) {
            System.out.println("Error in spawning ants");
            e.printStackTrace();
        }
    }

    public void update() {

        updateArea();
        spawnAnts();

        for (Ant ant : ants) {
            try {
                ant.update();

            } catch (AntArtException e) {
                System.out.println("Error in updating ants");
                e.printStackTrace();
            }
        }
    }

    public void shutDown() {
        //Remove nests
        for (Pair<Integer, Integer> nestLocation : nestLocations) {
            map[nestLocation.getKey()][nestLocation.getValue()].repaint(cellTypeColorMap.get(CellType.DEFAULT));
        }

        //Remove ants
        for (Ant ant : ants) {
            Pair<Integer, Integer> location = ant.getLocation();
            try {
                map[location.getKey()][location.getValue()].leave();

            } catch (AntArtException e) {
                System.out.println("Error in removing ants");
                e.printStackTrace();
            }
        }
    }
}
