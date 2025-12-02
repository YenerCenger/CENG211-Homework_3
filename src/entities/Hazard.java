package entities;

import enums.HazardType;
import interfaces.ICollidable;
import interfaces.ITerrainObject;

public abstract class Hazard implements ITerrainObject, ICollidable {
    protected int row, col;
    protected HazardType type;

    public Hazard(HazardType type) {
        this.type = type;
    }

    // ... (Diğer getter/setter metodları aynı kalacak, elleme) ...

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