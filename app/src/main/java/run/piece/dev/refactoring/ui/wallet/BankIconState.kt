package run.piece.dev.refactoring.ui.wallet

import dagger.hilt.android.scopes.ViewModelScoped

@ViewModelScoped
enum class BankIconState(val code: String, val title: String) {
    BANK001("001", "한국은행"),
    BANK002("002", "KDB산업은행"),
    BANK003("003", "IBK기업은행"),
    BANK004("004", "KB국민은행"),
    BANK007("007", "수협은행"),
    BANK008("008", "수출입은행"),
    BANK011("011", "NH농협은행"),
    BANK012("012", "지역농축협"),
    BANK020("020", "우리은행"),
    BANK021("021", "외환은행"),
    BANK023("023", "SC제일은행"),
    BANK026("026", "신한은행"),
    BANK027("027", "한국씨티은행"),
    BANK031("031", "대구은행"),
    BANK032("032", "부산은행"),
    BANK034("034", "광주은행"),
    BANK035("035", "제주은행"),
    BANK037("037", "전북은행"),
    BANK039("039", "경남은행"),
    BANK045("045", "새마을금고"),
    BANK047("047", "신협은행"),
    BANK064("064", "산림조합중앙회"),
    BANK071("071", "우체국"),
    BANK081("081", "하나은행"),
    BANK089("089", "케이뱅크"),
    BANK090("090", "카카오뱅크"),
    BANK092("092", "토스뱅크");

    companion object {
        fun getBankName(code: String): String {
            return values().find { it.code == code }?.name ?: ""
        }

        fun getBankCode(code: String) : String {
            return values().find { it.code == code }?.code ?: ""
        }
    }
}
