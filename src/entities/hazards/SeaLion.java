package entities.hazards;

import core.Cell;
import core.IcyTerrain;
import entities.Food;
import entities.Hazard;
import entities.Penguin;
import enums.Direction;
import enums.HazardType;
import interfaces.ICollidable;
import interfaces.ISlidable;
import interfaces.ITerrainObject;

public class SeaLion extends Hazard implements ISlidable {
    public SeaLion() {
        super(HazardType.SEA_LION);
    }

    @Override
    public String getSymbol() {
        return "SL";
    }

    // Çarpışma: Penguen bana çarparsa ne olur?
    @Override
    public ICollidable.CollisionResult onCollision(Penguin penguin, IcyTerrain terrain) {
        System.out.println("BOING! " + penguin.getSymbol() + " bounced off the SeaLion!");

        // 1. YÖN HESAPLAMA (Momentum Transferi)
        int pRow = penguin.getRow();
        int pCol = penguin.getCol();

        Direction slideDir = null;
        if (this.row > pRow) slideDir = Direction.DOWN;
        else if (this.row < pRow) slideDir = Direction.UP;
        else if (this.col > pCol) slideDir = Direction.RIGHT;
        else if (this.col < pCol) slideDir = Direction.LEFT;

        // 2. SeaLion Kaymaya Başlar
        if (slideDir != null) {
            System.out.println("   -> SeaLion absorbs momentum and slides " + slideDir);
            this.slide(slideDir, terrain);
        }

        // Penguen ters yöne sektirilir.
        Direction bounceDir = slideDir == null ? null : opposite(slideDir);
        return bounceDir == null ? ICollidable.CollisionResult.stop()
                : ICollidable.CollisionResult.continueWith(bounceDir);
    }

    private Direction opposite(Direction dir) {
        switch (dir) {
            case UP:
                return Direction.DOWN;
            case DOWN:
                return Direction.UP;
            case LEFT:
                return Direction.RIGHT;
            case RIGHT:
                return Direction.LEFT;
            default:
                return null;
        }
    }

    // SeaLion Kayması (Penguen gibi ama yemek yemez)
    @Override
    public ISlidable.SlideResult slide(Direction direction, IcyTerrain terrain) {
        return slide(direction, terrain, ISlidable.MAX_SLIDE_STEPS);
    }

    @Override
    public ISlidable.SlideResult slide(Direction direction, IcyTerrain terrain, int stepsRemaining) {
        if (stepsRemaining <= 0) {
            return ISlidable.SlideResult.stopped("max-steps");
        }
        // 1. Hedef Hesapla
        int nextRow = row;
        int nextCol = col;
        switch (direction) {
            case UP:
                nextRow--;
                break;
            case DOWN:
                nextRow++;
                break;
            case LEFT:
                nextCol--;
                break;
            case RIGHT:
                nextCol++;
                break;
        }

        // 2. Suya Düşme
        if (!terrain.isValidPosition(nextRow, nextCol)) {
            System.out.println("SeaLion fell into the water!");
            terrain.getCell(row, col).removeObject(this);
            return ISlidable.SlideResult.stopped("water");
        }

        Cell targetCell = terrain.getCell(nextRow, nextCol);

        // 3. Engel
        boolean hasObstacle = targetCell.getObjects().stream()
                .anyMatch(obj -> obj instanceof Hazard || obj instanceof Penguin);

        if (hasObstacle) {
            // Deliğe girerse tıka
            ITerrainObject hole = targetCell.getFirstObject(HoleInIce.class);
            if (hole != null) {
                System.out.println("SeaLion fell into a hole and plugged it!");
                terrain.getCell(row, col).removeObject(this);
                ((HoleInIce) hole).plug();
                return ISlidable.SlideResult.stopped("plugged-hole");
            }
            return ISlidable.SlideResult.stopped("blocked"); // Diğer engellerde dur
        }

        // 4. Hareket
        terrain.getCell(row, col).removeObject(this);
        this.row = nextRow;
        this.col = nextCol;
        targetCell.addObject(this);

        // 5. Yemek Ezme
        Food food = targetCell.getFirstObject(Food.class);
        if (food != null) {
            targetCell.removeObject(food); // SeaLion da yemeği yok eder (nadir durum)
        }

        // 6. Devam
        return slide(direction, terrain, stepsRemaining - 1);
    }
}
