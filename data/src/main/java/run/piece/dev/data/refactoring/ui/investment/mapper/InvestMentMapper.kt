package run.piece.dev.data.refactoring.ui.investment.mapper

import run.piece.dev.data.refactoring.ui.investment.dto.InvestMentAnswerDto
import run.piece.dev.data.refactoring.ui.investment.dto.InvestMentDto
import run.piece.dev.data.refactoring.ui.investment.dto.InvestMentQuestionDto
import run.piece.dev.data.utils.default
import run.piece.domain.refactoring.investment.model.InvestMentVo
import run.piece.domain.refactoring.investment.model.InvestmentAnswerVo
import run.piece.domain.refactoring.investment.model.InvestmentQuestionVo

fun InvestMentDto.mapperToInvestMentVo(): InvestMentVo = InvestMentVo(
    resultId = this.resultId.default(),
    minScore = this.minScore.default(),
    maxScore = this.maxScore.default(),
    result = this.result.default(),
    description = this.description.default(),
    resultImagePath = this.resultImagePath.default(),
    interestProductDescription = this.interestProductDescription.default(),
    memberId = this.memberId.default(),
    name = this.name.default(),
    score = this.score.default(),
    count = this.count.default(),
    isVulnerableInvestors = this.isVulnerableInvestors.default(),
    createdAt = this.createdAt.default()
)

fun List<InvestMentQuestionDto>.mapperToInvestMentQuestionVo(): List<InvestmentQuestionVo> {
    val list = arrayListOf<InvestmentQuestionVo>()
    forEach {
        it.let { questionDto ->
            list.add(
                InvestmentQuestionVo(
                    questionDto.questionId.default(),
                    questionDto.question.default(),
                    questionDto.displayOrder.default(),
                    questionDto.isMultiple.default(),
                    questionDto.description.default(),
                    questionDto.answers?.mapperToInvestMentAnswerVo() ?: emptyList(),
                    - 1,
                    false
                )
            )
        }
    }
    return list
}

fun List<InvestMentAnswerDto>.mapperToInvestMentAnswerVo(): List<InvestmentAnswerVo> {
    val list = arrayListOf<InvestmentAnswerVo>()
    forEach {
        list.add(
            InvestmentAnswerVo(
                it.questionId.default(),
                it.answerId.default(),
                it.answer.default(),
                it.displayOrder.default(),
                it.score.default(),
                false
            )
        )
    }
    return list
}