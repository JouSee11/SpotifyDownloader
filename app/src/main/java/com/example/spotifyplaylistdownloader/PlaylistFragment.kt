package com.example.spotifyplaylistdownloader

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [PlaylistFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PlaylistFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

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
        val view = inflater.inflate(R.layout.fragment_playlist, container, false)

        //widgets
        val imageView = view.findViewById<ImageView>(R.id.playlistImageView)
        val imageViewWide = view.findViewById<ImageView>(R.id.playlistImageViewWide)
        val playlistTextView = view.findViewById<TextView>(R.id.playlistName)

        val playlistLink = arguments?.getString("link").toString()

        //set the image to playlist thumbnail
        val imageUrl = myFunNames.call(playlistLink, "thumbnail").toString()
        //target to set the image to both normal and wide at once
        val targetImages = object : Target {
            override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                // Set the bitmap to both ImageViews
                imageView.setImageBitmap(bitmap)
                imageViewWide.setImageBitmap(bitmap)
            }

            override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
                // Handle bitmap loading failure
            }

            override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
                // Handle bitmap loading preparation
            }
        }
        Picasso.get()
            .load(imageUrl)
            .into(targetImages)


        //set playlist name
        val playlistName = myFunNames.call(playlistLink, "pl_name").toString()
        playlistTextView.text = playlistName

        return view
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment PlaylistFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            PlaylistFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}