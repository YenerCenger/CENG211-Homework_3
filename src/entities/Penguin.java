package entities;

import core.IcyTerrain;
import enums.Direction;
import interfaces.ISlidable;
import interfaces.ITerrainObject;

public abstract class Penguin implements ITerrainObject, ISlidable {
    protected java.util.List<Food> collectedFoods = new java.util.ArrayList<>();
    protected int row;
    protected int col;
    protected String name; // P1, P2, P3
    protected int score; // Toplanan yemek puanı
    protected boolean isAlive = true;

    public Penguin(String name) {
        this.name = name;
        this.score = 0;
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

    public boolean getIsAlive() {
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

    // Kayma hareketi
    @Override
    public void slide(Direction direction, IcyTerrain terrain) {
        if (!isAlive)
            return; // Ölü penguenler kayamaz

        // 1. Hedef koordinatı hesapla
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

        // 2. SUYA DÜŞME KONTROLÜ (Harita dışına çıkıyor mu?)
        if (!terrain.isValidPosition(nextRow, nextCol)) {
            System.out.println(name + " fell into the water!");
            die(terrain); // Öldür
            return;
        }

        // 3. HEDEF KARE KONTROLÜ
        core.Cell targetCell = terrain.getCell(nextRow, nextCol);

        // Engel Var mı? (Tehlike veya Başka Penguen)
        // Not: Food bir engel değildir, üstünden geçilir veya durulur.
        boolean hasObstacle = targetCell.getObjects().stream()
                .anyMatch(obj -> obj instanceof entities.Hazard ||
                        obj instanceof entities.Penguin);

        if (hasObstacle) {
            // a) Çarpılan nesneyi bul
            ITerrainObject obstacle = targetCell.getObjects().stream()
                    .filter(o -> o instanceof entities.Hazard
                            || o instanceof entities.Penguin)
                    .findFirst()
                    .orElse(null);

            // b) Eğer nesne ICollidable (Çarpışılabilir) ise özel kuralını çalıştır
            if (obstacle instanceof interfaces.ICollidable) {
                interfaces.ICollidable collidable = (interfaces.ICollidable) obstacle;
                boolean canContinue = collidable.onCollision(this, terrain);

                // Eğer onCollision 'false' dönerse dur (Örn: HeavyIceBlock, HoleInIce)
                if (!canContinue)
                    return;

                // Eğer 'true' dönerse (Örn: İleride SeaLion sekince true dönebilir) devam et.
            } else {
                // ICollidable değilse (Örn: Başka bir Penguen - şimdilik) küt diye dur.
                // (İleride buraya "momentum transferi" kuralı eklenecek)
                System.out.println(name + " bumped into " + obstacle.getSymbol() + " and stopped.");
                return;
            }
        }

        // 4. HAREKET ET (Eski kareden çık, yeniye gir)
        terrain.getCell(row, col).removeObject(this); // Eski kareden sil
        this.row = nextRow;
        this.col = nextCol;
        targetCell.addObject(this); // Yeni kareye ekle

        // 5. YEMEK VAR MI?
        Food food = targetCell.getFirstObject(Food.class);
        if (food != null) {
            // Yemek varsa dur ve ye
            eat(food, terrain);
            return; // Kayma biter
        }

        // 6. DEVAM ET (Recursion)
        // Önü boşsa ve yemek yoksa kaymaya devam et
        slide(direction, terrain);
    }

    protected boolean isStunned = false;

    public void stun() {
        this.isStunned = true;
        System.out.println(name + " is stunned! Next turn will be skipped.");
    }

    public boolean isStunned() {
        return isStunned;
    }

    // Stun durumunu sıfırlamak için (Tur başında çağrılır)
    public void recover() {
        if (isStunned) {
            isStunned = false;
            System.out.println(name + " recovered from stun.");
        }
    }

    public Direction chooseDirectionAI(IcyTerrain terrain) {
        // 4 Yönü Kontrol Et
        Direction[] directions = Direction.values();

        // Öncelik Listeleri
        java.util.List<Direction> toFood = new java.util.ArrayList<>();
        java.util.List<Direction> toSafeSpace = new java.util.ArrayList<>();
        java.util.List<Direction> toHazard = new java.util.ArrayList<>();
        java.util.List<Direction> toWater = new java.util.ArrayList<>();

        for (Direction d : directions) {
            // O yöne bakınca ne görüyoruz?
            Object result = scanDirection(d, terrain);

            if (result instanceof Food) {
                toFood.add(d);
            } else if (result instanceof entities.hazards.HoleInIce) {
                toWater.add(d); // Deliği su gibi tehlikeli sayalım
            } else if (result instanceof entities.Hazard) {
                toHazard.add(d);
            } else if (result.equals("WATER")) {
                toWater.add(d);
            } else {
                toSafeSpace.add(d); // Boş alan veya güvenli engel
            }
        }

        // KARAR MEKANİZMASI (Ödev Sıralaması)
        java.util.Random rng = new java.util.Random();

        // 1. Yemek varsa oraya git
        if (!toFood.isEmpty()) {
            return toFood.get(rng.nextInt(toFood.size()));
        }
        // 2. Güvenli bir yer varsa oraya git (Suya ve Hazarda gitme)
        if (!toSafeSpace.isEmpty()) {
            return toSafeSpace.get(rng.nextInt(toSafeSpace.size()));
        }
        // 3. Mecbursa Hazarda git (Hole hariç)
        if (!toHazard.isEmpty()) {
            return toHazard.get(rng.nextInt(toHazard.size()));
        }
        // 4. Çare yoksa rastgele git (Suya düşebilir)
        return directions[rng.nextInt(directions.length)];
    }

    // YARDIMCI METOT: Bir yöne bakınca ilk ne var?
    private Object scanDirection(Direction d, IcyTerrain terrain) {
        int r = row;
        int c = col;

        // Yönde 1 adım ilerle (Sadece bitişiği değil, yolun sonunu görmek gerekebilir
        // ama AI genelde "ilk gördüğüne" bakar. Basit simülasyon yapalım:
        // O yöndeki ilk dolu kareyi veya harita sınırını bul.

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

            // Suya mı düştü?
            if (!terrain.isValidPosition(r, c))
                return "WATER";

            core.Cell cell = terrain.getCell(r, c);

            // Bir şey var mı?
            if (!cell.isEmpty()) {
                // Yemek var mı?
                Food f = cell.getFirstObject(Food.class);
                if (f != null)
                    return f;

                // Tehlike var mı?
                entities.Hazard h = cell.getFirstObject(entities.Hazard.class);
                if (h != null)
                    return h;

                // Başka penguen? (Engel sayılır)
                Penguin p = cell.getFirstObject(Penguin.class);
                if (p != null)
                    return "OBSTACLE";
            }
            // Boşsa döngü devam eder (Kaymaya devam edeceği için ilerisine bakıyoruz)
        }
    }
}
