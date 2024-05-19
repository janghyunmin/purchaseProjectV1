package run.piece.dev.data.refactoring.ui.magazine

import androidx.paging.PagingSource
import androidx.paging.PagingState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import run.piece.dev.data.refactoring.ui.magazine.mapper.mapperToMagazineItemVo
import run.piece.dev.data.refactoring.ui.magazine.repository.remote.MagazineRemoteDataSource
import run.piece.dev.data.utils.default
import run.piece.domain.refactoring.magazine.vo.MagazineItemVo
import java.io.IOException

class MemberMagazinePagingSource(
    private val remoteDataSource: MagazineRemoteDataSource,
    private val accessToken: String,
    private val deviceId: String,
    private val memberId: String,
    private val magazineType: String
) : PagingSource<Int, MagazineItemVo>() {
    override fun getRefreshKey(state: PagingState<Int, MagazineItemVo>): Int? {
        // Try to find the page key of the closest page to anchorPosition, from
        // either the prevKey or the nextKey, but you need to handle nullability
        // here:
        //  * prevKey == null -> anchorPage가 현재 첫 번째 페이지임
        //  * nextKey == null -> anchorPage가 현재 마지막 페이지임
        //  * both prevKey and nextKey null -> anchorPage가 최초의 상태이기 때문에 그냥 null을 반환한다
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, MagazineItemVo> {
        return try {
            val next = params.key ?: 1
            var data: List<MagazineItemVo>? = null

//            CoroutineScope(Dispatchers.IO).launch {
//                data = remoteDataSource.getMemberMagazine(
//                    accessToken = accessToken,
//                    deviceId = deviceId,
//                    memberId = memberId,
//                    magazineType = magazineType,
//                    page = next
//                ).data.magazines?.mapperToMagazineItemVo().default()
//            }.join()

            //prevKey = if(next == PAGE) null else next -1 ,
            LoadResult.Page(
                data = data!!,
                prevKey = null,
                nextKey = if (data.isNullOrEmpty()) null else next + 1
            )
        } catch (e: IOException) {
            LoadResult.Error(e)
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    companion object {
        private const val PAGE = 1 // 초기 페이지 상수 값
    }
}
