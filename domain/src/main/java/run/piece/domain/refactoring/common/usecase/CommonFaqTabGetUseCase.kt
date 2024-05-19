package run.piece.domain.refactoring.common.usecase

import run.piece.domain.refactoring.common.repository.CommonRepository

class CommonFaqTabGetUseCase(private val repository: CommonRepository) {
    operator fun invoke() = repository.getFaqTabType(upperCode = "BRT03")
}