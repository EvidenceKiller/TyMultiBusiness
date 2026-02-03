package com.ty.tybusbase.dialog.plansinglestuchoose

import android.annotation.SuppressLint
import android.content.DialogInterface
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.adl.service.entity.ClassInfoEntity
import com.adl.service.entity.StudentEntity
import com.adl.service.outer.AdlService
import com.bumptech.glide.Glide
import com.ty.tybusbase.R
import com.ty.tybusbase.base.BaseBindingAdapter
import com.ty.tybusbase.base.BaseDialogFragment
import com.ty.tybusbase.base.VBViewHolder
import com.ty.tybusbase.databinding.BuBaseItemChooseStudentBinding
import com.ty.tybusbase.databinding.BuBaseItemClassNameBinding
import com.ty.tybusbase.databinding.BuBaseItemGradeInfoBinding
import com.ty.tybusbase.databinding.DialogSingleSportPlanStudentChooseBinding
import com.ty.tybusbase.entity.CustomTabEntity
import com.ty.tybusbase.entity.PlanGradeInfo
import com.ty.tybusbase.expand.Action1
import com.ty.tybusbase.expand.gone
import com.ty.tybusbase.expand.onDebouncedClick
import com.ty.tybusbase.expand.textChanges
import com.ty.tybusbase.expand.visible
import com.ty.tybusbase.helper.GridSpaceDecoration
import com.ty.tybusbase.utils.ToastUtil
import com.ty.tybusbase.view.SlidingTabLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

class SingleSportPlanStudentChooseDialog :
    BaseDialogFragment<DialogSingleSportPlanStudentChooseBinding, SingleSportPlanStudentChooseViewModel>() {
    private var chooseListener: Action1<StudentEntity>? = null

    private val mCustomTabEntities by lazy { ArrayList<CustomTabEntity>() }

    private var isGrade = true

    //被选中的学生
    private var chooseStudent: StudentEntity? = null

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
            object : BaseBindingAdapter<BuBaseItemGradeInfoBinding, PlanGradeInfo>(infos) {
                override fun convert(
                    holder: VBViewHolder<BuBaseItemGradeInfoBinding>,
                    item: PlanGradeInfo
                ) {
                    holder.vb.apply {
                        tvGradeName.text = item.gradeName
                        rvClass.layoutManager = GridLayoutManager(requireContext(), 6)
                        rvClass.addItemDecoration(GridSpaceDecoration(24, 24))
                        rvClass.adapter = object :
                            BaseBindingAdapter<BuBaseItemClassNameBinding, ClassInfoEntity>(item.classList) {
                            override fun convert(
                                holder: VBViewHolder<BuBaseItemClassNameBinding>,
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

    fun setPlanId(planId: String?): SingleSportPlanStudentChooseDialog {
        this.planId = planId
        return this
    }

    fun setChooseListener(listener: Action1<StudentEntity>): SingleSportPlanStudentChooseDialog {
        this.chooseListener = listener
        return this
    }

    override fun initData() {
        viewModel.querySportPlanClass(planId)
    }

    override fun initView() {
        super.initView()
        mBinding.tvCancel.setOnClickListener {
            dismiss()
        }

        mBinding.tvConfirm.setOnClickListener {
            if (chooseStudent != null) {
                viewModel.checkStudent(chooseStudent!!.accountId, planId) {
                    if (it) {
                        chooseListener?.invoke(chooseStudent!!)
                    } else {
                        toastWithoutPlan(chooseStudent!!.accountName)
                    }
                    dismiss()
                }
            } else {
                ToastUtil.getInstance(requireContext(), "请选择学生").show()
            }
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
                getString(R.string.busi_base_search_by_grade),
                R.mipmap.eye_close,
                R.mipmap.eye_open
            )
        )
        mCustomTabEntities.add(
            CustomTabEntity(
                getString(R.string.busi_base_search_by_name),
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
                    mBinding.rvStudent.adapter?.notifyDataSetChanged()
                } else {
                    mBinding.groupSearchStudent.visible()
                    mBinding.rvGrade.gone()
                    mBinding.groupClass.gone()
                    mBinding.rvSearchStudent.adapter?.notifyDataSetChanged()
                }
                chooseStudent = null
            }

            override fun onTabReselect(position: Int) {

            }
        })
    }

    private fun initStudentRv() {
        mBinding.rvStudent.layoutManager = GridLayoutManager(requireContext(), 4)
        mBinding.rvStudent.addItemDecoration(GridSpaceDecoration(24, 24))
        mBinding.rvStudent.adapter = StudentAdapter(studentList)
    }

    private fun initSearchStudentRv() {
        mBinding.rvSearchStudent.layoutManager = GridLayoutManager(requireContext(), 4)
        mBinding.rvSearchStudent.addItemDecoration(GridSpaceDecoration(24, 24))
        mBinding.rvSearchStudent.adapter = StudentAdapter(searchStudentList)
    }

    private fun onStudentSelected(studentEntity: StudentEntity) {
//        viewModel.checkStudent(studentEntity.accountId, planId) {
//            if (it) {
//                dispatchStudent(currentIndex, studentEntity)
//                currentIndex = -1
//                freshUi.invoke()
//            } else {
//                toastWithoutPlan(studentEntity.accountName)
//            }
//        }
        chooseStudent = studentEntity
    }

    private fun toastWithoutPlan(accountName: String) {
        ToastUtil.getInstance(
            mBinding.root.context,
            getString(R.string.busi_base_name_without_plan, accountName)
        )
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
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
    }

    inner class StudentAdapter(data: MutableList<StudentEntity>?) :
        BaseBindingAdapter<BuBaseItemChooseStudentBinding, StudentEntity>(data) {
        override fun convert(
            holder: VBViewHolder<BuBaseItemChooseStudentBinding>,
            item: StudentEntity
        ) {
            holder.vb.apply {
                val isSelected = chooseStudent?.accountId == data[holder.adapterPosition].accountId
                if (isSelected == true) {
                    holder.vb.ivChoose.setBackgroundResource(R.mipmap.ic_rb_pre)
                    holder.vb.root.setBackgroundResource(R.drawable.bu_base_item_choose_student_select_bg)
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
                        onStudentSelected(data[holder.layoutPosition])
                        notifyItemChanged(holder.layoutPosition)
                    }
                }
            }
        }
    }
}