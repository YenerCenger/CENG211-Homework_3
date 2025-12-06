package entities.hazards;

import core.IcyTerrain;
import entities.Hazard;
import entities.Penguin;
import enums.HazardType;
import interfaces.ICollidable;

public class HoleInIce extends Hazard {
    private boolean isPlugged = false;

    public HoleInIce() {
        super(HazardType.HOLE_IN_ICE);
    }

    public void plug() {
        this.isPlugged = true;
        System.out.println("A hole in the ice has been plugged!");
    }

    public boolean isPlugged() {
        return isPlugged;
    }

    @Override
    public String getSymbol() {
        return isPlugged ? "PH" : "HI"; // Tıkalıysa PH, değilse HI
    }

    // onCollision metodunu güncelle: Tıkalıysa penguen geçebilir!
    @Override
    public ICollidable.CollisionResult onCollision(Penguin penguin, IcyTerrain terrain) {
        if (isPlugged) {
            System.out.println(penguin.getSymbol() + " passed safely over a plugged hole.");
            return ICollidable.CollisionResult.continueSame(); // Hareket devam eder!
        }

        // Tıkalı değilse ölür
        System.out.println(penguin.getSymbol() + " fell into a HoleInIce!");
        penguin.die(terrain);
        return ICollidable.CollisionResult.stop();
    }
}
