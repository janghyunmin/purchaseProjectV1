package run.piece.dev.refactoring.widget.custom.pdf.interfaces

import java.io.IOException

interface OnErrorListener {

    fun onFileLoadError(e : Exception)

    fun onAttachViewError(e : Exception)

    fun onPdfRendererError(e : IOException)
}