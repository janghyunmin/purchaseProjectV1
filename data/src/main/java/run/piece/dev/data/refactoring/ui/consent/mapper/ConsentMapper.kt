package run.piece.dev.data.refactoring.ui.consent.mapper

import run.piece.dev.data.refactoring.ui.consent.dto.ConsentDetailDto
import run.piece.dev.data.refactoring.ui.consent.dto.ConsentDto
import run.piece.dev.data.refactoring.ui.consent.dto.ConsentMemberDto
import run.piece.dev.data.refactoring.ui.consent.dto.ConsentMemberItemDto
import run.piece.dev.data.refactoring.ui.consent.dto.ConsentMemberListItemDto
import run.piece.dev.data.refactoring.ui.consent.dto.ConsentSendDto
import run.piece.dev.data.utils.default
import run.piece.domain.refactoring.consent.model.ConsentDetailVo
import run.piece.domain.refactoring.consent.model.ConsentVo
import run.piece.domain.refactoring.consent.model.TermsMemberItemVo
import run.piece.domain.refactoring.consent.model.TermsMemberListItemVo
import run.piece.domain.refactoring.consent.model.TermsMemberVo
import run.piece.domain.refactoring.consent.model.ConsentSendVo

fun List<ConsentDto>.mapperToConsentListVo(): List<ConsentVo> {
    val list = arrayListOf<ConsentVo>()
    forEach {
        list.add(it.mapperToConsentVo())
    }
    return list
}

fun ConsentDto.mapperToConsentVo(): ConsentVo = ConsentVo(
    consentCode = consentCode.default(),
    consentGroup = consentGroup.default(),
    consentTitle = consentTitle.default(),
    isMandatory = isMandatory.default(),
    isChecked = isChecked.default()
)

fun ConsentDetailDto.mapperToConsentDetailVo(): ConsentDetailVo = ConsentDetailVo(
    consentCode = consentCode.default(),
    consentGroup = consentGroup.default(),
    consentTitle = consentTitle.default(),
    consentContents = consentContents.default(),
    isMandatory = isMandatory.default(),
    displayOrder = displayOrder.default(),
    createdAt = createdAt.default()
)

fun ConsentMemberDto.mapperToMemberTermsConsentVo(): TermsMemberVo = TermsMemberVo(
    required = required.mapperToMemberTermsConsentItemVo(),
    selective = selective.mapperToMemberTermsConsentItemVo(),
    policy = policy.mapperToMemberTermsConsentItemVo()
)

fun ConsentMemberItemDto.mapperToMemberTermsConsentItemVo(): TermsMemberItemVo = TermsMemberItemVo(
    date = date.default(),
    consent = consent.mapperToMemberTermsConsentListItemVo()
)

fun List<ConsentMemberListItemDto?>.mapperToMemberTermsConsentListItemVo(): List<TermsMemberListItemVo> {
    val list = arrayListOf<TermsMemberListItemVo>()
    forEach {
        list.add(
            TermsMemberListItemVo(
                date = it?.date.default(),
                consentCode = it?.consentCode.default(),
                isAgreement = it?.isAgreement.default(),
                consentTitle = it?.consentTitle.default()
            )
        )
    }
    return list
}

fun ConsentSendDto.mapperToConsentSendVo(): ConsentSendVo {
    return ConsentSendVo(status = status.default(), statusCode = statusCode.default(), message = message.default())
}