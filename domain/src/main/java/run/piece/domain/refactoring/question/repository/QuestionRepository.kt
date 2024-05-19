package run.piece.domain.refactoring.question.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import run.piece.domain.refactoring.question.model.QuestionItemVo

interface QuestionRepository {
    suspend fun getQuestionList(boardType: String, boardCategory: String, apiVersion: String): Flow<PagingData<QuestionItemVo>>
}