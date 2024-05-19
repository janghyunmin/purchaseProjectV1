package run.piece.dev.refactoring.ui.portfolio.detail

import androidx.lifecycle.SavedStateHandle
import com.google.gson.Gson
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Rule
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import run.piece.dev.data.refactoring.module.ResourcesProvider
import run.piece.dev.data.refactoring.ui.portfolio.dto.PortfolioDetailDto
import run.piece.dev.data.refactoring.ui.portfolio.mapper.mapperToPortfolioDetailVo
import run.piece.dev.refactoring.JsonFileReader
import run.piece.dev.refactoring.MainDispatcherRule
import run.piece.domain.refactoring.portfolio.model.PortfolioDetailVo
import run.piece.domain.refactoring.portfolio.usecase.PortfolioDetailGetUseCase

@ExperimentalCoroutinesApi
internal class PortfolioDetailNewViewModelTest {
    // 단위 테스트를 위한 기본 코루틴 디스패처 설정
    @ExperimentalCoroutinesApi
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val savedStateHandle: SavedStateHandle = mockk() // 저장된 상태 핸들 mock 생성
    private val resourcesProvider = mockk<ResourcesProvider>() // 리소스 제공자 mock 생성
    private val portfolioDetailGetUseCase = mockk<PortfolioDetailGetUseCase>() // 포트폴리오 디테일 조회 UseCase mock 생성
    private val gson: Gson = spyk()

    private val filePath = "../app/src/main/assets/tests/portfolio_detail.json"

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
    fun `포트폴리오 상세 공모총액은 10,000,000원이고 공모가액은 20,000원이다`() = runTest {
        // given

        every { savedStateHandle.get<String>("portfolioId") } returns "portfolioId"

        // ViewModel에 필요한 의존성 주입
        val viewModel = PortfolioDetailNewViewModel(
            savedStateHandle,
            resourcesProvider,
            portfolioDetailGetUseCase,
            mockk(), // 나머지 의존성은 필요에 따라 mock 또는 실제 인스턴스로 대체
            mockk(),
            mockk(),
            mockk(),
            mockk()
        )

        // JSON 파일을 읽어 객체로 변환하는 모의체 생성
        val jsonFileReader = JsonFileReader(gson, PortfolioDetailDto::class.java)
        val jsonObject = jsonFileReader.readJsonFileAndConvertToObject(filePath)

        // 모의 데이터 생성
        val expectedObject = jsonObject?.mapperToPortfolioDetailVo() as PortfolioDetailVo

        // 포트폴리오 디테일 조회 UseCase의 응답을 모의 데이터로 설정
        coEvery {
            portfolioDetailGetUseCase(any(), any(), any())
        } returns flowOf(expectedObject)

        // when
        // ViewModel의 getPortfolioDetail 함수 호출
        viewModel.getPortfolioDetail()

        // then
        // 함수 호출 후 상태가 올바르게 업데이트되었는지 확인
        assertEquals(PortfolioDetailNewViewModel.PortfolioDetailState.Success(expectedObject).portfolioDetailVo.portfolioStock.recruitmentAmount, "10000000")
        assertEquals(PortfolioDetailNewViewModel.PortfolioDetailState.Success(expectedObject).portfolioDetailVo.portfolioStock.faceValue, "20000")

        // 포트폴리오 디테일 조회 UseCase이 올바르게 호출되었는지 확인
        coVerify {
            portfolioDetailGetUseCase(
                memberId = any(),
                apiVersion = any(),
                id = "portfolioId"
            )
        }
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `getPortfolioDetail API 테스트 성공`() = runTest {
        // given
        // 저장된 상태 핸들 mock 생성
        every { savedStateHandle.get<String>("portfolioId") } returns "portfolioId"

        // ViewModel에 필요한 의존성 주입
        val viewModel = PortfolioDetailNewViewModel(
            savedStateHandle,
            resourcesProvider,
            portfolioDetailGetUseCase,
            mockk(), // 나머지 의존성은 필요에 따라 mock 또는 실제 인스턴스로 대체
            mockk(),
            mockk(),
            mockk(),
            mockk()
        )

        // JSON 파일을 읽어 객체로 변환하는 모의체 생성
        val jsonFileReader = JsonFileReader(gson, PortfolioDetailDto::class.java)
        val jsonObject = jsonFileReader.readJsonFileAndConvertToObject(filePath)

        // 모의 데이터 생성
        val expectedObject = jsonObject?.mapperToPortfolioDetailVo() as PortfolioDetailVo

        // 포트폴리오 디테일 조회 UseCase의 응답을 모의 데이터로 설정
        coEvery {
            portfolioDetailGetUseCase(any(), any(), any())
        } returns flowOf(expectedObject)

        // when
        // ViewModel의 getPortfolioDetail 함수 호출
        viewModel.getPortfolioDetail()

        // then
        // 함수 호출 후 상태가 올바르게 업데이트되었는지 확인
        assertEquals(PortfolioDetailNewViewModel.PortfolioDetailState.Success(expectedObject), viewModel.portfolioDetail.value)

        // 포트폴리오 디테일 조회 UseCase이 올바르게 호출되었는지 확인
        coVerify {
            portfolioDetailGetUseCase(
                memberId = any(),
                apiVersion = any(),
                id = "portfolioId"
            )
        }
    }
}