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

class NotMemberMagazinePagingSource(
    private val remoteDataSource: MagazineRemoteDataSource,
    private val magazineType: String
) : PagingSource<Int, MagazineItemVo>() {
    override fun getRefreshKey(state: PagingState<Int, MagazineItemVo>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1) ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, MagazineItemVo> {
        return try {
            val next = params.key ?: 1
            var data: List<MagazineItemVo>? = null

//            CoroutineScope(Dispatchers.IO).launch {
//                data = remoteDataSource.getNotMemberMagazine(
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

