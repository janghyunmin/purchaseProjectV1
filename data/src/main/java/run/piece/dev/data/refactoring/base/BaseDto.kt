package run.piece.dev.data.refactoring.base

data class BaseDto(var status: String?,
                   var statusCode:Int?,
                   var message: String?,
                   var subMessage: String?,
                   var data : Any?)