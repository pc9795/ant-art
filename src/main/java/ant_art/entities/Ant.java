package ant_art.entities;

import ant_art.AntDirections;
import ant_art.Configuration;
import ant_art.exceptions.AntArtException;
import javafx.util.Pair;

import java.awt.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Created By: Prashant Chaubey
 * Created On: 17-04-2020 16:17
 * Purpose: Ant that actually moves on the map looking for food
 **/
class Ant {
    //direction of the ant
    private Pair<Integer, Integer> directionVector;
    //location of the ant
    private Pair<Integer, Integer> location;
    //reference of the area where ant moves
    private final AntArea antArea;
    //color of the ant
    private Color color;
    //current food of the ant
    private int currFood;
    //capacity of the ant
    private int foodCapacity;
    //random generator for the ant
    private Random random = new Random();

    Ant(AntArea antArea, Pair<Integer, Integer> directionVector, Pair<Integer, Integer> location, Color color,
        int foodCapacity) throws AntArtException {
        this.antArea = antArea;
        this.directionVector = directionVector;
        this.location = location;
        this.color = color;
        this.foodCapacity = foodCapacity;
        //Move to the location
        antArea.getMap()[location.getKey()][location.getValue()].move(this);
    }

    Color getColor() {
        return color;
    }

    Pair<Integer, Integer> getLocation() {
        return location;
    }

    /**
     * @return true if ant collected all the food it can.
     */
    boolean collectedFood() {
        return currFood == foodCapacity;
    }

    /**
     * Get the cell in a particular direction. It assumes that the ant area has a torus like shape
     *
     * @param direction direction to look for
     * @return cell in the given direction
     */
    private AntArea.Cell getCellInDirection(Pair<Integer, Integer> direction) {
        int newX = location.getKey() + direction.getKey();
        int newY = location.getValue() + direction.getValue();

        //Torus shape
        newX = newX < 0 ? antArea.getAreaWidth() - 1 : newX;
        newY = newY < 0 ? antArea.getAreaHeight() - 1 : newY;
        newX %= antArea.getAreaWidth();
        newY %= antArea.getAreaHeight();

        return antArea.getMap()[newX][newY];
    }

    /**
     * @return cell at which ant is located
     */
    private AntArea.Cell getCurrentCell() {
        return antArea.getMap()[location.getKey()][location.getValue()];
    }

    /**
     * Try to move to a destination cell
     *
     * @param dest destination cell
     * @return true if moved successfully
     */
    private boolean moveTo(AntArea.Cell dest) throws AntArtException {
        if (dest.isAntPresent()) {
            return false;
        }

        getCurrentCell().leave();
        dest.move(this);

        //Update the ant location
        Pair<Integer, Integer> newLoc = dest.getLocation();
        location = new Pair<>(newLoc.getKey(), newLoc.getValue());

        return true;
    }

    /**
     * Move to a food cell from the list of cells
     *
     * @param cellList list of cells where ant can move
     * @return true if ant was able to move
     */
    private boolean moveToFoodCell(List<AntArea.Cell> cellList) throws AntArtException {
        //Find food cells
        List<AntArea.Cell> foodCellList = cellList.stream().filter(cell -> cell.getType() == AntArea.CellType.FOOD).collect(Collectors.toList());
        if (foodCellList.isEmpty()) {
            return false;
        }
        //Sort according to the quantity of the food
        foodCellList.sort((o1, o2) -> o2.getFood() - o1.getFood());
        //Do a random action based on a probability
        if (random.nextInt(Configuration.ANT_SELECTION_SEED) == 0) {
            Collections.shuffle(foodCellList);
        }
        //Try to move to the food cell
        for (AntArea.Cell cell : foodCellList) {
            if (moveTo(cell)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Move to a nest cell
     *
     * @param cellList list of cells where ant can move
     * @return true if able to move
     */
    private boolean moveToNestCell(List<AntArea.Cell> cellList) throws AntArtException {
        //Find nest cellsF
        List<AntArea.Cell> nestCellList = cellList.stream().filter(cell -> cell.getType() == AntArea.CellType.NEST).collect(Collectors.toList());
        if (nestCellList.isEmpty()) {
            return false;
        }
        //Do a random action based on a probability
        if (random.nextInt(Configuration.ANT_SELECTION_SEED) == 0) {
            Collections.shuffle(nestCellList);
        }
        //Try to move to the nest cell
        for (AntArea.Cell cell : nestCellList) {
            if (moveTo(cell)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Move to the food source
     */
    @SuppressWarnings("ConstantConditions")
    private void moveToFoodSource() throws AntArtException {
        AntArea.Cell forward = getCellInDirection(directionVector);
        AntArea.Cell left = getCellInDirection(AntDirections.moveCounterClockwise(directionVector));
        AntArea.Cell right = getCellInDirection(AntDirections.moveClockWise(directionVector));

        //Try to move to a food cell.
        List<AntArea.Cell> cellList = Arrays.asList(forward, left, right);
        if (moveToFoodCell(cellList)) {
            return;
        }
        //Sort according to food pheromone  levels
        cellList.sort((o1, o2) -> {
            float fp1 = o1.getFoodPheromone();
            float fp2 = o2.getFoodPheromone();
            if (fp1 == fp2) {
                return 0;
            }
            return fp1 > fp2 ? -1 : 1;
        });
        //Do a random action based on a probability
        if (random.nextInt(Configuration.ANT_SELECTION_SEED) == 0) {
            Collections.shuffle(cellList);
        }
        //Try to move to a cell
        for (AntArea.Cell cell : cellList) {
            if (moveTo(cell)) {
                break;
            }
        }
    }

    /**
     * Move to nest source
     */
    @SuppressWarnings("ConstantConditions")
    private void moveToNest() throws AntArtException {
        AntArea.Cell forward = getCellInDirection(directionVector);
        AntArea.Cell left = getCellInDirection(AntDirections.moveCounterClockwise(directionVector));
        AntArea.Cell right = getCellInDirection(AntDirections.moveClockWise(directionVector));

        //Try to move to a nest cell
        List<AntArea.Cell> cellList = Arrays.asList(forward, left, right);
        if (moveToNestCell(cellList)) {
            return;
        }

        //Sort according to home pheremone levels
        cellList.sort((o1, o2) -> {
            float hp1 = o1.getHomePheromone();
            float hp2 = o2.getHomePheromone();
            if (hp1 == hp2) {
                return 0;
            }
            return hp1 > hp2 ? -1 : 1;
        });
        //Do a random action based on a probability
        if (random.nextInt(Configuration.ANT_SELECTION_SEED) == 0) {
            Collections.shuffle(cellList);
        }
        //Try to move to a cell
        for (AntArea.Cell cell : cellList) {
            if (moveTo(cell)) {
                break;
            }
        }
    }

    //Update the state of the ant
    void update() throws AntArtException {
        AntArea.Cell curr = getCurrentCell();
        if (collectedFood()) {
            //If collected food and reached to a nest. Drop the food and move backwards to new food source.
            if (curr.getType() == AntArea.CellType.NEST) {
                curr.depositFood();
                currFood = 0;
                directionVector = AntDirections.moveBackward(directionVector);
                moveToFoodSource();
            } else {
                //If collected  food and not reached to a nest then look for a way to nest
                moveToNest();
            }
        } else {
            if (curr.getType() == AntArea.CellType.FOOD) {
                //If not collected food and on a cell with food. Pick up the food and if collecting this food fills
                //the current capacity turn around and look for nest
                curr.pickUpFood();
                currFood++;
                if (collectedFood()) {
                    directionVector = AntDirections.moveBackward(directionVector);
                    moveToNest();
                }
            } else {
                //If not collected food and not on a food cell look for a food source
                moveToFoodSource();
            }
        }

    }
}
