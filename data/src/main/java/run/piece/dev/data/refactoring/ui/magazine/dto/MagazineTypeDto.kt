package run.piece.dev.data.refactoring.ui.magazine.dto

import com.google.gson.annotations.SerializedName

data class MagazineTypeDto(
    @SerializedName("magazineType") var magazineType: String,
    @SerializedName("magazineTypeName") var magazineTypeName: String
)