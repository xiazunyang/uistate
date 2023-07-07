package cn.numeron.uistate

data class Failure<T> internal constructor(

    /** 失败原因 */
    val cause: Throwable,

    /** 消息提示 */
    val message: CharSequence,

    override val value: T?

) : UIState<T>(value)
