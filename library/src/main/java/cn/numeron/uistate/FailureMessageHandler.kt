package cn.numeron.uistate

fun interface FailureMessageHandler {

    fun getFailureMessage(throwable: Throwable): String

    companion object {
        val DEFAULT = FailureMessageHandler { throwable ->
            throwable.message!!
        }
    }

}