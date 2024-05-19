package run.piece.dev.data.refactoring.ui.common.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import run.piece.dev.data.refactoring.ui.common.mapper.mapperToAddressItemVo
import run.piece.dev.data.refactoring.ui.common.mapper.mapperToAddressVo
import run.piece.dev.data.refactoring.ui.common.mapper.mapperToCommonFaqVo
import run.piece.dev.data.refactoring.ui.common.repository.local.CommonLocalDataSource
import run.piece.dev.data.refactoring.ui.common.repository.remote.CommonRemoteDataSource
import run.piece.domain.refactoring.common.model.AddressDefaultVo
import run.piece.domain.refactoring.common.repository.CommonRepository
import run.piece.domain.refactoring.common.vo.CommonFaqVo

class CommonRepositoryImpl(
    private val localDataSource: CommonLocalDataSource,
    private val remoteDataSource: CommonRemoteDataSource
) : CommonRepository {

    override fun getSearchAddress(keyword: String,countPerPage: Int, currentPage : Int): Flow<AddressDefaultVo> =
        flow {
            emit(
                remoteDataSource.getSearchAddress(
                    keyword = keyword,
                    countPerPage = countPerPage,
                    currentPage = currentPage
                ).data.mapperToAddressVo()
            )
        }

    override fun getFaqTabType(upperCode: String): Flow<List<CommonFaqVo>> =
        flow {
            emit(
                remoteDataSource.getFaqTabType(
                    upperCode = upperCode
                ).data.mapperToCommonFaqVo()
            )
        }
}