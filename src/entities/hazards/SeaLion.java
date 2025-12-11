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
    public boolean onCollision(Penguin penguin, IcyTerrain terrain) {
        System.out.println("BOING! " + penguin.getSymbol() + " bounced off the SeaLion!");

        // 1. YÖN HESAPLAMA
        int pRow = penguin.getRow();
        int pCol = penguin.getCol();

        Direction pushDir = null;   // SeaLion'ın gideceği yön (Penguenin geldiği yön)
        Direction bounceDir = null; // Penguenin sekeceği yön (Ters yön)

        if (this.row > pRow) { 
            pushDir = Direction.DOWN;
            bounceDir = Direction.UP;
        } else if (this.row < pRow) { 
            pushDir = Direction.UP;
            bounceDir = Direction.DOWN;
        } else if (this.col > pCol) { 
            pushDir = Direction.RIGHT;
            bounceDir = Direction.LEFT;
        } else if (this.col < pCol) { 
            pushDir = Direction.LEFT;
            bounceDir = Direction.RIGHT;
        }

        // 2. SeaLion Kaymaya Başlar (Momentum Transferi)
        if (pushDir != null) {
            System.out.println("   -> SeaLion absorbs momentum and slides " + pushDir);
            this.slide(pushDir, terrain);
        }

        // 3. Penguen Geri Seker (Bounce)
        if (bounceDir != null) {
            System.out.println("   -> " + penguin.getSymbol() + " bounces back " + bounceDir);
            penguin.slide(bounceDir, terrain);
        }

        // return false diyerek penguenin ESKİ yöndeki hareketini bitiriyoruz.
        // Yeni hareket (bounce) yukarıdaki penguin.slide() ile başladı bile.
        return false; 
    }

    // SeaLion Kayması (Penguen gibi ama yemek yemez)
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

        // 2. Suya Düşme
        if (!terrain.isValidPosition(nextRow, nextCol)) {
            System.out.println("SeaLion fell into the water!");
            terrain.getCell(row, col).removeObject(this);
            return;
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
                return;
            }
            return; // Diğer engellerde dur
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
        slide(direction, terrain);
    }
}