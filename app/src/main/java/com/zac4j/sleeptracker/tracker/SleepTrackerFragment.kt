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

package com.zac4j.sleeptracker.tracker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.zac4j.sleeptracker.R
import com.zac4j.sleeptracker.database.SleepDatabase
import com.zac4j.sleeptracker.databinding.FragmentSleepTrackerBinding

/**
 * A fragment with buttons to record start and end times for sleep, which are saved in
 * a database. Cumulative data is displayed in a simple scrollable TextView.
 * (Because we have not learned about RecyclerView yet.)
 */
class SleepTrackerFragment : Fragment() {

  /**
   * Called when the Fragment is ready to display content to the screen.
   *
   * This function uses DataBindingUtil to inflate R.layout.fragment_sleep_quality.
   */
  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {

    // Get a reference to the binding object and inflate the fragment views.
    val binding: FragmentSleepTrackerBinding = FragmentSleepTrackerBinding.inflate(
        inflater, container, false
    )

    val application = requireNotNull(this.activity).application

    val dataSource = SleepDatabase.getInstance(application)
        .sleepDatabaseDao

    val viewModelFactory = SleepTrackerViewModelFactory(dataSource, application)
    val viewModel = viewModelFactory.create(SleepTrackerViewModel::class.java)

    // Set the current activity as the lifecycle owner of the binding.
    binding.lifecycleOwner = viewLifecycleOwner

    binding.sleepTrackerViewModel = viewModel

    val layoutManager = GridLayoutManager(activity, 3)
    layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
      override fun getSpanSize(position: Int) = when (position) {
        0 -> 3
        else -> 1
      }
    }
    binding.sleepList.layoutManager = layoutManager

    // Set up adapter
    val adapter = SleepNightAdapter(SleepNightListener { nightId ->
      viewModel.onSleepNightClicked(nightId)
    })
    binding.sleepList.adapter = adapter

    // Observe data set change and update list view
    viewModel.nights.observe(viewLifecycleOwner, Observer {
      it?.let {
        adapter.addHeaderAndSubmitList(it)
      }
    })

    viewModel.navigateToSleepQuality.observe(viewLifecycleOwner, Observer { night ->
      night?.let {
        this.findNavController()
            .navigate(
                SleepTrackerFragmentDirections.actionSleepTrackerFragmentToSleepQualityFragment(
                    night.nightId
                )
            )
        viewModel.doneNavigating()
      }
    })

    viewModel.navigateToSleepDataQuality.observe(viewLifecycleOwner, Observer { nightId ->
      nightId?.let {
        this.findNavController()
            .navigate(
                SleepTrackerFragmentDirections.actionSleepTrackerFragmentToSleepDetailFragment(
                    nightId
                )
            )
        viewModel.onSleepDataQualityNavigated()
      }
    })

    viewModel.showSnackbarEvent.observe(viewLifecycleOwner, Observer {
      if (it == true) {
        Snackbar.make(
            activity!!.findViewById(android.R.id.content), getString(R.string.cleared_message),
            Snackbar.LENGTH_SHORT
        )
            .show()
        viewModel.doneShowingSnackbar()
      }
    })

    return binding.root
  }

}
