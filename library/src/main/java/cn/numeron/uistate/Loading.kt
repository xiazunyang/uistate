package cn.numeron.uistate


data class Loading<T> internal constructor(

    /** 下载进度 */
    val progress: Float,

    /** 消息提示 */
    val message: CharSequence,

    override val value: T?

) : UIState<T>(value)
