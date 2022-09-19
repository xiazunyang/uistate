package cn.numeron.uistate

fun interface FailureMessageHandler {

    fun getMessage(throwable: Throwable): String

    companion object {
        val DEFAULT = FailureMessageHandler { throwable ->
            throwable.message!!
        }
    }

}