package run.piece.dev.data.refactoring.ui.popup.mapper

import run.piece.dev.data.refactoring.ui.popup.dto.PopupDto
import run.piece.dev.data.utils.default
import run.piece.domain.refactoring.popup.model.PopupVo

fun PopupDto.mapperToPopupVo() : PopupVo = PopupVo(
    popupId = this.popupId.default(),
    popupTitle = this.popupTitle.default(),
    popupType = this.popupType.default(),
    popupTypeName = this.popupTypeName.default(),
    popupImagePath = this.popupImagePath.default(),
    popupLinkType = this.popupLinkType.default(),
    popupLinkTypeName = this.popupLinkTypeName.default(),
    popupLinkUrl = this.popupLinkUrl.default()
)