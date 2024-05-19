package run.piece.dev.data.refactoring.ui.question

import androidx.paging.PagingSource
import androidx.paging.PagingState
import run.piece.dev.data.refactoring.ui.question.mapper.mapperToQuestionListVo
import run.piece.dev.data.refactoring.ui.question.repository.remote.QuestionRemoteDataSource
import run.piece.dev.data.utils.default
import run.piece.domain.refactoring.question.model.QuestionItemVo
import java.io.IOException
class QuestionPagingSource(private val remoteDataSource: QuestionRemoteDataSource, private val boardType: String, private val boardCategory: String, private val apiVersion: String): PagingSource<Int, QuestionItemVo>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, QuestionItemVo> {

        val page = params.key ?: PAGE

        return try {
            val next = params.key ?: 1
            val data = remoteDataSource.getQuestionList(page = next, boardType = boardType, boardCategory = boardCategory, apiVersion = apiVersion).data.boards.mapperToQuestionListVo().default()
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

    override fun getRefreshKey(state: PagingState<Int, QuestionItemVo>): Int? {
        return state.anchorPosition?.let {
                pos -> state.closestPageToPosition(pos)?.prevKey?.plus(1) ?:
        state.closestPageToPosition(pos)?.nextKey?.minus(1)
        }
    }
}

private const val PAGE = 1 // 초기 페이지 상수 값