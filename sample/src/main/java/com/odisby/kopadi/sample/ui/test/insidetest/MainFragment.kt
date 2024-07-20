package com.odisby.kopadi.sample.ui.test.insidetest

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.odisby.kopadi.sample.BaseFragment
import com.odisby.kopadi.sample.databinding.FragmentMainBinding

class MainFragment : BaseFragment() {
    private var _binding: FragmentMainBinding? = null

    private val binding get() = _binding!!

    private val viewModel: MainViewModel by viewModels { viewModelFactory }

    companion object {
       fun newInstance() = MainFragment()
   }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.textView.text = viewModel.displayInjectedValue()
    }
}
