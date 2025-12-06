package interfaces;

import entities.Penguin;
import core.IcyTerrain;
import enums.Direction;

public interface ICollidable {
    // Bir penguen bu nesneye çarptığında çarpışma etkilerini uygular.
    // Dönüş: penguen mevcut yönde devam edecek mi ve (gerekirse) yeni bir yönle devam edecek mi bilgisini taşır.
    // Çarpışma sırasında pozisyon/plug/penalty gibi yan etkiler bu metot içinde gerçekleştirilmelidir.
    CollisionResult onCollision(Penguin penguin, IcyTerrain terrain);

    // Basit taşıyıcı: çarpışma sonucunu üst kata bildirir.
    class CollisionResult {
        private final boolean shouldContinue;
        private final Direction overrideDirection; // null ise mevcut yön korunur

        private CollisionResult(boolean shouldContinue, Direction overrideDirection) {
            this.shouldContinue = shouldContinue;
            this.overrideDirection = overrideDirection;
        }

        public static CollisionResult stop() {
            return new CollisionResult(false, null);
        }

        public static CollisionResult continueSame() {
            return new CollisionResult(true, null);
        }

        public static CollisionResult continueWith(Direction direction) {
            return new CollisionResult(true, direction);
        }

        public boolean shouldContinue() {
            return shouldContinue;
        }

        public Direction getOverrideDirection() {
            return overrideDirection;
        }
    }
}
