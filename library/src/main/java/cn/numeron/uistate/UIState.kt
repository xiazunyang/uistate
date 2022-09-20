package cn.numeron.uistate

import android.content.Context

sealed class UIState<T>(open val value: T?) {

    /** 判断是否有值，不论它是什么状态 */
    val hasValue: Boolean
        get() {
            val value = value
            if (value is Iterable<*>) {
                return value.any()
            }
            if (value is Iterator<*>) {
                return value.hasNext()
            }
            return value != null
        }

    /** 将当前状态转换为成功状态 */
    fun toSuccess(value: T): UIState<T> {
        return Success(value)
    }

    /** 将当前状态转换为失败状态 */
    fun toFailure(
        cause: Throwable,
        message: String = failureMessageHandler.getFailureMessage(cause)
    ): UIState<T> {
        return Failure(cause, message, value)
    }

    /** 将当前状态转换为加载中状态 */
    fun toLoading(
        progress: Float = 0f,
        message: String = loadingMessage
    ): UIState<T> {
        return Loading(progress, message, value)
    }

    companion object : FailureMessageHandler {

        /** 加载状态下的提示消息 */
        lateinit var loadingMessage: String
            private set

        /** 空状态下提示消息 */
        lateinit var emptyMessage: String
            private set

        /** 错误状态下提示消息的处理器 */
        private var failureMessageHandler = FailureMessageHandler.DEFAULT

        /** 从[Context]中初始化 */
        fun init(
            context: Context,
            failureMessageHandler: FailureMessageHandler = FailureMessageHandler.DEFAULT
        ) {
            emptyMessage = context.getString(R.string.mvi_empty_message)
            loadingMessage = context.getString(R.string.mvi_loading_message)
            this.failureMessageHandler = if (context is FailureMessageHandler) context else failureMessageHandler
        }

        /** 自定义初始化 */
        fun init(
            emptyMessage: String,
            loadingMessage: String,
            failureMessageHandler: FailureMessageHandler = FailureMessageHandler.DEFAULT
        ) {
            this.emptyMessage = emptyMessage
            this.loadingMessage = loadingMessage
            this.failureMessageHandler = failureMessageHandler
        }

        override fun getFailureMessage(throwable: Throwable): String {
            return failureMessageHandler.getFailureMessage(throwable)
        }

        /** 创建一个空状态的[UIState] */
        operator fun <T> invoke(): UIState<T> {
            return Empty(emptyMessage, null)
        }

        /** 创建一个成功状态的[UIState] */
        operator fun <T> invoke(value: T): UIState<T> {
            return Success(value)
        }

    }

}
