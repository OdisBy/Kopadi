package com.odisby.kopadi.sample.ui.test.another_package

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.odisby.kopadi.sample.BaseFragment
import com.odisby.kopadi.sample.R

class AnotherFragment : BaseFragment() {

    companion object {
        fun newInstance() = AnotherFragment()
    }

    private val viewModel: AnotherViewModel by viewModels { viewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_another, container, false)
    }
}