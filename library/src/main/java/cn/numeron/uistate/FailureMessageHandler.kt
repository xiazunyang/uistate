package cn.numeron.uistate

fun interface FailureMessageHandler {

    fun getFailureMessage(throwable: Throwable): String

    companion object DEFAULT : FailureMessageHandler {
        override fun getFailureMessage(throwable: Throwable): String {
            return throwable.message!!
        }
    }

}