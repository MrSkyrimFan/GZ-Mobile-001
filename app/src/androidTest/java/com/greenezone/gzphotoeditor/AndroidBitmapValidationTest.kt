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
import com.greenezone.gzphotoeditor.editor.PrivacyEdit
import com.greenezone.gzphotoeditor.editor.PrivacyKind
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

    @Test fun opaqueBlackBarSurvivesBlurAndFreshJpegExport() {
        val source = Bitmap.createBitmap(96, 72, Bitmap.Config.ARGB_8888).apply {
            eraseColor(Color.WHITE)
        }
        val edits = EditDocument(privacy = listOf(
            PrivacyEdit(kind = PrivacyKind.BLUR, centerX = .5f, centerY = .5f,
                width = .9f, height = .7f, strength = .9f),
            PrivacyEdit(kind = PrivacyKind.BLACK_BAR, centerX = .5f, centerY = .5f,
                width = .45f, height = .18f)
        ))
        val rendered = ImagePipeline.render(source, edits)
        try {
            assertEquals(Color.BLACK, rendered.getPixel(48, 36))
            assertEquals(Color.WHITE, rendered.getPixel(0, 0))
            assertEquals(Color.WHITE, source.getPixel(48, 36))
            val output = ByteArrayOutputStream()
            assertTrue(rendered.compress(Bitmap.CompressFormat.JPEG, 100, output))
            val bytes = output.toByteArray()
            val reopened = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            try {
                val center = reopened.getPixel(48, 36)
                assertTrue(Color.red(center) < 12)
                assertTrue(Color.green(center) < 12)
                assertTrue(Color.blue(center) < 12)
            } finally { reopened.recycle() }
        } finally {
            rendered.recycle()
            source.recycle()
        }
    }

    @Test fun pixelateAndBlurChangeOnlyChosenRegion() {
        val source = Bitmap.createBitmap(96, 80, Bitmap.Config.ARGB_8888)
        for (y in 0 until source.height) for (x in 0 until source.width) {
            val value = if ((x / 2 + y / 2) % 2 == 0) 255 else 0
            source.setPixel(x, y, Color.rgb(value, value, value))
        }
        val pixelated = ImagePipeline.render(source, EditDocument(privacy = listOf(
            PrivacyEdit(kind = PrivacyKind.PIXELATE, centerX = .5f, centerY = .5f,
                width = .55f, height = .5f, strength = .8f, feather = 0f)
        )))
        val blurred = ImagePipeline.render(source, EditDocument(privacy = listOf(
            PrivacyEdit(kind = PrivacyKind.BLUR_BRUSH, strength = .9f, brushSize = .17f,
                strokes = listOf(BrushStroke(listOf(
                    BrushPoint(.5f, .5f), BrushPoint(.6f, .5f)
                ))))
        )))
        try {
            assertEquals(source.getPixel(0, 0), pixelated.getPixel(0, 0))
            assertEquals(source.getPixel(0, 0), blurred.getPixel(0, 0))
            var changedPixels = 0
            for (y in 30..50) for (x in 35..60) {
                if (source.getPixel(x, y) != pixelated.getPixel(x, y)) changedPixels++
            }
            assertTrue(changedPixels > 100)
            assertTrue(blurred.getPixel(48, 40) != source.getPixel(48, 40))
        } finally {
            pixelated.recycle()
            blurred.recycle()
            source.recycle()
        }
    }

    @Test fun blackBrushCreatesSolidEditableStroke() {
        val source = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888).apply {
            eraseColor(Color.rgb(250, 180, 140))
        }
        val rendered = ImagePipeline.render(source, EditDocument(privacy = listOf(
            PrivacyEdit(kind = PrivacyKind.BLACK_BRUSH,
                brushSize = .11f, feather = .01f,
                strokes = listOf(BrushStroke(listOf(
                    BrushPoint(.3f, .4f), BrushPoint(.7f, .4f)
                ))))
        )))
        try {
            assertEquals(Color.BLACK, rendered.getPixel(50, 40))
            assertEquals(Color.rgb(250, 180, 140), rendered.getPixel(0, 0))
            assertEquals(Color.rgb(250, 180, 140), source.getPixel(50, 40))
        } finally {
            rendered.recycle()
            source.recycle()
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
