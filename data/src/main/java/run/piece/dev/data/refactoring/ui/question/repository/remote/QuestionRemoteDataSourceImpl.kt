package run.piece.dev.data.refactoring.ui.question.repository.remote

import run.piece.dev.data.refactoring.ui.question.api.QuestionApi
import run.piece.dev.data.refactoring.ui.question.dto.QuestionListDto
import run.piece.dev.data.utils.WrappedResponse

class QuestionRemoteDataSourceImpl(private val questionApi: QuestionApi): QuestionRemoteDataSource {
    override suspend fun getQuestionList(page: Int, boardType: String, boardCategory: String, apiVersion: String): WrappedResponse<QuestionListDto> = questionApi.getQuestionList(page, boardType, boardCategory, apiVersion)
}