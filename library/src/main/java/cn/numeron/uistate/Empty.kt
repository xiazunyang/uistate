package cn.numeron.uistate


data class Empty<T> internal constructor(

    /** 消息提示 */
    val message: CharSequence,

    override val value: T?

) : UIState<T>(value)
