package run.piece.dev.data.refactoring.ui.portfolio.dto

import com.google.gson.annotations.SerializedName

data class PortfolioProductDto(
    @SerializedName("productId") var productId: String?,
    @SerializedName("title") var title: String?,
    @SerializedName("representThumbnailImagePath") var representThumbnailImagePath: String?,
    @SerializedName("recruitmentAmount") var recruitmentAmount: String?,
    @SerializedName("representImagePath") var representImagePath: String?,
    @SerializedName("owner") var owner: String?,
    @SerializedName("productDate") var productDate: String?,
    @SerializedName("productScale") var productScale: String?,
    @SerializedName("productOther") var productOther: String?,
    @SerializedName("categoryId") var categoryId: String?,
    @SerializedName("categoryName") var categoryName: String?,
    @SerializedName("storageLocation") var storageLocation: String?,
    @SerializedName("storageCompany") var storageCompany: String?,
    @SerializedName("productAttachFiles") var productAttachFiles: List<ProductAttachFileItemDto>,
    @SerializedName("productJoinBizInfo") var productJoinBizInfo: ProductJoinBizInfoDto,
    @SerializedName("xcoordinates") var xcoordinates: String?,
    @SerializedName("ycoordinates") var ycoordinates: String?
)

data class ProductAttachFileItemDto(var attachFilePath: String?,
                                    var attachFileCode: String?,
                                    var attachFileCodeName: String?)

data class ProductJoinBizInfoDto(var bizId: String?,
                                 var bizName: String?,
                                 var bizSubName: String?,
                                 var bizThumbnailPath: String?,
                                 var productJoinBizDetails: List<ProductJoinBizDetailDto>)

data class ProductJoinBizDetailDto(var seq: Int?,
                                   var title: String?,
                                   var description: String?)