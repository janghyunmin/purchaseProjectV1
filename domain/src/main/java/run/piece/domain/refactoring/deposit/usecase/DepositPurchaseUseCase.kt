package run.piece.domain.refactoring.deposit.usecase

import run.piece.domain.refactoring.deposit.repository.DepositRepository

class DepositPurchaseUseCase(private val repository: DepositRepository) {
    fun getDepositPurchaseV1(accessToken: String, deviceId: String, memberId: String, apiVersion: String) =
        repository.getDepositPurchaseV1(accessToken = accessToken, deviceId = deviceId , memberId = memberId , apiVersion = apiVersion)

    fun getDepositPurchaseV2(accessToken: String, deviceId: String , memberId: String , apiVersion: String) =
        repository.getDepositPurchaseV2(accessToken = accessToken, deviceId = deviceId , memberId = memberId , apiVersion = apiVersion)
}