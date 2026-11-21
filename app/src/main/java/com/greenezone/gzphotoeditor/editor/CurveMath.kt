package com.greenezone.gzphotoeditor.editor

import kotlin.math.roundToInt

/** Pure five-knot monotone cubic Hermite curve. No overshoot into new extrema. */
object CurveMath {
    val identity = CurvePoints()

    fun isIdentity(points: CurvePoints): Boolean = points == identity

    fun evaluate(points: CurvePoints, input: Float): Float {
        val x = input.coerceIn(0f, 1f)
        val segment = minOf(3, (x * 4f).toInt())
        val t = (x * 4f - segment).coerceIn(0f, 1f)
        val v = points.values
        val slopes = FloatArray(4) { v[it + 1] - v[it] }
        fun tangent(i: Int): Float {
            if (i == 0) return slopes[0]
            if (i == 4) return slopes[3]
            val a = slopes[i - 1]
            val b = slopes[i]
            return if (a * b <= 0f) 0f else 2f * a * b / (a + b)
        }
        val t2 = t * t
        val t3 = t2 * t
        val out = (2f * t3 - 3f * t2 + 1f) * v[segment] +
            (t3 - 2f * t2 + t) * tangent(segment) +
            (-2f * t3 + 3f * t2) * v[segment + 1] +
            (t3 - t2) * tangent(segment + 1)
        return out.coerceIn(0f, 1f)
    }

    fun table(points: CurvePoints): IntArray? {
        if (isIdentity(points)) return null
        return IntArray(256) { i -> (evaluate(points, i / 255f) * 255f).roundToInt().coerceIn(0, 255) }
    }

    data class Tables(
        val master: IntArray?, val red: IntArray?, val green: IntArray?, val blue: IntArray?
    ) {
        val isNeutral: Boolean get() = master == null && red == null && green == null && blue == null
        fun apply(argb: Int): Int {
            if (isNeutral) return argb
            val a = argb and -0x1000000
            fun channel(v: Int, per: IntArray?): Int {
                val masterValue = master?.get(v) ?: v
                return per?.get(masterValue) ?: masterValue
            }
            val r = channel((argb ushr 16) and 255, red)
            val g = channel((argb ushr 8) and 255, green)
            val b = channel(argb and 255, blue)
            return a or (r shl 16) or (g shl 8) or b
        }
    }

    fun prepare(grade: Grade) = Tables(
        table(grade.masterCurve),
        table(grade.redCurve),
        table(grade.greenCurve),
        table(grade.blueCurve)
    )

    fun get(grade: Grade, channel: CurveChannel): CurvePoints = when (channel) {
        CurveChannel.MASTER -> grade.masterCurve
        CurveChannel.RED -> grade.redCurve
        CurveChannel.GREEN -> grade.greenCurve
        CurveChannel.BLUE -> grade.blueCurve
    }

    fun put(grade: Grade, channel: CurveChannel, points: CurvePoints): Grade = when (channel) {
        CurveChannel.MASTER -> grade.copy(masterCurve = points)
        CurveChannel.RED -> grade.copy(redCurve = points)
        CurveChannel.GREEN -> grade.copy(greenCurve = points)
        CurveChannel.BLUE -> grade.copy(blueCurve = points)
    }
}
