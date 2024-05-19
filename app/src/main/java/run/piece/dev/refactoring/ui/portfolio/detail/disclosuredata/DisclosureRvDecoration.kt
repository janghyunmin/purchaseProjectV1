package run.piece.dev.refactoring.ui.portfolio.detail.disclosuredata

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class DisclosureRvDecoration(private val viewSpace: Int, private val itemSpace: Int, private val itemSize: Int) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)

        val position = parent.getChildAdapterPosition(view)
        outRect.left = itemSpace

        if (position == itemSize - 1) {
            outRect.right = viewSpace
        } else if (position == 0) {
            outRect.left = viewSpace
        }
    }
}