package cn.numeron.uistate


class Loading<T> internal constructor(

    /** 下载进度 */
    val progress: Float,

    /** 消息提示 */
    val message: String,

    value: T?

) : UIState<T>(value)
