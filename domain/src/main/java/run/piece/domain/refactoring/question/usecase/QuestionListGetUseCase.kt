package run.piece.domain.refactoring.question.usecase

import run.piece.domain.refactoring.question.repository.QuestionRepository

class QuestionListGetUseCase(private val repository: QuestionRepository) {
    suspend operator fun invoke(boardType: String, boardCategory: String, apiVersion: String) = repository.getQuestionList(boardType = boardType, boardCategory = boardCategory, apiVersion = apiVersion)
}