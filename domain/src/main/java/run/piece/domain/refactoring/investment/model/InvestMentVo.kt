package run.piece.domain.refactoring.investment.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


// 투자 성향 분석 요청 Vo
@Parcelize
class InvestMentVo(
    val resultId: String, // 결과 아이디
    val minScore: Int, // 최소 점수
    val maxScore: Int, // 최대 점수
    val result: String, // 결과
    val description: String, // 설명
    val resultImagePath: String, // 결과에 해당하는 이미지 경로
    val interestProductDescription: String, // 이런 금융상품에 관심이 많아요
    val memberId: String, // 고객아이디
    val name: String, // 고갹명
    val score: Int, // 투자성향 점수
    val count: Int, // createdAt 기준 투자성향 분석 횟수
    val isVulnerableInvestors: String,
    val createdAt: String // 투자 성향 분석 일시
) : Parcelable


// 투자 성향 분석 질문/답변 Vo
@Parcelize
data class InvestmentQuestionVo(
    val questionId: String,
    val question: String,
    val displayOrder: Int,
    val isMultiple: String,
    val description: String,
    val answers: List<InvestmentAnswerVo>,
    var answerPosition: Int,
    var isBack: Boolean
): Parcelable

@Parcelize
data class InvestmentAnswerVo(
    val questionId: String,
    val answerId: String,
    val answer: String,
    val displayOrder: Int,
    val score: Int,
    var isSelected: Boolean,
): Parcelable
