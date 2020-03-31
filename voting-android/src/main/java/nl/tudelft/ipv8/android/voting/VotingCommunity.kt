package nl.tudelft.ipv8.android.voting

import nl.tudelft.ipv8.Address
import nl.tudelft.ipv8.Community
import nl.tudelft.ipv8.IPv8
import nl.tudelft.ipv8.Overlay
import nl.tudelft.ipv8.android.IPv8Android
import nl.tudelft.ipv8.attestation.trustchain.TrustChainBlock
import nl.tudelft.ipv8.attestation.trustchain.TrustChainCommunity
import java.util.*

class VotingCommunity : Community() {
    override val serviceId = "02313685c1912a141279f8248fc8db5899c5d008"

    val discoveredAddressesContacted: MutableMap<Address, Date> = mutableMapOf()

    // Voting API
    val trustchainvoter: TrustChainVoter = TrustChainVoter()

    /**
     * Initialize TrustChainHelper so we can make use of the TrustChain
      */

    protected val trustchain: TrustChainHelper by lazy {
        TrustChainHelper(getTrustChainCommunity())
    }

    override fun walkTo(address: Address) {
        super.walkTo(address)
        discoveredAddressesContacted[address] = Date()
    }

    protected fun getTrustChainCommunity(): TrustChainCommunity {
        return getIpv8().getOverlay()
            ?: throw IllegalStateException("TrustChainCommunity is not configured")
    }

    protected fun getIpv8(): IPv8 {
        return IPv8Android.getInstance()
    }

    /**
     * Use startVote function from API
     */
    fun startVote(voters : List<String>, voteSubject: String) = trustchainvoter.startVote(voters, voteSubject)

    /**
     * Use respondToVote function from API
     */
    fun respondToVote(voteName: String, vote: Boolean, proposalBlock: TrustChainBlock) =  trustchainvoter.respondToVote(voteName, vote, proposalBlock)

    /**
     * Use countVotes function from API
     */
    fun countVotes(voters: List<String>, voteName: String, proposerKey: ByteArray): Pair<Int, Int> = trustchainvoter.countVotes(voters, voteName, proposerKey)

    class Factory : Overlay.Factory<VotingCommunity>(VotingCommunity::class.java)

}
