package br.com.movimentoemfoco.app.media

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import kotlin.math.max

/** Carrega uma imagem da galeria respeitando a orientação EXIF e limitando o tamanho. */
object ImageLoader {

    fun carregar(context: Context, uri: Uri, ladoMaximo: Int = 1280): Bitmap? {
        val resolver = context.contentResolver
        return try {
            val orientacao = resolver.openInputStream(uri)?.use {
                ExifInterface(it).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            } ?: ExifInterface.ORIENTATION_NORMAL

            val limites = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, limites) }
            if (limites.outWidth <= 0 || limites.outHeight <= 0) return null

            var amostra = 1
            while (max(limites.outWidth, limites.outHeight) / (amostra * 2) >= ladoMaximo) amostra *= 2

            val opcoes = BitmapFactory.Options().apply {
                inSampleSize = amostra
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            val bruto = resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opcoes) }
                ?: return null

            val matriz = Matrix()
            when (orientacao) {
                ExifInterface.ORIENTATION_ROTATE_90 -> matriz.postRotate(90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> matriz.postRotate(180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> matriz.postRotate(270f)
                ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matriz.postScale(-1f, 1f)
                ExifInterface.ORIENTATION_FLIP_VERTICAL -> matriz.postScale(1f, -1f)
                ExifInterface.ORIENTATION_TRANSPOSE -> { matriz.postRotate(90f); matriz.postScale(-1f, 1f) }
                ExifInterface.ORIENTATION_TRANSVERSE -> { matriz.postRotate(270f); matriz.postScale(-1f, 1f) }
                else -> return bruto
            }
            Bitmap.createBitmap(bruto, 0, 0, bruto.width, bruto.height, matriz, true)
        } catch (e: Exception) {
            null
        }
    }

    /** Reduz mantendo a proporção se o maior lado passar de [ladoMaximo]. Pode devolver o próprio bitmap. */
    fun reduzir(bitmap: Bitmap, ladoMaximo: Int): Bitmap {
        val maior = max(bitmap.width, bitmap.height)
        if (maior <= ladoMaximo) return bitmap
        val escala = ladoMaximo.toFloat() / maior
        val novo = Bitmap.createScaledBitmap(
            bitmap,
            (bitmap.width * escala).toInt().coerceAtLeast(1),
            (bitmap.height * escala).toInt().coerceAtLeast(1),
            true,
        )
        if (novo !== bitmap) bitmap.recycle()
        return novo
    }
}
