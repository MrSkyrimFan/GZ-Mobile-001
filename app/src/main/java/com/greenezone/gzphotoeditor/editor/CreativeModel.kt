package com.greenezone.gzphotoeditor.editor

/** Normalized coordinates keep layers editable at preview and export sizes. */
data class CropFrame(val left: Float, val top: Float, val right: Float, val bottom: Float) {
    val width get() = right - left
    val height get() = bottom - top
}

enum class TextFont(val label: String, val family: String) {
    SANS("Modern", "sans-serif"),
    SERIF("Classic", "serif"),
    MONO("Typewriter", "monospace"),
    SCRIPT("Script", "cursive"),
    CONDENSED("Condensed", "sans-serif-condensed"),
    HANDWRITTEN("Handwritten", "casual"),
    MEDIUM("Medium", "sans-serif-medium"),
    LIGHT("Light", "sans-serif-light"),
    HEAVY("Heavy", "sans-serif-black")
}

enum class TextAlignment(val label: String) {
    LEFT("Left"), CENTER("Center"), RIGHT("Right")
}

data class TextOverlay(
    val content: String = "Your text",
    val x: Float = .5f,
    val y: Float = .5f,
    val width: Float = .7f,
    val height: Float = .24f,
    val rotation: Float = 0f,
    val font: TextFont = TextFont.SANS,
    val size: Float = .075f,
    val bold: Boolean = false,
    val italic: Boolean = false,
    val color: Int = -1,
    val opacity: Float = 1f,
    val outline: Float = 0f,
    val outlineColor: Int = -0x1000000,
    val shadow: Float = 0f,
    val align: TextAlignment = TextAlignment.CENTER,
    val letterSpacing: Float = 0f
)

data class InkStroke(
    val points: List<BrushPoint>,
    val color: Int = -1,
    val thickness: Float = .012f,
    val opacity: Float = 1f,
    val erase: Boolean = false
)

enum class ShapeKind(val label: String) {
    RECTANGLE("Rectangle"), ELLIPSE("Ellipse"), LINE("Line"),
    ARROW("Arrow"), TRIANGLE("Triangle"), STAR("Star")
}

data class ShapeOverlay(
    val kind: ShapeKind = ShapeKind.RECTANGLE,
    val x: Float = .5f,
    val y: Float = .5f,
    val width: Float = .38f,
    val height: Float = .25f,
    val rotation: Float = 0f,
    val color: Int = -1,
    val opacity: Float = 1f,
    val thickness: Float = .009f,
    val filled: Boolean = false
)

data class CreativeDocument(
    val text: List<TextOverlay> = emptyList(),
    val strokes: List<InkStroke> = emptyList(),
    val shapes: List<ShapeOverlay> = emptyList()
)

enum class SocialPreset(val label: String, val width: Int, val height: Int) {
    ORIGINAL("Original dimensions", 0, 0),
    INSTAGRAM_SQUARE("Instagram · Square", 1080, 1080),
    INSTAGRAM_PORTRAIT("Instagram · Portrait", 1080, 1350),
    INSTAGRAM_STORY("Instagram · Story / Reel cover", 1080, 1920),
    FACEBOOK_SQUARE("Facebook · Square feed", 1080, 1080),
    FACEBOOK_FEED("Facebook · Link / landscape feed", 1200, 630),
    FACEBOOK_STORY("Facebook · Story", 1080, 1920),
    TIKTOK_PHOTO("TikTok · Photo", 1080, 1920),
    YOUTUBE_THUMBNAIL("YouTube · HD thumbnail", 1280, 720),
    YOUTUBE_THUMBNAIL_4K("YouTube · 4K thumbnail", 3840, 2160),
    PINTEREST_PIN("Pinterest · Pin", 1000, 1500),
    LINKEDIN_SQUARE("LinkedIn · Square post", 1200, 1200),
    X_LANDSCAPE("X · Landscape image", 1600, 900),
    SNAPCHAT_STORY("Snapchat · Story", 1080, 1920)
}

enum class ExportPlacement(val label: String) {
    FIT("Fit entire image"), FILL("Fill frame, crop edges")
}

enum class ExportBackground(val label: String, val color: Int) {
    BLACK("Black", -0x1000000), WHITE("White", -1), TRANSPARENT("Transparent", 0)
}

enum class ImageExportFormat(val label: String, val extension: String, val mime: String) {
    JPEG("JPEG · JPG", "jpg", "image/jpeg"),
    PNG("PNG · Lossless", "png", "image/png"),
    WEBP("WebP · Small", "webp", "image/webp"),
    WEBP_LOSSLESS("WebP · Lossless", "webp", "image/webp"),
    BMP("BMP · Uncompressed", "bmp", "image/bmp"),
    TIFF("TIFF · Uncompressed", "tiff", "image/tiff"),
    PDF("PDF · Printable", "pdf", "application/pdf")
}

data class ExportSettings(
    val format: ImageExportFormat = ImageExportFormat.JPEG,
    val preset: SocialPreset = SocialPreset.ORIGINAL,
    val placement: ExportPlacement = ExportPlacement.FIT,
    val background: ExportBackground = ExportBackground.BLACK,
    val quality: Int = 95
)
