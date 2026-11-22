package com.greenezone.gzphotoeditor

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.greenezone.gzphotoeditor.editor.CollageAspect
import com.greenezone.gzphotoeditor.editor.CollageBackground
import com.greenezone.gzphotoeditor.editor.CollageCellMode
import com.greenezone.gzphotoeditor.editor.CollageDocument
import com.greenezone.gzphotoeditor.editor.CollageLayout
import com.greenezone.gzphotoeditor.editor.CollageMath
import com.greenezone.gzphotoeditor.editor.CollagePhoto
import com.greenezone.gzphotoeditor.editor.CollageRaster
import com.greenezone.gzphotoeditor.editor.CollageStore
import com.greenezone.gzphotoeditor.editor.CollageTemplate
import com.greenezone.gzphotoeditor.editor.EditDocument
import com.greenezone.gzphotoeditor.editor.Grade
import com.greenezone.gzphotoeditor.editor.PrivacyEdit
import com.greenezone.gzphotoeditor.editor.PrivacyKind
import com.greenezone.gzphotoeditor.editor.TextOverlay
import com.greenezone.gzphotoeditor.editor.CreativeDocument
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.ByteArrayOutputStream

@RunWith(AndroidJUnit4::class)
class AndroidCollageValidationTest {
    private fun colored(color: Int, w: Int = 60, h: Int = 60): Bitmap =
        Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888).apply { eraseColor(color) }

    @Test fun twoPhotoCollageRendersTwoIndependentlyEditedSources() {
        val before = CollageDocument(
            photos = listOf(
                CollagePhoto("content://collage.local/red",
                    EditDocument(grade = Grade(exposure = -.4f))),
                CollagePhoto("content://collage.local/blue")
            ),
            layout = CollageLayout(
                template = CollageTemplate.SIDE_BY_SIDE,
                aspect = CollageAspect.CLASSIC,
                margin = 0f, gap = 0f,
                cellMode = CollageCellMode.FILL
            )
        )
        var calls = 0
        val output = CollageRaster.renderWithLoader(
            before, 300, 200, preview = false
        ) { photo, _, _ ->
            calls++
            colored(if (photo.uri.endsWith("red")) Color.RED else Color.BLUE)
        }
        try {
            assertEquals(2, calls)
            val left = output.getPixel(75, 100)
            val right = output.getPixel(225, 100)
            assertTrue(Color.red(left) < Color.red(Color.RED))
            assertTrue(Color.red(left) > 120)
            assertEquals(Color.BLUE, right)
        } finally { output.recycle() }
    }

    @Test fun mosaicRendersAllTwentyTilesWithDistinctPixels() {
        val layout = CollageLayout(
            template = CollageTemplate.MOSAIC,
            margin = 0f, gap = 0f, cornerRadius = 0f
        )
        val collage = CollageDocument(
            photos = (0 until 20).map { CollagePhoto("content://collage.local/$it") },
            layout = layout
        )
        var calls = 0
        val output = CollageRaster.renderWithLoader(
            collage, 800, 800, preview = false
        ) { photo, _, _ ->
            calls++
            val id = photo.uri.substringAfterLast('/').toInt()
            colored(Color.rgb((id * 11) % 256, (id * 19) % 256, (id * 37) % 256), 40, 40)
        }
        try {
            assertEquals(20, calls)
            val cells = CollageMath.innerFrames(20, layout)
            cells.forEachIndexed { index, cell ->
                val x = ((cell.left + cell.right) * 400f).toInt().coerceIn(0, 799)
                val y = ((cell.top + cell.bottom) * 400f).toInt().coerceIn(0, 799)
                val expected = Color.rgb((index * 11) % 256,
                    (index * 19) % 256, (index * 37) % 256)
                assertEquals("Photo $index should retain own pixels", expected, output.getPixel(x, y))
            }
        } finally { output.recycle() }
    }

    @Test fun collageProjectsRoundTripWithPerPhotoTextAndPrivacy() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val source = CollageDocument(
            photos = listOf(
                CollagePhoto("content://example.one/1", EditDocument(
                    grade = Grade(exposure = .34f),
                    creative = CreativeDocument(text = listOf(
                        TextOverlay(content = "Private caption", color = Color.WHITE)
                    ))
                )),
                CollagePhoto("content://example.two/2", EditDocument(
                    privacy = listOf(PrivacyEdit(kind = PrivacyKind.BLACK_BAR,
                        centerX = .45f, centerY = .65f))
                ))
            ),
            layout = CollageLayout(
                template = CollageTemplate.FEATURED,
                aspect = CollageAspect.PORTRAIT,
                margin = .045f,
                gap = .02f,
                background = CollageBackground.CREAM
            )
        )
        try {
            val encoded = CollageStore.encode(source)
            assertEquals(source, CollageStore.decode(encoded))
            assertTrue(CollageStore.save(context, "GZ Collage Test", source))
            val restored = CollageStore.load(context, "GZ Collage Test")
            assertEquals(source, restored)
            assertTrue(CollageStore.names(context).contains("GZ Collage Test"))
        } finally {
            CollageStore.delete(context, "GZ Collage Test")
        }
    }

    @Test fun collageRasterCanBeExportedAsJpegAndPng() {
        val collage = CollageDocument(
            photos = listOf(
                CollagePhoto("content://local/one"),
                CollagePhoto("content://local/two")
            ),
            layout = CollageLayout(template = CollageTemplate.STACKED)
        )
        val preview = CollageRaster.renderWithLoader(collage, 320, 400, true) {
            _, _, _ -> colored(Color.MAGENTA, 40, 30)
        }
        try {
            for (format in listOf(Bitmap.CompressFormat.PNG, Bitmap.CompressFormat.JPEG)) {
                val stream = ByteArrayOutputStream()
                assertTrue(preview.compress(format, 95, stream))
                val bytes = stream.toByteArray()
                assertTrue(bytes.size > 200)
                val decoded = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                assertEquals(320, decoded.width)
                assertEquals(400, decoded.height)
                decoded.recycle()
            }
        } finally { preview.recycle() }
    }

    @Test fun missingSourceReportsExactTileAndDoesNotSilentlyReplacePhoto() {
        val collage = CollageDocument(
            photos = listOf(
                CollagePhoto("content://ok/one"),
                CollagePhoto("content://missing/two"),
                CollagePhoto("content://ok/three")
            )
        )
        var index = 0
        try {
            CollageRaster.renderWithLoader(collage, 300, 300, true) {
                _, _, _ ->
                if (index++ == 1) throw IllegalStateException("URI permission revoked")
                colored(Color.BLUE)
            }
            throw AssertionError("Expected MissingPhoto failure")
        } catch (error: CollageRaster.MissingPhoto) {
            assertEquals(1, error.position)
            assertTrue(error.message!!.contains("Photo 2"))
        }
    }
}
