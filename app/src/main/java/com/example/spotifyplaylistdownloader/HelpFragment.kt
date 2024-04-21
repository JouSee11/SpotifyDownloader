package com.example.spotifyplaylistdownloader

import android.os.Bundle
import android.provider.MediaStore.Images.ImageColumns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import org.w3c.dom.Text

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [HelpFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HelpFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    lateinit var imageViewHelp: ImageView
    lateinit var infoText: TextView
    lateinit var buttonNext: ImageButton
    lateinit var buttonPrev: ImageButton

    private val gifResources = intArrayOf(
        R.drawable.help_1,
        R.drawable.help_2,
        R.drawable.help_3,
        R.drawable.help_4
    )
    private val stringResource = intArrayOf(
        R.string.help_first,
        R.string.help_second,
        R.string.help_third,
        R.string.help_fourth
    )
    private var currentPosition = 0
    private val lengthResources = gifResources.size - 1





    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_help_fragmnet, container, false)

        buttonNext = view.findViewById<ImageButton>(R.id.arrow_next)
        buttonPrev = view.findViewById<ImageButton>(R.id.arrow_previous)
        infoText = view.findViewById<TextView>(R.id.stageInfo)
        imageViewHelp = view.findViewById<ImageView>(R.id.imageViewHelp)

        initializeState()

        //actions for back and forward buttons
        buttonNext.setOnClickListener {
            //check if we are already in the end
            nextImage()
        }

        buttonPrev.setOnClickListener {
            //check if we are already in the end
            previousImage()
        }

        return view
    }

    private fun nextImage() {
        //check if we are already in the end
        if (currentPosition < lengthResources) {
            currentPosition++

            Glide.with(this)
                .load(gifResources[currentPosition])
                .into(imageViewHelp)

            infoText.setText(stringResource[currentPosition])
        } else {
            //Toast.makeText(requireContext(), "You are at the end", Toast.LENGTH_SHORT).show()
        }
    }

    private fun previousImage() {
        //check if we are already in the end
        if (currentPosition > 0) {
            currentPosition--

            Glide.with(this)
                .load(gifResources[currentPosition])
                .into(imageViewHelp)

            infoText.setText(stringResource[currentPosition])
        } else {
            //Toast.makeText(requireContext(), "You are at the start", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initializeState() {
        Glide.with(this)
            .load(gifResources[currentPosition])
            .into(imageViewHelp)

        infoText.setText(stringResource[currentPosition])
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment HelpFragmnet.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HelpFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}