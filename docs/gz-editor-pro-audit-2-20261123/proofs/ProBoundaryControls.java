/*
 * Independent, offline arithmetic controls for Pro audit findings.
 * Run with a JDK 17+ source launcher: java ProBoundaryControls.java
 * This program does not run Android, Kotlin application code, or install an APK.
 */
public class ProBoundaryControls {
    public static void main(String[] args) {
        checkRecipeLength();
        checkPanoramaHit();
        checkOriginalExportSize();
    }

    private static void checkRecipeLength() {
        float a = 0.00012345678f;
        float b = 0.00045678912f;
        StringBuilder points = new StringBuilder();
        for (int sample = 0; sample < 500; sample++) {
            if (sample > 0) {
                points.append(';');
            }
            boolean even = sample % 2 == 0;
            points.append(even ? a : b).append(',').append(even ? b : a);
        }
        int coordinateCharacters = 160 * points.length();
        require(coordinateCharacters == 2_079_840, "Unexpected JVM float output");
        require(coordinateCharacters > 2_000_000 && coordinateCharacters < 3_000_000,
            "Control does not straddle the two format limits");
        System.out.printf("PRO-05: %d ASCII coordinate characters before field names, using %s and %s.%n",
            coordinateCharacters, Float.toString(a), Float.toString(b));
    }

    private static void checkPanoramaHit() {
        double artboard = 1000;
        double storedLayerAspect = 10;
        double sourceAspect = 4000.0 / 100;
        double actualHeight = artboard / sourceAspect;
        double guideHeight = artboard / storedLayerAspect;
        double probeY = 530;
        boolean insideRenderedImage = Math.abs(probeY - 500) <= actualHeight / 2.0;
        boolean insideStoredGuide = Math.abs(probeY - 500) <= guideHeight / 2.0;
        require(!insideRenderedImage && insideStoredGuide, "Probe must separate visible and selectable extents");
        System.out.printf("PRO-03: image height %.1f px, guide height %.1f px. At y=530 the guide hits empty canvas.%n",
            actualHeight, guideHeight);
    }

    private static void checkOriginalExportSize() {
        long safePixelBudget = (128 - 64) * 1024L * 1024L / 20;
        int edge = (int)(4096 * Math.min(1.0, Math.sqrt(safePixelBudget / (4096.0 * 4096.0))));
        require(edge == 1831, "Unexpected square edge");
        System.out.printf("PRO-04: with a 128 MiB memory class, the computed square edge is %d for an 80 px crop.%n", edge);
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
