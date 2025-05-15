### [中文](README.md) | English

# UIState
  * A universal state class for controlling the UI layer in the ViewModel layer under MVVM architecture.
  * Compatible with both View and Compose UI. The ViewModel only needs to manage the UIState class, while the UI layer can freely choose between View or Compose, or switch between them without modifying ViewModel code.

#### Installation  
  Current latest version: [![](https://jitpack.io/v/xiazunyang/uistate.svg)](https://jitpack.io/#xiazunyang/uistate)
  * Add JitPack repository
    ```groovy
    dependencyResolutionManagement {
      repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
        repositories {
          mavenCentral()
          maven { url 'https://jitpack.io' }
      }
    }
    ```
  * Add dependency
    ```groovy
    dependencies {
      implementation 'cn.numeron:uistate:latest_version'
    }
    ```

#### Usage
  * ViewModel
    * Single network model
      ```kotlin
      /** ViewModel */
      class ViewModel {
        // Use MutableStateFlow to hold data, UIState is Empty by default
        val uiStateFlow = MutableStateFlow(UIState<List<Data>>())
  
        // Quick access/modify uiStateFlow's value
        private var uiState by uiStateFlow
  
        // Refresh
        fun refresh() {
          viewModelScope.launch {
            try {
              // Update state to Loading
              uiState = uiState.toLoading("Loading data", 0f)
              // do something
              val dataList = Network.getDataList()
              if (dataList.isNullOrEmpty()) {
                // Update state to Empty
                uiState = uiState.toEmpty()
              } else {
                // Update state to Success
                uiState = uiState.toSuccess(dataList)
              }
            } catch (throwable: Throwable) {
              // Update state to Failure
              uiState = uiState.toFailure(throwable)
            }
          }
        }
      }
      ```
    * Network + Database model
      ```kotlin
      /** ViewModel */
      class ViewModel {
      
        // Represents network state without storing actual data
        private val uiStateFlow = MutableStateFlow(UIState<Unit>())
  
        // Flow for dataList from database (not representing state)
        private val dataListFlow = Db.getDataListAsFlow()
      
        // Combine UIStateFlow and dataListFlow into Flow<UIState<List<Data>>>
        val dataListStateFlow = combine(uiStateFlow, dataListFlow) { uiState, dataList ->
          uiState.map { dataList }
        }
  
        // Quick access/modify uiStateFlow's value
        private var uiState by uiStateFlow
  
        // Refresh
        fun refresh() {
          viewModelScope.launch {
            try {
              // Update state to Loading
              uiState = uiState.toLoading("Loading data", 0f)
              // Fetch from network
              val dataList = Network.getDataList()
              // Save to database
              Db.saveDataList(dataList)
              if (dataList.isNullOrEmpty()) {
                // Update state to Empty
                uiState = uiState.toEmpty()
              } else {
                // Update state to Success
                uiState = uiState.toSuccess(Unit)
              }
            } catch (throwable: Throwable) {
              // Update state to Failure
              uiState = uiState.toFailure(throwable)
            }
          }
        }
      }
      ```
  * UI
    * Compose + Network model
    ```kotlin
    @Composable
    fun DataList(viewModel: ViewModel = viewModel()) {
      val dataListState by viewModel.dataListStateFlow.collectAsState()
      // For single network model, handle UI states directly
      when(dataListState) {
        is Empty -> EmptyState(dataListState.message)
        is Loading -> LoadingState(dataListState.message)
        is Failure -> FailureState(dataListState.message)
        else -> DataListState(dataListState.value)
      }
    }
    ```
    * Compose + Network + Database model 
    ```kotlin
    @Composable
    fun DataList(viewModel: ViewModel = viewModel()) {
      val dataListState by viewModel.dataListStateFlow.collectAsState()
      val dataList = dataListState.value
        if (dataList.isNullOrEmpty()) {
          // Handle empty data states
          when(dataListState) {
            is Loading -> LoadingState(dataListState.message)
            is Failure -> FailureState(dataListState.message)
            else -> EmptyState(dataListState.message)
          }
        } else {
          // Handle states when database has data
          DataListState(list = dataList)
           when(dataListState) {
            is Loading -> {
              // Handle Loading state when data exists (e.g., load more/pull-to-refresh)
            }
            is Failure -> {
              // Handle Failure state when data exists (e.g., failed to load more/refresh)
            }
          }
        }
    }
    ```
    * View + Network + Database model
    ```kotlin
    class Fragment {
      private val viewModel: ViewModel by viewModels()
      private val viewBinding: ViewBinding by viewBindings()
    
      override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewLifecycleOwner.lifecycleScope.launch {
          viewModel.dataListStateFlow.collectLatest { uiState ->
            // Data from database
            val dataList = uiState.value
            if (dataList.isNullOrEmpty()) {
              // Handle no-data states
              viewBinding.statefulLayout.state = when(uiState) {
                is Empty -> State.Empty
                is Loading -> State.Loading
                is Failure -> State.Failure
              }
            } else {
              // Submit data to adapter
              dataAdapter.submitList(dataList)
              // Switch to success view
              viewBinding.statefulLayout.state = State.Success
               when(uiState) {
                is Loading -> {
                  // Handle Loading state with data (e.g., load more/pull-to-refresh)
                }
                is Failure -> {
                  // Handle Failure state with data (e.g., failed to load more/refresh)
                }
              }
            }
          }
        }
      }
    }
    ```
