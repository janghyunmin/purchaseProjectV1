package run.piece.domain.refactoring.member.usecase

import retrofit2.HttpException
import run.piece.domain.refactoring.member.repository.MemberRepository

class GetAuthPinUseCase(private val repository: MemberRepository) {
    //operator fun invoke(accessToken: String, deviceId: String, memberId: String, pinNumber: String) = repository.getAuthPin(accessToken = accessToken, deviceId = deviceId, memberId = memberId, pinNumber = pinNumber)
    fun authPinVerify(accessToken: String, deviceId: String, memberId: String, pinNumber: String) = repository.getAuthPin(accessToken = accessToken, deviceId = deviceId , memberId = memberId , pinNumber = pinNumber)

}