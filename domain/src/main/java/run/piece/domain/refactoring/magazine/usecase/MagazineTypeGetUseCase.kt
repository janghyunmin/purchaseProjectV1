package run.piece.domain.refactoring.magazine.usecase

import run.piece.domain.refactoring.magazine.repository.MagazineRepository

class MagazineTypeGetUseCase(private val repository: MagazineRepository) {
    operator fun invoke() = repository.getMagazineType()
}