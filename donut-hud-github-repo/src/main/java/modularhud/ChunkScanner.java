package modularhud;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.class_1923;
import net.minecraft.class_2818;
import net.minecraft.class_310;

/** Incrementally scans loaded chunks around the player (nearest first) and caches per-chunk results. */
final class ChunkScanner<T> {
    final Map<Long, List<T>> cache = new HashMap<>();
    private final List<int[]> order = new ArrayList<>();
    private final Function<class_2818, List<T>> scan;
    private final int perTick;
    private int orderIdx, lastCx = Integer.MIN_VALUE, lastCz = Integer.MIN_VALUE, lastR = -1;

    ChunkScanner(Function<class_2818, List<T>> scan, int perTick) {
        this.scan = scan;
        this.perTick = perTick;
    }

    void clear() {
        cache.clear();
        order.clear();
        lastCx = lastCz = Integer.MIN_VALUE;
        lastR = -1;
    }

    /** Forces a full rescan (e.g. after the target list changed). */
    void invalidate() {
        lastR = -1;
    }

    void tick(class_310 mc, int r) {
        int cx = mc.field_1724.method_31477() >> 4;
        int cz = mc.field_1724.method_31479() >> 4;
        if (cx != lastCx || cz != lastCz || r != lastR) rebuildOrder(cx, cz, r);
        if (order.isEmpty()) return;

        for (int n = 0; n < perTick; n++) {
            int[] o = order.get(orderIdx);
            orderIdx = (orderIdx + 1) % order.size();
            int ccx = cx + o[0], ccz = cz + o[1];
            long key = class_1923.method_8331(ccx, ccz);
            class_2818 chunk = mc.field_1687.method_2935().method_21730(ccx, ccz);
            if (chunk == null) {
                cache.remove(key);
                continue;
            }
            List<T> hits = scan.apply(chunk);
            if (hits.isEmpty()) cache.remove(key);
            else cache.put(key, hits);
        }
    }

    private void rebuildOrder(int cx, int cz, int r) {
        order.clear();
        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                if (dx * dx + dz * dz <= r * r + r) order.add(new int[]{dx, dz});
            }
        }
        order.sort(Comparator.comparingInt(o -> o[0] * o[0] + o[1] * o[1]));
        orderIdx = 0;

        Iterator<Long> it = cache.keySet().iterator();
        while (it.hasNext()) {
            long key = it.next();
            int kx = (int) (key & 0xFFFFFFFFL);
            int kz = (int) (key >>> 32);
            int dx = kx - cx, dz = kz - cz;
            if (dx * dx + dz * dz > r * r + r) it.remove();
        }
        lastCx = cx;
        lastCz = cz;
        lastR = r;
    }
}
