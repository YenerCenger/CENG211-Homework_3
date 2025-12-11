package entities.penguins;

import core.Cell;
import core.IcyTerrain;
import entities.Food;
import entities.Penguin;

public class RoyalPenguin extends Penguin {

    public RoyalPenguin(String name) {
        super(name);
    }

    @Override
    public void performSpecialAction(IcyTerrain terrain) {
        System.out.println(name + " (Royal) uses special action: Move 1 step safely.");

        int dr = 0, dc = 0;

        if (this.isPlayer()) {
            // OYUNCU İSE: Girdiyi IcyTerrain üzerinden al
            String input = terrain.askForInput("Choose 1-step direction (U/D/L/R) --> ");
            
            switch (input) {
                case "U": dr = -1; break;
                case "D": dr = 1; break;
                case "L": dc = -1; break;
                case "R": dc = 1; break;
                default:
                    System.out.println("Invalid direction, action wasted.");
                    return;
            }
        } else {
            // AI İSE:
            // Sadece GÜVENLİ (Su, Tehlike veya Penguen olmayan) kareleri bul
            java.util.List<Integer> safeDirections = new java.util.ArrayList<>();
            int[][] deltas = { {-1, 0}, {1, 0}, {0, -1}, {0, 1} }; // UP, DOWN, LEFT, RIGHT

            for (int i = 0; i < 4; i++) {
                int tr = row + deltas[i][0];
                int tc = col + deltas[i][1];

                // 1. Suya düşüyor mu?
                if (!terrain.isValidPosition(tr, tc)) continue;

                // 2. Tehlike veya başka penguen var mı?
                Cell target = terrain.getCell(tr, tc);
                boolean isSafe = target.isEmpty() || target.getObjects().stream().noneMatch(o -> o instanceof entities.Hazard || o instanceof Penguin);
                
                if (isSafe) {
                    safeDirections.add(i);
                }
            }

            int selectedDir;
            if (!safeDirections.isEmpty()) {
                // Güvenli yer varsa oradan seç
                selectedDir = safeDirections.get(new java.util.Random().nextInt(safeDirections.size()));
            } else {
                // "UNLESS THEY HAVE NO OTHER CHOICE":
                // Güvenli yer yoksa, mecburen rastgele bir yöne (tehlikeye/suya) git.
                selectedDir = new java.util.Random().nextInt(4);
            }

            switch (selectedDir) {
                case 0: dr = -1; break; // UP
                case 1: dr = 1; break; // DOWN
                case 2: dc = -1; break; // LEFT
                case 3: dc = 1; break; // RIGHT
            }
        }
        
        // --- HAREKET VE SONUÇ MANTIĞI ---
        
        int nextRow = row + dr;
        int nextCol = col + dc;

        // 1. Harita dışına çıkıyor mu? (SUYA DÜŞME)
        // Eğer AI'nın "başka şansı yoksa" ve burayı seçtiyse, düşer ve ölür.
        if (!terrain.isValidPosition(nextRow, nextCol)) {
            System.out.println("Oops! " + name + " stepped into the water using special action!");
            die(terrain);
            return;
        }

        Cell targetCell = terrain.getCell(nextRow, nextCol);

        // 2. Hedef Dolu mu? (Penguen veya Tehlike varsa)
        if (!targetCell.isEmpty()) {
            // Engel ne?
            interfaces.ITerrainObject obstacle = targetCell.getObjects().stream()
                    .filter(obj -> obj instanceof entities.Hazard || obj instanceof entities.Penguin)
                    .findFirst()
                    .orElse(null);

            if (obstacle != null) {
                // EĞER ENGEL BİR ÇUKUR İSE (HoleInIce):
                // "Başka şansı yoksa" buraya adım atar ve düşer.
                if (obstacle instanceof entities.hazards.HoleInIce) {
                    System.out.println(name + " stepped into a HoleInIce by accident while using special action!");
                    
                    // Haritadan sil ve öldür
                    terrain.getCell(row, col).removeObject(this);
                    // Teknik olarak çukura girmiş sayılır
                    this.row = nextRow; 
                    this.col = nextCol;
                    die(terrain); // Oyundan çıkar
                    return;
                }

                // EĞER ENGEL DUVAR GİBİ İSE (HeavyIceBlock, SeaLion, Penguin):
                // Fiziksel olarak oraya adım atılamaz. Olduğu yerde kalır.
                System.out.println("Cannot move there, tile is occupied. Action wasted.");
                return;
            }
        }

        // 3. ADIM AT (Engel yoksa)
        System.out.println(" -> " + name + " steps to (" + nextRow + ", " + nextCol + ")");
        terrain.getCell(row, col).removeObject(this);
        this.row = nextRow;
        this.col = nextCol;
        targetCell.addObject(this);

        // 4. Yemek varsa ye
        Food food = targetCell.getFirstObject(Food.class);
        if (food != null) {
            eat(food, terrain);
        }
    }
}