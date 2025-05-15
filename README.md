### 中文 | [English](README_EN.md)

## UIState
  * 用于MVVM架构下在ViewModel层控制UI层的通用状态类。
  * 兼容View视图与Compose视图，ViewModel只需要管理UIState类，UI层可以任意选择使用View或Compose视图，亦或是在两者之间任意切换而无需变更ViewModel的代码。

#### 安装  
  当前最新版本：[![](https://jitpack.io/v/xiazunyang/uistate.svg)](https://jitpack.io/#xiazunyang/uistate)
  * 添加jitpack仓库
    ```groove
    dependencyResolutionManagement {
      repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
        repositories {
          mavenCentral()
          maven { url 'https://jitpack.io' }
      }
    }
    ```
  * 添加依赖
    ``` groovy
    dependencies {
      implementation 'com.github.xiazunyang:uistate:latest_version'
    }
    ```

#### 使用
  * ViewModel
    * 单网络模型
      ```kotlin
      /** ViewModel */
      class ViewModel {
        // 使用MutableStateFlow存放数据，默认情况下UIState状态为Empty
        val uiStateFlow = MutableStateFlow(UIState<List<Data>>())
  
        // 快捷获取、设置uiStateFlow的value
        private var uiState by uiStateFlow
  
        // 刷新
        fun refresh() {
          viewModelScopy.launch {
            try {
              // 把状态更新为Loading
              uiState = uiState.toLoading("正在获取数据", 0f)
              // do something
              val dataList = Network.getDataList()
              if (dataList.isNullOrEmpty()) {
                // 把状态更新为Empty
                uiState = uiState.toEmpty()
              } else {
                // 把状态更新为Success
                uiState = uiState.toSuccess(dataList)
              }
            } catch (throwable: Throwable) {
              // 把状态更新为Failure
              uiState = uiState.toFailure(throwable)
            }
          }
        }
      }
      ```
    * 网络 + 数据库模型
      ```kotlin
      /** ViewModel */
      class ViewModel {
      
        // 表示网络状态，但不记录实际数据
        private val uiStateFlow = MutableStateFlow(UIState<Unit>())
  
        // 从数据获取dataList的Flow，但不表示状态
        private val dataListFlow = Db.getDataListAsFlow()
      
        // 合并UIStateFlow和dataListFlow，转换为Flow<UIState<List<Data>>>类型
        val dataListStateFlow = combine(uiStateFlow, dataListFlow) { uiState, dataList ->
          uiState.map { dataList }
        }
  
        // 快捷获取、设置uiStateFlow的value
        private var uiState by uiStateFlow
  
        // 刷新
        fun refresh() {
          viewModelScopy.launch {
            try {
              // 把状态更新为Loading
              uiState = uiState.toLoading("正在获取数据", 0f)
              // 从网络获取数据
              val dataList = Network.getDataList()
              // 保存到数据库
              Db.saveDataList(dataList)
              if (dataList.isNullOrEmpty()) {
                // 把状态更新为Empty
                uiState = uiState.toEmpty()
              } else {
                // 把状态更新为Success
                uiState = uiState.toSuccess(Unit)
              }
            } catch (throwable: Throwable) {
              // 把状态更新为Failure
              uiState = uiState.toFailure(throwable)
            }
          }
        }
      }
      ```
  * UI
    * Compose + 网络模型
    ```kotlin
    @Composable
    fun DataList(viewModel: ViewModel = viewModel()) {
      val dataListState by viewModel.dataListStateFlow.collectAsState()
      // 单网络模型下，直接按uiState的状态处理页面就行了
      when(dataListState) {
        is Empty -> EmptyState(dataListState.message)
        is Loading -> LoadingState(dataListState.message)
        is Failure -> FailureState(dataListState.message)
        else -> DataListState(dataListState.value)
      }
    }
    * Compose + 网络模型 + 数据库模型 
    ```kotlin
    @Composable
    fun DataList(viewModel: ViewModel = viewModel()) {
      val dataListState by viewModel.dataListStateFlow.collectAsState()
      val dataList = users.value
        if (dataList.isNullOrEmpty()) {
          // 处理空数据时的各种状态
          when(dataListState) {
            is Loading -> LoadingState(dataListState.message)
            is Failure -> FailureState(dataListState.message)
            else -> EmptyState(dataListState.message)
          }
        } else {
          // 处理数据库有数据时的各种状态
          DataListState(list = dataList)
           when(dataListState) {
            is Loading -> {
              // 处理有数据时的Loading状态、如加载更多、下拉刷新
            }
            is Failure -> {
              // 处理有数据时的Failure状态、如加载更多、下拉刷新时失败了
            }
          }
        }
    }
    ```
    * View视图 + 网络 + 数据库模型
    ```kotlin
    class Fragment {
      private val viewModel: ViewModel by viewModels()
      private val viewBinding: ViewBinding by viewBindings()
    
      override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewLifecycleOwner.lifecycleScope.launch {
          viewModel.dataListStateFlow.collectLatest { uiState ->
            // 从数据库中取出的数据
            val dataList = uiState.value
            if (dataList.isNullOrEmpty()) {
              // 处理数据库中没有数据的状态
              viewBinding.statefulLayout.state = when(uiState) {
                is Empty -> State.Empty
                is Loading -> State.Loading
                is Failure -> State.Failure
              }
            } else {
              // 数据列表提交到adapter中
              dataAdapter.submitList(dataList)
              // 需要切换至有数据的视图
              viewBinding.statefulLayout.state = State.Success
               when(dataListState) {
                is Loading -> {
                  // 处理有数据时的Loading状态、如加载更多、下拉刷新
                }
                is Failure -> {
                  // 处理有数据时的Failure状态、如加载更多、下拉刷新时失败了
                }
              }
            }
          }
        }
      }
    }
    ```   
