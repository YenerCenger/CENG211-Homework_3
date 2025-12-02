package entities.penguins;

import java.util.Scanner;

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
            Scanner scanner = new Scanner(System.in);
            System.out.print("Choose 1-step direction (U/D/L/R) --> ");
            String input = scanner.next().toUpperCase();
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
            // AI (Rastgele geçerli bir yön seçmeye çalışır)
            // Basitçe rastgele bir yön deneyelim
            int randomDir = new java.util.Random().nextInt(4);
            switch (randomDir) {
                case 0:
                    dr = -1;
                    break; // UP
                case 1:
                    dr = 1;
                    break; // DOWN
                case 2:
                    dc = -1;
                    break; // LEFT
                case 3:
                    dc = 1;
                    break; // RIGHT
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