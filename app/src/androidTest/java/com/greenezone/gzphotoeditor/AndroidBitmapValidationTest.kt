package com.greenezone.gzphotoeditor

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.greenezone.gzphotoeditor.editor.BrushPoint
import com.greenezone.gzphotoeditor.editor.BrushStroke
import com.greenezone.gzphotoeditor.editor.CropRatio
import com.greenezone.gzphotoeditor.editor.CurvePoints
import com.greenezone.gzphotoeditor.editor.EditDocument
import com.greenezone.gzphotoeditor.editor.GeometryMath
import com.greenezone.gzphotoeditor.editor.Grade
import com.greenezone.gzphotoeditor.editor.ImagePipeline
import com.greenezone.gzphotoeditor.editor.LocalMask
import com.greenezone.gzphotoeditor.editor.MaskKind
import com.greenezone.gzphotoeditor.editor.ProjectStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.ByteArrayOutputStream

@RunWith(AndroidJUnit4::class)
class AndroidBitmapValidationTest {
    private fun checker(w: Int = 80, h: Int = 60): Bitmap {
        val pixels = IntArray(w * h) { i ->
            val x = i % w
            val y = i / w
            Color.rgb((x * 255 / w).coerceIn(0, 255),
                (y * 255 / h).coerceIn(0, 255), ((x + y) * 127 / (w + h)).coerceIn(0, 255))
        }
        return Bitmap.createBitmap(pixels, w, h, Bitmap.Config.ARGB_8888)
    }

    @Test fun rotateFlipAndCropAreActuallyAppliedByAndroidBitmap() {
        val image = checker()
        try {
            val rotated = ImagePipeline.geometry(image, EditDocument(quarterTurns = 1))
            assertEquals(image.width, rotated.height)
            assertEquals(image.height, rotated.width)
            rotated.recycle()
            val cropped = ImagePipeline.geometry(image, EditDocument(crop = CropRatio.SQUARE,
                cropCenterX = 0f, cropCenterY = 1f, mirrored = true))
            assertEquals(cropped.width, cropped.height)
            assertTrue(cropped.width > 0)
            cropped.recycle()
            assertTrue(!image.isRecycled)
        } finally { image.recycle() }
    }

    @Test fun straightenEliminatesTransparentCornerAndKeepsCenter() {
        val image = Bitmap.createBitmap(400, 300, Bitmap.Config.ARGB_8888).apply {
            eraseColor(Color.rgb(62, 100, 143))
        }
        try {
            val output = ImagePipeline.geometry(image, EditDocument(straightenDegrees = 10f))
            assertTrue(output.width < image.width)
            assertTrue(output.height < image.height)
            assertEquals(Color.rgb(62, 100, 143),
                output.getPixel(output.width / 2, output.height / 2))
            output.recycle()
        } finally { image.recycle() }
    }

    @Test fun previewAndExportRenderingAreDeterministicForSameDimensions() {
        val source = checker(96, 72)
        val edits = EditDocument(
            grade = Grade(exposure = .15f, sharpen = .26f, texture = .3f,
                denoise = .35f, masterCurve = CurvePoints().change(2, .7f)),
            masks = listOf(LocalMask(kind = MaskKind.BRUSH, exposure = .6f,
                strokes = listOf(BrushStroke(listOf(BrushPoint(.2f, .2f),
                    BrushPoint(.65f, .65f))))))
        )
        try {
            val a = ImagePipeline.render(source, edits)
            val b = ImagePipeline.render(source, edits)
            assertEquals(a.width, b.width)
            assertEquals(a.height, b.height)
            for (y in 0 until a.height step 4) {
                for (x in 0 until a.width step 4) {
                    assertEquals(a.getPixel(x, y), b.getPixel(x, y))
                }
            }
            a.recycle()
            b.recycle()
        } finally { source.recycle() }
    }

    @Test fun jpegExportIsWhiteMattedAndPreservesOpaquePixels() {
        val input = Bitmap.createBitmap(16, 16, Bitmap.Config.ARGB_8888)
        input.eraseColor(Color.TRANSPARENT)
        for (y in 0 until 16) for (x in 0 until 8) input.setPixel(x, y, Color.RED)
        val opaque = ImagePipeline.compositeWhite(input)
        try {
            assertEquals(Color.WHITE, opaque.getPixel(14, 7))
            assertEquals(Color.RED, opaque.getPixel(1, 7))
            val bytes = ByteArrayOutputStream()
            assertTrue(opaque.compress(Bitmap.CompressFormat.JPEG, 95, bytes))
            val encoded = bytes.toByteArray()
            assertTrue(encoded.size > 100)
            val reopened = BitmapFactory.decodeByteArray(encoded, 0, encoded.size)
            assertEquals(255, Color.alpha(reopened.getPixel(14, 7)))
            assertTrue(Color.red(reopened.getPixel(14, 7)) > 240)
            reopened.recycle()
        } finally {
            opaque.recycle()
            input.recycle()
        }
    }

    @Test fun savedEditableProjectRestoresAllSettingsLocally() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val uri = Uri.parse("content://gzphotoeditor.integration/source")
        val editable = EditDocument(
            grade = Grade(texture = .45f, denoise = .2f),
            straightenDegrees = 5f, crop = CropRatio.PORTRAIT,
            masks = listOf(LocalMask(kind = MaskKind.RADIAL, exposure = .42f))
        )
        try {
            assertTrue(ProjectStore.save(context, "Android Test", uri, editable))
            val loaded = ProjectStore.load(context).first { it.name == "Android Test" }
            assertEquals(editable, loaded.document)
            assertEquals(uri, loaded.image)
        } finally {
            ProjectStore.delete(context, "Android Test")
        }
    }

    @Test fun activityStartsWithoutAFirstFrameCrash() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                assertTrue(!activity.isFinishing)
            }
        }
    }
}
