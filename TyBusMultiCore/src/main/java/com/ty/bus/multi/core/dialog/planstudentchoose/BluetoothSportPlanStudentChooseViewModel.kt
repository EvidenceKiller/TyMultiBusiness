package com.ty.bus.multi.core.dialog.planstudentchoose

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.adl.service.entity.StudentEntity
import com.adl.service.outer.AdlService
import com.adl.service.web.SportPlanService
import com.ty.bus.multi.core.entity.PlanGradeInfo
import com.ty.tybusbase.base.BaseLiveData
import com.ty.tybusbase.base.BaseViewModel
import com.ty.tybusbase.constants.BusiBaseConstantsValue
import com.ty.tybusbase.expand.Action1
import com.ty.tybusbase.utils.LogUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BluetoothSportPlanStudentChooseViewModel(application: Application) :
    BaseViewModel(application) {
    val gradeListLD = BaseLiveData<MutableList<PlanGradeInfo>>()
    val studentListLD = BaseLiveData<MutableList<StudentEntity>>()
    val searchStudentListLD = BaseLiveData<MutableList<StudentEntity>>()
    val cacheStudentMap = HashMap<String, MutableList<StudentEntity>>()

    val searchTextFlow = flowOf("")
        .debounce(300)
        .distinctUntilChanged()
        .flatMapLatest { query ->
            flow {
                emit(query)
            }
        }

    fun querySportPlanClass(planId: String?) {
        if (planId.isNullOrEmpty()) {
            gradeListLD.postValue(mutableListOf<PlanGradeInfo>())
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            val classList = SportPlanService.querySportPlanClass(planId)
            if (classList.isNullOrEmpty().not()) {
                val gradeList = classList.groupBy { it.gradeName }
                    .map {
                        PlanGradeInfo(
                            it.key,
                            if (it.value.isNullOrEmpty()) 0 else it.value[0].gradeNum,
                            it.value.toMutableList()
                        )
                    }.toMutableList()
                gradeListLD.postValue(gradeList)
            } else {
                gradeListLD.postValue(mutableListOf<PlanGradeInfo>())
            }
        }
    }

    fun queryStudentByClassID(classId: String) {
        val cacheList = cacheStudentMap.getOrDefault(classId, null)
        if (cacheList != null) {
            studentListLD.postValue(cacheList)
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            val studentList = AdlService.getService().studentDao.queryStudentByClassIds(
                mutableListOf<String>(classId)
            )
            cacheStudentMap.put(classId, studentList)
            studentListLD.postValue(studentList)
        }
    }

    fun searchStudent(textFlow: Flow<String>) {
        viewModelScope.launch(Dispatchers.IO) {
            textFlow.collect {
                LogUtil.d("searchStudent: 开始搜索 $it")
                val studentList = AdlService.getService().studentDao.queryStudentByKeyWord(it)
                LogUtil.d("searchStudent: 搜索结果 ${studentList.size}")
                searchStudentListLD.postValue(studentList)
            }
        }
    }

    /**
     * 检查学生是否在计划内
     */
    fun checkStudent(accountId: String, planId: String?, callback: Action1<Boolean>) {
        viewModelScope.launch(Dispatchers.IO) {
            if (planId == null) {
                withContext(Dispatchers.Main) {
                    callback.invoke(true)
                }
            } else {
                if (accountId == BusiBaseConstantsValue.VISITOR_USER_ID) {
                    withContext(Dispatchers.Main) {
                        callback.invoke(false)
                    }
                } else {
                    //掉接口判断学生是否在计划内
                    val result = SportPlanService.checkStudent(planId, accountId)
                    withContext(Dispatchers.Main) {
                        callback.invoke(result?:false)
                    }
                }
            }
        }
    }
}