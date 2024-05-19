package run.piece.dev.data.refactoring.ui.notice

import androidx.paging.PagingSource
import androidx.paging.PagingState
import run.piece.dev.data.refactoring.ui.notice.mapper.mapperToNoticeListVo
import run.piece.dev.data.refactoring.ui.notice.repository.remote.NoticeRemoteDataSource
import run.piece.dev.data.utils.default
import run.piece.domain.refactoring.notice.model.NoticeItemVo
import java.io.IOException

class NoticePagingSource(private val remoteDataSource: NoticeRemoteDataSource, private val boardType: String): PagingSource<Int, NoticeItemVo>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, NoticeItemVo> {
        // 첫번째 시작 페이지
        // 처음에 null 인 값을 고려하여 시작값 부여
        val page = params.key ?: PAGE

        return try {
            val next = params.key ?: 1
            val data = remoteDataSource.getNoticeList(page = next, boardType = boardType).data.boards.mapperToNoticeListVo().default()
            LoadResult.Page(
                data = data,
                prevKey = if(page == PAGE) null else page -1 ,
                nextKey = if(data.isNullOrEmpty()) null else page+1
            )
        } catch (e: IOException) {
            LoadResult.Error(e)
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, NoticeItemVo>): Int? {
        return state.anchorPosition?.let {
                pos -> state.closestPageToPosition(pos)?.prevKey?.plus(1) ?:
        state.closestPageToPosition(pos)?.nextKey?.minus(1)
        }
    }
}

private const val PAGE = 1 // 초기 페이지 상수 값