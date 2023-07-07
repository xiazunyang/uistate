package cn.numeron.uistate

data class Success<T> internal constructor(override val value: T) : UIState<T>(value)
