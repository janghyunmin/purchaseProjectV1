package run.piece.domain.refactoring.common.repository

import kotlinx.coroutines.flow.Flow
import run.piece.domain.refactoring.common.model.AddressDefaultVo
import run.piece.domain.refactoring.common.model.JusoVo
import run.piece.domain.refactoring.common.vo.CommonFaqVo

interface CommonRepository {
    fun getSearchAddress(keyword: String,countPerPage: Int, currentPage: Int) : Flow<AddressDefaultVo>

    fun getFaqTabType(upperCode: String) : Flow<List<CommonFaqVo>>
}