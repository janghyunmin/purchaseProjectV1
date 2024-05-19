package run.piece.domain.refactoring.event.usecase

import run.piece.domain.refactoring.event.repository.EventRepository
class EventDetailGetUseCase(private val repository: EventRepository) {
    suspend operator fun invoke(eventId: String) = repository.getEventDetail(eventId)
}