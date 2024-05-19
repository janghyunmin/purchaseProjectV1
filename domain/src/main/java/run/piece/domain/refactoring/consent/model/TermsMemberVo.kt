package run.piece.domain.refactoring.consent.model
data class TermsMemberVo (val required: TermsMemberItemVo,
                          val selective: TermsMemberItemVo,
                          val policy: TermsMemberItemVo)
data class TermsMemberItemVo (val date: String,
                              val consent: List<TermsMemberListItemVo>)

data class TermsMemberListItemVo (val date: String,
                                  val consentCode: String,
                                  val isAgreement: String,
                                  val consentTitle: String)