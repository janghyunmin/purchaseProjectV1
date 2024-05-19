package run.piece.domain.refactoring.alarm.model

data class AlarmBaseVo(val status: String,
                       val statusCode: Int,
                       val message: String,
                       val data : Int)