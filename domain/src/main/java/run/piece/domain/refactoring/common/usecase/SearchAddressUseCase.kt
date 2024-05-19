package run.piece.domain.refactoring.common.usecase

import run.piece.domain.refactoring.common.repository.CommonRepository

class SearchAddressUseCase(private val repository: CommonRepository) {
    fun getSearchAddress(keyword: String, countPerPage: Int, currentPage: Int) = repository.getSearchAddress(keyword = keyword,countPerPage = countPerPage, currentPage = currentPage)
}