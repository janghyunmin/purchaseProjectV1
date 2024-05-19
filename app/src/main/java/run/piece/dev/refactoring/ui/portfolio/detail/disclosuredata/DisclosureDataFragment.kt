package run.piece.dev.refactoring.ui.portfolio.detail.disclosuredata

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import run.piece.dev.R
import run.piece.dev.databinding.FragmentDisclosureDataBinding
import run.piece.domain.refactoring.portfolio.model.AttachFileItemVo

@AndroidEntryPoint
class DisclosureDataFragment: Fragment(R.layout.fragment_disclosure_data) {
    private var _binding: FragmentDisclosureDataBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DisclosureDataVewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentDisclosureDataBinding.bind(view)
        _binding?.lifecycleOwner = this

        binding.apply {
            arguments?.let {
                val data = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    it.getParcelableArrayList("data", AttachFileItemVo::class.java)
                } else {
                    it.getParcelableArrayList("data")
                }

                data?.let {
                    val disclosureDataRvAdapter = DisclosureDataRvAdapter(requireActivity())

                    disclosureRv.apply {
                        layoutManager = LinearLayoutManager(requireActivity(), LinearLayoutManager.HORIZONTAL, false)
                        adapter = disclosureDataRvAdapter

                        addItemDecoration(DisclosureRvDecoration(viewModel.dpToPixel(16), viewModel.dpToPixel(8), data.size))
                    }

                    disclosureDataRvAdapter.submitList(data)
                }
            }
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(data: List<AttachFileItemVo>) : DisclosureDataFragment {
            val fragment = DisclosureDataFragment()
            val bundle = Bundle().apply {
                putParcelableArrayList("data", data as ArrayList)
            }
            fragment.arguments = bundle
            return fragment
        }
    }
}