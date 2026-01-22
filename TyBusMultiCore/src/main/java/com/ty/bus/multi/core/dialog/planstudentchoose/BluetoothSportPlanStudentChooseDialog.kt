package com.ty.bus.multi.core.dialog.planstudentchoose

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.Group
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.adl.lib.AdlCameraFrameProcessor
import com.adl.lib.AdlCameraPreviewInfo
import com.adl.service.entity.ClassInfoEntity
import com.adl.service.entity.StudentEntity
import com.adl.service.outer.AdlService
import com.adl.service.outer.UserCaller
import com.bumptech.glide.Glide
import com.ty.bus.multi.R
import com.ty.bus.multi.core.dialog.identify.IdentifyViewModel
import com.ty.bus.multi.core.entity.PlanGradeInfo
import com.ty.bus.multi.databinding.BmItemChooseStudentBinding
import com.ty.bus.multi.databinding.BmItemClassNameBinding
import com.ty.bus.multi.databinding.BmItemGradeInfoBinding
import com.ty.bus.multi.databinding.DialogBluetoothSportPlanStudentChooseBinding
import com.ty.bus.multi.databinding.DialogIdentifyBinding
import com.ty.nfc.reader.base.NfcType
import com.ty.nfc.reader.base.ReadCallback
import com.ty.nfc.reader.manager.NfcReaderManager
import com.ty.tybusbase.base.BaseBindingAdapter
import com.ty.tybusbase.base.BaseDialogFragment
import com.ty.tybusbase.base.VBViewHolder
import com.ty.tybusbase.config.BusiBaseConfig.getIdentityType
import com.ty.tybusbase.constants.BusiBaseConstantsValue
import com.ty.tybusbase.constants.MMKVBusiKey
import com.ty.tybusbase.entity.AccountInfo
import com.ty.tybusbase.entity.CustomTabEntity
import com.ty.tybusbase.expand.Action
import com.ty.tybusbase.expand.Action1
import com.ty.tybusbase.expand.gone
import com.ty.tybusbase.expand.invisible
import com.ty.tybusbase.expand.onDebouncedClick
import com.ty.tybusbase.expand.textChanges
import com.ty.tybusbase.expand.visible
import com.ty.tybusbase.helper.GridSpaceDecoration
import com.ty.tybusbase.helper.SpeechWrapper.speech
import com.ty.tybusbase.identify.IdentifyMachineListener
import com.ty.tybusbase.identify.MultiIFrameCountIdentityEngine
import com.ty.tybusbase.utils.LogUtil
import com.ty.tybusbase.utils.MMKVUtil
import com.ty.tybusbase.utils.MMKVUtil.decodeInt
import com.ty.tybusbase.utils.ToastUtil
import com.ty.tybusbase.view.SlidingTabLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class BluetoothSportPlanStudentChooseDialog :
    BaseDialogFragment<DialogBluetoothSportPlanStudentChooseBinding, BluetoothSportPlanStudentChooseViewModel>() {

    private var identifyResultListener: Action1<HashMap<Int, StudentEntity>>? = null

    private var identifiedMap: HashMap<Int, StudentEntity>? = null

    //连接状态
    private var blueConnectList = HashMap<Int, Boolean>()

    private var currentIndex: Int = -1

    private val mCustomTabEntities by lazy { ArrayList<CustomTabEntity>() }

    private var isGrade = true

    //学生列表
    private val studentList = mutableListOf<StudentEntity>()
    private val searchStudentList = mutableListOf<StudentEntity>()

    //    private var planId: String? = null
    private var planId: String? = "a1a2370a7c3d4961b026483c64033c19"

    @SuppressLint("NotifyDataSetChanged")
    override fun initViewObservable() {
        viewModel.gradeListLD.observe(this) {
            initGradeView(it)
        }
        viewModel.studentListLD.observe(this) {
            studentList.clear()
            if (it.isNullOrEmpty()) {
                mBinding.groupNoData.visible()
            } else {
                mBinding.groupNoData.gone()
                studentList.addAll(it)
            }
            mBinding.rvStudent.adapter?.notifyDataSetChanged()
        }

        viewModel.searchStudentListLD.observe(this) {
            searchStudentList.clear()
            if (it.isNullOrEmpty()) {
                mBinding.groupNoData.visible()
            } else {
                mBinding.groupNoData.gone()
                searchStudentList.addAll(it)
            }
            mBinding.rvSearchStudent.adapter?.notifyDataSetChanged()
        }
    }

    private fun initGradeView(infos: MutableList<PlanGradeInfo>) {
        mBinding.rvGrade.layoutManager = LinearLayoutManager(requireContext())
        mBinding.rvGrade.adapter =
            object : BaseBindingAdapter<BmItemGradeInfoBinding, PlanGradeInfo>(infos) {
                override fun convert(
                    holder: VBViewHolder<BmItemGradeInfoBinding>,
                    item: PlanGradeInfo
                ) {
                    holder.vb.apply {
                        tvGradeName.text = item.gradeName
                        rvClass.layoutManager = GridLayoutManager(requireContext(), 6)
                        rvClass.addItemDecoration(GridSpaceDecoration(24, 24))
                        rvClass.adapter = object :
                            BaseBindingAdapter<BmItemClassNameBinding, ClassInfoEntity>(item.classList) {
                            override fun convert(
                                holder: VBViewHolder<BmItemClassNameBinding>,
                                item: ClassInfoEntity
                            ) {
                                holder.vb.apply {
                                    tvClassName.text = item.className
                                    root.setOnClickListener {
                                        onClassClick(item)
                                    }
                                }
                            }
                        }
                    }
                }
            }
    }

    fun setPlanId(planId: String?): BluetoothSportPlanStudentChooseDialog {
        this.planId = planId
        return this
    }

    override fun initData() {
        viewModel.querySportPlanClass(planId)
    }

    override fun initView() {
        super.initView()
        mBinding.tvCancel.setOnClickListener {
            identifiedMap?.clear()
            dismiss()
        }

        mBinding.tvConfirm.setOnClickListener {
            dismiss()
            identifyResultListener?.invoke(identifiedMap ?: HashMap())
        }

        //如果添加了已识别的人，处理显示已识别的人
        identifiedMap?.forEach { (index, value) ->
            dispatchStudent(index, value)
        }

        mBinding.llAdd1.setOnClickListener {
            resetIdentifyingView(currentIndex)
            currentIndex = 1
            dispatchIdentifyingView(1)
        }

        mBinding.llAdd2.setOnClickListener {
            resetIdentifyingView(currentIndex)
            currentIndex = 2
            dispatchIdentifyingView(2)
        }

        mBinding.llAdd3.setOnClickListener {
            resetIdentifyingView(currentIndex)
            currentIndex = 3
            dispatchIdentifyingView(3)
        }

        mBinding.cvGrade.setOnClickListener {
            mBinding.groupClass.gone()
            mBinding.rvGrade.visible()
            mBinding.groupNoData.gone()
            isGrade = true
        }

        handleClickEnable()
        initTab()
        initStudentRv()
        initSearchStudentRv()
        initSearchEt()
    }

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    private fun initSearchEt() {
        viewModel.searchStudent(
            mBinding.etSearch.textChanges()
                .debounce(500)
                .distinctUntilChanged()
                .filter {
                    withContext(Dispatchers.Main) {
                        if (it.isEmpty()) {
                            mBinding.ivClearAll.gone()
                        } else {
                            mBinding.ivClearAll.visible()
                        }
                    }
                    it.isNotEmpty()
                }
                .flatMapLatest {
                    flow {
                        emit(it)
                    }
                })
        mBinding.ivClearAll.setOnClickListener {
            mBinding.etSearch.setText("")
        }
    }


    private fun initTab() {
        mCustomTabEntities.add(
            CustomTabEntity(
                getString(R.string.busi_multi_search_by_grade),
                R.mipmap.eye_close,
                R.mipmap.eye_open
            )
        )
        mCustomTabEntities.add(
            CustomTabEntity(
                getString(R.string.busi_multi_search_by_name),
                R.mipmap.eye_close,
                R.mipmap.eye_open
            )
        )
        mBinding.tab.setTabData(mCustomTabEntities)
        mBinding.tab.setOnTabSelectListener(object : SlidingTabLayout.OnTabSelectListener {
            override fun onTabSelect(position: Int) {
                if (position == 0) {
                    if (isGrade) {
                        mBinding.rvGrade.visible()
                        mBinding.groupClass.gone()
                    } else {
                        mBinding.rvGrade.gone()
                        mBinding.groupClass.visible()
                    }
                    mBinding.groupSearchStudent.gone()
                } else {
                    mBinding.groupSearchStudent.visible()
                    mBinding.rvGrade.gone()
                    mBinding.groupClass.gone()
                }
            }

            override fun onTabReselect(position: Int) {

            }
        })
    }

    private fun initStudentRv() {
        mBinding.rvStudent.layoutManager = GridLayoutManager(requireContext(), 3)
        mBinding.rvStudent.addItemDecoration(GridSpaceDecoration(24, 24))
        mBinding.rvStudent.adapter = StudentAdapter(studentList)
    }

    private fun initSearchStudentRv() {
        mBinding.rvSearchStudent.layoutManager = GridLayoutManager(requireContext(), 3)
        mBinding.rvSearchStudent.addItemDecoration(GridSpaceDecoration(24, 24))
        mBinding.rvSearchStudent.adapter = StudentAdapter(searchStudentList)
    }

    private fun onStudentSelected(studentEntity: StudentEntity, freshUi: Action) {
        viewModel.checkStudent(studentEntity.accountId, planId) {
            if (it) {
                dispatchStudent(currentIndex, studentEntity)
                currentIndex = -1
                freshUi.invoke()
            } else {
                toastWithoutPlan()
            }
        }
    }

    private fun toastWithoutPlan() {
        ToastUtil.getInstance(mBinding.root.context, getString(R.string.busi_base_without_plan))
            .show()
    }

    @SuppressLint("SetTextI18n")
    private fun onClassClick(classInfoEntity: ClassInfoEntity) {
        isGrade = false
        studentList.clear()
        viewModel.queryStudentByClassID(classInfoEntity.classId)
        mBinding.rvGrade.gone()
        mBinding.groupClass.visible()
        mBinding.tvGradeName.text = "${classInfoEntity.gradeName}${classInfoEntity.className}"
    }

    /**
     * 处理未连接的位置不能识别人员逻辑
     */
    private fun handleClickEnable() {
        blueConnectList.forEach { (index, value) ->
            when (index) {
                1 -> {
                    if (!value) {
                        mBinding.llAdd1.isClickable = false
                        mBinding.ivAdd1Icon.setBackgroundResource(R.mipmap.ic_blueteeth_disabled)
                        mBinding.llAdd1.setBackgroundResource(R.drawable.bg_border_dashed_r5_white_20_shape)
                        mBinding.tvAdd1Tip.text = getString(R.string.busi_multi_device_not_connect)
                        mBinding.tvAdd1Tip.setTextColor(resources.getColor(R.color.white_40))
                    }
                }

                2 -> {
                    if (!value) {
                        mBinding.llAdd2.isClickable = false
                        mBinding.ivAdd2Icon.setBackgroundResource(R.mipmap.ic_blueteeth_disabled)
                        mBinding.llAdd2.setBackgroundResource(R.drawable.bg_border_dashed_r5_white_20_shape)
                        mBinding.tvAdd2Tip.text = getString(R.string.busi_multi_device_not_connect)
                        mBinding.tvAdd2Tip.setTextColor(resources.getColor(R.color.white_40))
                    }
                }

                3 -> {
                    if (!value) {
                        mBinding.llAdd3.isClickable = false
                        mBinding.ivAdd3Icon.setBackgroundResource(R.mipmap.ic_blueteeth_disabled)
                        mBinding.llAdd3.setBackgroundResource(R.drawable.bg_border_dashed_r5_white_20_shape)
                        mBinding.tvAdd3Tip.text = getString(R.string.busi_multi_device_not_connect)
                        mBinding.tvAdd3Tip.setTextColor(resources.getColor(R.color.white_40))
                    }
                }
            }
        }
    }

    private fun dispatchIdentifyingView(index: Int) {
        when (index) {
            1 -> {
                mBinding.ivAdd1Icon.setBackgroundResource(R.mipmap.ic_user_add_loading)
                mBinding.tvAdd1Tip.text = com.ty.tybusbase.expand.getString(R.string.tj_ing)
                mBinding.llAdd1.setBackgroundResource(R.drawable.bg_border_r5_white_shape)
            }

            2 -> {
                mBinding.ivAdd2Icon.setBackgroundResource(R.mipmap.ic_user_add_loading)
                mBinding.tvAdd2Tip.text = com.ty.tybusbase.expand.getString(R.string.tj_ing)
                mBinding.llAdd2.setBackgroundResource(R.drawable.bg_border_r5_white_shape)
            }

            3 -> {
                mBinding.ivAdd3Icon.setBackgroundResource(R.mipmap.ic_user_add_loading)
                mBinding.tvAdd3Tip.text = com.ty.tybusbase.expand.getString(R.string.tj_ing)
                mBinding.llAdd3.setBackgroundResource(R.drawable.bg_border_r5_white_shape)
            }
        }
    }

    /**
     * 重置正在识别的View
     */
    private fun resetIdentifyingView(index: Int) {
        when (index) {
            1 -> {
                mBinding.ivAdd1Icon.setBackgroundResource(R.mipmap.sports_ic_adduser)
                mBinding.tvAdd1Tip.text = com.ty.tybusbase.expand.getString(R.string.tj)
                mBinding.llAdd1.setBackgroundResource(R.drawable.bg_border_dashed_r5_white_shape)
            }

            2 -> {
                mBinding.ivAdd2Icon.setBackgroundResource(R.mipmap.sports_ic_adduser)
                mBinding.tvAdd2Tip.text = com.ty.tybusbase.expand.getString(R.string.tj)
                mBinding.llAdd2.setBackgroundResource(R.drawable.bg_border_dashed_r5_white_shape)
            }

            3 -> {
                mBinding.ivAdd3Icon.setBackgroundResource(R.mipmap.sports_ic_adduser)
                mBinding.tvAdd3Tip.text = com.ty.tybusbase.expand.getString(R.string.tj)
                mBinding.llAdd3.setBackgroundResource(R.drawable.bg_border_dashed_r5_white_shape)
            }
        }
    }

    fun isContainStudent(stu: StudentEntity): Boolean {
        if (identifiedMap == null) {
            return false
        }
        for ((_, value1) in identifiedMap!!) {
            if (value1.accountId != BusiBaseConstantsValue.VISITOR_USER_ID && value1.accountId == stu.accountId) {
                return true
            }
        }
        return false
    }

    /**
     * 分发学生到UI
     */
    private fun dispatchStudent(index: Int, value: StudentEntity?) {
        if (value == null) {
            return
        }
        if (index == -1) {
            autoDispatchCardStudent(value)
            return
        }
        identifiedMap?.put(index, value)
        when (index) {
            1 -> {
                handleIdentifyView(
                    index,
                    mBinding.groupAdd1,
                    mBinding.groupAdded1,
                    mBinding.ivHeader1,
                    mBinding.tvAddName1,
                    mBinding.ivDeleteUser1,
                    value
                )
            }

            2 -> {
                handleIdentifyView(
                    index,
                    mBinding.groupAdd2,
                    mBinding.groupAdded2,
                    mBinding.ivHeader2,
                    mBinding.tvAddName2,
                    mBinding.ivDeleteUser2,
                    value
                )
            }

            3 -> {
                handleIdentifyView(
                    index,
                    mBinding.groupAdd3,
                    mBinding.groupAdded3,
                    mBinding.ivHeader3,
                    mBinding.tvAddName3,
                    mBinding.ivDeleteUser3,
                    value
                )
            }
        }
    }

    /**
     * 未选择卡片，自动分配学生
     */
    private fun autoDispatchCardStudent(value: StudentEntity) {
        val index =
            if (mBinding.groupAdded1.visibility != View.VISIBLE && blueConnectList.getOrDefault(
                    1,
                    false
                )
            ) {
                1
            } else if (mBinding.groupAdded2.visibility != View.VISIBLE && blueConnectList.getOrDefault(
                    2,
                    false
                )
            ) {
                2
            } else if (mBinding.groupAdded3.visibility != View.VISIBLE && blueConnectList.getOrDefault(
                    3,
                    false
                )
            ) {
                3
            } else {
                return
            }
        identifiedMap?.put(index, value)
        dispatchStudent(index, value)
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
    }

    private fun handleIdentifyView(
        index: Int,
        addGroup: Group,
        identifyGroup: Group,
        ivHeader: ImageView,
        tvName: TextView,
        deleteView: ImageView,
        studentEntity: StudentEntity
    ) {
        addGroup.invisible()
        identifyGroup.visible()
        deleteView.setOnClickListener {
            identifiedMap?.remove(index)
            addGroup.visible()
            identifyGroup.gone()
            mBinding.rvStudent.adapter?.notifyDataSetChanged()
        }
        Glide.with(requireContext())
            .load(studentEntity.userFaceImgUrl)
            .placeholder(R.mipmap.ic_head_default)
            .error(R.mipmap.ic_head_default)
            .into(ivHeader)
        tvName.text = studentEntity.accountName
    }


    fun setIdentifiedMap(identifiedMap: HashMap<Int, StudentEntity>): BluetoothSportPlanStudentChooseDialog {
        this.identifiedMap = identifiedMap
        return this
    }

    fun setBlueConnectList(blue: HashMap<Int, Boolean>): BluetoothSportPlanStudentChooseDialog {
        this.blueConnectList = blue
        return this
    }

    fun setIdentifyResultListener(listener: Action1<HashMap<Int, StudentEntity>>): BluetoothSportPlanStudentChooseDialog {
        identifyResultListener = listener
        return this
    }

    inner class StudentAdapter(data: MutableList<StudentEntity>?) :
        BaseBindingAdapter<BmItemChooseStudentBinding, StudentEntity>(data) {
        override fun convert(
            holder: VBViewHolder<BmItemChooseStudentBinding>,
            item: StudentEntity
        ) {
            holder.vb.apply {
                val isSelected = isContainStudent(item)
                if (isSelected == true) {
                    holder.vb.ivChoose.setBackgroundResource(R.mipmap.ic_rb_pre)
                    holder.vb.root.setBackgroundResource(R.drawable.bm_item_choose_student_select_bg)
                } else {
                    holder.vb.ivChoose.setBackgroundResource(R.mipmap.ic_rb_normal)
                    holder.vb.root.setBackgroundResource(R.drawable.bg_662572ff_r8_shape)
                }
                tvName.text = item.accountName
                tvClassName.text = "${item.gradeName}${item.className}"
                if (item.userFaceImgUrl.isNullOrEmpty().not() && item.userFaceImgUrl.startsWith(
                        AdlService.getService().facePrefix
                    ).not()
                ) {
                    item.userFaceImgUrl =
                        AdlService.getService().facePrefix + item.userFaceImgUrl
                }
                Glide.with(requireContext())
                    .load(item.userFaceImgUrl)
                    .placeholder(R.mipmap.ic_head_default)
                    .error(R.mipmap.ic_head_default).into(civHeader)
                if (isSelected.not()) {
                    root.onDebouncedClick {
                        val connectSize = blueConnectList.filter { it.value }.size
                        if (identifiedMap?.size == connectSize) {
                            return@onDebouncedClick
                        }
                        onStudentSelected(item) {
                            notifyItemChanged(holder.layoutPosition)
                        }
                    }
                }
            }
        }
    }
}