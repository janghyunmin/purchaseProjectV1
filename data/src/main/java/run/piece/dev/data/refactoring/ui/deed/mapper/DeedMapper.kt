package run.piece.dev.data.refactoring.ui.deed.mapper
import run.piece.dev.data.refactoring.ui.deed.model.MemberDocumentModel
import run.piece.domain.refactoring.deed.model.MemberDocumentVo

fun MemberDocumentVo.mapperToMemberDocumentDTO(): MemberDocumentModel = MemberDocumentModel(memberId, purchaseId, sendDvn)