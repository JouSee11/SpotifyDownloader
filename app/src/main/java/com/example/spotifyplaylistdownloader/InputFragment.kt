package com.example.spotifyplaylistdownloader

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.navigation.fragment.findNavController
import com.chaquo.python.PyObject
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [InputFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class InputFragment : Fragment() {
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
        val view = inflater.inflate(R.layout.fragment_input, container, false)

        lateinit var permissionsLauncher: ActivityResultLauncher<Array<String>>
        //get permissions
        updateOrRequestPermissions(permissionsLauncher)

        //widgets
        val pasteButton = view.findViewById<Button>(R.id.pasteButton)
        val editText = view.findViewById<EditText>(R.id.editText)

        //get reference to python to validate the link
        //val pyInnit = Python.start(AndroidPlatform(requireContext()))
        val py = Python.getInstance()
        val myModule: PyObject? = py.getModule("get_spotify_names")
        val myFunNames: PyObject? = myModule?.get("get_names")

        //button press action
        pasteButton.setOnClickListener {
            //check if the user is connected
            val connected = checkInternetConnectivity(requireContext())
            if (!connected) {
                AlertDialog.Builder(requireContext())
                    .setTitle("Not connected")
                    .setMessage("Please connect to the internet to proceed")
                    .setPositiveButton("Ok") {_, _ -> }
                    .show()
                return@setOnClickListener
            }


            val spotifyLink = editText.text.toString()

            //check if the input is valid
            val isValid = myFunNames?.call(spotifyLink, "validate")?.toString()
            Log.println(Log.DEBUG, "test", isValid.toString())
            if (isValid == "False") {
                println("here")
                Toast.makeText(context, "Enter a valid link", Toast.LENGTH_SHORT).show()
            }
            else {
            findNavController().navigate(R.id.action_inputFragment_to_playlistFragment)
            }
        }

        return view
    }

    private fun updateOrRequestPermissions(launcher: ActivityResultLauncher<Array<String>>) {
        val hasReadPermission = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
        val hasWritePermission = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
        val minSdk29 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

        val readPermissionGranted = hasReadPermission
        val writePermissionGranted = hasWritePermission || minSdk29

        val permissionsToRequest = mutableListOf<String>()
        if (!readPermissionGranted) {
            permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        if (!writePermissionGranted) {
            permissionsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        if (permissionsToRequest.isNotEmpty()) {
            //val permissionsLauncher: ActivityResultLauncher<Array<String>>
            launcher.launch(permissionsToRequest.toTypedArray())
        }
    }



    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment InputFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            InputFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}