package modularhud;

import net.minecraft.class_310;
import net.minecraft.class_332;

/**
 * Own camera projection onto the 2D HUD layer (so overlays show through walls), plus
 * line / wireframe-box drawing. Camera position is interpolated between client ticks.
 */
public final class Projector {
    private static double prevX, prevY, prevZ, curX, curY, curZ;
    private static long tickNanos;
    private static boolean havePos;

    // per-frame camera state (valid after a successful begin())
    static double ex, ey, ez, fx, fy, fz, rx, ry, rz, ux, uy, uz, tanHalf, aspect;
    static int sw, sh;

    private static final double NEAR = 0.05;
    private static final int[][] EDGES = {
            {0, 1}, {2, 3}, {4, 5}, {6, 7},   // along z
            {0, 2}, {1, 3}, {4, 6}, {5, 7},   // along y
            {0, 4}, {1, 5}, {2, 6}, {3, 7}    // along x
    };

    private Projector() {}

    public static void tick(class_310 mc) {
        if (mc.field_1724 == null) {
            havePos = false;
            return;
        }
        double x = mc.field_1724.method_23317(), y = mc.field_1724.method_23318(), z = mc.field_1724.method_23321();
        prevX = havePos ? curX : x;
        prevY = havePos ? curY : y;
        prevZ = havePos ? curZ : z;
        curX = x;
        curY = y;
        curZ = z;
        tickNanos = System.nanoTime();
        havePos = true;
    }

    /** Sets up the camera for this frame. Returns false when nothing should be drawn. */
    public static boolean begin(class_332 ctx, class_310 mc) {
        if (mc.field_1724 == null || !havePos) return false;
        if (!mc.field_1690.method_31044().method_31034()) return false;

        sw = ctx.method_51421();
        sh = ctx.method_51443();

        double t = Math.min(1.0, (System.nanoTime() - tickNanos) / 5.0e7);
        ex = prevX + (curX - prevX) * t;
        ey = prevY + (curY - prevY) * t + (mc.field_1724.method_23320() - mc.field_1724.method_23318());
        ez = prevZ + (curZ - prevZ) * t;

        double yaw = Math.toRadians(mc.field_1724.method_36454());
        double pitch = Math.toRadians(mc.field_1724.method_36455());
        fx = -Math.sin(yaw) * Math.cos(pitch);
        fy = -Math.sin(pitch);
        fz = Math.cos(yaw) * Math.cos(pitch);
        rx = -Math.cos(yaw);
        ry = 0;
        rz = -Math.sin(yaw);
        ux = ry * fz - rz * fy;
        uy = rz * fx - rx * fz;
        uz = rx * fy - ry * fx;
        tanHalf = Math.tan(Math.toRadians(mc.field_1690.method_41808().method_41753()) / 2.0);
        aspect = (double) sw / sh;
        return true;
    }

    /** World point -> camera space {right, up, forward}. */
    static void toCam(double x, double y, double z, double[] out) {
        double dx = x - ex, dy = y - ey, dz = z - ez;
        out[0] = dx * rx + dy * ry + dz * rz;
        out[1] = dx * ux + dy * uy + dz * uz;
        out[2] = dx * fx + dy * fy + dz * fz;
    }

    /** Camera space -> screen. Only valid when forward > 0. */
    static void toScreen(double[] cam, double[] out) {
        double z = Math.max(NEAR, cam[2]);
        out[0] = (cam[0] / (z * tanHalf * aspect) * 0.5 + 0.5) * sw;
        out[1] = (0.5 - cam[1] / (z * tanHalf) * 0.5) * sh;
    }

    /** Projects a world point. Returns false if it is behind the camera. */
    static boolean project(double x, double y, double z, double[] out) {
        double[] cam = new double[3];
        toCam(x, y, z, cam);
        if (cam[2] < NEAR) return false;
        toScreen(cam, out);
        return true;
    }

    // ------------------------------------------------------------------ drawing

    /** True 3D wireframe: the 12 edges of the box, clipped against the near plane and the screen. */
    public static void wireBox(class_332 c, double minX, double minY, double minZ,
                               double maxX, double maxY, double maxZ, int col) {
        double[][] cam = new double[8][3];
        int behind = 0;
        for (int xi = 0; xi < 2; xi++) {
            for (int yi = 0; yi < 2; yi++) {
                for (int zi = 0; zi < 2; zi++) {
                    double[] p = cam[xi * 4 + yi * 2 + zi];
                    toCam(xi == 0 ? minX : maxX, yi == 0 ? minY : maxY, zi == 0 ? minZ : maxZ, p);
                    if (p[2] < NEAR) behind++;
                }
            }
        }
        if (behind == 8) return;
        for (int[] e : EDGES) edge(c, cam[e[0]], cam[e[1]], col);
    }

    private static void edge(class_332 c, double[] a, double[] b, int col) {
        double ax = a[0], ay = a[1], az = a[2], bx = b[0], by = b[1], bz = b[2];
        if (az < NEAR && bz < NEAR) return;
        if (az < NEAR) {
            double t = (NEAR - az) / (bz - az);
            ax += (bx - ax) * t;
            ay += (by - ay) * t;
            az = NEAR;
        } else if (bz < NEAR) {
            double t = (NEAR - bz) / (az - bz);
            bx += (ax - bx) * t;
            by += (ay - by) * t;
            bz = NEAR;
        }
        double[] s0 = new double[2], s1 = new double[2];
        toScreen(new double[]{ax, ay, az}, s0);
        toScreen(new double[]{bx, by, bz}, s1);
        clippedLine(c, s0[0], s0[1], s1[0], s1[1], col);
    }

    /** Liang-Barsky clip to the screen, then rasterise. */
    public static void clippedLine(class_332 c, double x0, double y0, double x1, double y1, int col) {
        double dx = x1 - x0, dy = y1 - y0, t0 = 0, t1 = 1;
        double[] p = {-dx, dx, -dy, dy};
        double[] q = {x0, sw - x0, y0, sh - y0};
        for (int i = 0; i < 4; i++) {
            if (p[i] == 0) {
                if (q[i] < 0) return;
            } else {
                double r = q[i] / p[i];
                if (p[i] < 0) {
                    if (r > t1) return;
                    if (r > t0) t0 = r;
                } else {
                    if (r < t0) return;
                    if (r < t1) t1 = r;
                }
            }
        }
        line(c, (int) Math.round(x0 + t0 * dx), (int) Math.round(y0 + t0 * dy),
                (int) Math.round(x0 + t1 * dx), (int) Math.round(y0 + t1 * dy), col);
    }

    /** 1px line built from merged runs (far fewer fills than per-pixel). */
    public static void line(class_332 c, int x0, int y0, int x1, int y1, int col) {
        int dx = Math.abs(x1 - x0), dy = Math.abs(y1 - y0);
        if (dx == 0 && dy == 0) {
            c.method_25294(x0, y0, x0 + 1, y0 + 1, col);
            return;
        }
        if (dx >= dy) {
            if (x0 > x1) { int t = x0; x0 = x1; x1 = t; t = y0; y0 = y1; y1 = t; }
            int span = x1 - x0;
            int runStart = x0, runY = y0;
            for (int x = x0 + 1; x <= x1; x++) {
                int y = (int) Math.round(y0 + (double) (x - x0) * (y1 - y0) / span);
                if (y != runY) {
                    c.method_25294(runStart, runY, x, runY + 1, col);
                    runStart = x;
                    runY = y;
                }
            }
            c.method_25294(runStart, runY, x1 + 1, runY + 1, col);
        } else {
            if (y0 > y1) { int t = x0; x0 = x1; x1 = t; t = y0; y0 = y1; y1 = t; }
            int span = y1 - y0;
            int runStart = y0, runX = x0;
            for (int y = y0 + 1; y <= y1; y++) {
                int x = (int) Math.round(x0 + (double) (y - y0) * (x1 - x0) / span);
                if (x != runX) {
                    c.method_25294(runX, runStart, runX + 1, y, col);
                    runStart = y;
                    runX = x;
                }
            }
            c.method_25294(runX, runStart, runX + 1, y1 + 1, col);
        }
    }

    /** Small arrow at the screen edge pointing toward an off-screen / behind-you target. */
    public static void edgeArrow(class_332 c, double[] cam, int col) {
        double dx = cam[0], dy = -cam[1];
        double len = Math.hypot(dx, dy);
        if (len < 1e-6) { dx = 0; dy = -1; len = 1; }
        dx /= len;
        dy /= len;
        double margin = 14;
        double sx = dx == 0 ? Double.MAX_VALUE : (sw / 2.0 - margin) / Math.abs(dx);
        double sy = dy == 0 ? Double.MAX_VALUE : (sh / 2.0 - margin) / Math.abs(dy);
        double s = Math.min(sx, sy);
        double tipX = sw / 2.0 + dx * s, tipY = sh / 2.0 + dy * s;
        double bx = tipX - dx * 9, by = tipY - dy * 9;
        double px = -dy * 4, py = dx * 4;
        line(c, (int) bx, (int) by, (int) tipX, (int) tipY, col);
        line(c, (int) (bx + px), (int) (by + py), (int) tipX, (int) tipY, col);
        line(c, (int) (bx - px), (int) (by - py), (int) tipX, (int) tipY, col);
    }
}
