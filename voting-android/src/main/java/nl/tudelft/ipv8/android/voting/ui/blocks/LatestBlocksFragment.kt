package nl.tudelft.ipv8.android.voting.ui.blocks

import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_blocks.*
import nl.tudelft.ipv8.attestation.trustchain.TrustChainBlock
import kotlin.math.min

class LatestBlocksFragment : BlocksFragment() {
    override val isNewBlockAllowed = false

    override fun getBlocks(): List<TrustChainBlock> {
        val allBlocks = getTrustChainCommunity().database.getAllBlocks()
            .sortedByDescending { it.insertTime }
        return allBlocks.subList(0, min(1000, allBlocks.size))
    }

    override fun getPublicKey(): ByteArray {
        return getTrustChainCommunity().myPeer.publicKey.keyToBin()
    }

    override suspend fun updateView() {
        val layoutManager = recyclerView.layoutManager as LinearLayoutManager
        val firstVisibleItem = layoutManager.findFirstCompletelyVisibleItemPosition()

        super.updateView()

        if (firstVisibleItem == 0) {
            recyclerView.smoothScrollToPosition(0)
        }
    }
}
