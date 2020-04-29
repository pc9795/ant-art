package ant_art.entities;

import ant_art.utils.AntDirections;
import ant_art.config.Configuration;
import ant_art.utils.ImageUtils;
import ant_art.utils.MarkovChain;
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
        NEST, DEFAULT, FOOD, SITE
    }

    /**
     * This class represents an ant food which represents a particular target color. It also generate alternatives
     * for this target color which seems not so nice at first but I have not found any better way yet.
     */
    class AntFood {
        @SuppressWarnings("unused")
        private int id;
        Color color;
        private MarkovChain chain;
        private Color prevRandomColor;

        AntFood(int id, Color color, MarkovChain chain) {
            this.id = id;
            this.color = color;
            this.chain = chain;
        }

        Color getRandomColor() {
            if (prevRandomColor == null) {
                prevRandomColor = chain.getRandomColor();
                return prevRandomColor;
            }
            prevRandomColor = chain.getRandomNeighboringColor(prevRandomColor);
            return prevRandomColor;
        }
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
        //If this cell contains then store the food id.
        private int foodId = -1;

        Cell(Pair<Integer, Integer> location, int size, boolean identifyFood) {
            this.type = CellType.DEFAULT;
            this.location = location;
            this.size = size;
            this.food = Configuration.DEFAULT_FOOD_IN_CELL;
            //Try to identify the cell type from the area contents on the location
            if (identifyFood) {
                identifyFood();
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
         * Check whether the cell contains the particular food
         *
         * @param foodId id of the food
         * @return true if cell contains the food with given id
         */
        boolean isContainingFood(int foodId) {
            return type == CellType.FOOD && this.foodId == foodId;
        }

        /**
         * Check whether the cell contains an ant food or not.
         */
        private void identifyFood() {
            //Get the coordinates in the area
            int imageX = location.getKey() * size;
            int imageY = location.getValue() * size;

            Map<Integer, Integer> foodIdToCountMap = new HashMap<>();

            //Check pixel by pixel that the color of the cell is similar to any of the ant-foods.
            for (int i = imageX; i < imageX + size; i++) {
                for (int j = imageY; j < imageY + size; j++) {
                    Color pixelColor = new Color(mapImage.getRGB(i, j));
                    for (int foodId : antFoodMap.keySet()) {
                        boolean isSimilar = ImageUtils.isSimilar(antFoodMap.get(foodId).color, pixelColor);
                        if (isSimilar) {
                            foodIdToCountMap.put(foodId, foodIdToCountMap.getOrDefault(foodId, 0) + 1);
                        }
                    }
                }
            }

            //If the color count for any food crosses a threshold then setup with that foodId.
            for (int foodId : antFoodMap.keySet()) {
                //Food never found
                if (!foodIdToCountMap.containsKey(foodId)) {
                    continue;
                }
                float colorRatio = (float) foodIdToCountMap.get(foodId) / (size * size);
                if (colorRatio > Configuration.TYPE_IDENTIFICATION_THRESHOLD) {
                    setFood(foodId);
                    break;
                }
            }
        }

        /**
         * Set this cell as a site.
         */
        private void setSite() {
            //We have not set foodId as this site should remember its color forever.
            this.type = CellType.SITE;
        }

        /**
         * Set cell as a nest
         */
        private void setNest() {
            this.type = CellType.NEST;
            repaint(Configuration.Colors.NEST);
            this.foodId = -1;
        }

        /**
         * Set cell as a food
         *
         * @param foodId id of the food
         */
        private void setFood(int foodId) {
            this.type = CellType.FOOD;
            this.foodId = foodId;
            repaint(antFoodMap.get(foodId).color);
        }

        /**
         * Let the food to be picked up from the cell.
         * todo return the food picked
         *
         * @param foodId id of the food to pickup
         */
        void pickUpFood(int foodId) throws AntArtException {
            if (type != CellType.FOOD) {
                throw new AntArtException("Invalid operation: Not a food source");
            }
            if (this.foodId != foodId) {
                throw new AntArtException(String.format("Not contain food:%s", foodId));
            }
            food -= Configuration.FOOD_PICKUP_QUANTITY;
            //All food is gone.
            if (food == 0) {
                //After all food is gone make it as a site.
                //THIS IS THE MAIN PART WHICH LET THE ART STAY IN THE FRAME
                setSite();
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
            //We don't change intensity of FOOD and NEST cells. A color is set once a ant moves in this cell.
            if (type == CellType.FOOD || type == CellType.NEST || color == null) {
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
        void leave(Ant ant) throws AntArtException {
            if (!antPresent) {
                throw new AntArtException("Can't leave as no ant is currently present here");
            }
            switch (type) {
                case DEFAULT:
                    //Get a random color when an ant leave this cell.
                    color = antFoodMap.get(ant.getFoodId()).getRandomColor();
                    repaintAccordingToPheromoneIntensity();
                    break;
                case NEST:
                    repaint(Configuration.Colors.NEST);
                    break;
                case FOOD:
                    repaint(antFoodMap.get(foodId).color);
                    break;
                case SITE:
                    //A site will be updated by a random color for the food id which it used to contain
                    repaint(antFoodMap.get(foodId).getRandomColor());
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

    //Map of food id to the ant food.
    private Map<Integer, AntFood> antFoodMap = new HashMap<>();
    private Color defaultColor;
    private int lastFoodIdForWhichAntSpawned;

    public AntArea(MarkovChain[] mkvChains, BufferedImage frame, List<Color> targetColors, Color background) throws AntArtException {

        //Round up to the multiple of the cell size
        int cellSize = Configuration.CELL_SIZE;
        this.width = (frame.getWidth() / cellSize) * cellSize;
        this.height = (frame.getHeight() / cellSize) * cellSize;

        this.defaultColor = background;
        this.mapImage = frame;
        this.map = new Cell[width / cellSize][height / cellSize];

        if (mkvChains.length != targetColors.size()) {
            throw new AntArtException("The number of markov chains and target colors should be same");
        }

        //Updated target colors as ant foods
        for (int i = 0; i < targetColors.size(); i++) {
            antFoodMap.put(i, new AntFood(i, targetColors.get(i), mkvChains[i]));
        }

        //So that spawning starts from zero. Look for `spawnAnt` and you will understand this.
        this.lastFoodIdForWhichAntSpawned = targetColors.size() - 1;

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
                    map[l][k].setNest();
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

        //Single food; setting food id as 0
        this.antFoodMap.put(0, new AntFood(0, Configuration.Colors.FOOD, mkvChain));

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
                    map[l][k].setNest();
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
                    //This constructor is for ant area with single food. This food will have a food id of 0
                    map[k][l].setFood(0);
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
     * Spawn an ant
     *
     * @param directionVector direction of the ant
     * @param location        location of the ant
     * @throws AntArtException if not able to create ant on the given position
     */
    private void spawnAnt(Pair<Integer, Integer> directionVector, Pair<Integer, Integer> location) throws AntArtException {
        lastFoodIdForWhichAntSpawned++;
        lastFoodIdForWhichAntSpawned %= antFoodMap.size();

        ants.add(new Ant(this, directionVector, location, antColor, Configuration.ANT_FOOD_CAPACITY, lastFoodIdForWhichAntSpawned));
        currAnts++;
    }

    /**
     * Update the area
     */
    private void updateArea() {
        for (int i = 0; i < getAreaWidth(); i++) {
            for (int j = 0; j < getAreaHeight(); j++) {
                Cell cell = map[i][j];
                //Skip for food, site or nest cell
                if (cell.getType() != CellType.DEFAULT) {
                    continue;
                }
                //Decay the pheromone levels
                cell.foodPheromone = cell.foodPheromone * (1 - Configuration.PHEROMONE_DECAY_RATE);
                cell.homePheromone = cell.homePheromone * (1 - Configuration.PHEROMONE_DECAY_RATE);
                //Remove the color if pheromone level drops below a level
                if ((cell.foodPheromone + cell.homePheromone) < Configuration.MINIMUM_PHEROMONE_THRESHOLD) {
                    cell.repaint(defaultColor);
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
        int areaWidth = getAreaWidth();
        int areaHeight = getAreaHeight();

        //Remove the food which ants are not able to find
        for (int i = 0; i < areaWidth; i++) {
            for (int j = 0; j < areaHeight; j++) {
                if (map[i][j].type == CellType.FOOD) {
                    map[i][j].repaint(defaultColor);
                }
            }
        }
        //Remove nests
        for (Pair<Integer, Integer> nestLocation : nestLocations) {
            map[nestLocation.getKey()][nestLocation.getValue()].repaint(defaultColor);
        }

        //Remove ants
        for (Ant ant : ants) {
            Pair<Integer, Integer> location = ant.getLocation();
            try {
                map[location.getKey()][location.getValue()].leave(ant);

            } catch (AntArtException e) {
                System.out.println("Error in removing ants");
                e.printStackTrace();
            }
        }
    }
}
