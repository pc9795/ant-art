package ant_art;

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
 * Purpose: TODO:
 **/
public class Ant {
    private Pair<Integer, Integer> directionVector;
    private Pair<Integer, Integer> location;
    private final AntArea antArea;
    private Color color;
    private int currFood;
    private int foodCapacity;
    private Random random = new Random();
    private int selectionSeed = 3;

    public Ant(AntArea antArea, Pair<Integer, Integer> directionVector, Pair<Integer, Integer> location, Color color,
               int foodCapacity) {
        this.antArea = antArea;
        this.directionVector = directionVector;
        this.location = location;
        this.color = color;
        this.foodCapacity = foodCapacity;
        antArea.getMap()[location.getKey()][location.getValue()].move(this);
    }

    public Color getColor() {
        return color;
    }

    public boolean collectedFood() {
        return currFood == foodCapacity;
    }

    private AntArea.Cell getCellInDirection(Pair<Integer, Integer> direction) {
        int newX = location.getKey() + direction.getKey();
        int newY = location.getValue() + direction.getValue();

        newX = newX < 0 ? antArea.getAreaWidth() - 1 : newX;
        newY = newY < 0 ? antArea.getAreaHeight() - 1 : newY;
        newX %= antArea.getAreaWidth();
        newY %= antArea.getAreaHeight();

        return antArea.getMap()[newX][newY];
    }

    private AntArea.Cell getCurrentCell() {
        return antArea.getMap()[location.getKey()][location.getValue()];
    }

    private boolean moveTo(AntArea.Cell dest) {
        if (!dest.canMove()) {
            return false;
        }

        getCurrentCell().leave(this);
        dest.move(this);

        Pair<Integer, Integer> newLoc = dest.getLocation();
        location = new Pair<>(newLoc.getKey(), newLoc.getValue());
        return true;
    }

    private boolean moveToFoodCell(List<AntArea.Cell> cellList) {
        List<AntArea.Cell> foodCellList = cellList.stream().filter(cell -> cell.getType() == AntArea.CellType.FOOD).collect(Collectors.toList());
        if (foodCellList.isEmpty()) {
            return false;
        }
        foodCellList.sort((o1, o2) -> o2.getFood() - o1.getFood());
        //There is one in a `selectionSeed` chance of random selection.
        if (random.nextInt(selectionSeed) == 0) {
            Collections.shuffle(foodCellList);
        }
        for (AntArea.Cell cell : foodCellList) {
            if (moveTo(cell)) {
                return true;
            }
        }
        return false;
    }

    private boolean moveToNestCell(List<AntArea.Cell> cellList) {
        List<AntArea.Cell> nestCellList = cellList.stream().filter(cell -> cell.getType() == AntArea.CellType.NEST).collect(Collectors.toList());
        if (nestCellList.isEmpty()) {
            return false;
        }
        //There is one in a `selectionSeed` chance of random selection.
        if (random.nextInt(selectionSeed) == 0) {
            Collections.shuffle(nestCellList);
        }
        for (AntArea.Cell cell : nestCellList) {
            if (moveTo(cell)) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("ConstantConditions")
    private void moveToFoodSource() {
        AntArea.Cell forward = getCellInDirection(directionVector);
        AntArea.Cell left = getCellInDirection(AntDirections.moveCounterClockwise(directionVector));
        AntArea.Cell right = getCellInDirection(AntDirections.moveClockWise(directionVector));

        List<AntArea.Cell> cellList = Arrays.asList(forward, left, right);
        if (moveToFoodCell(cellList)) {
            return;
        }

        cellList.sort((o1, o2) -> {
            float foodPheremone1 = o1.getFoodPheremone();
            float foodPheremone2 = o2.getFoodPheremone();
            if (foodPheremone1 == foodPheremone2) {
                return 0;
            }
            return foodPheremone1 > foodPheremone2 ? -1 : 1;
        });
        //There is one in a `selectionSeed` chance of random selection.
        if (random.nextInt(selectionSeed) == 0) {
            Collections.shuffle(cellList);
        }
        for (AntArea.Cell cell : cellList) {
            if (moveTo(cell)) {
                break;
            }
        }
    }

    @SuppressWarnings("ConstantConditions")
    private void moveToNest() {
        AntArea.Cell forward = getCellInDirection(directionVector);
        AntArea.Cell left = getCellInDirection(AntDirections.moveCounterClockwise(directionVector));
        AntArea.Cell right = getCellInDirection(AntDirections.moveClockWise(directionVector));

        List<AntArea.Cell> cellList = Arrays.asList(forward, left, right);
        if (moveToNestCell(cellList)) {
            return;
        }

        cellList.sort((o1, o2) -> {
            float homePheremone1 = o1.getHomePheremone();
            float homePheremone2 = o2.getHomePheremone();
            if (homePheremone1 == homePheremone2) {
                return 0;
            }
            return homePheremone1 > homePheremone2 ? -1 : 1;
        });
        //There is one in a `selectionSeed` chance of random selection.
        if (random.nextInt(selectionSeed) == 0) {
            Collections.shuffle(cellList);
        }
        for (AntArea.Cell cell : cellList) {
            if (moveTo(cell)) {
                break;
            }
        }
    }

    public void update() {
        AntArea.Cell curr = getCurrentCell();
        if (collectedFood()) {
            if (curr.getType() == AntArea.CellType.NEST) {
                curr.depositFood();
                currFood = 0;
                directionVector = AntDirections.moveBackward(directionVector);
                moveToFoodSource();
            } else {
                moveToNest();
            }
        } else {
            if (curr.getType() == AntArea.CellType.FOOD) {
                curr.pickUpFood();
                currFood++;
                if (collectedFood()) {
                    directionVector = AntDirections.moveBackward(directionVector);
                    moveToNest();
                }
            } else {
                moveToFoodSource();
            }
        }

    }
}
