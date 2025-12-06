package interfaces;

import enums.Direction;
import core.IcyTerrain;

public interface ISlidable {
    int MAX_SLIDE_STEPS = 150; // Sonsuz kaymayı engellemek için üst sınır

    // Varsayılan giriş noktası: verilen yönde kaymayı başlatır.
    default SlideResult slide(Direction direction, IcyTerrain terrain) {
        return slide(direction, terrain, MAX_SLIDE_STEPS);
    }

    // Verilen yönde, sınır/engel/çarpışma kuralı durdurana kadar kayma hareketi yapar.
    // stepsRemaining her adımda azaltılarak sonsuz kayma riskini önler.
    // Dönüş değeri, kaymanın durduğu bilgisi ve (isteğe bağlı) nedeni iletir.
    SlideResult slide(Direction direction, IcyTerrain terrain, int stepsRemaining);

    class SlideResult {
        private final boolean stopped;
        private final String reason; // null ise sebep belirtilmedi

        private SlideResult(boolean stopped, String reason) {
            this.stopped = stopped;
            this.reason = reason;
        }

        public static SlideResult stopped(String reason) {
            return new SlideResult(true, reason);
        }

        public static SlideResult stopped() {
            return new SlideResult(true, null);
        }

        public static SlideResult ongoing() {
            return new SlideResult(false, null);
        }

        public boolean isStopped() {
            return stopped;
        }

        public String getReason() {
            return reason;
        }
    }
}
