package nl.tudelft.ipv8.android.voting.ui.users

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.mattskala.itemadapter.ItemAdapter
import kotlinx.android.synthetic.main.fragment_peers.*
import kotlinx.coroutines.*
import nl.tudelft.ipv8.android.voting.R
import nl.tudelft.ipv8.android.voting.ui.BaseFragment
import nl.tudelft.ipv8.android.keyvault.AndroidCryptoProvider
import nl.tudelft.ipv8.util.toHex

class UsersFragment : BaseFragment() {
    private val adapter = ItemAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        adapter.registerRenderer(UserItemRenderer {
            findNavController().navigate(
                UsersFragmentDirections.actionUsersFragmentToBlocksFragment(it.publicKey)
            )
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_users, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.addItemDecoration(DividerItemDecoration(context, LinearLayout.VERTICAL))

        loadNetworkInfo()
    }


    private fun loadNetworkInfo() {
        lifecycleScope.launchWhenStarted {
            while (isActive) {
                val items = withContext(Dispatchers.IO) {
                    val users = trustchain.getUsers()

                    users.map {
                        val peerId = AndroidCryptoProvider.keyFromPublicBin(it.publicKey)
                            .keyToHash().toHex()
                        val storedBlocks = trustchain.getStoredBlockCountForUser(it.publicKey)
                        UserItem(
                            peerId,
                            it.publicKey.toHex(),
                            it.latestSequenceNumber,
                            storedBlocks
                        )
                    }
                }
                adapter.updateItems(items)

                imgEmpty.isVisible = items.isEmpty()

                delay(1000)
            }
        }
    }
}
