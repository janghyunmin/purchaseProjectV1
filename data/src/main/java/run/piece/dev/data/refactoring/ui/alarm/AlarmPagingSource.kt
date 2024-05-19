//package run.piece.dev.data.refactoring.ui.alarm
//
//import android.util.Log
//import androidx.paging.PagingSource
//import androidx.paging.PagingState
//import run.piece.dev.data.refactoring.ui.alarm.mapper.mapperToAlarmItemVo
//import run.piece.dev.data.refactoring.ui.alarm.repository.local.AlarmLocalDataSource
//import run.piece.dev.data.refactoring.ui.alarm.repository.remote.AlarmRemoteDataSource
//import run.piece.dev.data.utils.default
//import run.piece.domain.refactoring.alarm.model.AlarmItemVo
//import run.piece.domain.refactoring.portfolio.model.PortfolioVo
//import java.io.IOException
//
//class AlarmPagingSource(private val remoteDataSource: AlarmRemoteDataSource,
//                        private val accessToken: String,
//                        private val deviceId: String,
//                        private val memberId: String,
//                        private val type: String): PagingSource<Int, AlarmItemVo>() {
//
//    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, AlarmItemVo> {
//        val next = params.key ?: 1
//        return try {
//            val data = remoteDataSource.getAlarm(accessToken, deviceId, memberId, next, type).data.alarms.default().mapperToAlarmItemVo()
//            LoadResult.Page(
//                data,
//                prevKey = if (next == 1) null else next - 1,
//                nextKey = if (data.isEmpty()) null else next + 1
//            )
//        } catch (exception: IOException) {
//            return LoadResult.Error(exception)
//        } catch (exception: HttpException) {
//            return LoadResult.Error(exception)
//        }
//    }
//    override fun getRefreshKey(state: PagingState<Int, AlarmItemVo>): Int? {
//        return state.anchorPosition?.let { anchorPosition ->
//            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
//                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
//        }
//    }
//}