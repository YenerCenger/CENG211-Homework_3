package entities.penguins;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

import core.Cell;
import core.IcyTerrain;
import entities.Food;
import entities.Penguin;
import enums.Direction;

public class RoyalPenguin extends Penguin {

    private static final Scanner INPUT = new Scanner(System.in);

    public RoyalPenguin(String name) {
        super(name);
    }

    @Override
    public void performSpecialAction(IcyTerrain terrain) {
        System.out.println(name + " (Royal) uses special action: Move 1 step safely.");

        // --- YÖN SEÇİMİ ---
        // Not: Normalde AI için rastgele seçmek gerekir ama şimdilik P2 (Siz) için
        // Scanner kullanıyoruz.
        // Eğer bu penguen yapay zeka ise rastgele bir yön de seçtirebiliriz.
        // Basitlik adına burada kullanıcıdan alıyoruz (Sıra sizdeyse).

        int dr = 0, dc = 0;

        // Eğer bu penguen "P2" ise (Sizin pengueniniz varsayımıyla) kullanıcıdan
        // soralım.
        // Değilse (AI) rastgele güvenli bir yer seçsin.

        if (name.equals("P2")) {
            // Oyuncu pengueni için girdiyi al
            System.out.print("Choose 1-step direction (U/D/L/R) --> ");
            String input = INPUT.next().toUpperCase();
            switch (input) {
                case "U":
                    dr = -1;
                    break;
                case "D":
                    dr = 1;
                    break;
                case "L":
                    dc = -1;
                    break;
                case "R":
                    dc = 1;
                    break;
                default:
                    System.out.println("Invalid direction, action wasted.");
                    return;
            }
        } else {
            // AI: Güvenli bir komşu kare bulmak için yönleri karıştırıp dener.
            List<Direction> dirs = Arrays.asList(Direction.values());
            Collections.shuffle(dirs);
            boolean found = false;
            for (Direction d : dirs) {
                int candR = row;
                int candC = col;
                switch (d) {
                    case UP:
                        candR--;
                        break;
                    case DOWN:
                        candR++;
                        break;
                    case LEFT:
                        candC--;
                        break;
                    case RIGHT:
                        candC++;
                        break;
                }
                if (!terrain.isValidPosition(candR, candC)) {
                    continue;
                }
                Cell target = terrain.getCell(candR, candC);
                boolean blocked = target.getObjects().stream()
                        .anyMatch(obj -> obj instanceof entities.Hazard || obj instanceof Penguin);
                if (!blocked) {
                    dr = candR - row;
                    dc = candC - col;
                    found = true;
                    break;
                }
            }
            if (!found) {
                System.out.println("No safe adjacent square found. Action wasted.");
                return;
            }
        }

        int nextRow = row + dr;
        int nextCol = col + dc;

        // 1. Harita dışına çıkıyor mu?
        if (!terrain.isValidPosition(nextRow, nextCol)) {
            System.out.println("Oops! " + name + " stepped into the water using special action!");
            die(terrain);
            return;
        }

        Cell targetCell = terrain.getCell(nextRow, nextCol);

        // 2. Hedef Dolu mu? (Penguen veya Tehlike varsa gidemez)
        if (!targetCell.isEmpty()) {
            boolean hasObstacle = targetCell.getObjects().stream()
                    .anyMatch(obj -> obj instanceof entities.Hazard ||
                            obj instanceof Penguin);

            if (hasObstacle) {
                System.out.println("Cannot move there, tile is occupied. Action wasted.");
                return;
            }
        }

        // 3. ADIM AT (Kayma değil)
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
