package run.piece.domain.refactoring.event.usecase

import run.piece.domain.refactoring.event.repository.EventRepository
class EventListGetUseCase(private val repository: EventRepository) {
    suspend operator fun invoke() = repository.getEventList()
}