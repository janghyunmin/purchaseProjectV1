package run.piece.dev.refactoring.ui.portfolio.detail.business

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import run.piece.dev.R
import run.piece.dev.databinding.FragmentBusinessBinding
import run.piece.domain.refactoring.portfolio.model.PortfolioJoinBizItemVo

@AndroidEntryPoint
class BusinessFragment: Fragment(R.layout.fragment_business) {
    private var _binding: FragmentBusinessBinding? = null
    private val binding get() = _binding!!
    private val viewModel: BusinessViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentBusinessBinding.bind(view)
        _binding?.lifecycleOwner = this

        binding.apply {

            businessRv.setHasFixedSize(true)
            businessRv.layoutManager = LinearLayoutManager(requireContext())

            val businessRvAdapter = BusinessRvAdapter(requireContext())

            businessRv.adapter = businessRvAdapter

            arguments?.let { bundle ->
                val data = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    bundle.getParcelableArrayList("data", PortfolioJoinBizItemVo::class.java)
                } else {
                    bundle.getParcelableArrayList("data")
                }

                data?.let {
                    businessRvAdapter.submitList(it)
                }
            }


        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(data: List<PortfolioJoinBizItemVo>) : BusinessFragment {
            val fragment = BusinessFragment()
            val bundle = Bundle().apply {
                putParcelableArrayList("data", data as ArrayList)
            }
            fragment.arguments = bundle
            return fragment
        }
    }
}