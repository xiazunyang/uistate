package cn.numeron.uistate

class Failure<T> internal constructor(

    /** 失败原因 */
    val cause: Throwable,

    /** 消息提示 */
    val message: CharSequence,

    value: T?

) : UIState<T>(value)
