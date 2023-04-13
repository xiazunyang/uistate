package cn.numeron.uistate

import android.content.Context

sealed class UIState<T>(open val value: T?) {

    /** 将当前状态转换为成功状态 */
    fun toSuccess(value: T): UIState<T> {
        return Success(value)
    }

    /** 将当前状态转换为成功状态 */
    fun toEmpty(message: CharSequence = emptyMessage): UIState<T> {
        return Empty(message, value)
    }

    /** 将当前状态转换为失败状态 */
    fun toFailure(
        cause: Throwable,
        message: CharSequence = getFailureMessage(cause)
    ): UIState<T> {
        return Failure(cause, message, value)
    }

    /** 将当前状态转换为加载中状态 */
    fun toLoading(
        progress: Float = -1f,
        message: CharSequence = loadingMessage
    ): UIState<T> {
        return Loading(progress, message, value)
    }

    /**
     * 将当前状态的值转换成相同状态下的另一个类型的[UIState]
     * 注意，如果[mapper]返回null，则[Success]状态会坍缩为[Empty]状态。
     */
    fun <R> map(mapper: (T?) -> R?): UIState<R> {
        val mapped = mapper(value)
        return when (this) {
            is Empty -> Empty(message, mapped)
            is Failure -> Failure(cause, message, mapped)
            is Loading -> Loading(progress, message, mapped)
            is Success -> Success(mapped ?: return Empty(emptyMessage, null))
            else -> throw IllegalStateException()
        }
    }

    companion object : FailureMessageHandler {

        /** 加载状态下的提示消息 */
        lateinit var loadingMessage: CharSequence
            private set

        /** 空状态下提示消息 */
        lateinit var emptyMessage: CharSequence
            private set

        /** 错误状态下提示消息的处理器 */
        private var failureMessageHandler: FailureMessageHandler = FailureMessageHandler.DEFAULT

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
            emptyMessage: CharSequence,
            loadingMessage: CharSequence,
            failureMessageHandler: FailureMessageHandler = FailureMessageHandler.DEFAULT
        ) {
            this.emptyMessage = emptyMessage
            this.loadingMessage = loadingMessage
            this.failureMessageHandler = failureMessageHandler
        }

        override fun getFailureMessage(throwable: Throwable): CharSequence {
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
