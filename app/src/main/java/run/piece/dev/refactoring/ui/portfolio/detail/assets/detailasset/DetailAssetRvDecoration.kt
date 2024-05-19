package run.piece.dev.refactoring.ui.portfolio.detail.assets.detailasset

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView


class DetailAssetRvDecoration(private val space: Int, private val itemSize: Int) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)

        val position = parent.getChildAdapterPosition(view)
        outRect.left = space

        if (position == itemSize - 1) {
            outRect.right = space
        }
    }
}