package ant_art;

import javafx.util.Pair;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

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
    private boolean food;
    private Random random = new Random();

    public Ant(AntArea antArea, Pair<Integer, Integer> directionVector, Pair<Integer, Integer> location, Color color) {
        this.antArea = antArea;
        this.directionVector = directionVector;
        this.location = location;
        this.color = color;

        antArea.getMap()[location.getKey()][location.getValue()].move(this);
    }

    public Color getColor() {
        return color;
    }

    public boolean hasFood() {
        return food;
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

    @SuppressWarnings("ConstantConditions")
    private void moveToFoodSource() {
        AntArea.Cell forward = getCellInDirection(directionVector);
        AntArea.Cell left = getCellInDirection(AntDirections.moveCounterClockwise(directionVector));
        AntArea.Cell right = getCellInDirection(AntDirections.moveClockWise(directionVector));
        //Trying to find food.
        List<AntArea.Cell> cellList = Arrays.asList(forward, left, right);
        cellList.sort((o1, o2) -> {
            int score1 = o1.getType() == AntArea.CellType.FOOD ? o1.getFood() : 0;
            int score2 = o2.getType() == AntArea.CellType.FOOD ? o2.getFood() : 0;
            return score2 - score1;
        });
        boolean moved = false;
        for (AntArea.Cell cell : cellList) {
            if (cell.getType() == AntArea.CellType.FOOD && moveTo(cell)) {
                moved = true;
                break;
            }
        }
        if (moved) {
            return;
        }
        //1 in 3 chance to move to a random position.
        if (random.nextInt(3) == 0 && moveTo(cellList.get(random.nextInt(cellList.size())))) {
            return;
        }
        //Trying to find the direction with strongest food pheremone;
        cellList.sort((o1, o2) -> {
            float foodPheremone1 = o1.getFoodPheremone();
            float foodPheremone2 = o2.getFoodPheremone();
            if (foodPheremone1 == foodPheremone2) {
                return 0;
            }
            return foodPheremone1 > foodPheremone2 ? -1 : 1;
        });
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
        //Trying to find nest.
        List<AntArea.Cell> cellList = Arrays.asList(forward, left, right);
        cellList.sort((o1, o2) -> {
            int score1 = o1.getType() == AntArea.CellType.NEST ? 1 : 0;
            int score2 = o2.getType() == AntArea.CellType.FOOD ? 1 : 0;
            return score2 - score1;
        });
        boolean moved = false;
        for (AntArea.Cell cell : cellList) {
            if (cell.getType() == AntArea.CellType.NEST && moveTo(cell)) {
                moved = true;
                break;
            }
        }
        if (moved) {
            return;
        }
        //1 in 3 chance to move to a random position.
        if (random.nextInt(3) == 0 && moveTo(cellList.get(random.nextInt(cellList.size())))) {
            return;
        }
        //Trying to find the direction with strongest food pheremone;
        cellList.sort((o1, o2) -> {
            float homePheremone1 = o1.getHomePheremone();
            float homePheremone2 = o2.getHomePheremone();
            if (homePheremone1 == homePheremone2) {
                return 0;
            }
            return homePheremone1 > homePheremone2 ? -1 : 1;
        });
        for (AntArea.Cell cell : cellList) {
            if (moveTo(cell)) {
                break;
            }
        }
    }

    public void update() {
        AntArea.Cell curr = getCurrentCell();
        if (food) {
            if (curr.getType() == AntArea.CellType.NEST) {
                curr.depositFood();
                food = false;
                directionVector = AntDirections.moveBackward(directionVector);
                moveToFoodSource();
            } else {
                moveToNest();
            }
        } else {
            if (curr.getType() == AntArea.CellType.FOOD) {
                curr.pickUpFood();
                food = true;
                directionVector = AntDirections.moveBackward(directionVector);
                moveToNest();
            } else {
                moveToFoodSource();
            }
        }

    }
}
