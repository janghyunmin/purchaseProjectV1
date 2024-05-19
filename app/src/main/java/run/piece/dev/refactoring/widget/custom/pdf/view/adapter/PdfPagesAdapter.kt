package run.piece.dev.refactoring.widget.custom.pdf.view.adapter

import android.annotation.SuppressLint
import android.graphics.Bitmap
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import kotlinx.coroutines.CoroutineDispatcher
import run.piece.dev.refactoring.widget.custom.pdf.renderer.PdfPageRenderer
import run.piece.dev.refactoring.widget.custom.pdf.utils.PdfPageQuality
import java.io.File

abstract class PdfPagesAdapter<T : PdfPageViewHolder>(
    private val pdfFile: File,
    private val quality: PdfPageQuality,
    private val dispatcher: CoroutineDispatcher,
) : ListAdapter<Bitmap, T>(DiffCallback()) {

    private val pdfPageRenderer: PdfPageRenderer by lazy {
        PdfPageRenderer(pdfFile, quality, dispatcher)
    }

    suspend fun renderPage(position: Int): Result<Bitmap> {
        return pdfPageRenderer.render(position)
    }

    override fun getItemCount(): Int {
        return pdfPageRenderer.pageCount
    }

    class DiffCallback: DiffUtil.ItemCallback<Bitmap>() {
        override fun areItemsTheSame(oldItem: Bitmap, newItem: Bitmap): Boolean {
            return oldItem === newItem
        }

        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(oldItem: Bitmap, newItem: Bitmap): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }
}