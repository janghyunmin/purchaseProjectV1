package run.piece.dev.data.refactoring.ui.faq

import androidx.paging.PagingSource
import androidx.paging.PagingState
import run.piece.dev.data.refactoring.ui.faq.mapper.mapperToFaqListVo
import run.piece.dev.data.refactoring.ui.faq.repository.remote.FaqRemoteDataSource
import run.piece.dev.data.utils.default
import run.piece.domain.refactoring.faq.model.FaqItemVo
import java.io.IOException

class FaqPagingSource(private val remoteDataSource: FaqRemoteDataSource, private val boardType: String, private val boardCategory: String, private val apiVersion: String): PagingSource<Int, FaqItemVo>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, FaqItemVo> {
        // 첫번째 시작 페이지
        // 처음에 null 인 값을 고려하여 시작값 부여
        val page = params.key ?: PAGE

        return try {
            val next = params.key ?: 1
            val data = remoteDataSource.getFaqList(page = next, boardType = boardType, boardCategory = boardCategory, apiVersion = apiVersion).data.boards.mapperToFaqListVo().default()
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

    override fun getRefreshKey(state: PagingState<Int, FaqItemVo>): Int? {
        return state.anchorPosition?.let {
                pos -> state.closestPageToPosition(pos)?.prevKey?.plus(1) ?:
        state.closestPageToPosition(pos)?.nextKey?.minus(1)
        }
    }
}

private const val PAGE = 1 // 초기 페이지 상수 값