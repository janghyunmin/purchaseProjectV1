package run.piece.domain.refactoring.member.model

data class PostSmsVerificationVo(val rsltCd: String,
                                 val rsltMsg: String,
                                 val cpCd: String,
                                 val txSeqNo: String,
                                 val di: String,
                                 val ci: String,
                                 val ci2: String,
                                 val ciUpdate: String,
                                 val rsltName: String,
                                 val telNo: String,
                                 val telComCd: String,
                                 val rsltBirthday: String,
                                 val rsltSexCd: String,
                                 val rsltNtvFrnrCd: String,
                                 val publicKey: String)