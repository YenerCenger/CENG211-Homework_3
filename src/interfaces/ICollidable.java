package interfaces;

import entities.Penguin;
import core.IcyTerrain;

public interface ICollidable {
    // Bir penguen bu nesneye çarparsa ne olur?
    // "return true" dönerse: Penguen harekete devam edebilir (veya seker).
    // "return false" dönerse: Penguen durur.
    boolean onCollision(Penguin penguin, IcyTerrain terrain);
}