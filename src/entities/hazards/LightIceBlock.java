package entities.hazards;

import core.Cell;
import core.IcyTerrain;
import entities.Food;
import entities.Hazard;
import entities.Penguin;
import enums.Direction;
import enums.HazardType;
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
    public boolean onCollision(Penguin penguin, IcyTerrain terrain) {
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

        return false; // Penguen durur (Blok kaymaya devam eder)
    }

    // Buz Bloğunun Kendi Hareketi
    @Override
    public void slide(Direction direction, IcyTerrain terrain) {
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
            return;
        }

        Cell targetCell = terrain.getCell(nextRow, nextCol);

        // 3. Engel Kontrolü (Başka Buz, Penguen, SeaLion)
        // DÜZELTME: Tıkalı delikler (PH) engel sayılmaz, üzerinden geçilir!
        boolean hasObstacle = targetCell.getObjects().stream()
                .anyMatch(obj -> {
                    if (obj instanceof HoleInIce) {
                        return !((HoleInIce) obj).isPlugged(); // Tıkalı DEĞİLSE engeldir
                    }
                    return obj instanceof Hazard || obj instanceof Penguin;
                });

        if (hasObstacle) {
            // a) Deliğe girerse (HoleInIce) -> TIKA
            ITerrainObject hole = targetCell.getFirstObject(HoleInIce.class);
            // DÜZELTME: Sadece tıkalı değilse düşer!
            if (hole != null && !((HoleInIce) hole).isPlugged()) {
                System.out.println("LightIceBlock fell into a hole and plugged it!");
                terrain.getCell(row, col).removeObject(this); // Eski yerden sil
                ((HoleInIce) hole).plug(); // Deliği tıka
                return; // Hareket biter
            }

            // b) SeaLion'a çarparsa -> Momentum aktar (SeaLion kayar, ben dururum)
            ITerrainObject sealion = targetCell.getFirstObject(SeaLion.class);
            if (sealion != null) {
                System.out.println("LightIceBlock hit a SeaLion. Momentum transferred!");
                ((SeaLion) sealion).slide(direction, terrain); // SeaLion kaymaya başlar
                return; // Ben dururum
            }

            // c) Diğer engellerde dur
            return;
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
        slide(direction, terrain);
    }
}