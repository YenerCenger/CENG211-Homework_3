package entities;

import java.util.Random;
import enums.FoodType;
import interfaces.ITerrainObject;

public class Food implements ITerrainObject {
    private int row;
    private int col;
    private FoodType type;
    private int weight; // 1-5 birim arası rastgele

    public Food(FoodType type) {
        this.type = type;
        // 1 ile 5 arasında rastgele ağırlık ata
        this.weight = new Random().nextInt(5) + 1;
    }

    public int getWeight() {
        return weight;
    }

    @Override
    public String getSymbol() {
        // Enum'daki kısa adı döndür (Örn: "Kr", "Sq")
        return type.getShortName();
    }

    @Override
    public int getRow() {
        return row;
    }

    @Override
    public int getCol() {
        return col;
    }

    @Override
    public void setPosition(int row, int col) {
        this.row = row;
        this.col = col;
    }
}