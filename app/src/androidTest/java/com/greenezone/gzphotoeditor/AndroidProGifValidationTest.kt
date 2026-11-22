package com.greenezone.gzphotoeditor

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Movie
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.greenezone.gzphotoeditor.editor.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.io.ByteArrayOutputStream

@RunWith(AndroidJUnit4::class)
class AndroidProGifValidationTest {
    private fun solid(color: Int) = Bitmap.createBitmap(24,24,Bitmap.Config.ARGB_8888)
        .apply { eraseColor(color) }

    @Test fun layersComposeWithCorrectOrderWithoutFlatteningSource() {
        val red = ProLayer("content://layers/red", name = "Red", scale = .65f)
        val blue = ProLayer("content://layers/blue", name = "Blue", x = .85f, scale = .5f)
        val doc = ProDocument(listOf(red,blue), background = ProBackground.WHITE)
        val output = ProRaster.renderWithLoader(doc,120,120,false,loader = { item,_,_ ->
            solid(if(item.name == "Red") Color.RED else Color.BLUE)
        })
        try {
            assertEquals(Color.RED, output.getPixel(45,60))
            assertEquals(Color.BLUE, output.getPixel(102,60))
            assertEquals(Color.WHITE, output.getPixel(0,0))
        } finally { output.recycle() }
    }
    @Test fun proAndGifProjectsSurviveSaveCodecRoundtrip() {
        val edit = EditDocument(grade = Grade(exposure = .3f),
            creative = CreativeDocument(text = listOf(TextOverlay(content = "Editable"))))
        val pro = ProDocument(listOf(ProLayer("content://one", edits = edit,
            opacity = .55f, blend = ProBlend.MULTIPLY), ProLayer("content://two", visible = false)),
            ProAspect.STORY, ProBackground.TRANSPARENT)
        assertEquals(pro, ProStore.decode(ProStore.encode(pro)))
        val gif = GifDocument(listOf("content://one", "content://two"), delayMs = 400,
            size = GifSize.SMALL, fit = GifFit.FILL, aspect = GifAspect.PORTRAIT,
            forever = false)
        assertEquals(gif, GifStore.decode(GifStore.encode(gif)))
        assertNull(ProStore.decode("{}"))
        assertNull(GifStore.decode("{}"))
    }
    @Test fun gifPreviewAndExportUseIdenticalFitFillCanvasComposition() {
        val src = solid(Color.RED)
        val doc = GifDocument(listOf("content://one", "content://two"),
            size = GifSize.MEDIUM, aspect = GifAspect.LANDSCAPE, fit = GifFit.FIT)
        val fitted = GifEncoder.renderFrame(src, doc)
        val filled = GifEncoder.renderFrame(src, doc.copy(fit = GifFit.FILL))
        try {
            assertEquals(480, fitted.width)
            assertEquals(270, fitted.height)
            assertEquals(Color.BLACK, fitted.getPixel(0, 135))
            assertEquals(Color.RED, fitted.getPixel(240, 135))
            assertEquals(Color.RED, filled.getPixel(0, 135))
        } finally {
            fitted.recycle()
            filled.recycle()
            src.recycle()
        }
    }

    @Test fun exportedGifIsActualDecodableAnimatedGif89a() {
        val gif = GifDocument(listOf("content://one","content://two"),
            delayMs = 400, size = GifSize.SMALL)
        val out = ByteArrayOutputStream()
        GifEncoder.write(out,gif,loader = { if (it == 0) solid(Color.RED) else solid(Color.BLUE) })
        val data = out.toByteArray()
        assertEquals("GIF89a", String(data,0,6,Charsets.US_ASCII))
        assertEquals(0x3B, data.last().toInt() and 255)
        val decoded = Movie.decodeByteArray(data,0,data.size)
        assertNotNull("Animated GIF must decode on Android", decoded)
        assertEquals(320, decoded!!.width())
        assertEquals(320, decoded.height())
        assertTrue("GIF must have frame duration", decoded.duration() >= 400)
    }
}
