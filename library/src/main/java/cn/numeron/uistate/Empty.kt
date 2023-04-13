package cn.numeron.uistate


class Empty<T> internal constructor(

    /** 消息提示 */
    val message: CharSequence,

    value: T?

) : UIState<T>(value)
