package com.elacqua.gpacademic.ui.lessons

import android.app.Dialog
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.elacqua.gpacademic.R
import com.elacqua.gpacademic.data.local.Lesson
import com.elacqua.gpacademic.data.local.Term
import com.elacqua.gpacademic.utility.AppPreferences
import kotlinx.android.synthetic.main.dialog_term.*
import kotlinx.android.synthetic.main.fragment_home.*


private const val TAG = "HomeFragment"

class HomeFragment : Fragment() {

    private lateinit var recyclerViewLessonAdapter: RecyclerViewLessonAdapter
    private val homeViewModel: HomeViewModel by navGraphViewModels(R.id.mobile_navigation)
    private var bundle: Bundle? = null
    private var type: Int ?= null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        bundle = arguments
        getGpaType()
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    private fun getGpaType() {
        val sharedRef = AppPreferences(requireContext())
        type = sharedRef.gpaType
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initRecyclerView()
        initSwipeToDelete()
        setValuesFromBundle()

        btnAddLesson.setOnClickListener {
            homeViewModel.addLesson()
        }

        btnAddTerm.setOnClickListener {
            createAddTermDialog()
        }

        btnCalculateGpa.setOnClickListener {
            val gpa = homeViewModel.getGpa(type!!)
            btnCalculateGpa.text = gpa
        }

        homeViewModel.lessonLiveDataList.observe(viewLifecycleOwner, Observer{ lessonList ->
            val newLessonList = ArrayList<Lesson>().apply { addAll(lessonList) }
            recyclerViewLessonAdapter.submitList(newLessonList)
        })
    }

    private fun initRecyclerView() {
        recyclerViewLessonAdapter =
            RecyclerViewLessonAdapter(
                requireContext(),
                type!!
            )
        recyclerView.run {
            adapter = recyclerViewLessonAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            setHasFixedSize(true)
        }
    }

    private fun initSwipeToDelete() {
        ItemTouchHelper(object: ItemTouchHelper.SimpleCallback(0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT){
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
                                target: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                homeViewModel.deleteLessonAt(position)
            }
        }).attachToRecyclerView(recyclerView)
    }

    private fun setValuesFromBundle() {
        if (bundle != null){
            val term = bundle!!.getParcelable<Term>("getTerm") ?: return
            homeViewModel.updateId = term.id
            homeViewModel.setLessonList(term.lessons)
        }
    }

    private fun createAddTermDialog() {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_term)
        dialog.txtTermName.requestFocus()
        dialog.btnDialogSave.setOnClickListener {
            val termName = (dialog.txtTermName.text).toString()
            addTerm(termName)
            dialog.dismiss()
        }
        dialog.btnDialogCancel.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun addTerm(termName: String) {
        homeViewModel.insertOrUpdateTerm(termName)
        Handler().postDelayed({findNavController().navigate(R.id.navigation_term)}, 700)
    }

    override fun onPause() {
        super.onPause()
        homeViewModel.updateId = null
    }

    override fun onDestroyView() {
        recyclerView.adapter = null
        super.onDestroyView()
    }
}