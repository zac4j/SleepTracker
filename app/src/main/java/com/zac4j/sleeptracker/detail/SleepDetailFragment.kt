/*
 * Copyright 2018, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zac4j.sleeptracker.detail

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.zac4j.sleeptracker.R
import com.zac4j.sleeptracker.database.SleepDatabase
import com.zac4j.sleeptracker.databinding.FragmentSleepDetailBinding

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [SleepDetailFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [SleepDetailFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class SleepDetailFragment : Fragment() {

  private lateinit var mBinding: FragmentSleepDetailBinding

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {

    // Get a reference to the binding object and inflate the fragment views.
    mBinding = FragmentSleepDetailBinding.inflate(
        inflater, container, false
    )

    val application = requireNotNull(this.activity).application
    val arguments = SleepDetailFragmentArgs.fromBundle(arguments!!)

    // Create an instance of the ViewModel Factory.
    val dataSource = SleepDatabase.getInstance(application)
        .sleepDatabaseDao
    val viewModelFactory = SleepDetailViewModelFactory(arguments.sleepNightKey, dataSource)

    // Get a reference to the ViewModel associated with this fragment.
    val sleepDetailViewModel = viewModelFactory.create(SleepDetailViewModel::class.java)

    // To use the View Model with data binding, you have to explicitly
    // give the binding object a reference to it.
    mBinding.sleepDetailViewModel = sleepDetailViewModel

    mBinding.lifecycleOwner = this

    // Add an Observer to the state variable for Navigating when a Quality icon is tapped.
    sleepDetailViewModel.navigateToSleepTracker.observe(viewLifecycleOwner, Observer {
      if (it == true) { // Observed state is true.
        this.findNavController()
            .navigate(
                SleepDetailFragmentDirections.actionSleepDetailFragmentToSleepTrackerFragment()
            )
        // Reset state to make sure we only navigate once, even if the device
        // has a configuration change.
        sleepDetailViewModel.doneNavigating()
      }
    })

    setHasOptionsMenu(true)

    return mBinding.root
  }

  override fun onCreateOptionsMenu(
    menu: Menu,
    inflater: MenuInflater
  ) {
    super.onCreateOptionsMenu(menu, inflater)
    inflater.inflate(R.menu.options_menu, menu)
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    when (item.itemId) {
      R.id.share -> {
        val shareContent =
          getString(R.string.share_content, mBinding.sleepLength.text, mBinding.qualityString.text)
        val i = createShareIntent(shareContent)
        startActivity(i)
      }
    }

    return super.onOptionsItemSelected(item)
  }

  private fun createShareIntent(sleepQuality: String) = Intent(Intent.ACTION_SEND).apply {
    type = "text/plain"
    putExtra(Intent.EXTRA_TEXT, sleepQuality)
  }

}
