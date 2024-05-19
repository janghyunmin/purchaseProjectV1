package run.piece.dev.data.refactoring.ui.question.repository.remote

import run.piece.dev.data.refactoring.ui.question.dto.QuestionListDto
import run.piece.dev.data.utils.WrappedResponse

interface QuestionRemoteDataSource {
    suspend fun getQuestionList(page:Int, boardType: String, boardCategory: String, apiVersion: String): WrappedResponse<QuestionListDto>
}