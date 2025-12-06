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

public class LightIceBlock extends Hazard implements ISlidable {
    public LightIceBlock() {
        super(HazardType.LIGHT_ICE_BLOCK);
    }

    @Override
    public String getSymbol() {
        return "LB";
    }

    // Çarpışma Mantığı (Biri bana çarparsa ne olur?)
    @Override
    public ICollidable.CollisionResult onCollision(Penguin penguin, IcyTerrain terrain) {
        System.out.println(penguin.getSymbol() + " kicked a LightIceBlock!");

        // 1. Pengueni Sersemlet
        penguin.stun();

        // 2. YÖN HESAPLAMA (Penguenin konumuna göre)
        // Penguen nerede, ben neredeyim?
        int pRow = penguin.getRow();
        int pCol = penguin.getCol();

        Direction slideDir = null;

        if (this.row > pRow) { // Ben aşağıdayım, Penguen yukarıda -> AŞAĞI git
            slideDir = Direction.DOWN;
        } else if (this.row < pRow) { // Ben yukarıdayım, Penguen aşağıda -> YUKARI git
            slideDir = Direction.UP;
        } else if (this.col > pCol) { // Ben sağdayım, Penguen solda -> SAĞA git
            slideDir = Direction.RIGHT;
        } else if (this.col < pCol) { // Ben soldayım, Penguen sağda -> SOLA git
            slideDir = Direction.LEFT;
        }

        // 3. Buz Bloğunu Kaydır
        if (slideDir != null) {
            System.out.println("   -> LightIceBlock starts sliding " + slideDir);
            this.slide(slideDir, terrain);
        }

        return ICollidable.CollisionResult.stop(); // Penguen durur (Blok kaymaya devam eder)
    }

    // Buz Bloğunun Kendi Hareketi
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

        // 2. Harita Dışı (Suya Düşme)
        if (!terrain.isValidPosition(nextRow, nextCol)) {
            System.out.println("LightIceBlock fell into the water!");
            terrain.getCell(row, col).removeObject(this);
            return ISlidable.SlideResult.stopped("water");
        }

        Cell targetCell = terrain.getCell(nextRow, nextCol);

        // 3. Engel Kontrolü (Başka Buz, Penguen, SeaLion)
        boolean hasObstacle = targetCell.getObjects().stream()
                .anyMatch(obj -> obj instanceof Hazard || obj instanceof Penguin);

        if (hasObstacle) {
            // a) Deliğe girerse (HoleInIce) -> TIKA
            ITerrainObject hole = targetCell.getFirstObject(HoleInIce.class);
            if (hole != null) {
                System.out.println("LightIceBlock fell into a hole and plugged it!");
                terrain.getCell(row, col).removeObject(this); // Eski yerden sil
                ((HoleInIce) hole).plug(); // Deliği tıka
                return ISlidable.SlideResult.stopped("plugged-hole"); // Hareket biter
            }

            // b) SeaLion'a çarparsa -> Momentum aktar (SeaLion kayar, ben dururum)
            ITerrainObject sealion = targetCell.getFirstObject(SeaLion.class);
            if (sealion != null) {
                System.out.println("LightIceBlock hit a SeaLion. Momentum transferred!");
                ((SeaLion) sealion).slide(direction, terrain, stepsRemaining - 1); // SeaLion kaymaya başlar
                return ISlidable.SlideResult.stopped("sealion-collision"); // Ben dururum
            }

            // c) Diğer engellerde dur
            return ISlidable.SlideResult.stopped("blocked");
        }

        // 4. HAREKET ET
        terrain.getCell(row, col).removeObject(this);
        this.row = nextRow;
        this.col = nextCol;
        targetCell.addObject(this);

        // 5. Yemek Varsa Ez (Remove Food)
        Food food = targetCell.getFirstObject(Food.class);
        if (food != null) {
            System.out.println("LightIceBlock crushed a " + food.getSymbol());
            targetCell.removeObject(food);
        }

        // 6. Devam Et
        return slide(direction, terrain, stepsRemaining - 1);
    }
}
