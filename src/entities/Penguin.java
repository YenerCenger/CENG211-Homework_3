package entities;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import core.Cell;
import core.IcyTerrain;
import enums.Direction;
import interfaces.ICollidable;
import interfaces.ICollidable.CollisionResult;
import interfaces.ISlidable;
import interfaces.ITerrainObject;
import entities.hazards.HoleInIce;

public abstract class Penguin implements ITerrainObject, ISlidable {
    protected int row;
    protected int col;
    protected String name; // P1, P2, P3
    protected int score; // Toplanan yemek puanı
    protected boolean isAlive;
    protected boolean isStunned = false;

    // ÖZEL YETENEK DEĞİŞKENLERİ
    protected boolean canJump = false; // Rockhopper için
    protected int stopAtStep = -1; // King (5) ve Emperor (3) için. -1 ise kapalı.

    // Toplanan yemekler
    protected List<Food> collectedFoods = new ArrayList<>();

    public Penguin(String name) {
        this.name = name;
        this.score = 0;
        this.isAlive = true;
    }

    @Override
    public String getSymbol() {
        return name;
    }

    @Override
    public int getRow() {
        return row;
    }

    @Override
    public int getCol() {
        return col;
    }

    public int getScore() {
        return score;
    }

    public List<Food> getCollectedFoods() {
        return new ArrayList<>(collectedFoods);
    }

    public boolean isAlive() {
        return isAlive;
    }

    @Override
    public void setPosition(int row, int col) {
        this.row = row;
        this.col = col;
    }

    // Yeme işlemi
    protected void eat(Food food, IcyTerrain terrain) {
        System.out.println(name + " ate " + food.getSymbol() + " (" + food.getWeight() + " units).");
        // Yemeği haritadan sil
        terrain.getCell(row, col).removeObject(food);
        // Puanı ve listeyi güncelle
        this.score += food.getWeight();
        this.collectedFoods.add(food);
        System.out.println("Total Score: " + score);
    }

    // Ölme işlemi (Suya düşme veya Deliğe girme)
    public void die(IcyTerrain terrain) {
        this.isAlive = false;
        // Haritadan tamamen sil (Eğer hala listedeyse)
        terrain.getCell(row, col).removeObject(this);
        System.out.println(name + " is removed from the game.");
    }

    // Aşırı yükleme (Override) hatası almamak için parametresiz versiyonu da
    // tutabilirsin
    public void die() {
        this.isAlive = false;
        System.out.println(name + " is removed from the game.");
    }

    // Ceza: En hafif yemeği kaybet (HeavyIceBlock için)
    public void loseLightestFood() {
        if (collectedFoods.isEmpty()) {
            System.out.println(name + " has no food to lose.");
            return;
        }

        Food lightest = collectedFoods.get(0);
        for (Food f : collectedFoods) {
            if (f.getWeight() < lightest.getWeight()) {
                lightest = f;
            }
        }

        collectedFoods.remove(lightest);
        this.score -= lightest.getWeight();
        System.out.println(
                name + " lost food: " + lightest.getSymbol() + " (" + lightest.getWeight() + " units) as penalty.");
    }

    // Her penguenin özel yeteneği farklıdır (Abstract)
    public abstract void performSpecialAction(IcyTerrain terrain);

    @Override
    public ISlidable.SlideResult slide(Direction direction, IcyTerrain terrain) {
        return slide(direction, terrain, ISlidable.MAX_SLIDE_STEPS);
    }

    @Override
    public ISlidable.SlideResult slide(Direction direction, IcyTerrain terrain, int stepsRemaining) {
        if (!isAlive)
            return ISlidable.SlideResult.stopped("dead");

        if (stepsRemaining <= 0)
            return ISlidable.SlideResult.stopped("max-steps");

        // 1. DURMA YETENEĞİ KONTROLÜ (King & Emperor)
        boolean willStopAfterThisMove = false;
        if (stopAtStep > 0) {
            stopAtStep--; // bu adım sayıldı
            willStopAfterThisMove = (stopAtStep == 0);
        } else if (stopAtStep == 0) {
            System.out.println(name + " stopped early due to special action.");
            stopAtStep = -1; // Yeteneği sıfırla
            return ISlidable.SlideResult.stopped("special-stop");
        }

        // 2. HEDEF HESAPLA
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

        // 3. SUYA DÜŞME KONTROLÜ
        if (!terrain.isValidPosition(nextRow, nextCol)) {
            System.out.println(name + " fell into the water!");
            die(terrain);
            return ISlidable.SlideResult.stopped("water");
        }

        Cell targetCell = terrain.getCell(nextRow, nextCol);

        // 4. ENGEL KONTROLÜ
        boolean hasObstacle = targetCell.getObjects().stream()
                .anyMatch(obj -> obj instanceof Hazard ||
                        obj instanceof Penguin);

        if (hasObstacle) {
            // *** ROCKHOPPER ZIPLAMA ***
            if (this.canJump) {
                System.out.println(name + " attempts to JUMP over the obstacle...");

                int jumpRow = nextRow;
                int jumpCol = nextCol;
                switch (direction) {
                    case UP:
                        jumpRow--;
                        break;
                    case DOWN:
                        jumpRow++;
                        break;
                    case LEFT:
                        jumpCol--;
                        break;
                    case RIGHT:
                        jumpCol++;
                        break;
                }

                // Zıplanacak yer güvenli mi?
                if (terrain.isValidPosition(jumpRow, jumpCol) && terrain.getCell(jumpRow, jumpCol).isEmpty()) {
                    System.out.println("   -> JUMP SUCCESSFUL! Landed on (" + jumpRow + ", " + jumpCol + ")");

                    terrain.getCell(row, col).removeObject(this);
                    this.row = jumpRow;
                    this.col = jumpCol;
                    terrain.getCell(row, col).addObject(this);

                    this.canJump = false; // Yetenek kullanıldı

                    // İndiği yerde yemek var mı?
                    Food f = terrain.getCell(row, col).getFirstObject(Food.class);
                    if (f != null)
                        eat(f, terrain);

                    return slide(direction, terrain, stepsRemaining - 1); // Devam et
                } else {
                    System.out.println("   -> Jump FAILED! Target square not empty or water.");
                    this.canJump = false; // Yetenek boşa gitti
                }
            }

            // *** NORMAL ÇARPIŞMA ***
            ITerrainObject obstacle = targetCell.getObjects().stream()
                    .filter(o -> o instanceof ICollidable)
                    .findFirst().orElse(null);
            if (obstacle != null) {
                ICollidable collidable = (ICollidable) obstacle;
                CollisionResult result = collidable.onCollision(this, terrain);
                if (result == null || !result.shouldContinue()) {
                    return ISlidable.SlideResult.stopped("collision-stop");
                }

                // Çarpışma yeni bir yön gerektiriyorsa, mevcut kareden o yönde kaymaya yeniden başla.
                if (result.getOverrideDirection() != null) {
                    return slide(result.getOverrideDirection(), terrain, stepsRemaining - 1);
                }
                // Aksi halde aynı yönle devam edip hedef kareye ilerleyecek.
            } else {
                System.out.println(name + " bumped into an obstacle and stopped.");
                return ISlidable.SlideResult.stopped("obstacle");
            }
        }

        // 5. HAREKET ET
        System.out.println("   -> " + name + " slid to (" + nextRow + ", " + nextCol + ")");
        terrain.getCell(row, col).removeObject(this);
        this.row = nextRow;
        this.col = nextCol;
        targetCell.addObject(this);

        // 6. YEMEK YE
        Food food = targetCell.getFirstObject(Food.class);
        if (food != null) {
            eat(food, terrain);
            return ISlidable.SlideResult.stopped("ate");
        }

        // Özel durdurma bu adım sonrası tetiklenmişse burada dur.
        if (willStopAfterThisMove) {
            System.out.println(name + " stopped early due to special action.");
            stopAtStep = -1;
            return ISlidable.SlideResult.stopped("special-stop");
        }

        // 7. DEVAM ET
        return slide(direction, terrain, stepsRemaining - 1);
    }

    // Stun (Sersemletme) Durumu
    public void stun() {
        this.isStunned = true;
        System.out.println(name + " is stunned! Next turn will be skipped.");
    }

    public boolean isStunned() {
        return isStunned;
    }

    public void recover() {
        if (isStunned) {
            isStunned = false;
            System.out.println(name + " recovered from stun.");
        }
    }

    // AI Yön Seçimi
    public Direction chooseDirectionAI(IcyTerrain terrain) {
        Direction[] directions = Direction.values();
        List<Direction> toFood = new ArrayList<>();
        List<Direction> toSafeSpace = new ArrayList<>();
        List<Direction> toHazardNonHole = new ArrayList<>();
        List<Direction> toWater = new ArrayList<>();

        for (Direction d : directions) {
            Object result = scanDirection(d, terrain);
            if (result instanceof Food) {
                toFood.add(d);
            } else if (result instanceof Hazard) {
                if (result instanceof HoleInIce) {
                    toHazardNonHole.add(d); // HoleInIce de kaçınılacak
                } else {
                    toHazardNonHole.add(d);
                }
            } else if ("WATER".equals(result)) {
                toWater.add(d);
            } else {
                toSafeSpace.add(d);
            }
        }

        Random rng = new Random();
        if (!toFood.isEmpty())
            return toFood.get(rng.nextInt(toFood.size()));
        if (!toSafeSpace.isEmpty())
            return toSafeSpace.get(rng.nextInt(toSafeSpace.size()));
        if (!toHazardNonHole.isEmpty())
            return toHazardNonHole.get(rng.nextInt(toHazardNonHole.size()));
        if (!toWater.isEmpty())
            return toWater.get(rng.nextInt(toWater.size()));
        return directions[rng.nextInt(directions.length)];
    }

    private Object scanDirection(Direction d, IcyTerrain terrain) {
        int r = row;
        int c = col;
        while (true) {
            switch (d) {
                case UP:
                    r--;
                    break;
                case DOWN:
                    r++;
                    break;
                case LEFT:
                    c--;
                    break;
                case RIGHT:
                    c++;
                    break;
            }
            if (!terrain.isValidPosition(r, c))
                return "WATER";
            Cell cell = terrain.getCell(r, c);
            if (!cell.isEmpty()) {
                Food f = cell.getFirstObject(Food.class);
                if (f != null)
                    return f;
                ITerrainObject h = cell.getFirstObject(Hazard.class);
                if (h != null)
                    return h;
                return "OBSTACLE";
            }
        }
    }
}
