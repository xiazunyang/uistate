package cn.numeron.uistate

class Success<T> internal constructor(override val value: T) : UIState<T>(value)
