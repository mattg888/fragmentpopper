package uk.co.mattgrundy.fragmentpopper

import android.os.Bundle
import uk.co.mattgrundy.fragmentpopper.fragments.FirstFragment
import uk.co.mattgrundy.fragmentpopper.fragments.SecondFragment
import uk.co.mattgrundy.fragmentpopperhelper.FragmentPopperActivity

class MainActivity : FragmentPopperActivity(), FirstFragment.OnFirstFragmentInteractionListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        replaceFragment(FirstFragment())
    }

    override fun getFragmentContainerId(): Int {
        return R.id.fragment_container
    }

    override fun onNextFragment() {
        replaceFragment(SecondFragment())
    }

}
