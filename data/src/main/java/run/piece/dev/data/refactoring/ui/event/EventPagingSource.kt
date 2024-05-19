package run.piece.dev.data.refactoring.ui.event

import androidx.paging.PagingSource
import androidx.paging.PagingState
import run.piece.dev.data.refactoring.ui.event.mapper.mapperToEventItemListVo
import run.piece.dev.data.refactoring.ui.event.repository.remote.EventRemoteDataSource
import run.piece.domain.refactoring.event.model.EventItemVo

import java.io.IOException
class EventPagingSource(private val remoteDataSource: EventRemoteDataSource): PagingSource<Int, EventItemVo>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, EventItemVo> {

        val page = params.key ?: PAGE

        return try {
            val next = params.key ?: 1
            val data = remoteDataSource.getEventList(page = next).data.events.mapperToEventItemListVo()
            LoadResult.Page(
                data = data,
                prevKey = if (page == PAGE) null else page - 1,
                nextKey = if (data.isNullOrEmpty()) null else page + 1
            )
        } catch (e: IOException) {
            LoadResult.Error(e)
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, EventItemVo>): Int? {
        return state.anchorPosition?.let {
                pos -> state.closestPageToPosition(pos)?.prevKey?.plus(1) ?:
        state.closestPageToPosition(pos)?.nextKey?.minus(1)
        }
    }
}

private const val PAGE = 1 // 초기 페이지 상수 값