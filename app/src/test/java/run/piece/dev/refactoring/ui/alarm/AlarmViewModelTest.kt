package run.piece.dev.refactoring.ui.alarm

import com.google.gson.Gson
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Rule
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import run.piece.dev.data.refactoring.ui.alarm.dto.AlarmDto
import run.piece.dev.data.refactoring.ui.alarm.mapper.mapperToAlarmItemVo
import run.piece.dev.refactoring.JsonFileReader
import run.piece.dev.refactoring.MainDispatcherRule
import run.piece.domain.refactoring.alarm.model.AlarmItemVo
import run.piece.domain.refactoring.alarm.usecase.AlarmListGetUseCase
import run.piece.domain.refactoring.alarm.usecase.AlarmPutUseCase

@ExperimentalCoroutinesApi // 실험적인 코루틴 API를 사용하는 테스트 코드임을 나타냅니다.
internal class AlarmViewModelTest {
    private val gson: Gson = spyk() // Gson 모의체 생성
    private val filePath = "../app/src/main/assets/tests/alarm_list.json" // JSON 파일 경로 설정

    // 단위 테스트를 위한 기본 코루틴 디스패처 설정
    @ExperimentalCoroutinesApi
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(Dispatchers.Unconfined)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }
    @ExperimentalCoroutinesApi
    @Test
    fun `getAlarm API 테스트 성공`() = runTest {
        // 의존성을 목업(Mock)으로 대체
        val alarmListGetUseCase = mockk<AlarmListGetUseCase>()

        // 테스트할 AlarmViewModel 인스턴스 생성
        val alarmViewModel = AlarmViewModel(alarmPutUseCase = mockk(), alarmListGetUseCase = alarmListGetUseCase)

        // JSON 파일을 읽어 객체로 변환하는 모의체 생성
        val jsonFileReader = JsonFileReader(gson, AlarmDto::class.java)
        val jsonObject = jsonFileReader.readJsonFileAndConvertToObject(filePath)

        // 모의 데이터 생성
        val expectedList = jsonObject?.alarms?.mapperToAlarmItemVo() as List<AlarmItemVo>

        // AlarmListGetUseCase 호출에 대한 응답 설정
        coEvery {
            alarmListGetUseCase(any(), any(), any(), any())
        } returns flowOf(expectedList)

        // 테스트할 함수 호출
        alarmViewModel.getAlarm("테스트")

        // 상태가 올바르게 업데이트되었는지 확인
        advanceTimeBy(1000) // 시간이 지났다고 가정 (필요에 따라 조정)

        assertEquals(AlarmViewModel.AlarmGetState.Success("테스트", expectedList), alarmViewModel.alarmList.value)

        // 의존성이 올바르게 호출되었는지 확인
        verify { alarmListGetUseCase(any(), any(), any(), any()) }

        // 호출 횟수 및 인자 확인
        coVerify(exactly = 1) {
            alarmListGetUseCase(any(), any(), any(), any())
        }
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `getAlarm API 테스트 실패`() = runTest {
        // 의존성을 목업(Mock)으로 대체
        val alarmListGetUseCase = mockk<AlarmListGetUseCase>()

        // 테스트할 AlarmViewModel 인스턴스 생성
        val alarmViewModel = AlarmViewModel(alarmPutUseCase = mockk(), alarmListGetUseCase = alarmListGetUseCase)

        // JSON 파일을 읽어 객체로 변환하는 모의체 생성
        val jsonFileReader = JsonFileReader(gson, AlarmDto::class.java)
        val jsonObject = jsonFileReader.readJsonFileAndConvertToObject(filePath)

        // 모의 데이터 생성
        val expectedList = jsonObject?.alarms?.mapperToAlarmItemVo() as List<AlarmItemVo>

        // AlarmListGetUseCase 호출에 대한 응답 설정
        coEvery {
            alarmListGetUseCase(any(), any(), any(), any())
        } returns flowOf(expectedList)

        // 테스트할 함수 호출
        alarmViewModel.getAlarm("테스트")

        // 상태가 올바르게 업데이트되었는지 확인
        advanceTimeBy(1000) // 시간이 지났다고 가정 (필요에 따라 조정)

        // ***** 실패 케이스 *****
        assertEquals(AlarmViewModel.AlarmGetState.Failure("테스트에 실패 했습니다"), alarmViewModel.alarmList.value)

        // 의존성이 올바르게 호출되었는지 확인
        verify { alarmListGetUseCase(any(), any(), any(), any()) }

        // 호출 횟수 및 인자 확인
        coVerify(exactly = 1) {
            alarmListGetUseCase(any(), any(), any(), any())
        }
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `putAlarm API 테스트 성공`() = runTest {
        // 의존성을 목업(Mock)으로 대체
        val alarmPutUseCase = mockk<AlarmPutUseCase>()
        val alarmViewModel = AlarmViewModel(alarmPutUseCase = alarmPutUseCase, alarmListGetUseCase = mockk())

        // 테스트용 데이터 목업(Mock) 설정
        coEvery { alarmPutUseCase(any(), any(), any()) } returns flowOf(/* 여기에 테스트 데이터 입력 */)

        // 테스트할 함수 호출
        alarmViewModel.putAlarm()

        // 상태가 올바르게 업데이트되었는지 확인
        advanceTimeBy(1000) // 몇 초가 지났다고 가정합니다 (필요에 따라 조정)

        assertEquals(AlarmViewModel.AlarmPutState.IsLoading(true), alarmViewModel.putAlarm.value)

        // 의존성이 올바르게 호출되었는지 확인
        verify { alarmPutUseCase(any(), any(), any()) }

        // 호출 횟수 및 인자 확인
        coVerify(exactly = 1) {
            alarmPutUseCase(any(), any(), any())
        }
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `putAlarm API 테스트 실패`() = runTest {
        // 의존성을 목업(Mock)으로 대체
        val alarmPutUseCase = mockk<AlarmPutUseCase>()
        val alarmViewModel = AlarmViewModel(alarmPutUseCase = alarmPutUseCase, alarmListGetUseCase = mockk())

        // 실패하는 경우를 목업(Mock)으로 설정
        coEvery { alarmPutUseCase(any(), any(), any()) } returns flow {
            throw RuntimeException("통신 실패") // 통신 실패를 시뮬레이션하기 위해 예외를 던집니다.
        }

        // 테스트할 함수 호출
        alarmViewModel.putAlarm()

        // 상태가 올바르게 업데이트되었는지 확인
        advanceTimeBy(1000) // 몇 초가 지났다고 가정합니다 (필요에 따라 조정) //성공

        assertEquals(AlarmViewModel.AlarmPutState.IsLoading(false), alarmViewModel.putAlarm.value) //실패
        assertEquals(AlarmViewModel.AlarmPutState.Failure("통신 실패"), alarmViewModel.putAlarm.value) //성공

        // 의존성이 올바르게 호출되었는지 확인
        verify { alarmPutUseCase(any(), any(), any()) } //성공

        // 호출 횟수 및 인자 확인
        coVerify(exactly = 1) {
            alarmPutUseCase(any(), any(), any()) //성공
        }
    }
}