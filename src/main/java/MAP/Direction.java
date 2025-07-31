package MAP;

public enum Direction {
    UP, DOWN, LEFT, RIGHT;

    @Override
    public String toString() {
        switch (this) {
            case UP:
                return "up";
            case DOWN:
                return "down";
            case LEFT:
                return "left";
            case RIGHT:
                return "right";
        }
        return null;
    }
}