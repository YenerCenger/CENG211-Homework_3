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
import interfaces.ISlidable;

public class IcyTerrain {
    private ArrayList<ArrayList<Cell>> map; // 2D Grid
    private List<Penguin> penguins;
    private final int ROWS = 10;
    private final int COLS = 10;
    private Random random;
    private final Scanner input;
    private Penguin playerPenguin;

    public IcyTerrain() {
        this.map = new ArrayList<>();
        this.penguins = new ArrayList<>();
        this.random = new Random();
        this.input = new Scanner(System.in); // paylaşımlı giriş kaynağı

        initializeEmptyGrid();
        initializeGameObjects(); // Nesneleri yerleştir
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
        System.out.println("Welcome to Sliding Penguins Puzzle Game App. An 10x10 icy terrain grid is being generated.");
    }

    // *** ANA METOT: TÜM NESNELERİ YERLEŞTİRİR ***
    private void initializeGameObjects() {
        System.out.println("Penguins, Hazards, and Food items are also being generated.");

        generatePenguins();
        generateHazards();
        generateFoods();
        // Başlangıç özetini göster
        printInitialSummary();
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

    public Penguin getPlayerPenguin() {
        return playerPenguin;
    }

    public void addObjectToGrid(ITerrainObject object, int row, int col) {
        if (isValidPosition(row, col)) {
            object.setPosition(row, col);
            getCell(row, col).addObject(object);
        }
    }

    // Başlangıç gridini ve legend'i yazdır
    public void printInitialSummary() {
        System.out.println("\nInitial icy terrain grid:");
        printTerrain();
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
        // Rastgele bir pengueni oyuncuya ata
        playerPenguin = penguins.get(random.nextInt(penguins.size()));
        System.out.println("\nThese are the penguins on the icy terrain:");
        for (Penguin p : penguins) {
            System.out.println("- " + p.getSymbol() + " (" + p.getClass().getSimpleName() + ")" + (p == playerPenguin ? " ---> YOUR PENGUIN" : ""));
        }

        int maxTurns = 4; // Her penguenin 4 turu var [cite: 18]

        for (int turn = 1; turn <= maxTurns; turn++) {
            System.out.println("\n*** Turn " + turn);

            // Sırayla her penguen oynar (P1 -> P2 -> P3) [cite: 101]
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
                boolean isPlayer = (p == playerPenguin);

                // 1. ÖZEL GÜÇ KULLANIMI
                if (isPlayer) {
                    // DOĞRU GİRİŞ YAPILANA KADAR SOR (WHILE DÖNGÜSÜ)
                    boolean validInput = false;
                    while (!validInput) {
                        System.out.print("Will " + p.getSymbol() + " use its special action? (Y/N) --> ");
                        String inputStr = input.next().toUpperCase();
                        if (inputStr.equals("Y")) {
                            p.performSpecialAction(this);
                            validInput = true;
                        } else if (inputStr.equals("N")) {
                            validInput = true;
                        } else {
                            System.out.println("Invalid input! Please enter Y or N.");
                        }
                    }
                } else {
                    // AI: %30 şansla kullan [cite: 96]
                    if (random.nextInt(100) < 30) {
                        System.out.println(p.getSymbol() + " chooses to USE its special action.");
                        p.performSpecialAction(this);
                    } else {
                        System.out.println(p.getSymbol() + " does NOT use its special action.");
                    }
                }

                // 2. YÖN SEÇİMİ
                Direction moveDir = null;
                if (isPlayer) {
                    // DOĞRU GİRİŞ YAPILANA KADAR SOR (WHILE DÖNGÜSÜ)
                    while (moveDir == null) {
                        System.out.print("Which direction will " + p.getSymbol() + " move? (U/D/L/R) --> ");
                        String dStr = input.next().toUpperCase();
                        switch (dStr) {
                            case "U":
                                moveDir = Direction.UP;
                                break;
                            case "D":
                                moveDir = Direction.DOWN;
                                break;
                            case "L":
                                moveDir = Direction.LEFT;
                                break;
                            case "R":
                                moveDir = Direction.RIGHT;
                                break;
                            default:
                                System.out.println("Invalid input! Please enter U, D, L, or R.");
                                // moveDir hala null olduğu için döngü başa döner
                        }
                    }
                } else {
                    // AI Karar versin
                    moveDir = p.chooseDirectionAI(this);
                    System.out.println(p.getSymbol() + " chooses to move " + moveDir);
                }

                // 3. HAREKETİ BAŞLAT
                ISlidable.SlideResult slideResult = p.slide(moveDir, this);
                if (slideResult != null && "max-steps".equals(slideResult.getReason())) {
                    System.out.println(p.getSymbol() + " stopped due to max slide steps guard.");
                }

                // Her hamleden sonra haritayı göster
                printTerrain();
            }
        }

        // OYUN BİTTİ - SKOR TABLOSU
        showLeaderboard();

        // Oyun bittiğinde giriş kaynağını kapat.
        input.close();
    }

    public Scanner getInput() {
        return input;
    }

    private void showLeaderboard() {
        System.out.println("\n***** GAME OVER *****");
        System.out.println("***** SCOREBOARD FOR THE PENGUINS *****");

        // Skorları sırala (Yüksekten düşüğe)
        penguins.sort((p1, p2) -> Integer.compare(p2.getScore(), p1.getScore()));

        int rank = 1;
        for (Penguin p : penguins) {
            System.out.println(rank + ". place: " + p.getSymbol());
            List<entities.Food> foods = p.getCollectedFoods();
            if (foods.isEmpty()) {
                System.out.println("|--> Food items: none");
            } else {
                System.out.print("|--> Food items: ");
                for (int i = 0; i < foods.size(); i++) {
                    entities.Food f = foods.get(i);
                    System.out.print(f.getSymbol() + " (" + f.getWeight() + " units)");
                    if (i < foods.size() - 1) {
                        System.out.print(", ");
                    }
                }
                System.out.println();
            }
            System.out.println("|--> Total weight: " + p.getScore() + " units\n");
            rank++;
        }
    }
}
