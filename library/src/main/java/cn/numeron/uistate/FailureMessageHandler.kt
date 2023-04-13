package cn.numeron.uistate

fun interface FailureMessageHandler {

    fun getFailureMessage(throwable: Throwable): CharSequence

    companion object DEFAULT : FailureMessageHandler {
        override fun getFailureMessage(throwable: Throwable): CharSequence {
            return throwable.message!!
        }
    }

}