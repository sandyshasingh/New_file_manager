package com.editor.hiderx.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.editor.hiderx.R
import com.editor.hiderx.dataclass.FileDataClass


private const val ARG_PATH = "path"

class PhotosFragment : Fragment() {
    private var path : String? = null
    private var arrayList : ArrayList<FileDataClass>? = null
    private  var index : Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            path = it.getString(ARG_PATH)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_photos, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    companion object {
        @JvmStatic
        fun newInstance(path: String, list: ArrayList<FileDataClass>?, index : Int?) =
            PhotosFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PATH, path)
                }
                this.arrayList = list
                this.index = index
            }
    }
}