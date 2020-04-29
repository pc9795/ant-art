package ant_art.utils;

import javafx.util.Pair;

import java.util.HashMap;
import java.util.Map;

/**
 * Created By: Prashant Chaubey
 * Created On: 17-04-2020 16:18
 * Purpose: Directions possible for ants
 **/
public class AntDirections {
    public static final Pair<Integer, Integer> WEST = new Pair<>(-1, 0);
    public static final Pair<Integer, Integer> SOUTH = new Pair<>(0, 1);
    public static final Pair<Integer, Integer> EAST = new Pair<>(1, 0);
    public static final Pair<Integer, Integer> NORTH = new Pair<>(0, -1);
    public static final Pair<Integer, Integer> NORTH_EAST = new Pair<>(1, -1);
    public static final Pair<Integer, Integer> NORTH_WEST = new Pair<>(-1, -1);
    public static final Pair<Integer, Integer> SOUTH_WEST = new Pair<>(-1, 1);
    public static final Pair<Integer, Integer> SOUTH_EAST = new Pair<>(1, 1);

    //These two maps make rotating code clutter-free. No use as individually.
    private static Map<Pair<Integer, Integer>, Integer> forwardMap = new HashMap<>();
    private static Map<Integer, Pair<Integer, Integer>> reverseMap = new HashMap<>();

    private static int NO_OF_DIRECTIONS = 8;

    static {
        forwardMap.put(NORTH, 0);
        forwardMap.put(NORTH_EAST, 1);
        forwardMap.put(EAST, 2);
        forwardMap.put(SOUTH_EAST, 3);
        forwardMap.put(SOUTH, 4);
        forwardMap.put(SOUTH_WEST, 5);
        forwardMap.put(WEST, 6);
        forwardMap.put(NORTH_WEST, 7);

        reverseMap.put(0, NORTH);
        reverseMap.put(1, NORTH_EAST);
        reverseMap.put(2, EAST);
        reverseMap.put(3, SOUTH_EAST);
        reverseMap.put(4, SOUTH);
        reverseMap.put(5, SOUTH_WEST);
        reverseMap.put(6, WEST);
        reverseMap.put(7, NORTH_WEST);
    }

    /**
     * Move in clockwise direction
     *
     * @param currDirection current direction
     * @return new direction
     */
    public static Pair<Integer, Integer> moveClockWise(Pair<Integer, Integer> currDirection) {
        int loc = forwardMap.getOrDefault(currDirection, -1);
        if (loc == -1) {
            return null;
        }
        loc++;
        loc %= NO_OF_DIRECTIONS;
        return reverseMap.get(loc);
    }

    /**
     * Move in counter clock wise direction
     *
     * @param currDirection current direction
     * @return new direction
     */
    public static Pair<Integer, Integer> moveCounterClockwise(Pair<Integer, Integer> currDirection) {
        int loc = forwardMap.getOrDefault(currDirection, -1);
        if (loc == -1) {
            return null;
        }
        loc--;
        loc = loc < 0 ? NO_OF_DIRECTIONS - 1 : loc;
        return reverseMap.get(loc);
    }

    /**
     * Move in opposite direction
     *
     * @param currDirection current direction
     * @return new direction
     */
    public static Pair<Integer, Integer> moveBackward(Pair<Integer, Integer> currDirection) {
        int loc = forwardMap.getOrDefault(currDirection, -1);
        if (loc == -1) {
            return null;
        }
        loc += NO_OF_DIRECTIONS / 2;
        loc %= NO_OF_DIRECTIONS;
        return reverseMap.get(loc);
    }
}
