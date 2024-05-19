package run.piece.dev.data.refactoring.ui.investment.dto

import com.google.gson.annotations.SerializedName

// 투자 성향 분석 요청 DTO
data class InvestMentDto(
    @SerializedName("resultId") var resultId: String?, // 결과 아이디
    @SerializedName("minScore") var minScore: Int?, // 최소 점수
    @SerializedName("maxScore") var maxScore: Int?, // 최대 점수
    @SerializedName("result") var result: String?, // 결과
    @SerializedName("description") var description: String?, // 설명
    @SerializedName("resultImagePath") var resultImagePath: String?, // 결과에 해당하는 이미지 경로
    @SerializedName("interestProductDescription") var interestProductDescription: String?, // 이런 금융상품에 관심이 많아요
    @SerializedName("memberId") var memberId: String?, // 고객 아이디
    @SerializedName("name") var name: String?, // 고객명
    @SerializedName("score") var score: Int?, // 투자성향 점수
    @SerializedName("count") var count: Int?, // createAt 기준 투자성향 분석 횟수
    @SerializedName("isVulnerableInvestors") var isVulnerableInvestors: String?,
    @SerializedName("createdAt") var createdAt: String? // 투자 성향 분석 일시
)

// 투자 성향 분석 질문/답변 목록 조회 DTO
data class InvestMentQuestionDto(
    @SerializedName("questionId") val questionId: String?,
    @SerializedName("question") val question: String?,
    @SerializedName("displayOrder") val displayOrder: Int?,
    @SerializedName("isMultiple") val isMultiple: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("answers") val answers: List<InvestMentAnswerDto>?
)

data class InvestMentAnswerDto(
    @SerializedName("questionId") val questionId: String?,
    @SerializedName("answerId") val answerId: String?,
    @SerializedName("answer") val answer: String?,
    @SerializedName("displayOrder") val displayOrder: Int?,
    @SerializedName("score") val score: Int?
)