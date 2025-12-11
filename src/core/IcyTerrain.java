package core;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import entities.Food;
import entities.Hazard;
import entities.Penguin;
import entities.hazards.HeavyIceBlock;
import entities.hazards.HoleInIce;
import entities.hazards.LightIceBlock;
import entities.hazards.SeaLion;
import entities.penguins.EmperorPenguin;
import entities.penguins.KingPenguin;
import entities.penguins.RockhopperPenguin;
import entities.penguins.RoyalPenguin;
import enums.Direction;
import enums.FoodType;
import enums.HazardType;
import enums.PenguinType;
import interfaces.ITerrainObject;

public class IcyTerrain {
    private ArrayList<ArrayList<Cell>> map; // 2D Grid
    private List<Penguin> penguins;
    private final int ROWS = 10;
    private final int COLS = 10;
    private Random random;
    private Scanner scanner;

    public IcyTerrain() {
        this.map = new ArrayList<>();
        this.penguins = new ArrayList<>();
        this.random = new Random();
        this.scanner = new Scanner(System.in);

        initializeEmptyGrid();
        initializeGameObjects(); // Nesneleri yerleştir
    }

    // RoyalPenguin veya diğer sınıfların input alabilmesi için yardımcı metot:
    public String askForInput(String message) {
        System.out.print(message);
        return scanner.next().toUpperCase();
    }
    // 10x10'luk boş hücreleri oluşturur
    private void initializeEmptyGrid() {
        for (int i = 0; i < ROWS; i++) {
            ArrayList<Cell> row = new ArrayList<>();
            for (int j = 0; j < COLS; j++) {
                row.add(new Cell());
            }
            map.add(row);
        }
        System.out.println("An 10x10 icy terrain grid is being generated.");
    }

    // *** ANA METOT: TÜM NESNELERİ YERLEŞTİRİR ***
    private void initializeGameObjects() {
        System.out.println("Penguins, Hazards, and Food items are also being generated.");

        generatePenguins();
        generateHazards();
        generateFoods();
    }

    // 1. PENGUENLERİ OLUŞTUR (3 Adet, Kenarlara)
    private void generatePenguins() {
        String[] names = { "P1", "P2", "P3" };

        for (String name : names) {
            Penguin penguin = createRandomPenguin(name);
            boolean placed = false;

            while (!placed) {
                // Rastgele bir kenar koordinatı seç [cite: 26]
                int[] pos = getRandomEdgePosition();
                int r = pos[0];
                int c = pos[1];

                // Eğer o karede başka penguen yoksa yerleştir [cite: 27]
                if (getCell(r, c).isEmpty()) {
                    addObjectToGrid(penguin, r, c);
                    penguins.add(penguin);
                    placed = true;
                }
            }
        }
    }

    // 2. TEHLİKELERİ OLUŞTUR (15 Adet)
    private void generateHazards() {
        for (int i = 0; i < 15; i++) { // [cite: 28]
            Hazard hazard = createRandomHazard();
            boolean placed = false;

            while (!placed) {
                int r = random.nextInt(ROWS);
                int c = random.nextInt(COLS);

                // Tehlikeler penguenlerle aynı karede olamaz [cite: 29]
                // Ayrıca başka bir tehlikenin üstüne binmesin (basitlik için)
                if (getCell(r, c).isEmpty()) {
                    addObjectToGrid(hazard, r, c);
                    placed = true;
                }
            }
        }
    }

    // 3. YEMEKLERİ OLUŞTUR (20 Adet)
    private void generateFoods() {
        for (int i = 0; i < 20; i++) { // [cite: 30]
            Food food = createRandomFood();
            boolean placed = false;

            while (!placed) {
                int r = random.nextInt(ROWS);
                int c = random.nextInt(COLS);
                Cell cell = getCell(r, c);

                // Yemekler tehlikelerle aynı karede olamaz [cite: 30]
                // Ama Penguenlerle olabilir [cite: 31] (fakat şu an başlangıçtayız, boş yer
                // tercih edelim)
                boolean hasHazard = cell.getObjects().stream().anyMatch(o -> o instanceof Hazard);
                boolean hasFood = cell.getObjects().stream().anyMatch(o -> o instanceof Food);

                if (!hasHazard && !hasFood) {
                    addObjectToGrid(food, r, c);
                    placed = true;
                }
            }
        }
    }

    private Penguin createRandomPenguin(String name) {
        PenguinType[] types = PenguinType.values();
        PenguinType selectedType = types[random.nextInt(types.length)]; // [cite: 38]

        switch (selectedType) {
            case KING:
                return new KingPenguin(name);
            case EMPEROR:
                return new EmperorPenguin(name);
            case ROYAL:
                return new RoyalPenguin(name);
            case ROCKHOPPER:
                return new RockhopperPenguin(name);
            default:
                return new KingPenguin(name);
        }
    }

    private Hazard createRandomHazard() {
        HazardType[] types = HazardType.values();
        HazardType selectedType = types[random.nextInt(types.length)]; // [cite: 63]

        switch (selectedType) {
            case HEAVY_ICE_BLOCK:
                return new HeavyIceBlock();
            case HOLE_IN_ICE:
                return new HoleInIce();
            case LIGHT_ICE_BLOCK:
                return new LightIceBlock();
            case SEA_LION:
                return new SeaLion();
            default:
                return new HeavyIceBlock();
        }
    }

    private Food createRandomFood() {
        FoodType[] types = FoodType.values();
        FoodType selectedType = types[random.nextInt(types.length)]; // [cite: 58]
        return new Food(selectedType);
    }

    // Kenar koordinatı üretir (0. satır, 9. satır, 0. sütun veya 9. sütun)
    private int[] getRandomEdgePosition() {
        if (random.nextBoolean()) {
            // Sabit Satır (0 veya 9), Rastgele Sütun
            int r = random.nextBoolean() ? 0 : ROWS - 1;
            int c = random.nextInt(COLS);
            return new int[] { r, c };
        } else {
            // Sabit Sütun (0 veya 9), Rastgele Satır
            int r = random.nextInt(ROWS);
            int c = random.nextBoolean() ? 0 : COLS - 1;
            return new int[] { r, c };
        }
    }

    public Cell getCell(int row, int col) {
        if (isValidPosition(row, col)) {
            return map.get(row).get(col);
        }
        return null;
    }

    public boolean isValidPosition(int row, int col) {
        return row >= 0 && row < ROWS && col >= 0 && col < COLS;
    }

    public void addObjectToGrid(ITerrainObject object) {
        if (isValidPosition(object.getRow(), object.getCol())) {
            getCell(object.getRow(), object.getCol()).addObject(object);
        }
    }

    public void addObjectToGrid(ITerrainObject object, int row, int col) {
        if (isValidPosition(row, col)) {
            object.setPosition(row, col);
            getCell(row, col).addObject(object);
        }
    }

    public void printTerrain() {
        System.out.println("\nCurrent Grid State:");
        for (ArrayList<Cell> row : map) {
            // Üst çizgi
            System.out.println("-----------------------------------------");
            System.out.print("|"); // Satır başı
            for (Cell cell : row) {
                // Hücre içeriğini ortala veya sola yasla (3 karakterlik alan)
                String content = cell.isEmpty() ? "   " : String.format("%-3s", cell.toString());
                System.out.print(content + "|");
            }
            System.out.println(); // Alt satıra geç
        }
        System.out.println("-----------------------------------------");
    }

    public void startGame() {
        // Yerel scanner tanımı SİLİNDİ (Sınıfın en tepesinde tanımladığımız this.scanner kullanılacak)

        // Rastgele bir pengueni oyuncuya ata (İndeks 0, 1 veya 2)
        int randomIndex = random.nextInt(penguins.size());
        Penguin playerPenguin = penguins.get(randomIndex);
        playerPenguin.setPlayer(true); // Pengueni oyuncu olarak işaretle
        
        System.out.println("\nPenguin " + playerPenguin.getSymbol() + ": " + playerPenguin.getClass().getSimpleName() + " ---> YOUR PENGUIN");

        System.out.println("The initial icy terrain grid:");
        printTerrain(); // OYUN BAŞLAMADAN HARİTAYI GÖSTER

        int maxTurns = 4; // Her penguenin 4 turu var

        for (int turn = 1; turn <= maxTurns; turn++) {
            System.out.println("\n*** Turn " + turn);

            // Sırayla her penguen oynar (P1 -> P2 -> P3)
            for (Penguin p : penguins) {
                if (!p.isAlive())
                    continue; // Ölüler oynayamaz

                // Stun kontrolü
                if (p.isStunned()) {
                    p.recover(); // Stun kaldırılır ama bu turu yanar
                    continue;
                }

                System.out.println("\n" + p.getSymbol() + "'s turn:");

                // OYUNCU MU, AI MI?
                // DİKKAT: Artık isimle değil, isPlayer() ile kontrol ediyoruz
                boolean isPlayer = p.isPlayer(); 

                // 1. ÖZEL GÜÇ KULLANIMI
                if (isPlayer) {
                    // DOĞRU GİRİŞ YAPILANA KADAR SOR (WHILE DÖNGÜSÜ)
                    boolean validInput = false;
                    while (!validInput) {
                        String input = askForInput("Will " + p.getSymbol() + " use its special action? (Y/N) --> ");
                        if (input.equals("Y")) {
                            p.performSpecialAction(this);
                            validInput = true;
                        } else if (input.equals("N")) {
                            validInput = true;
                        } else {
                            System.out.println("Invalid input! Please enter Y or N.");
                        }
                    }
                } else {
                    // --- AI MANTIĞI (DÜZELTİLMİŞ HALİ) ---
                    boolean shouldUseSpecial = false;

                    // Kural: Rockhopper tehlikeye gidiyorsa OTOMATİK kullanır
                    if (p instanceof entities.penguins.RockhopperPenguin) {
                        // 1. Önce AI'nın nereye gitmek istediğine bakalım
                        enums.Direction intendedDir = p.chooseDirectionAI(this);
                        
                        // 2. O yönde ne var?
                        int checkR = p.getRow();
                        int checkC = p.getCol();
                        switch (intendedDir) {
                            case UP: checkR--; break;
                            case DOWN: checkR++; break;
                            case LEFT: checkC--; break;
                            case RIGHT: checkC++; break;
                        }

                        // 3. Eğer o yönde Tehlike (Hazard) varsa yeteneği aç
                        if (isValidPosition(checkR, checkC)) {
                            Cell cell = getCell(checkR, checkC);
                            boolean hasHazard = cell.getObjects().stream().anyMatch(o -> o instanceof entities.Hazard);
                            boolean isHole = cell.getObjects().stream().anyMatch(o -> o instanceof entities.hazards.HoleInIce);
                            
                            // Delik değilse ve tehlikeyse zıpla
                            if (hasHazard && !isHole) {
                                shouldUseSpecial = true;
                            }
                        }
                        
                        // Eğer tehlike yoksa yine de %30 şansını deneyebilir
                        if (!shouldUseSpecial && random.nextInt(100) < 30) {
                            shouldUseSpecial = true;
                        }

                    } else {
                        // Diğer penguenler için standart %30 kuralı
                        if (random.nextInt(100) < 30) {
                            shouldUseSpecial = true;
                        }
                    }

                    if (shouldUseSpecial) {
                        System.out.println(p.getSymbol() + " chooses to USE its special action.");
                        p.performSpecialAction(this);
                    } else {
                        System.out.println(p.getSymbol() + " does NOT use its special action.");
                    }
                }

                // 2. YÖN SEÇİMİ
                enums.Direction moveDir = null;
                if (isPlayer) {
                    // DOĞRU GİRİŞ YAPILANA KADAR SOR (WHILE DÖNGÜSÜ)
                    while (moveDir == null) {
                        String dStr = askForInput("Which direction will " + p.getSymbol() + " move? (U/D/L/R) --> ");
                        switch (dStr) {
                            case "U": moveDir = enums.Direction.UP; break;
                            case "D": moveDir = enums.Direction.DOWN; break;
                            case "L": moveDir = enums.Direction.LEFT; break;
                            case "R": moveDir = enums.Direction.RIGHT; break;
                            default:
                                System.out.println("Invalid input! Please enter U, D, L, or R.");
                        }
                    }
                } else {
                    // AI Karar versin
                    moveDir = p.chooseDirectionAI(this);
                    System.out.println(p.getSymbol() + " chooses to move " + moveDir);
                }

                // 3. HAREKETİ BAŞLAT
                p.slide(moveDir, this);

                // Her hamleden sonra haritayı göster
                printTerrain();
            }
        }

        // OYUN BİTTİ - SKOR TABLOSU
        showLeaderboard();
    }

    private void showLeaderboard() {
        System.out.println("\nGAME OVER");
        System.out.println("SCOREBOARD FOR THE PENGUINS *****");
        // Skorları sırala (Yüksekten düşüğe)
        penguins.sort((p1, p2) -> Integer.compare(p2.getScore(), p1.getScore())); // getScore() eklemelisin

        int rank = 1;
        for (Penguin p : penguins) {
            System.out.println(rank + ". place: " + p.getSymbol() + " - Total Score: " + p.getScore());
            rank++;
        }
    }
}
