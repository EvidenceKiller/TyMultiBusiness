package com.ty.bus.multi.core.dialog.planstudentchoose

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.adl.service.entity.StudentEntity
import com.adl.service.outer.AdlService
import com.adl.service.web.SportPlanService
import com.ty.bus.multi.core.entity.PlanGradeInfo
import com.ty.tybusbase.base.BaseLiveData
import com.ty.tybusbase.base.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BluetoothSportPlanStudentChooseViewModel(application: Application) :
    BaseViewModel(application) {
    val gradeListLD = BaseLiveData<MutableList<PlanGradeInfo>>()
    val studentListLD = BaseLiveData<MutableList<StudentEntity>>()
    val cacheStudentMap = HashMap<String, MutableList<StudentEntity>>()
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
                    }.sortedBy { it.gradeNum }.toMutableList()
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
}