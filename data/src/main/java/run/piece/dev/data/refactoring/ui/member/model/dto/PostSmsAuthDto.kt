package run.piece.dev.data.refactoring.ui.member.model.dto

data class PostSmsAuthDto(var txSeqNo: String,
                          var rsltCd: String,
                          var cpCd: String,
                          var mdlTkn: String,
                          var rsltMsg: String,
                          var telComCd: String,
                          var resendCnt: String)