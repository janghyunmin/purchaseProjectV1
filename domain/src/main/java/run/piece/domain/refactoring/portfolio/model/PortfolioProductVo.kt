package run.piece.domain.refactoring.portfolio.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PortfolioProductVo(
    val productId: String,
    val title: String,
    val representThumbnailImagePath: String,
    val recruitmentAmount: String,
    val representImagePath: String,
    val owner: String,
    val productDate: String,
    val productScale: String,
    val productOther: String,
    val categoryId: String,
    val categoryName: String,
    val storageLocation: String,
    val storageCompany: String,
    val productAttachFiles: List<ProductAttachFileItemVo>,
    val productJoinBizInfo: ProductJoinBizInfoVo,
    val xcoordinates: String,
    val ycoordinates: String,
    var isClicked: Boolean
) : Parcelable

@Parcelize
data class ProductAttachFileItemVo(
    val attachFilePath: String,
    val attachFileCode: String,
    val attachFileCodeName: String
) : Parcelable

@Parcelize
data class ProductJoinBizInfoVo(
    val bizId: String,
    val bizName: String,
    val bizSubName: String,
    val bizThumbnailPath: String,
    val productJoinBizDetails: List<ProductJoinBizDetailVo>
) : Parcelable

@Parcelize
data class ProductJoinBizDetailVo(
    val seq: Int,
    val title: String,
    val description: String
) : Parcelable