package cn.numeron.uistate


class Empty<T> internal constructor(

    /** 消息提示 */
    val message: String,

    value: T?

) : UIState<T>(value)
